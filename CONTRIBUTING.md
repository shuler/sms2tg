# Contributing to SMS2TG

Thanks for your interest — bug reports, feature ideas and pull requests are all welcome.

## Before opening a PR

1. **Open an issue first** for anything larger than a typo or a one‑file fix, so we can agree on the shape of the change before you spend time on it.
2. **Keep PRs focused.** One change per PR — makes review and bisecting easier.
3. **Don't bundle formatting churn** with logic changes.

## Development

### Prerequisites

- JDK 17
- Android SDK (compileSdk 35, build‑tools 35.0.0)

```bash
brew install openjdk@17
brew install --cask android-commandlinetools
export JAVA_HOME=$(brew --prefix)/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export ANDROID_HOME=$(brew --prefix)/share/android-commandlinetools
yes | sdkmanager --licenses
sdkmanager "platforms;android-35" "build-tools;35.0.0" "platform-tools"
```

### Before pushing

```bash
./gradlew lintDebug         # must pass without new warnings
./gradlew assembleDebug     # must produce app-debug.apk
```

If your change touches UI, include a screenshot or a short screen recording in the PR description.

## Code style

- Kotlin official style (`kotlin.code.style=official` is already set in `gradle.properties`).
- Keep public APIs small. If a field doesn't need to be `public`, don't make it so.
- No new dependencies without discussion — every extra library ships to every user's phone.
- Prefer boring code. Coroutines + `CoroutineWorker` + `EncryptedSharedPreferences` already cover most needs.

## Commit messages

Short imperative subject line, reference issue numbers when applicable:

```
SmsReceiver: drop PII from logcat

Redacts sender address from Log.d so sensitive numbers don't
end up in system logcat on shared devices.

Closes #42
```

## Security issues

**Do not** open a public issue. See [SECURITY.md](SECURITY.md) for the private disclosure process.

## License

By contributing, you agree that your contributions will be licensed under the project's [MIT License](LICENSE).
