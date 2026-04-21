# Changelog

All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.0] - 2026-04-21

### Added
- Initial public release.
- 4‑step setup wizard: BotFather → token → chat binding → test message.
- Foreground service (`dataSync` type, silent `IMPORTANCE_MIN` notification) keeps the SMS receiver alive when the app is closed.
- Boot receiver restarts forwarding after device reboot when enabled.
- Diagnostic status card: permission state, last SMS seen, last Telegram delivery, last error.
- MarkdownV2 template with `{from}` / `{body}` placeholders and optional SIM slot for dual‑SIM.
- Conditional "MIUI settings" shortcut on Xiaomi / Redmi / POCO / HyperOS devices.
- Bot token stored encrypted via Android Keystore (`EncryptedSharedPreferences`).
- WorkManager delivery with exponential backoff; permanent failures (401, unknown chat) don't retry forever.

[Unreleased]: https://github.com/shuler/sms2tg/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/shuler/sms2tg/releases/tag/v0.1.0
