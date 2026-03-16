# jude-minimal-laucher

Minimal Android launcher with:
- Whitelisted apps only
- Per‑app daily soft + hard limits
- Local VPN service stub for telemetry blocking (no root)
- Swipe‑up all apps list
- Round clock home screen

## Build
Requirements: Android Studio + SDK 35, JDK 17.

```bash
./gradlew :app:assembleDebug
```
APK will be at:
`app/build/outputs/apk/debug/app-debug.apk`

## Usage
- Set as **Default Home app**.
- Grant **Usage Access** for limits.

## Notes
- VPN blocking is a stub; add DNS/IP parsing to enforce blocklists.

## License
MIT
