package com.shuler.sms2tg

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.WorkerParameters
import com.shuler.sms2tg.data.SettingsRepository
import com.shuler.sms2tg.telegram.MarkdownEscape
import com.shuler.sms2tg.telegram.TelegramClient

class ForwardSmsWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val settings = SettingsRepository.get(applicationContext)
        val token = settings.getToken()
        val chatId = settings.getChatId()

        if (token.isNullOrBlank() || chatId == null) {
            Log.w(TAG, "not configured, dropping SMS")
            return Result.failure()
        }

        val from = inputData.getString(KEY_FROM) ?: "unknown"
        val body = inputData.getString(KEY_BODY) ?: ""
        val simSlot = inputData.getInt(KEY_SIM_SLOT, -1)

        val template = settings.getTemplate()
        val simSuffix = if (settings.includeSim() && simSlot >= 0) {
            "\nSIM ${simSlot + 1}"
        } else ""

        val rendered = template
            .replace("{from}", MarkdownEscape.escape(from))
            .replace("{body}", MarkdownEscape.escape(body)) + simSuffix

        val result = TelegramClient(token).sendMessage(chatId, rendered, markdown = true)

        return result.fold(
            onSuccess = {
                settings.setLastSuccessAt(System.currentTimeMillis())
                Result.success()
            },
            onFailure = { e ->
                val msg = e.message ?: e.toString()
                Log.w(TAG, "send failed (attempt ${runAttemptCount + 1})")
                val permanent = msg.contains("401") ||
                    msg.contains("Unauthorized", ignoreCase = true) ||
                    msg.contains("chat not found", ignoreCase = true) ||
                    msg.contains("bot was blocked", ignoreCase = true)
                if (permanent || runAttemptCount >= MAX_ATTEMPTS) {
                    settings.setLastError(System.currentTimeMillis(), msg)
                    Result.failure()
                } else {
                    Result.retry()
                }
            }
        )
    }

    companion object {
        private const val TAG = "ForwardSmsWorker"
        private const val MAX_ATTEMPTS = 10

        const val KEY_FROM = "from"
        const val KEY_BODY = "body"
        const val KEY_TIMESTAMP = "timestamp"
        const val KEY_SIM_SLOT = "sim_slot"

        fun constraints(): Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    }
}
