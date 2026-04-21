package com.shuler.sms2tg.ui

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shuler.sms2tg.ForwarderService
import com.shuler.sms2tg.data.SettingsRepository
import com.shuler.sms2tg.telegram.MarkdownEscape
import com.shuler.sms2tg.telegram.TelegramClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UiState(
    val enabled: Boolean = false,
    val smsPermissionGranted: Boolean = false,
    val token: String = "",
    val tokenStatus: TokenStatus = TokenStatus.Untested,
    val botUsername: String? = null,
    val chatId: Long? = null,
    val chatTitle: String? = null,
    val chatStatus: ChatStatus = ChatStatus.Idle,
    val template: String = SettingsRepository.DEFAULT_TEMPLATE,
    val includeSim: Boolean = false,
    val testStatus: TestStatus = TestStatus.Idle,
    val lastSmsSeenAt: Long = 0L,
    val lastSuccessAt: Long = 0L,
    val lastErrorAt: Long = 0L,
    val lastError: String? = null,
)

sealed class TokenStatus {
    data object Untested : TokenStatus()
    data object Checking : TokenStatus()
    data class Ok(val username: String) : TokenStatus()
    data class Error(val message: String) : TokenStatus()
}

sealed class ChatStatus {
    data object Idle : ChatStatus()
    data object Searching : ChatStatus()
    data class Found(val id: Long, val title: String) : ChatStatus()
    data object NotFound : ChatStatus()
    data class Error(val message: String) : ChatStatus()
}

sealed class TestStatus {
    data object Idle : TestStatus()
    data object Sending : TestStatus()
    data object Ok : TestStatus()
    data class Error(val message: String) : TestStatus()
}

class SetupViewModel(app: Application) : AndroidViewModel(app) {

    private val settings = SettingsRepository.get(app)

    private val _state = MutableStateFlow(loadState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private fun loadState(): UiState {
        val username = settings.getBotUsername()
        val chatId = settings.getChatId()
        val chatTitle = settings.getChatTitle()
        return UiState(
            enabled = settings.isEnabled(),
            smsPermissionGranted = checkSmsPermission(),
            token = settings.getToken().orEmpty(),
            tokenStatus = username?.let { TokenStatus.Ok(it) } ?: TokenStatus.Untested,
            botUsername = username,
            chatId = chatId,
            chatTitle = chatTitle,
            chatStatus = if (chatId != null) {
                ChatStatus.Found(chatId, chatTitle.orEmpty())
            } else ChatStatus.Idle,
            template = settings.getTemplate(),
            includeSim = settings.includeSim(),
            lastSmsSeenAt = settings.getLastSmsSeenAt(),
            lastSuccessAt = settings.getLastSuccessAt(),
            lastErrorAt = settings.getLastErrorAt(),
            lastError = settings.getLastError(),
        )
    }

    fun refreshRuntimeState() {
        _state.value = _state.value.copy(
            smsPermissionGranted = checkSmsPermission(),
            lastSmsSeenAt = settings.getLastSmsSeenAt(),
            lastSuccessAt = settings.getLastSuccessAt(),
            lastErrorAt = settings.getLastErrorAt(),
            lastError = settings.getLastError(),
        )
    }

    private fun checkSmsPermission(): Boolean = ContextCompat.checkSelfPermission(
        getApplication(),
        Manifest.permission.RECEIVE_SMS,
    ) == PackageManager.PERMISSION_GRANTED

    fun updateToken(value: String) {
        val trimmed = value.trim()
        _state.value = _state.value.copy(
            token = trimmed,
            tokenStatus = TokenStatus.Untested,
            botUsername = null,
        )
        settings.setToken(trimmed)
        settings.setBotUsername(null)
    }

    fun checkToken() {
        val token = _state.value.token
        if (token.isBlank()) return
        _state.value = _state.value.copy(tokenStatus = TokenStatus.Checking)
        viewModelScope.launch {
            val result = TelegramClient(token).getMe()
            result.fold(
                onSuccess = { info ->
                    settings.setBotUsername(info.username)
                    _state.value = _state.value.copy(
                        tokenStatus = TokenStatus.Ok(info.username),
                        botUsername = info.username,
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        tokenStatus = TokenStatus.Error(e.message ?: "unknown error"),
                    )
                }
            )
        }
    }

    fun findChat() {
        val token = _state.value.token
        if (token.isBlank()) return
        _state.value = _state.value.copy(chatStatus = ChatStatus.Searching)
        viewModelScope.launch {
            val result = TelegramClient(token).getUpdates()
            result.fold(
                onSuccess = { updates ->
                    val chat = updates.asReversed()
                        .firstNotNullOfOrNull { it.message?.chat }
                    if (chat == null) {
                        _state.value = _state.value.copy(chatStatus = ChatStatus.NotFound)
                    } else {
                        val title = chat.title
                            ?: chat.username?.let { "@$it" }
                            ?: listOfNotNull(chat.first_name, chat.last_name)
                                .joinToString(" ")
                                .ifBlank { "id ${chat.id}" }
                        settings.setChatId(chat.id)
                        settings.setChatTitle(title)
                        _state.value = _state.value.copy(
                            chatStatus = ChatStatus.Found(chat.id, title),
                            chatId = chat.id,
                            chatTitle = title,
                        )
                    }
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        chatStatus = ChatStatus.Error(e.message ?: "unknown error"),
                    )
                }
            )
        }
    }

    fun sendTest() {
        val token = _state.value.token
        val chatId = _state.value.chatId
        if (token.isBlank() || chatId == null) return
        _state.value = _state.value.copy(testStatus = TestStatus.Sending)
        viewModelScope.launch {
            val text = "✅ Test message from SMS2TG"
            val result = TelegramClient(token)
                .sendMessage(chatId, MarkdownEscape.escape(text), markdown = true)
            result.fold(
                onSuccess = {
                    settings.setLastSuccessAt(System.currentTimeMillis())
                    _state.value = _state.value.copy(
                        testStatus = TestStatus.Ok,
                        lastSuccessAt = System.currentTimeMillis(),
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        testStatus = TestStatus.Error(e.message ?: "unknown error"),
                    )
                }
            )
        }
    }

    fun setEnabled(value: Boolean) {
        settings.setEnabled(value)
        _state.value = _state.value.copy(enabled = value)
        val ctx = getApplication<Application>()
        if (value) ForwarderService.start(ctx) else ForwarderService.stop(ctx)
    }

    fun setTemplate(value: String) {
        settings.setTemplate(value)
        _state.value = _state.value.copy(template = value)
    }

    fun setIncludeSim(value: Boolean) {
        settings.setIncludeSim(value)
        _state.value = _state.value.copy(includeSim = value)
    }
}
