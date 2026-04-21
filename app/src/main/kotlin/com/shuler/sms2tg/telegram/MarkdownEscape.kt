package com.shuler.sms2tg.telegram

object MarkdownEscape {
    private val SPECIAL = "_*[]()~`>#+-=|{}.!\\".toCharArray().toSet()

    fun escape(text: String): String = buildString(text.length) {
        for (c in text) {
            if (c in SPECIAL) append('\\')
            append(c)
        }
    }
}
