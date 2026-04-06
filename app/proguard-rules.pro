# AetherConnect ProGuard Rules

# WebRTC
-keep class org.webrtc.** { *; }
-dontwarn org.webrtc.**

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.aether.connect.data.model.** { *; }
-keep class com.aether.connect.network.AetherProtocol.** { *; }
-keep class com.aether.connect.nfc.NFCPayload { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Java-WebSocket
-keep class org.java_websocket.** { *; }
-dontwarn org.java_websocket.**

# ZXing
-keep class com.google.zxing.** { *; }
