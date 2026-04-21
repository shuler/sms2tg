package com.shuler.sms2tg

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shuler.sms2tg.data.SettingsRepository

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_LOCKED_BOOT_COMPLETED &&
            action != "android.intent.action.QUICKBOOT_POWERON" &&
            action != "com.htc.intent.action.QUICKBOOT_POWERON"
        ) return

        if (SettingsRepository.get(context).isEnabled()) {
            ForwarderService.start(context)
        }
    }
}
