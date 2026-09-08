# Pixel (Un)Fold — Installation & Setup Guide 📱⚡

This guide explains how to install and configure **Pixel (Un)Fold** on your **Google Pixel Fold**, **Pixel 9 Pro Fold**, **Pixel 10 / 11 Pro Fold**, or compatible foldable Android device.

---

## 📦 Quick Download Links

| File | Description | Recommended For |
| :--- | :--- | :--- |
| **[`Pixel-UnFold-latest.apk`](release/Pixel-UnFold-latest.apk)** | Standalone installable Android APK (~14.3 MB) | Direct phone install / fast sideload |
| **[`Pixel-UnFold-v0.1.0-Pixel-Fold.zip`](release/Pixel-UnFold-v0.1.0-Pixel-Fold.zip)** | Full release bundle with 1-Click Windows Installer tools | Windows PC users (USB & Wireless ADB) |

---

## 📲 Method 1: Direct Phone Installation (No PC Required)

1. **Download the APK**: Download `Pixel-UnFold-latest.apk` directly onto your Pixel Fold using Google Chrome, Drive, or Telegram.
2. **Open the APK**: In your notification tray or Files app, tap the downloaded `.apk` file.
3. **Allow Unknown Apps**: If prompted with *"For your security, your phone is not allowed to install unknown apps from this source"*:
   - Tap **Settings**.
   - Toggle **Allow from this source** to **ON**.
   - Tap back and tap **Install**.
4. **Launch Pixel (Un)Fold**: Tap **Open** from the installer or find **Pixel (Un)Fold** in your app drawer.

---

## 💻 Method 2: Windows 1-Click Desktop Installer GUI (USB or Wireless)

Pixel (Un)Fold includes a dedicated desktop utility with automatic device detection and wireless ADB pairing support.

### Requirements:
- A Windows PC with **Python** installed (or ADB).
- Your Pixel Fold connected via USB cable or on the same Wi-Fi network.

### Steps:
1. Double-click **`Launch_Installer_GUI.bat`** in the project folder (or run `python installer_gui.py`).
2. **For USB**:
   - Enable **Developer Options** (*Settings > About phone > tap "Build number" 7 times*).
   - Enable **USB Debugging** (*Settings > System > Developer options > USB debugging*).
   - Plug your phone into your PC and tap "Always allow" on your phone's screen when prompted.
   - The installer GUI will automatically detect `Pixel 11 Pro Fold` (or your device model).
3. **For Wireless ADB**:
   - Turn on **Wireless Debugging** in *Developer options*.
   - Copy the IP address and port (e.g. `192.168.1.45:38291`) into the GUI input box.
   - Click **⚡ Connect Wireless**.
4. Click **🚀 1-Click Fast Install** to push the prebuilt release APK in under 3 seconds.
5. Click **📱 Launch App** to immediately open Pixel (Un)Fold on your device.

---

## ⚡ Method 3: PowerShell 1-Click Deployer

If you prefer PowerShell:
1. Right-click **`Launch_Deployer.bat`** and run it (or execute `.\deployer.ps1` in PowerShell).
2. Click **Deploy Latest Pixel (Un)Fold APK**.
3. The tool verifies your device connection, pushes the APK, restarts the activity, and displays real-time console logs.

---

## 🖥️ Method 4: Manual ADB Command Line

If you have `adb` installed in your terminal or Android SDK:

```bash
# Verify your device is connected
adb devices

# Install or upgrade Pixel (Un)Fold
adb install -r release/Pixel-UnFold-latest.apk

# Launch Pixel (Un)Fold directly to the main screen
adb shell am start -n com.pixel.foldpaper/.MainActivity
```

---

## ⚙️ Initial Configuration: Setting Up Dual Wallpapers

Once installed, follow these 3 simple steps inside the Pixel (Un)Fold app:

### 1. Configure the Cover Screen (Folded)
- Tap **Choose Image** in the **Cover Screen (Folded)** card.
- Select your preferred wallpaper for when the phone is closed (tall 9:20 aspect ratio).
- Choose **Scaling Mode**:
  - **Crop (Fill)**: Fills the entire screen edge-to-edge without borders.
  - **Fit (Center)**: Preserves the original image aspect ratio with letterboxing.

### 2. Configure the Inner Screen (Unfolded)
- Tap **Choose Image** in the **Inner Screen (Unfolded)** card.
- Select your preferred wallpaper for when the phone is unfolded (square ~1:1 aspect ratio).
- Choose **Scaling Mode** (**Crop (Fill)** or **Fit (Center)**).

### 3. Activate as System Live Wallpaper
- Tap the gold button at the bottom: **Set as Active Wallpaper**.
- The Android Live Wallpaper preview will appear.
- Tap **Set wallpaper** (select *Home screen* or *Home screen and lock screen*).
- You're all set! Now close and open your phone to enjoy instant, zero-latency dual-wallpaper switching!

---

## 🔒 Privacy & Battery Guarantee

- **0% Background Battery Drain**: Renders exclusively through hardware canvas when visible. Freezes rendering instantly when the screen is turned off or apps are in the foreground.
- **100% On-Device Privacy**: No internet permission (`android.permission.INTERNET` is not included in the manifest). All images remain strictly inside the app's sandboxed local storage.
