# Security Policy

## Supported Versions

Only the latest release receives security fixes.

| Version | Supported          |
| ------- | ------------------ |
| 0.1.x   | :white_check_mark: |

## Reporting a Vulnerability

Please **do not** open a public issue for security bugs.

Report privately by one of:

- GitHub's "Report a vulnerability" button on the repo's Security tab (preferred — creates a private advisory).
- Email: `eugene.shulepin@gmail.com` with the subject line `SMS2TG security`.

Expect an acknowledgement within a few days. Once a fix lands, you'll be credited in the release notes unless you prefer otherwise.

## Scope

In scope:

- Token or SMS content leaking to any destination other than the user‑configured Telegram chat.
- Bypass of `EncryptedSharedPreferences` storage of the bot token.
- Unauthorized start/stop of forwarding without user consent.
- Any exported component (activity, receiver, service) accepting unintended external input.

Out of scope:

- OEM ROM behavior that kills background processes (documented as a known limitation).
- Telegram availability in jurisdictions where the Bot API is blocked.
- Social‑engineering the user into installing a malicious APK.
