package com.example.jarvis.model

import java.util.UUID

enum class LanguageMode(val displayName: String, val code: String, val speechLocaleTag: String) {
    ENGLISH("English", "en", "en-US"),
    BENGALI("বাংলা", "bn", "bn-BD"),
    BANGLISH("Banglish", "bng", "en-US")
}

enum class MessageSender {
    USER,
    JARVIS
}

enum class CommandType {
    NONE,
    TORCH_ON,
    TORCH_OFF,
    BATTERY_CHECK,
    CAMERA_LAUNCH,
    APP_OPEN,
    SETTINGS_WIFI,
    SETTINGS_BLUETOOTH,
    SETTINGS_SYSTEM,
    GEMINI_QUERY,
    GREETING
}

enum class MessageStatus {
    SUCCESS,
    EXECUTED,
    THINKING,
    ERROR
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val commandType: CommandType = CommandType.NONE,
    val status: MessageStatus = MessageStatus.SUCCESS,
    val actionDetail: String? = null
)

data class JarvisSystemState(
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val isThinking: Boolean = false,
    val voiceRmsLevel: Float = 0f,
    val isTorchOn: Boolean = false,
    val batteryPercentage: Int = -1,
    val isBatteryCharging: Boolean = false,
    val batteryHealth: String = "Good",
    val activeLanguage: LanguageMode = LanguageMode.ENGLISH,
    val ttsEnabled: Boolean = true,
    val statusText: String = "SYSTEM ONLINE // AWAITING COMMAND",
    val lastExecutedAction: String? = null
)

data class BatteryInfo(
    val percentage: Int,
    val isCharging: Boolean,
    val powerSource: String,
    val temperatureCelsius: Float,
    val health: String
)
