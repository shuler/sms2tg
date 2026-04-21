package com.shuler.sms2tg

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.shuler.sms2tg.data.SettingsRepository
import com.shuler.sms2tg.util.SmsFormatter
import java.time.Duration

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        if (messages.isEmpty()) return

        val sms = SmsFormatter.fromMessages(messages, intent)
        val settings = SettingsRepository.get(context)

        settings.setLastSmsSeenAt(System.currentTimeMillis())
        Log.d(TAG, "received SMS (${sms.body.length} chars)")

        if (!settings.isEnabled()) {
            Log.d(TAG, "forwarding disabled, skipping send")
            return
        }

        val data = Data.Builder()
            .putString(ForwardSmsWorker.KEY_FROM, sms.from)
            .putString(ForwardSmsWorker.KEY_BODY, sms.body)
            .putLong(ForwardSmsWorker.KEY_TIMESTAMP, sms.timestamp)
            .putInt(ForwardSmsWorker.KEY_SIM_SLOT, sms.simSlot ?: -1)
            .build()

        val request = OneTimeWorkRequestBuilder<ForwardSmsWorker>()
            .setInputData(data)
            .setConstraints(ForwardSmsWorker.constraints())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, Duration.ofSeconds(30))
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }

    companion object {
        private const val TAG = "SmsReceiver"
    }
}
