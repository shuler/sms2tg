package com.shuler.sms2tg.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shuler.sms2tg.R

@Composable
fun SettingsScreen(
    vm: SetupViewModel = viewModel(),
    onOpenAppSettings: () -> Unit = {},
    onRequestPermissions: () -> Unit = {},
    onRequestBatteryExemption: () -> Unit = {},
    onOpenMiuiSettings: () -> Unit = {},
    isMiui: Boolean = false,
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    LifecycleResumeEffect(Unit) {
        vm.refreshRuntimeState()
        onPauseOrDispose { }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
        )

        StatusCard(
            state = state,
            onToggle = vm::setEnabled,
            onRequestPermissions = onRequestPermissions,
            onOpenAppSettings = onOpenAppSettings,
            onBatteryExemption = onRequestBatteryExemption,
            onOpenMiuiSettings = onOpenMiuiSettings,
            isMiui = isMiui,
        )

        StepCard(number = 1, title = "Create a bot") {
            Text("1. Open Telegram and message @BotFather")
            Text("2. Send the /newbot command")
            Text("3. Set a name and username (must end with \"bot\")")
            Text("4. Copy the token BotFather sends you")
            Text(
                text = "Recommended: /setjoingroups → Disable, /setprivacy → Enabled",
                style = MaterialTheme.typography.bodySmall,
            )
            FilledTonalButton(onClick = { openUri(context, "https://t.me/BotFather") }) {
                Text("Open @BotFather")
            }
        }

        StepCard(number = 2, title = "Bot token") {
            var visible by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = state.token,
                onValueChange = vm::updateToken,
                label = { Text("Token") },
                singleLine = true,
                visualTransformation = if (visible) VisualTransformation.None
                else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { visible = !visible }) {
                        Text(if (visible) "hide" else "show")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            )
            Button(
                onClick = vm::checkToken,
                enabled = state.token.isNotBlank() &&
                    state.tokenStatus !is TokenStatus.Checking,
            ) {
                Text("Check token")
            }
            StatusText(state.tokenStatus)
        }

        StepCard(number = 3, title = "Link a chat") {
            Text("1. Open your bot in Telegram and tap Start")
            Text("2. Come back here and tap \"Find chat\"")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val username = state.botUsername
                if (username != null) {
                    FilledTonalButton(
                        onClick = { openUri(context, "https://t.me/$username") }
                    ) { Text("Open bot") }
                }
                Button(
                    onClick = vm::findChat,
                    enabled = state.tokenStatus is TokenStatus.Ok &&
                        state.chatStatus !is ChatStatus.Searching,
                ) { Text("Find chat") }
            }
            ChatStatusText(state.chatStatus)
        }

        StepCard(number = 4, title = "Test") {
            Button(
                onClick = vm::sendTest,
                enabled = state.chatId != null &&
                    state.testStatus !is TestStatus.Sending,
            ) { Text("Send test message") }
            TestStatusText(state.testStatus)
        }

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Message format", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = state.template,
                    onValueChange = vm::setTemplate,
                    label = { Text("Template (MarkdownV2)") },
                    supportingText = { Text("Placeholders: {from}, {body}") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = state.includeSim, onCheckedChange = vm::setIncludeSim)
                    Spacer(Modifier.width(8.dp))
                    Text("Show SIM slot (for dual-SIM)")
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun StatusCard(
    state: UiState,
    onToggle: (Boolean) -> Unit,
    onRequestPermissions: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onBatteryExemption: () -> Unit,
    onOpenMiuiSettings: () -> Unit,
    isMiui: Boolean,
) {
    val canEnable = state.chatId != null &&
        state.tokenStatus is TokenStatus.Ok &&
        state.smsPermissionGranted

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("SMS forwarding", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = if (state.enabled) "Enabled" else "Disabled",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (state.enabled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = state.enabled,
                    onCheckedChange = onToggle,
                    enabled = canEnable || state.enabled,
                )
            }

            HorizontalDivider()

            ChecklistRow(
                ok = state.smsPermissionGranted,
                okText = "RECEIVE_SMS permission granted",
                failText = "⚠️ SMS read permission missing",
            )
            if (!state.smsPermissionGranted) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onRequestPermissions) { Text("Request") }
                    TextButton(onClick = onOpenAppSettings) { Text("Settings") }
                }
            }

            ChecklistRow(
                ok = state.tokenStatus is TokenStatus.Ok,
                okText = "Bot configured",
                failText = "Bot not configured (Step 1–2)",
            )
            ChecklistRow(
                ok = state.chatId != null,
                okText = "Chat linked",
                failText = "Chat not linked (Step 3)",
            )
            ChecklistRow(
                ok = state.enabled,
                okText = "Forwarding enabled",
                failText = "Forwarding disabled — turn on the switch above",
            )

            HorizontalDivider()

            DiagnosticRow(
                label = "Last incoming SMS",
                ts = state.lastSmsSeenAt,
                emptyText = "none yet",
            )
            DiagnosticRow(
                label = "Last Telegram delivery",
                ts = state.lastSuccessAt,
                emptyText = "none yet",
            )
            if (state.lastError != null) {
                Text(
                    text = "Last error: ${state.lastError}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            TextButton(onClick = onBatteryExemption) {
                Text("Disable battery optimization")
            }

            if (isMiui) {
                HorizontalDivider()
                Text(
                    text = "MIUI/HyperOS restricts background processes. Allow autostart and remove battery restrictions in the Security settings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FilledTonalButton(onClick = onOpenMiuiSettings) {
                    Text("MIUI settings")
                }
            }
        }
    }
}

@Composable
private fun ChecklistRow(ok: Boolean, okText: String, failText: String) {
    Text(
        text = if (ok) "✓ $okText" else "• $failText",
        style = MaterialTheme.typography.bodyMedium,
        color = if (ok) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun DiagnosticRow(label: String, ts: Long, emptyText: String) {
    val value = if (ts > 0) relativeTime(ts) else emptyText
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
private fun StepCard(
    number: Int,
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = {
                Text(
                    text = "Step $number. $title",
                    style = MaterialTheme.typography.titleMedium,
                )
                content()
            },
        )
    }
}

@Composable
private fun StatusText(status: TokenStatus) {
    when (status) {
        TokenStatus.Checking -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        is TokenStatus.Ok -> Text(
            text = "✓ Bot: @${status.username}",
            color = MaterialTheme.colorScheme.primary,
        )
        is TokenStatus.Error -> Text(
            text = "✗ ${status.message}",
            color = MaterialTheme.colorScheme.error,
        )
        TokenStatus.Untested -> {}
    }
}

@Composable
private fun ChatStatusText(status: ChatStatus) {
    when (status) {
        ChatStatus.Searching -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        is ChatStatus.Found -> Text(
            text = "✓ Chat: ${status.title} (id ${status.id})",
            color = MaterialTheme.colorScheme.primary,
        )
        ChatStatus.NotFound -> Text(
            text = "✗ No updates found. Send /start to the bot and try again.",
            color = MaterialTheme.colorScheme.error,
        )
        is ChatStatus.Error -> Text(
            text = "✗ ${status.message}",
            color = MaterialTheme.colorScheme.error,
        )
        ChatStatus.Idle -> {}
    }
}

@Composable
private fun TestStatusText(status: TestStatus) {
    when (status) {
        TestStatus.Sending -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        TestStatus.Ok -> Text(
            text = "✓ Delivered",
            color = MaterialTheme.colorScheme.primary,
        )
        is TestStatus.Error -> Text(
            text = "✗ ${status.message}",
            color = MaterialTheme.colorScheme.error,
        )
        TestStatus.Idle -> {}
    }
}

private fun openUri(context: Context, url: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

private fun relativeTime(ts: Long): String {
    val seconds = (System.currentTimeMillis() - ts) / 1000
    return when {
        seconds < 60 -> "${seconds}s ago"
        seconds < 3600 -> "${seconds / 60}m ago"
        seconds < 86400 -> "${seconds / 3600}h ago"
        else -> "${seconds / 86400}d ago"
    }
}
