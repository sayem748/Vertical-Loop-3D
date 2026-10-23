# 3D Vertical Loop - Android project

Offline WebView wrapper (game + three.js/cannon.js are bundled in app/src/main/assets).

## Build the APK (pick one)
1. Android Studio: File > Open this folder, wait for sync, then Build > Build APK(s).
   Output: app/build/outputs/apk/debug/app-debug.apk
2. Command line (JDK 17 + Android SDK 34 + Gradle 8.7): gradle assembleDebug
3. No install: push this folder to a GitHub repo; Actions > "Build APK" builds it and
   attaches app-debug.apk as a downloadable artifact.

Install on the phone (allow "install unknown apps"). Landscape, fullscreen.
Tap once to enable sound. To update the game, replace app/src/main/assets/index.html.
