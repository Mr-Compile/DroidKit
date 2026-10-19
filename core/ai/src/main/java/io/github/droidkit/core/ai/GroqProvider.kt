package io.github.droidkit.core.ai

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Groq chat-completions provider (OpenAI-compatible API).
 * Zero third-party HTTP deps — plain HttpURLConnection on Dispatchers.IO.
 */
@Singleton
class GroqProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val store: AiSettingsStore,
) : AiProvider {

    override suspend fun testConnection(): AiError? {
        if (apiKeyOrNull() == null) return AiError.NO_KEY
        if (!isOnline()) return AiError.OFFLINE
        return complete(AiRequest(SYSTEM_PING, "ping", maxTokens = 8))
            .fold({ null }, { (it as? AiException)?.error ?: AiError.PROVIDER_ERROR })
    }

    override suspend fun complete(request: AiRequest): Result<AiResponse> =
        withContext(Dispatchers.IO) {
            val key = apiKeyOrNull() ?: return@withContext Result.failure(
                AiException(AiError.NO_KEY, "no API key stored"),
            )
            if (!isOnline()) {
                return@withContext Result.failure(AiException(AiError.OFFLINE, "device offline"))
            }
            runCatching {
                val model = store.model.first()
                val payload = JSONObject()
                    .put("model", model)
                    .put("max_tokens", request.maxTokens)
                    .put("temperature", request.temperature)
                    .put(
                        "messages",
                        JSONArray()
                            .put(JSONObject().put("role", "system").put("content", request.system))
                            .put(JSONObject().put("role", "user").put("content", request.user)),
                    )

                val conn = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = TIMEOUT_MS
                    readTimeout = TIMEOUT_MS
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Authorization", "Bearer $key")
                    doOutput = true
                }
                conn.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }

                val code = conn.responseCode
                val body = runCatching {
                    (if (code in 200..299) conn.inputStream else conn.errorStream)
                        ?.bufferedReader()?.readText().orEmpty()
                }.getOrDefault("")
                conn.disconnect()

                when (code) {
                    in 200..299 -> parse(body)
                    401, 403 -> throw AiException(AiError.AUTH_ERROR, "HTTP $code — check API key")
                    429 -> throw AiException(AiError.RATE_LIMITED, "HTTP 429 — rate limited")
                    else -> throw AiException(AiError.PROVIDER_ERROR, "HTTP $code")
                }
            }.recoverCatching { t ->
                if (t is AiException) throw t
                if (t is IOException) throw AiException(AiError.OFFLINE, t.message ?: "io error")
                throw AiException(AiError.PROVIDER_ERROR, t.message ?: "unknown error")
            }
        }

    private fun parse(body: String): AiResponse {
        val text = runCatching {
            JSONObject(body)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        }.getOrNull() ?: throw AiException(AiError.MALFORMED_RESPONSE, "unexpected response shape")
        val model = runCatching { JSONObject(body).optString("model") }.getOrDefault("")
        return AiResponse(text, model)
    }

    private suspend fun apiKeyOrNull(): String? = store.apiKey()

    private fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val caps = cm.activeNetwork?.let { cm.getNetworkCapabilities(it) } ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private companion object {
        const val ENDPOINT = "https://api.groq.com/openai/v1/chat/completions"
        const val TIMEOUT_MS = 20_000
        const val SYSTEM_PING = "Reply with the single word: ok"
    }
}
