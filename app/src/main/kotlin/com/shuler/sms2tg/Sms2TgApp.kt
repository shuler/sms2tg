package com.shuler.sms2tg

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class Sms2TgApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val nm = getSystemService(NotificationManager::class.java)

        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_FORWARD,
                "SMS forwarding",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Status of forwarding incoming SMS to Telegram"
            }
        )

        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ERRORS,
                "Errors",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Message delivery errors"
            }
        )

        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_SERVICE,
                "Background service",
                NotificationManager.IMPORTANCE_MIN,
            ).apply {
                description = "Keeps the process alive so incoming SMS are received"
                setShowBadge(false)
            }
        )
    }

    companion object {
        const val CHANNEL_FORWARD = "forward"
        const val CHANNEL_ERRORS = "errors"
        const val CHANNEL_SERVICE = "service"
    }
}
