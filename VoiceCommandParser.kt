package com.example.jarvis.service

import com.example.jarvis.model.CommandType
import com.example.jarvis.model.LanguageMode

data class ParsedCommand(
    val type: CommandType,
    val targetApp: String? = null,
    val queryParam: String? = null,
    val responseEn: String,
    val responseBn: String,
    val responseBng: String
) {
    fun getLocalizedResponse(language: LanguageMode): String {
        return when (language) {
            LanguageMode.ENGLISH -> responseEn
            LanguageMode.BENGALI -> responseBn
            LanguageMode.BANGLISH -> responseBng
        }
    }
}

object VoiceCommandParser {

    fun parse(input: String): ParsedCommand {
        val raw = input.trim()
        val lower = raw.lowercase()

        // 1. Flashlight / Torch ON
        if (matchesAny(
                lower,
                // English
                "turn on flashlight", "turn on torch", "flashlight on", "torch on", "light on",
                "enable flashlight", "enable torch", "turn on the light", "turn light on",
                // Bengali
                "ফ্ল্যাশলাইট অন করো", "টর্চ জ্বালাও", "আলো জ্বালাও", "টর্চ অন করো",
                "ফ্ল্যাশলাইট জ্বালাও", "টর্চ চালু করো", "আলো অন করো", "ফ্লাশলাইট জ্বালাও",
                // Banglish
                "flashlight on koro", "torch jalao", "light jalao", "torch on koro",
                "light on koro", "flashlight jalao", "torch chalu koro", "alo jalao"
            )
        ) {
            return ParsedCommand(
                type = CommandType.TORCH_ON,
                responseEn = "Illumination protocols engaged. Flashlight is now ON, Sir.",
                responseBn = "ফ্ল্যাশলাইট চালু করা হয়েছে, স্যার।",
                responseBng = "Flashlight on kore diyechi, Sir."
            )
        }

        // 2. Flashlight / Torch OFF
        if (matchesAny(
                lower,
                // English
                "turn off flashlight", "turn off torch", "flashlight off", "torch off", "light off",
                "disable flashlight", "disable torch", "turn off the light", "turn light off",
                // Bengali
                "ফ্ল্যাশলাইট বন্ধ করো", "টর্চ বন্ধ করো", "আলো নিভাও", "আলো বন্ধ করো",
                "টর্চ অফ করো", "ফ্ল্যাশলাইট অফ করো", "ফ্লাশলাইট বন্ধ করো",
                // Banglish
                "flashlight off koro", "torch bondho koro", "light off koro", "torch off koro",
                "light bondho koro", "flashlight bondho koro", "alo nivao", "torch off"
            )
        ) {
            return ParsedCommand(
                type = CommandType.TORCH_OFF,
                responseEn = "Torch disabled. Conserving auxiliary power.",
                responseBn = "ফ্ল্যাশলাইট বন্ধ করা হয়েছে, স্যার।",
                responseBng = "Flashlight bondho kore diyechi, Sir."
            )
        }

        // 3. Battery Status
        if (matchesAny(
                lower,
                // English
                "battery", "battery status", "battery level", "battery percentage", "check battery",
                "how much battery", "power status", "charge level", "battery life",
                // Bengali
                "ব্যাটারি কত", "ব্যাটারি কত পার্সেন্ট", "চার্জ কত আছে", "ব্যাটারির অবস্থা",
                "চার্জ কত", "ব্যাটারি চেক করো", "ব্যাটারি লেভেল কত",
                // Banglish
                "battery koto", "charge koto", "battery status bolo", "charge koto ache",
                "battery percentage koto", "battery check koro", "charge koto percent"
            )
        ) {
            return ParsedCommand(
                type = CommandType.BATTERY_CHECK,
                responseEn = "Diagnostics complete. Reading power cell telemetry.",
                responseBn = "ব্যাটারির তথ্য সংগ্রহ করা হচ্ছে।",
                responseBng = "Battery status check korchi, Sir."
            )
        }

        // 4. Camera Launch
        if (matchesAny(
                lower,
                // English
                "open camera", "launch camera", "take a picture", "take a photo", "camera", "start camera",
                // Bengali
                "ক্যামেরা খোলো", "ছবি তোলো", "ক্যামেরা চালু করো", "ক্যামেরা ওপেন করো",
                // Banglish
                "camera kholo", "camera open koro", "chobi tolo", "camera chalu koro"
            )
        ) {
            return ParsedCommand(
                type = CommandType.CAMERA_LAUNCH,
                responseEn = "Initializing optical visual sensors. Launching Camera.",
                responseBn = "ক্যামেরা চালু করা হচ্ছে, স্যার।",
                responseBng = "Camera open korchi, Sir."
            )
        }

        // 5. Facebook
        if (matchesAny(
                lower,
                "open facebook", "launch facebook", "facebook", "open fb",
                "ফেসবুক খোলো", "ফেসবুক চালু করো", "ফেসবুক ওপেন করো",
                "facebook kholo", "fb kholo", "facebook open koro"
            )
        ) {
            return ParsedCommand(
                type = CommandType.APP_OPEN,
                targetApp = "facebook",
                responseEn = "Accessing social telemetry. Opening Facebook.",
                responseBn = "ফেসবুক ওপেন করা হচ্ছে।",
                responseBng = "Facebook open korchi, Sir."
            )
        }

        // 6. WhatsApp
        if (matchesAny(
                lower,
                "open whatsapp", "launch whatsapp", "whatsapp", "open wa",
                "হোয়াটসঅ্যাপ খোলো", "হোয়াটসঅ্যাপ চালু করো", "হোয়াটসঅ্যাপ ওপেন করো",
                "whatsapp kholo", "wa kholo", "whatsapp open koro"
            )
        ) {
            return ParsedCommand(
                type = CommandType.APP_OPEN,
                targetApp = "whatsapp",
                responseEn = "Establishing encrypted link. Launching WhatsApp.",
                responseBn = "হোয়াটসঅ্যাপ চালু করা হচ্ছে।",
                responseBng = "WhatsApp open korchi, Sir."
            )
        }

        // 7. YouTube
        if (matchesAny(
                lower,
                "open youtube", "launch youtube", "youtube", "open yt",
                "ইউটিউব খোলো", "ইউটিউব চালু করো", "ইউটিউব ওপেন করো",
                "youtube kholo", "yt kholo", "youtube open koro"
            )
        ) {
            return ParsedCommand(
                type = CommandType.APP_OPEN,
                targetApp = "youtube",
                responseEn = "Streaming frequency ready. Launching YouTube.",
                responseBn = "ইউটিউব চালু করা হচ্ছে।",
                responseBng = "YouTube open korchi, Sir."
            )
        }

        // 8. Chrome / Browser
        if (matchesAny(
                lower,
                "open chrome", "open browser", "launch chrome", "chrome", "google chrome",
                "ক্রোম খোলো", "ব্রাউজার খোলো", "ক্রোম ব্রাউজার খোলো", "ক্রোম চালু করো",
                "chrome kholo", "browser kholo", "chrome open koro", "browser open koro"
            )
        ) {
            return ParsedCommand(
                type = CommandType.APP_OPEN,
                targetApp = "chrome",
                responseEn = "Connecting to global network grid. Opening Google Chrome.",
                responseBn = "ক্রোম ব্রাউজার চালু করা হচ্ছে।",
                responseBng = "Chrome browser open korchi, Sir."
            )
        }

        // 9. Wi-Fi Settings
        if (matchesAny(
                lower,
                "open wifi settings", "open wifi", "wifi settings", "wifi", "turn on wifi",
                "ওয়াইফাই সেটিংস খোলো", "ওয়াইফাই সেটিংস", "ওয়াইফাই খোলো",
                "wifi settings kholo", "wifi kholo", "wifi open koro", "wifi settings"
            )
        ) {
            return ParsedCommand(
                type = CommandType.SETTINGS_WIFI,
                responseEn = "Opening wireless network settings interface.",
                responseBn = "ওয়াইফাই সেটিংস ওপেন করা হচ্ছে।",
                responseBng = "Wi-Fi settings open korchi, Sir."
            )
        }

        // 10. Bluetooth Settings
        if (matchesAny(
                lower,
                "open bluetooth settings", "open bluetooth", "bluetooth settings", "bluetooth",
                "ব্লুটুথ সেটিংস খোলো", "ব্লুটুথ খোলো", "ব্লুটুথ সেটিংস",
                "bluetooth settings kholo", "bluetooth kholo", "bluetooth settings"
            )
        ) {
            return ParsedCommand(
                type = CommandType.SETTINGS_BLUETOOTH,
                responseEn = "Opening Bluetooth communication settings.",
                responseBn = "ব্লুটুথ সেটিংস ওপেন করা হচ্ছে।",
                responseBng = "Bluetooth settings open korchi, Sir."
            )
        }

        // 11. System Settings
        if (matchesAny(
                lower,
                "open settings", "system settings", "android settings", "settings",
                "সেটিংস খোলো", "সিস্টেম সেটিংস খোলো", "সেটিংস",
                "settings kholo", "phone settings kholo", "settings open koro"
            )
        ) {
            return ParsedCommand(
                type = CommandType.SETTINGS_SYSTEM,
                responseEn = "Accessing core Android system configurations.",
                responseBn = "সিস্টেম সেটিংস খোলা হচ্ছে।",
                responseBng = "System settings open korchi, Sir."
            )
        }

        // 12. Generic Open App: "open X", "launch X", "X kholo", "X open koro", "X খোলো"
        val genericOpenMatch = parseGenericApp(lower)
        if (genericOpenMatch != null) {
            return ParsedCommand(
                type = CommandType.APP_OPEN,
                targetApp = genericOpenMatch,
                responseEn = "Locating application '$genericOpenMatch' in system manifests.",
                responseBn = "'$genericOpenMatch' অ্যাপটি খোঁজা হচ্ছে এবং চালু করা হচ্ছে।",
                responseBng = "'$genericOpenMatch' app open korchi, Sir."
            )
        }

        // 13. Greetings / Status
        if (matchesAny(
                lower,
                "who are you", "what is your name", "jarvis", "hello jarvis", "hi jarvis",
                "কে তুমি", "তোমার নাম কি", "কেমন আছো", "হ্যালো জারভিস",
                "tumi ke", "kemon acho", "ki obostha", "jarvis tumi ke", "hello"
            )
        ) {
            return ParsedCommand(
                type = CommandType.GREETING,
                responseEn = "Greetings. I am JARVIS, your personal cybernetic intelligence. All systems online and operational. How may I assist you today, Sir?",
                responseBn = "নমস্কার / আসসালামু আলাইকুম। আমি জারভিস, আপনার ব্যক্তিগত এআই সহকারী। সব সিস্টেম সচল রয়েছে। আপনাকে কীভাবে সাহায্য করতে পারি, স্যার?",
                responseBng = "Hello! Ami JARVIS, apnar personal AI assistant. Shob system online ache. Bolun kivabe sahajjo korte pari, Sir?"
            )
        }

        // 14. Fallback to Gemini AI conversational agent
        return ParsedCommand(
            type = CommandType.GEMINI_QUERY,
            queryParam = raw,
            responseEn = "Analyzing query with Gemini neural core...",
            responseBn = "জেমিনাই এআই দিয়ে বিশ্লেষণ করা হচ্ছে...",
            responseBng = "Gemini AI diye process korchi..."
        )
    }

    private fun matchesAny(text: String, vararg phrases: String): Boolean {
        for (phrase in phrases) {
            if (text == phrase || text.startsWith("$phrase ") || text.endsWith(" $phrase") || text.contains(phrase)) {
                return true
            }
        }
        return false
    }

    private fun parseGenericApp(text: String): String? {
        val prefixes = listOf("open app ", "open ", "launch app ", "launch ")
        for (prefix in prefixes) {
            if (text.startsWith(prefix) && text.length > prefix.length) {
                val candidate = text.removePrefix(prefix).trim()
                if (candidate.isNotEmpty() && !candidate.contains("settings") && !candidate.contains("wifi")) {
                    return candidate
                }
            }
        }

        val suffixes = listOf(" kholo", " open koro", " chalu koro", " অ্যাপ খোলো", " অ্যাপটি খোলো", " খোলো")
        for (suffix in suffixes) {
            if (text.endsWith(suffix) && text.length > suffix.length) {
                val candidate = text.removeSuffix(suffix).trim()
                if (candidate.isNotEmpty() && !candidate.contains("settings")) {
                    return candidate
                }
            }
        }

        return null
    }
}
