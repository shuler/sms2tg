# Privacy Policy

_Last updated: 2026-04-21_

SMS2TG is an open‑source app that runs entirely on your device and a Telegram Bot you create and control. The project operates no servers.

## What SMS2TG reads

- **Incoming SMS messages.** Required to forward them. Granted via `RECEIVE_SMS` permission.
- **Your chosen Telegram Bot token**, which you paste into the app during setup.
- **The `chat_id`** derived from the first `/start` you send to the bot.

## What SMS2TG transmits

- **Incoming SMS content** is transmitted to **api.telegram.org** using your bot token, targeting the `chat_id` you bound in setup. Nothing else.
- **Bot API metadata requests** (`getMe`, `getUpdates`, `sendMessage`) are sent to `api.telegram.org` during configuration and message delivery.

The app does **not**:

- Contact any server other than `api.telegram.org`.
- Include analytics, crash reporters, advertising or tracking SDKs.
- Upload your token, `chat_id`, message content or device identifiers anywhere else.

## Storage

- The bot token is stored on‑device in `EncryptedSharedPreferences`, backed by an Android Keystore master key. A factory reset invalidates the stored token.
- The `chat_id`, chat title and optional message template are stored in the same file.
- SMS messages themselves are **not persisted** by SMS2TG — they are only held in memory long enough to be forwarded to Telegram.

## Permissions summary

| Permission                                    | Why                                                         |
| --------------------------------------------- | ----------------------------------------------------------- |
| `RECEIVE_SMS`                                 | Receive incoming SMS broadcasts.                            |
| `INTERNET`, `ACCESS_NETWORK_STATE`            | Deliver messages to Telegram Bot API.                       |
| `POST_NOTIFICATIONS`                          | Show the foreground service notification and error alerts.  |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_DATA_SYNC` | Keep the process alive in the background.           |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`        | Optional — prevents the system from killing the service.    |
| `RECEIVE_BOOT_COMPLETED`                      | Resume forwarding after reboot when enabled.                |

## Third parties

Your Telegram messages reach Telegram servers when delivered via the Bot API. Their handling is governed by the [Telegram Privacy Policy](https://telegram.org/privacy).

## Changes

Significant changes to this policy will be called out in the [CHANGELOG](../CHANGELOG.md).

## Contact

Questions? Open an issue or email `eugene.shulepin@gmail.com`.
