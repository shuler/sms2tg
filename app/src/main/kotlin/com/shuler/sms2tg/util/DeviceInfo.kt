package com.shuler.sms2tg.util

import android.annotation.SuppressLint
import android.os.Build

object DeviceInfo {

    fun isMiui(): Boolean {
        if (isXiaomiBrand()) return true
        return hasMiuiSystemProperty()
    }

    private fun isXiaomiBrand(): Boolean {
        val values = listOf(Build.MANUFACTURER, Build.BRAND)
        return values.any { v ->
            v.equals("Xiaomi", ignoreCase = true) ||
                v.equals("Redmi", ignoreCase = true) ||
                v.equals("POCO", ignoreCase = true)
        }
    }

    @SuppressLint("PrivateApi")
    private fun hasMiuiSystemProperty(): Boolean = try {
        val clazz = Class.forName("android.os.SystemProperties")
        val get = clazz.getMethod("get", String::class.java)
        val raw = get.invoke(null, "ro.miui.ui.version.name")
        val version = raw as? String
        version != null && version.isNotBlank()
    } catch (_: Throwable) {
        false
    }
}
