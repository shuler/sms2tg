package com.shuler.sms2tg.util

import android.content.Intent
import android.telephony.SmsMessage

data class ParsedSms(
    val from: String,
    val body: String,
    val timestamp: Long,
    val simSlot: Int?,
)

object SmsFormatter {
    private val SIM_EXTRA_KEYS = listOf("subscription", "phone", "slot", "simId", "simSlot")

    fun fromMessages(messages: Array<SmsMessage>, intent: Intent): ParsedSms {
        val first = messages.firstOrNull()
        val from = first?.displayOriginatingAddress
            ?: first?.originatingAddress
            ?: "unknown"

        val body = messages.joinToString("") { it.messageBody ?: "" }
        val timestamp = first?.timestampMillis ?: System.currentTimeMillis()

        @Suppress("DEPRECATION")
        val slot = SIM_EXTRA_KEYS.firstNotNullOfOrNull { key ->
            intent.extras?.get(key) as? Int
        }

        return ParsedSms(from = from, body = body, timestamp = timestamp, simSlot = slot)
    }
}
