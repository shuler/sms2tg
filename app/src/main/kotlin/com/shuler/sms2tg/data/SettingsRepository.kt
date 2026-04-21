package com.shuler.sms2tg.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SettingsRepository private constructor(context: Context) {

    private val prefs: SharedPreferences = run {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)
    fun setToken(value: String?) = prefs.edit().putString(KEY_TOKEN, value).apply()

    fun getBotUsername(): String? = prefs.getString(KEY_BOT_USERNAME, null)
    fun setBotUsername(value: String?) = prefs.edit().putString(KEY_BOT_USERNAME, value).apply()

    fun getChatId(): Long? = prefs.getLong(KEY_CHAT_ID, Long.MIN_VALUE)
        .takeIf { it != Long.MIN_VALUE }

    fun setChatId(value: Long?) = prefs.edit().apply {
        if (value == null) remove(KEY_CHAT_ID) else putLong(KEY_CHAT_ID, value)
    }.apply()

    fun getChatTitle(): String? = prefs.getString(KEY_CHAT_TITLE, null)
    fun setChatTitle(value: String?) = prefs.edit().putString(KEY_CHAT_TITLE, value).apply()

    fun isEnabled(): Boolean = prefs.getBoolean(KEY_ENABLED, false)
    fun setEnabled(value: Boolean) = prefs.edit().putBoolean(KEY_ENABLED, value).apply()

    fun getTemplate(): String =
        prefs.getString(KEY_TEMPLATE, DEFAULT_TEMPLATE) ?: DEFAULT_TEMPLATE

    fun setTemplate(value: String) = prefs.edit().putString(KEY_TEMPLATE, value).apply()

    fun includeSim(): Boolean = prefs.getBoolean(KEY_INCLUDE_SIM, false)
    fun setIncludeSim(value: Boolean) = prefs.edit().putBoolean(KEY_INCLUDE_SIM, value).apply()

    fun getLastSuccessAt(): Long = prefs.getLong(KEY_LAST_SUCCESS, 0L)
    fun setLastSuccessAt(value: Long) = prefs.edit().putLong(KEY_LAST_SUCCESS, value).apply()

    fun getLastSmsSeenAt(): Long = prefs.getLong(KEY_LAST_SMS_SEEN, 0L)
    fun setLastSmsSeenAt(value: Long) = prefs.edit().putLong(KEY_LAST_SMS_SEEN, value).apply()

    fun getLastErrorAt(): Long = prefs.getLong(KEY_LAST_ERROR_AT, 0L)
    fun getLastError(): String? = prefs.getString(KEY_LAST_ERROR_MSG, null)
    fun setLastError(ts: Long, message: String?) = prefs.edit()
        .putLong(KEY_LAST_ERROR_AT, ts)
        .putString(KEY_LAST_ERROR_MSG, message)
        .apply()

    companion object {
        private const val FILE_NAME = "sms2tg_settings"

        private const val KEY_TOKEN = "bot_token"
        private const val KEY_BOT_USERNAME = "bot_username"
        private const val KEY_CHAT_ID = "chat_id"
        private const val KEY_CHAT_TITLE = "chat_title"
        private const val KEY_ENABLED = "enabled"
        private const val KEY_TEMPLATE = "template"
        private const val KEY_INCLUDE_SIM = "include_sim"
        private const val KEY_LAST_SUCCESS = "last_success_at"
        private const val KEY_LAST_SMS_SEEN = "last_sms_seen_at"
        private const val KEY_LAST_ERROR_AT = "last_error_at"
        private const val KEY_LAST_ERROR_MSG = "last_error_msg"

        const val DEFAULT_TEMPLATE = "📱 *{from}*\n{body}"

        @Volatile
        private var instance: SettingsRepository? = null

        fun get(context: Context): SettingsRepository =
            instance ?: synchronized(this) {
                instance ?: SettingsRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}
