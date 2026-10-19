# TC Vacuum Study Android App

Android WebView wrapper for the offline study portal. The Gradle asset task copies the shared iOS/PWA page and its three linked lecture PDFs at build time, so Android and iOS use the same progress card and study data flow.

## Build

Requires JDK 17, Gradle 8.7, Android SDK Platform 35, and Android Build Tools 34.0.0 or later. Run the PDF restore script first when building from a Git checkout, then use the same Android signing key as the installed copy if you want to update it without uninstalling.

From the repository root, run:

```powershell
bash .github/scripts/reassemble-pdfs.sh
$keyFile = Get-ChildItem "$env:TEMP\TCVacuumAndroidBuild-*\tc-debug.keystore" -File -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1
$keyPath = if ($keyFile) { $keyFile.FullName } else { Join-Path $env:USERPROFILE ".android\debug.keystore" }
gradle "-PtcDebugKeystore=$keyPath" -p AndroidApp assembleDebug
```

The installable APK is written to `AndroidApp/app/build-output/outputs/apk/debug/app-debug.apk`.

The app uses a local HTTPS-style origin for persistent browser storage, requires no Internet permission, and opens bundled lecture PDFs with the Android PDF viewer selected by the user. PDF assets use ASCII filenames internally so all Android devices can open them consistently; the study portal continues to show the original Chinese document titles.
