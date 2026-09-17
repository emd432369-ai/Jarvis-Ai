package com.example.jarvis.service

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = false)
data class GeminiPart(
    val text: String? = null
)

@JsonClass(generateAdapter = false)
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = false)
data class GeminiGenerationConfig(
    val temperature: Float = 0.7f,
    val maxOutputTokens: Int = 800
)

@JsonClass(generateAdapter = false)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null,
    val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = false)
data class GeminiCandidate(
    val content: GeminiContent?
)

@JsonClass(generateAdapter = false)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null,
    val error: GeminiError? = null
)

@JsonClass(generateAdapter = false)
data class GeminiError(
    val message: String? = null,
    val code: Int? = null
)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

class GeminiService {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        })
        .build()

    private val api: GeminiApi = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(GeminiApi::class.java)

    private val systemInstruction = GeminiContent(
        parts = listOf(
            GeminiPart(
                text = "You are JARVIS (Just A Rather Very Intelligent System), a futuristic, highly intelligent, loyal personal AI assistant inspired by Tony Stark's AI. " +
                        "You serve the user with utmost respect, calling them 'Sir' or 'Boss' as fitting. " +
                        "You are fully bilingual/trilingual in English, Bengali (বাংলা), and Banglish (Bengali written in English letters). " +
                        "CRITICAL: Always reply in the exact language style of the user's prompt. If they speak in Bengali script, answer in elegant Bengali. If they speak Banglish, answer in conversational natural Banglish. If English, answer in crisp, witty, polite English. " +
                        "Keep your responses concise, futuristic, practical, and direct (within 2-4 sentences unless detailed explanation is asked)."
            )
        )
    )

    fun isApiKeyConfigured(): Boolean {
        val key = getApiKey()
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }
    }

    suspend fun query(prompt: String, conversationHistory: List<Pair<String, String>> = emptyList()): String =
        withContext(Dispatchers.IO) {
            val apiKey = getApiKey()
            if (!isApiKeyConfigured()) {
                return@withContext getOfflineLocalResponse(prompt)
            }

            val contents = mutableListOf<GeminiContent>()

            // Add recent history for context
            conversationHistory.takeLast(4).forEach { (userMsg, aiMsg) ->
                contents.add(GeminiContent(role = "user", parts = listOf(GeminiPart(text = userMsg))))
                contents.add(GeminiContent(role = "model", parts = listOf(GeminiPart(text = aiMsg))))
            }

            // Current prompt
            contents.add(GeminiContent(role = "user", parts = listOf(GeminiPart(text = prompt))))

            val request = GeminiRequest(
                contents = contents,
                systemInstruction = systemInstruction,
                generationConfig = GeminiGenerationConfig(temperature = 0.7f, maxOutputTokens = 600)
            )

            try {
                val response = api.generateContent(apiKey, request)
                val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!reply.isNullOrBlank()) {
                    reply.trim()
                } else {
                    response.error?.message ?: "Neural link returned an empty signal, Sir."
                }
            } catch (e: Exception) {
                // Fallback to offline assistant response if network error or quota exceeded
                getOfflineLocalResponse(prompt, networkError = e.localizedMessage)
            }
        }

    private fun getOfflineLocalResponse(prompt: String, networkError: String? = null): String {
        val lower = prompt.lowercase()
        val isBengali = prompt.any { it in '\u0980'..'\u09FF' }
        val isBanglish = lower.contains("kemon") || lower.contains("ki ") || lower.contains("bolo") || lower.contains("korcho")

        return when {
            isBengali -> {
                "আমি আপনার নির্দেশ শুনতে পাচ্ছি, স্যার। ${if (networkError != null) "সার্ভার সংযোগে সমস্যা হয়েছে ($networkError)।" else "সম্পূর্ণ জেমিনাই ক্লাউড বুদ্ধিমত্তা সক্রিয় করতে সিক্রেটস প্যানেলে GEMINI_API_KEY যুক্ত করুন।"} আমি ডিভাইস নিয়ন্ত্রণ ও ভয়েস কমান্ডের জন্য সম্পূর্ণ প্রস্তুত!"
            }
            isBanglish -> {
                "Ami apnar kotha shunte pacchi, Sir. ${if (networkError != null) "Cloud connection error: $networkError." else "Full Gemini AI cloud intelligence on korte Secrets panele GEMINI_API_KEY add korun."} Device controls & commands ekdom ready!"
            }
            else -> {
                "I am monitoring all telemetry, Sir. ${if (networkError != null) "Neural relay warning: $networkError." else "To activate full Gemini cloud cognition, provide your GEMINI_API_KEY in the Secrets panel."} Local hardware controls, voice diagnostics, and system utilities remain fully operational."
            }
        }
    }
}
