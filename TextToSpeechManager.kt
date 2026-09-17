package com.example.jarvis.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.jarvis.model.LanguageMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

class TextToSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speakingRms = MutableStateFlow(0f)
    val speakingRms: StateFlow<Float> = _speakingRms.asStateFlow()

    private var waveformJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.setPitch(1.02f)
            tts?.setSpeechRate(1.05f)

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    startSpeakingWaveform()
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    stopSpeakingWaveform()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    stopSpeakingWaveform()
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                    stopSpeakingWaveform()
                }
            })
        }
    }

    fun speak(text: String, languageMode: LanguageMode, onCompleted: (() -> Unit)? = null) {
        if (!isInitialized || tts == null) return

        stop()

        val cleanText = text.replace(Regex("[#*`_]"), "").trim()
        if (cleanText.isEmpty()) return

        // Configure language
        val hasBengaliChars = cleanText.any { it in '\u0980'..'\u09FF' }
        val targetLocale = if (hasBengaliChars || languageMode == LanguageMode.BENGALI) {
            val bengaliLocale = Locale.forLanguageTag("bn-BD")
            val result = tts?.isLanguageAvailable(bengaliLocale) ?: TextToSpeech.LANG_NOT_SUPPORTED
            if (result >= TextToSpeech.LANG_AVAILABLE) {
                bengaliLocale
            } else {
                Locale.US
            }
        } else {
            Locale.US
        }

        try {
            tts?.language = targetLocale
        } catch (_: Exception) {
            tts?.language = Locale.US
        }

        val utteranceId = UUID.randomUUID().toString()
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    private fun startSpeakingWaveform() {
        waveformJob?.cancel()
        waveformJob = scope.launch {
            while (isActive && _isSpeaking.value) {
                // Generate natural animated speech oscillation for the waveform
                val base = Random.nextFloat() * 0.6f + 0.3f
                _speakingRms.value = base
                delay(80)
            }
            _speakingRms.value = 0f
        }
    }

    private fun stopSpeakingWaveform() {
        waveformJob?.cancel()
        _speakingRms.value = 0f
    }

    fun stop() {
        try {
            tts?.stop()
            _isSpeaking.value = false
            stopSpeakingWaveform()
        } catch (_: Exception) {
        }
    }

    fun shutdown() {
        try {
            stop()
            tts?.shutdown()
            tts = null
        } catch (_: Exception) {
        }
    }
}
