package com.example.jarvis.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jarvis.audio.SpeechManager
import com.example.jarvis.audio.TextToSpeechManager
import com.example.jarvis.model.ChatMessage
import com.example.jarvis.model.CommandType
import com.example.jarvis.model.JarvisSystemState
import com.example.jarvis.model.LanguageMode
import com.example.jarvis.model.MessageSender
import com.example.jarvis.model.MessageStatus
import com.example.jarvis.service.DeviceActionManager
import com.example.jarvis.service.GeminiService
import com.example.jarvis.service.VoiceCommandParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    val deviceManager = DeviceActionManager(application)
    val speechManager = SpeechManager(application)
    val ttsManager = TextToSpeechManager(application)
    private val geminiService = GeminiService()

    private val _systemState = MutableStateFlow(JarvisSystemState())
    val systemState: StateFlow<JarvisSystemState> = _systemState.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    init {
        // Initial welcome message
        val welcomeMsg = ChatMessage(
            sender = MessageSender.JARVIS,
            text = "J.A.R.V.I.S. neural systems online. All hardware diagnostics nominal. " +
                    "I am ready to receive voice or text directives in English, Bengali (বাংলা), or Banglish. How may I assist you today, Sir?",
            commandType = CommandType.GREETING,
            status = MessageStatus.SUCCESS
        )
        _messages.value = listOf(welcomeMsg)

        // Observe torch hardware state changes
        viewModelScope.launch {
            deviceManager.torchState.collect { torchOn ->
                _systemState.update { it.copy(isTorchOn = torchOn) }
            }
        }

        // Combine listening/speaking RMS levels for unified visualizer waveform
        viewModelScope.launch {
            combine(
                speechManager.isListening,
                speechManager.rmsLevel,
                ttsManager.isSpeaking,
                ttsManager.speakingRms
            ) { isList, micRms, isSpk, spkRms ->
                val activeRms = if (isList) micRms else if (isSpk) spkRms else 0f
                _systemState.update {
                    it.copy(
                        isListening = isList,
                        isSpeaking = isSpk,
                        voiceRmsLevel = activeRms
                    )
                }
            }.collect {}
        }

        // Sync initial battery
        refreshBatteryTelemetry()
    }

    fun refreshBatteryTelemetry() {
        val batteryInfo = deviceManager.getBatteryInfo()
        _systemState.update {
            it.copy(
                batteryPercentage = batteryInfo.percentage,
                isBatteryCharging = batteryInfo.isCharging,
                batteryHealth = batteryInfo.health
            )
        }
    }

    fun setLanguage(mode: LanguageMode) {
        _systemState.update { it.copy(activeLanguage = mode) }
    }

    fun toggleTts() {
        val newState = !_systemState.value.ttsEnabled
        if (!newState) {
            ttsManager.stop()
        }
        _systemState.update { it.copy(ttsEnabled = newState) }
    }

    fun startListening() {
        ttsManager.stop()
        val lang = _systemState.value.activeLanguage

        _systemState.update {
            it.copy(
                isListening = true,
                statusText = "LISTENING [${lang.displayName.uppercase()}]"
            )
        }

        speechManager.startListening(
            languageMode = lang,
            onResult = { recognizedText ->
                processUserInput(recognizedText)
            },
            onError = { errMsg ->
                _systemState.update {
                    it.copy(
                        isListening = false,
                        statusText = "STANDBY // $errMsg"
                    )
                }
            }
        )
    }

    fun stopListening() {
        speechManager.stopListening()
        _systemState.update {
            it.copy(
                isListening = false,
                statusText = "SYSTEM ONLINE // AWAITING COMMAND"
            )
        }
    }

    fun toggleListening() {
        if (_systemState.value.isListening) {
            stopListening()
        } else {
            startListening()
        }
    }

    fun processUserInput(rawInput: String) {
        val text = rawInput.trim()
        if (text.isEmpty()) return

        stopListening()

        // 1. Post user message
        val userMsg = ChatMessage(
            sender = MessageSender.USER,
            text = text
        )
        _messages.update { it + userMsg }

        // 2. Parse command
        val parsed = VoiceCommandParser.parse(text)
        val lang = _systemState.value.activeLanguage

        when (parsed.type) {
            CommandType.TORCH_ON -> {
                val result = deviceManager.setTorchMode(true)
                val replyText = if (result.isSuccess) {
                    parsed.getLocalizedResponse(lang)
                } else {
                    "Hardware exception: ${result.exceptionOrNull()?.localizedMessage ?: "Unable to access torch"}"
                }
                deliverJarvisResponse(replyText, CommandType.TORCH_ON)
            }

            CommandType.TORCH_OFF -> {
                val result = deviceManager.setTorchMode(false)
                val replyText = if (result.isSuccess) {
                    parsed.getLocalizedResponse(lang)
                } else {
                    "Hardware exception: ${result.exceptionOrNull()?.localizedMessage ?: "Unable to access torch"}"
                }
                deliverJarvisResponse(replyText, CommandType.TORCH_OFF)
            }

            CommandType.BATTERY_CHECK -> {
                refreshBatteryTelemetry()
                val info = deviceManager.getBatteryInfo()
                val replyText = when (lang) {
                    LanguageMode.BENGALI -> {
                        "বর্তমানে ব্যাটারি লেভেল ${info.percentage}%, চার্জিং অবস্থা: ${if (info.isCharging) "চার্জ হচ্ছে (${info.powerSource})" else "ডিসচার্জ হচ্ছে"}। তাপমাত্রা: ${info.temperatureCelsius}°C, হেলথ: ${info.health}।"
                    }
                    LanguageMode.BANGLISH -> {
                        "Battery level ache ${info.percentage}%, status: ${if (info.isCharging) "Charging (${info.powerSource})" else "Discharging"}. Battery temp: ${info.temperatureCelsius}°C, condition: ${info.health}."
                    }
                    LanguageMode.ENGLISH -> {
                        "Power cell capacity is at ${info.percentage}%. Power intake: ${if (info.isCharging) "Charging via ${info.powerSource}" else "On battery power"}. Core thermal readout is ${info.temperatureCelsius}°C with ${info.health} health status."
                    }
                }
                deliverJarvisResponse(replyText, CommandType.BATTERY_CHECK)
            }

            CommandType.CAMERA_LAUNCH -> {
                val result = deviceManager.launchCamera()
                val replyText = if (result.isSuccess) {
                    parsed.getLocalizedResponse(lang)
                } else {
                    "Unable to launch camera viewfinder: ${result.exceptionOrNull()?.localizedMessage}"
                }
                deliverJarvisResponse(replyText, CommandType.CAMERA_LAUNCH)
            }

            CommandType.APP_OPEN -> {
                val app = parsed.targetApp ?: "target"
                val result = deviceManager.openApp(app)
                val replyText = if (result.isSuccess) {
                    result.getOrNull() ?: parsed.getLocalizedResponse(lang)
                } else {
                    result.exceptionOrNull()?.localizedMessage ?: "Application '$app' could not be opened."
                }
                deliverJarvisResponse(replyText, CommandType.APP_OPEN)
            }

            CommandType.SETTINGS_WIFI -> {
                val result = deviceManager.openWifiSettings()
                val replyText = if (result.isSuccess) {
                    parsed.getLocalizedResponse(lang)
                } else {
                    "Error opening Wi-Fi settings: ${result.exceptionOrNull()?.localizedMessage}"
                }
                deliverJarvisResponse(replyText, CommandType.SETTINGS_WIFI)
            }

            CommandType.SETTINGS_BLUETOOTH -> {
                val result = deviceManager.openBluetoothSettings()
                val replyText = if (result.isSuccess) {
                    parsed.getLocalizedResponse(lang)
                } else {
                    "Error opening Bluetooth settings: ${result.exceptionOrNull()?.localizedMessage}"
                }
                deliverJarvisResponse(replyText, CommandType.SETTINGS_BLUETOOTH)
            }

            CommandType.SETTINGS_SYSTEM -> {
                val result = deviceManager.openSystemSettings()
                val replyText = if (result.isSuccess) {
                    parsed.getLocalizedResponse(lang)
                } else {
                    "Error accessing system settings: ${result.exceptionOrNull()?.localizedMessage}"
                }
                deliverJarvisResponse(replyText, CommandType.SETTINGS_SYSTEM)
            }

            CommandType.GREETING -> {
                deliverJarvisResponse(parsed.getLocalizedResponse(lang), CommandType.GREETING)
            }

            CommandType.GEMINI_QUERY, CommandType.NONE -> {
                executeGeminiQuery(text)
            }
        }
    }

    private fun executeGeminiQuery(prompt: String) {
        val thinkingMsgId = UUID.randomUUID().toString()
        val thinkingMsg = ChatMessage(
            id = thinkingMsgId,
            sender = MessageSender.JARVIS,
            text = "Processing neural query...",
            commandType = CommandType.GEMINI_QUERY,
            status = MessageStatus.THINKING
        )
        _messages.update { it + thinkingMsg }
        _systemState.update { it.copy(isThinking = true, statusText = "NEURAL COGNITION ACTIVE") }

        viewModelScope.launch {
            // Collect last 6 message pairs for conversational context
            val history = mutableListOf<Pair<String, String>>()
            val currentList = _messages.value
            for (i in 0 until currentList.size - 1) {
                if (currentList[i].sender == MessageSender.USER && currentList[i + 1].sender == MessageSender.JARVIS) {
                    history.add(Pair(currentList[i].text, currentList[i + 1].text))
                }
            }

            val reply = geminiService.query(prompt, history)

            _messages.update { list ->
                list.map { msg ->
                    if (msg.id == thinkingMsgId) {
                        msg.copy(
                            text = reply,
                            status = MessageStatus.SUCCESS
                        )
                    } else msg
                }
            }

            _systemState.update { it.copy(isThinking = false, statusText = "SYSTEM ONLINE // AWAITING COMMAND") }

            if (_systemState.value.ttsEnabled) {
                ttsManager.speak(reply, _systemState.value.activeLanguage)
            }
        }
    }

    private fun deliverJarvisResponse(reply: String, commandType: CommandType) {
        val jarvisMsg = ChatMessage(
            sender = MessageSender.JARVIS,
            text = reply,
            commandType = commandType,
            status = MessageStatus.SUCCESS
        )
        _messages.update { it + jarvisMsg }
        _systemState.update {
            it.copy(
                statusText = "COMMAND EXECUTED",
                lastExecutedAction = commandType.name
            )
        }

        if (_systemState.value.ttsEnabled) {
            ttsManager.speak(reply, _systemState.value.activeLanguage)
        }
    }

    fun speakMessage(text: String) {
        ttsManager.speak(text, _systemState.value.activeLanguage)
    }

    fun clearChat() {
        _messages.value = emptyList()
        ttsManager.stop()
    }

    override fun onCleared() {
        super.onCleared()
        deviceManager.cleanup()
        speechManager.destroy()
        ttsManager.shutdown()
    }
}
