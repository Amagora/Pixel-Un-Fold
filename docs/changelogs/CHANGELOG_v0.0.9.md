# Changelog - PaperFold v0.0.9

**Release Date:** Current Release  
**Target Device:** Google Pixel Fold / Foldables  

### Performance & Battery Optimization
- **Hardware-Accelerated Canvas Rendering**: Updated `FoldPaperService` engine to use `SurfaceHolder.lockHardwareCanvas()` (Android 8.0+ API 26+) with graceful fallback to `lockCanvas()`, rendering wallpapers directly via the hardware GPU pipeline (Vulkan/OpenGL ES) and eliminating costly CPU blitting.
- **True Zero Background Battery Drain**: Confirmed zero wake locks, zero alarm managers, and zero background services. Rendering occurs strictly on demand when the wallpaper is visible; when screen is off or app is in foreground, execution halts completely.

### Added
- **Windows 1-Click Installer GUI (`installer_gui.py`)**:
  - Python Tkinter desktop GUI supporting both USB and Wireless ADB (`adb connect <ip:port>`).
  - Automatic scanning of attached Android devices.
  - One-click build & install via Gradle daemon.
  - One-click fast install of pre-built release APK without compiling.
  - One-click launch button to open PaperFold on the device.
  - Real-time command console log and colored status feedback.
  - Double-clickable launcher `Launch_Installer_GUI.bat`.
- **A.I., Experimental & Privacy Disclosures**:
  - **First-Launch Modal Dialog**: One-time required disclosure dialog warning the user that PaperFold was built with AI assistance, is in early experimental development (may contain bugs or crash), and strictly guarantees 100% on-device privacy (zero telemetry, zero analytics, zero data collection or transmission).
  - **Settings Disclosure Access**: Added dedicated "A.I. & Privacy Disclosure" row inside Appearance & Settings bottom sheet allowing users to re-read the notice at any time.
- **Security & Privacy Hardening**: Set `android:allowBackup="false"` in `AndroidManifest.xml` to prevent data leakage via ADB or cloud backups. Verified zero network permissions.
- **Automated Release Packaging**: Output standalone APK to `release/PaperFold-v0.0.9.apk` and `release/PaperFold-latest.apk`.
