package com.shuler.sms2tg.telegram

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class TelegramClient(private val token: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getMe(): Result<BotInfo> = withContext(Dispatchers.IO) {
        runCatchingCancellable {
            val envelope = httpGet<ApiResult<BotInfo>>("getMe")
            envelope.requireResult()
        }
    }

    suspend fun getUpdates(): Result<List<TgUpdate>> = withContext(Dispatchers.IO) {
        runCatchingCancellable {
            val envelope = httpGet<ApiResult<List<TgUpdate>>>("getUpdates")
            envelope.result ?: emptyList()
        }
    }

    suspend fun sendMessage(
        chatId: Long,
        text: String,
        markdown: Boolean = true,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatchingCancellable {
            val body = FormBody.Builder()
                .add("chat_id", chatId.toString())
                .add("text", text)
                .apply { if (markdown) add("parse_mode", "MarkdownV2") }
                .add("disable_web_page_preview", "true")
                .build()
            val req = Request.Builder()
                .url("https://api.telegram.org/bot$token/sendMessage".toHttpUrl())
                .post(body)
                .build()
            client.newCall(req).execute().use { response ->
                val responseText = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    error(parseErrorOrHttp(responseText, response.code))
                }
                val env = json.decodeFromString<ApiEnvelope>(responseText)
                if (!env.ok) error(env.description ?: "Telegram API error")
            }
        }
    }

    private inline fun <reified T> httpGet(method: String): T {
        val req = Request.Builder()
            .url("https://api.telegram.org/bot$token/$method".toHttpUrl())
            .build()
        client.newCall(req).execute().use { response ->
            val responseText = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                error(parseErrorOrHttp(responseText, response.code))
            }
            return json.decodeFromString(responseText)
        }
    }

    private fun parseErrorOrHttp(body: String, code: Int): String = try {
        val env = json.decodeFromString<ApiEnvelope>(body)
        "HTTP $code: ${env.description ?: body}"
    } catch (_: Exception) {
        "HTTP $code: $body"
    }
}

private inline fun <R> runCatchingCancellable(block: () -> R): Result<R> = try {
    Result.success(block())
} catch (ce: CancellationException) {
    throw ce
} catch (t: Throwable) {
    Result.failure(t)
}

private fun <T> ApiResult<T>.requireResult(): T =
    result ?: error(description ?: "Telegram API error")

@Serializable
data class ApiEnvelope(
    val ok: Boolean = false,
    val description: String? = null,
    val error_code: Int? = null,
)

@Serializable
data class ApiResult<T>(
    val ok: Boolean = false,
    val result: T? = null,
    val description: String? = null,
    val error_code: Int? = null,
)

@Serializable
data class BotInfo(
    val id: Long,
    val username: String,
    val first_name: String = "",
    val is_bot: Boolean = true,
)

@Serializable
data class TgUpdate(
    val update_id: Long,
    val message: TgMessage? = null,
)

@Serializable
data class TgMessage(
    val message_id: Long,
    val chat: TgChat,
)

@Serializable
data class TgChat(
    val id: Long,
    val type: String,
    val title: String? = null,
    val username: String? = null,
    val first_name: String? = null,
    val last_name: String? = null,
)
