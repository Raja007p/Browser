# Aegis Browser 🛡️

A modern, lightweight, Chromium-based Android privacy browser featuring real-time ad/tracker blocking, zero-knowledge encrypted cross-device synchronization, integrated secure DNS/VPN proxy, web extensions (UserScripts), and a centralized Progressive Web App (PWA) hub.

---

## 🚀 How to Get the APK from GitHub

### Option 1: Automatic Download via GitHub Actions (Recommended)
1. Push this repository to your **GitHub** account.
2. In your GitHub repository, click on the **Actions** tab at the top.
3. Select the workflow **"Build Android APK"** (or click on the latest workflow run from your commit).
4. Scroll down to the **Artifacts** section at the bottom of the page.
5. Click on **`aegis-browser-debug-apk`** to download the ZIP containing the compiled `.apk` file!
6. Unzip and install the APK directly on your Android phone or tablet.

### Option 2: Build the APK Locally via Command Line
If you have cloned the repository to your computer:

```bash
# Make gradlew executable (Linux/macOS)
chmod +x gradlew

# Build the debug APK
./gradlew assembleDebug
```
The output APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🌟 Key Features

- **Chromium Web Engine**: Fast rendering with built-in hardware acceleration.
- **Aegis Shield**: Blocks invasive ads, trackers, analytics scripts, and cryptominers before network fetch.
- **Encrypted Sync & Backup**: Zero-knowledge PBKDF2 + AES-256-GCM export and import for bookmarks, PWAs, settings, and extensions.
- **Secure DNS / VPN Proxy**: Integrated Swiss/Iceland relays and encrypted DNS-over-HTTPS (DoH).
- **Web Extensions Support**: Built-in UserScript engine with Dark Reader, Cookie Banner Killer, and Anti-Canvas Fingerprinting.
- **Web Apps (PWA) Hub**: Install any website as a standalone application.
- **Battery-Saver & AMOLED Dark Modes**: Throttles heavy background timers and provides true `#000000` AMOLED styling.
