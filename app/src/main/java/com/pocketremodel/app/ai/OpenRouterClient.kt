package com.pocketremodel.app.ai

import android.util.Log
import com.pocketremodel.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * The AI brain's connection to the outside world.
 *
 * This class IS the "9Router traffic controller" from the blueprint, built natively:
 *  - It talks to OpenRouter (one key -> every top AI model on earth).
 *  - It tries a prioritized chain of FREE models. If one is slow, rate-limited, or
 *    returns junk, it instantly falls through to the next — the user never sees a glitch.
 *  - "openrouter/free" is OpenRouter's own auto-router and sits first in the chain.
 */
class OpenRouterClient(
    /** Live key — updated at runtime after the user pastes it in setup. */
    @Volatile var apiKey: String = BuildConfig.OPENROUTER_API_KEY
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val http = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    /**
     * Ordered failover chain. First that succeeds wins.
     * All are free tiers. Reorder to taste; add paid models below the free ones
     * if you ever want a premium fallback.
     */
    private val modelChain = listOf(
        "openrouter/free",                       // OpenRouter's own smart free auto-router
        "meta-llama/llama-4-maverick:free",      // strong, supports tool/JSON, 128k ctx
        "meta-llama/llama-4-scout:free",         // fast fallback
        "deepseek/deepseek-chat:free",           // reliable JSON follower
        "google/gemini-2.0-flash-exp:free"       // last-resort fallback
    )

    /** A single chat turn. Returns the assistant's raw reply text, or null if all models fail. */
    suspend fun complete(
        systemPrompt: String,
        history: List<ChatMessage>,
        userText: String
    ): String? = withContext(Dispatchers.IO) {

        if (apiKey.isBlank()) {
            Log.e(TAG, "No OpenRouter key set. Add OPENROUTER_API_KEY to local.properties.")
            return@withContext null
        }

        val messages = buildList {
            add(ChatMessage("system", systemPrompt))
            addAll(history.takeLast(8))           // keep recent context, stay cheap
            add(ChatMessage("user", userText))
        }

        for (model in modelChain) {
            val reply = runCatching { callOnce(model, messages) }
                .onFailure { Log.w(TAG, "Model $model failed: ${it.message}") }
                .getOrNull()
            if (!reply.isNullOrBlank()) {
                Log.i(TAG, "Answered by: $model")
                return@withContext reply
            }
        }
        Log.e(TAG, "Entire model chain exhausted.")
        null
    }

    /**
     * Tiny round-trip used by the setup screen's "Test Connection" button.
     * Returns success (green check) or a friendly failure message (red X).
     */
    suspend fun testConnection(key: String): Result<String> = withContext(Dispatchers.IO) {
        if (key.isBlank()) return@withContext Result.failure(Exception("Paste your key first."))
        val probe = listOf(ChatMessage("user", "ping"))
        val payload = ChatRequest(
            model = "meta-llama/llama-4-scout:free",
            messages = probe,
            maxTokens = 5,
            responseFormat = null
        )
        val body = json.encodeToString(ChatRequest.serializer(), payload)
            .toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://openrouter.ai/api/v1/chat/completions")
            .addHeader("Authorization", "Bearer ${key.trim()}")
            .addHeader("HTTP-Referer", "https://pocketremodel.app")
            .addHeader("X-Title", "Pocket Remodel")
            .post(body)
            .build()
        runCatching {
            http.newCall(request).execute().use { resp ->
                when (resp.code) {
                    in 200..299 -> Result.success("Connected!")
                    401 -> Result.failure(Exception("That key was rejected — double-check it."))
                    402 -> Result.success("Connected (free quota used — add credit for more).")
                    429 -> Result.success("Connected (busy right now, but the key works).")
                    else -> Result.failure(Exception("Couldn't connect (code ${resp.code})."))
                }
            }
        }.getOrElse { Result.failure(Exception("No internet, or the service is unreachable.")) }
    }

    /**
     * Looks at a photo and returns the catalog id of the closest furniture we have.
     * Uses a free VISION model (Llama 4 Maverick). [imageDataUrl] is a
     * "data:image/jpeg;base64,..." string. Returns the raw id text or null.
     */
    suspend fun visionMatchCatalog(imageDataUrl: String, manifest: String): String? =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) return@withContext null

            val instruction = """
                You are matching a photo to a furniture catalog. Here is the catalog:
                $manifest
                Reply with ONLY the single best-matching id from the list. No other words.
            """.trimIndent()

            // Build the multimodal request. OpenRouter expects "content" to be an
            // array mixing a text part and an image_url part. We assemble it with the
            // JSON DSL so strings are escaped safely.
            val payload = buildJsonObject {
                put("model", "meta-llama/llama-4-maverick:free")
                put("max_tokens", 20)
                put("temperature", 0.0)
                putJsonArray("messages") {
                    addJsonObject {
                        put("role", "user")
                        putJsonArray("content") {
                            addJsonObject {
                                put("type", "text")
                                put("text", instruction)
                            }
                            addJsonObject {
                                put("type", "image_url")
                                putJsonObject("image_url") {
                                    put("url", imageDataUrl)
                                }
                            }
                        }
                    }
                }
            }
            val raw = payload.toString()

            val request = Request.Builder()
                .url("https://openrouter.ai/api/v1/chat/completions")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("HTTP-Referer", "https://pocketremodel.app")
                .addHeader("X-Title", "Pocket Remodel")
                .post(raw.toRequestBody("application/json".toMediaType()))
                .build()

            runCatching {
                http.newCall(request).execute().use { resp ->
                    val text = resp.body?.string().orEmpty()
                    if (!resp.isSuccessful) return@use null
                    json.decodeFromString(ChatResponse.serializer(), text)
                        .choices.firstOrNull()?.message?.content?.trim()
                }
            }.getOrNull()
        }

    private fun callOnce(model: String, messages: List<ChatMessage>): String? {
        val payload = ChatRequest(model = model, messages = messages)
        val body = json.encodeToString(ChatRequest.serializer(), payload)
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://openrouter.ai/api/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            // Optional but recommended by OpenRouter for free-tier ranking:
            .addHeader("HTTP-Referer", "https://pocketremodel.app")
            .addHeader("X-Title", "Pocket Remodel")
            .post(body)
            .build()

        http.newCall(request).execute().use { resp ->
            val raw = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                Log.w(TAG, "HTTP ${resp.code} on $model: $raw")
                return null
            }
            val parsed = json.decodeFromString(ChatResponse.serializer(), raw)
            parsed.error?.let { Log.w(TAG, "API error on $model: ${it.message}") }
            return parsed.choices.firstOrNull()?.message?.content
        }
    }

    companion object { private const val TAG = "OpenRouterClient" }
}
