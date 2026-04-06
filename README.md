# AetherConnect

**Open-source cross-device ecosystem for Android** — File sharing, clipboard sync, NFC tap-to-pair, screen casting, and KDE Connect interoperability.

## 🔥 Features

### V1 (Core Engine)
- ⚡ **Multi-Protocol Discovery** — BLE + mDNS + WiFi Direct
- 🔒 **Secure Pairing** — QR code + NFC tap-to-pair
- 📁 **P2P File Transfer** — WebRTC DataChannel + WiFi Direct fallback
- 📋 **Clipboard Sync** — Real-time bidirectional clipboard sync
- 📲 **NFC Touch Share** — Tap to pair, tap to send
- 📤 **Share Sheet Integration** — Send via AetherConnect from any app
- 🔄 **Boot Auto-Start** — Always-on background service

### V2 (Planned)
- 🖥️ Screen casting (WebRTC MediaStream)
- ⌨️ Remote input (keyboard + trackpad)
- 🔔 Notification sync
- 🔗 App handoff
- 🐧 KDE Connect bridge
- 💻 Desktop companion agent

## 🏗️ Architecture

```
AetherService (Foreground Daemon)
├── DiscoveryService (BLE + mDNS + WiFi Direct)
├── TransferService (WebRTC + WiFi Direct)
├── ClipboardService (ClipboardManager listener)
└── NFCService (NDEF Read/Write)
```

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material3
- **Database**: Room (SQLite)
- **Networking**: WebRTC native, WebSocket, WiFi Direct, BLE
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 35 (Android 15)

## 📱 Build

1. Open `AetherConnect/` in Android Studio
2. Sync Gradle
3. Build → Make Project
4. Run on device or `./gradlew assembleDebug`

## 📦 Package

```
com.aether.connect
```

## 🔧 Protocols

| Protocol | Purpose |
|----------|---------|
| BLE | Device discovery (nearby) |
| mDNS (NsdManager) | LAN service registration |
| WiFi Direct | P2P without router |
| WebRTC DataChannel | File transfer |
| WebSocket | Signaling + clipboard relay |
| NDEF (NFC) | Tap-to-pair, tap-to-send |

## 📄 License

MIT
