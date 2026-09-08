#!/usr/bin/env python3
"""
Pixel (Un)Fold Windows 1-Click Installer GUI
---------------------------------------------
A modern, standalone desktop tool for Windows to wirelessly or via USB
connect, build, and push Pixel (Un)Fold updates to Google Pixel Fold in one click.
"""

import os
import sys
import subprocess
import threading
import time
from pathlib import Path
import tkinter as tk
from tkinter import ttk, messagebox, scrolledtext

# Color Palette (Pixel (Un)Fold Dark Mode / Pixel AMOLED aesthetic)
COLOR_BG = "#0D0F14"
COLOR_SURFACE = "#161922"
COLOR_CARD = "#1F2330"
COLOR_CARD_BORDER = "#2E3448"
COLOR_ACCENT = "#78A6FF"
COLOR_ACCENT_HOVER = "#5B8DEF"
COLOR_SUCCESS = "#4ADE80"
COLOR_WARNING = "#FBBF24"
COLOR_DANGER = "#F87171"
COLOR_TEXT_PRIMARY = "#F8FAFC"
COLOR_TEXT_SECONDARY = "#94A3B8"
COLOR_TEXT_MUTED = "#64748B"
COLOR_CONSOLE_BG = "#08090C"
COLOR_CONSOLE_TEXT = "#CBD5E1"

PROJECT_ROOT = Path(__file__).resolve().parent

def find_adb():
    """Locate adb.exe on Windows."""
    # 1. Check PATH
    try:
        res = subprocess.run(["where", "adb"], capture_output=True, text=True, timeout=3)
        if res.returncode == 0:
            first = res.stdout.strip().splitlines()[0]
            if os.path.isfile(first):
                return first
    except Exception:
        pass

    # 2. Check local AppData Android SDK
    local_appdata = os.environ.get("LOCALAPPDATA", "")
    if local_appdata:
        candidate = Path(local_appdata) / "Android" / "Sdk" / "platform-tools" / "adb.exe"
        if candidate.exists():
            return str(candidate)

    # 3. Check USERPROFILE Android SDK
    user_home = Path.home()
    candidate2 = user_home / "AppData" / "Local" / "Android" / "Sdk" / "platform-tools" / "adb.exe"
    if candidate2.exists():
        return str(candidate2)

    return "adb"

ADB_PATH = find_adb()


class PixelUnFoldInstallerApp:
    def __init__(self, root):
        self.root = root
        self.root.title("Pixel (Un)Fold 1-Click Installer (Pixel Fold)")
        self.root.geometry("780x720")
        self.root.minsize(700, 640)
        self.root.configure(bg=COLOR_BG)

        self.selected_device = tk.StringVar(value="")
        self.status_var = tk.StringVar(value="Ready")
        self.is_busy = False

        self._build_ui()
        self.refresh_devices_async()

    def _build_ui(self):
        # Header Frame
        header = tk.Frame(self.root, bg=COLOR_BG, padx=20, pady=16)
        header.pack(fill=tk.X)

        title_lbl = tk.Label(
            header,
            text="Pixel (Un)Fold Installer",
            font=("Segoe UI", 18, "bold"),
            fg=COLOR_TEXT_PRIMARY,
            bg=COLOR_BG
        )
        title_lbl.pack(anchor="w")

        subtitle_lbl = tk.Label(
            header,
            text="One-click push & wireless deployment for Google Pixel Fold",
            font=("Segoe UI", 10),
            fg=COLOR_TEXT_SECONDARY,
            bg=COLOR_BG
        )
        subtitle_lbl.pack(anchor="w", pady=(2, 0))

        # Main Content
        content = tk.Frame(self.root, bg=COLOR_BG, padx=20, pady=0)
        content.pack(fill=tk.BOTH, expand=True)

        # 1. Device Connection Card
        dev_card = tk.LabelFrame(
            content,
            text=" 1. Connected Devices & Wireless ADB ",
            font=("Segoe UI", 11, "bold"),
            fg=COLOR_ACCENT,
            bg=COLOR_CARD,
            padx=14,
            pady=12,
            bd=1,
            relief=tk.SOLID
        )
        dev_card.pack(fill=tk.X, pady=(0, 12))

        dev_row1 = tk.Frame(dev_card, bg=COLOR_CARD)
        dev_row1.pack(fill=tk.X, pady=(0, 8))

        tk.Label(dev_row1, text="Target Device:", font=("Segoe UI", 10, "bold"), fg=COLOR_TEXT_PRIMARY, bg=COLOR_CARD).pack(side=tk.LEFT)

        self.device_combo = ttk.Combobox(
            dev_row1,
            textvariable=self.selected_device,
            state="readonly",
            font=("Segoe UI", 10),
            width=36
        )
        self.device_combo.pack(side=tk.LEFT, padx=(10, 10))

        btn_refresh = tk.Button(
            dev_row1,
            text="↻ Refresh Devices",
            font=("Segoe UI", 9, "bold"),
            bg="#2A3042",
            fg=COLOR_TEXT_PRIMARY,
            activebackground="#3A4259",
            activeforeground="#FFFFFF",
            relief=tk.FLAT,
            padx=10,
            pady=3,
            cursor="hand2",
            command=self.refresh_devices_async
        )
        btn_refresh.pack(side=tk.LEFT)

        # Wireless Row
        wireless_row = tk.Frame(dev_card, bg=COLOR_CARD)
        wireless_row.pack(fill=tk.X)

        tk.Label(wireless_row, text="Wireless IP:Port:", font=("Segoe UI", 9), fg=COLOR_TEXT_SECONDARY, bg=COLOR_CARD).pack(side=tk.LEFT)

        self.ip_entry = tk.Entry(
            wireless_row,
            font=("Segoe UI", 10),
            bg="#12151D",
            fg=COLOR_TEXT_PRIMARY,
            insertbackground=COLOR_TEXT_PRIMARY,
            relief=tk.FLAT,
            width=24
        )
        self.ip_entry.pack(side=tk.LEFT, padx=(8, 10))
        self.ip_entry.insert(0, "192.168.1.")

        btn_connect_wifi = tk.Button(
            wireless_row,
            text="⚡ Connect Wireless",
            font=("Segoe UI", 9, "bold"),
            bg="#223048",
            fg=COLOR_ACCENT,
            activebackground="#2E4266",
            activeforeground="#FFFFFF",
            relief=tk.FLAT,
            padx=10,
            pady=3,
            cursor="hand2",
            command=self.connect_wireless_async
        )
        btn_connect_wifi.pack(side=tk.LEFT)

        # 2. Action Buttons Card
        action_card = tk.LabelFrame(
            content,
            text=" 2. Push & Install Actions ",
            font=("Segoe UI", 11, "bold"),
            fg=COLOR_ACCENT,
            bg=COLOR_CARD,
            padx=14,
            pady=12,
            bd=1,
            relief=tk.SOLID
        )
        action_card.pack(fill=tk.X, pady=(0, 12))

        btn_grid = tk.Frame(action_card, bg=COLOR_CARD)
        btn_grid.pack(fill=tk.X)

        # 1-Click Fast Install
        self.btn_fast_install = tk.Button(
            btn_grid,
            text="🚀 1-Click Fast Install\n(Push Latest Pre-built APK)",
            font=("Segoe UI", 10, "bold"),
            bg="#1E3A8A",
            fg="#FFFFFF",
            activebackground="#2563EB",
            activeforeground="#FFFFFF",
            relief=tk.FLAT,
            padx=12,
            pady=8,
            cursor="hand2",
            command=lambda: self.run_action_async("install_prebuilt")
        )
        self.btn_fast_install.pack(side=tk.LEFT, fill=tk.X, expand=True, padx=(0, 6))

        # 1-Click Build & Install
        self.btn_build_install = tk.Button(
            btn_grid,
            text="🔨 Build & Install Latest\n(Gradle assembleDebug + Push)",
            font=("Segoe UI", 10, "bold"),
            bg="#065F46",
            fg="#FFFFFF",
            activebackground="#059669",
            activeforeground="#FFFFFF",
            relief=tk.FLAT,
            padx=12,
            pady=8,
            cursor="hand2",
            command=lambda: self.run_action_async("build_and_install")
        )
        self.btn_build_install.pack(side=tk.LEFT, fill=tk.X, expand=True, padx=(6, 6))

        # Launch App
        self.btn_launch_app = tk.Button(
            btn_grid,
            text="📱 Launch App\n(Open on Fold)",
            font=("Segoe UI", 10, "bold"),
            bg="#374151",
            fg="#FFFFFF",
            activebackground="#4B5563",
            activeforeground="#FFFFFF",
            relief=tk.FLAT,
            padx=12,
            pady=8,
            cursor="hand2",
            command=lambda: self.run_action_async("launch_app")
        )
        self.btn_launch_app.pack(side=tk.LEFT, fill=tk.X, expand=True, padx=(6, 0))

        # 3. Live Log Console
        console_frame = tk.LabelFrame(
            content,
            text=" Output Log & Diagnostics ",
            font=("Segoe UI", 10, "bold"),
            fg=COLOR_TEXT_SECONDARY,
            bg=COLOR_CARD,
            padx=10,
            pady=8,
            bd=1,
            relief=tk.SOLID
        )
        console_frame.pack(fill=tk.BOTH, expand=True, pady=(0, 10))

        self.console = scrolledtext.ScrolledText(
            console_frame,
            bg=COLOR_CONSOLE_BG,
            fg=COLOR_CONSOLE_TEXT,
            font=("Consolas", 9),
            insertbackground=COLOR_CONSOLE_TEXT,
            relief=tk.FLAT,
            wrap=tk.WORD,
            height=12
        )
        self.console.pack(fill=tk.BOTH, expand=True)
        self.log(f"Initialized Pixel (Un)Fold Installer. Using ADB: {ADB_PATH}")

        # Status Bar
        status_bar = tk.Frame(self.root, bg=COLOR_SURFACE, padx=16, pady=8)
        status_bar.pack(fill=tk.X, side=tk.BOTTOM)

        lbl_status_prefix = tk.Label(status_bar, text="Status:", font=("Segoe UI", 9, "bold"), fg=COLOR_TEXT_SECONDARY, bg=COLOR_SURFACE)
        lbl_status_prefix.pack(side=tk.LEFT)

        self.lbl_status = tk.Label(status_bar, textvariable=self.status_var, font=("Segoe UI", 9), fg=COLOR_ACCENT, bg=COLOR_SURFACE)
        self.lbl_status.pack(side=tk.LEFT, padx=(6, 0))

    def log(self, message):
        """Append log message to console in thread-safe manner."""
        def _append():
            ts = time.strftime("%H:%M:%S")
            self.console.insert(tk.END, f"[{ts}] {message}\n")
            self.console.see(tk.END)
        self.root.after(0, _append)

    def set_status(self, text, color=COLOR_ACCENT):
        def _update():
            self.status_var.set(text)
            self.lbl_status.config(fg=color)
        self.root.after(0, _update)

    def set_busy(self, busy):
        self.is_busy = busy
        state = tk.DISABLED if busy else tk.NORMAL
        self.btn_fast_install.config(state=state)
        self.btn_build_install.config(state=state)
        self.btn_launch_app.config(state=state)

    def run_command(self, cmd, cwd=None):
        """Runs a subprocess command and streams stdout/stderr to the console."""
        self.log(f"$ {' '.join(cmd)}")
        process = subprocess.Popen(
            cmd,
            cwd=cwd or str(PROJECT_ROOT),
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            bufsize=1,
            universal_newlines=True
        )

        for line in process.stdout:
            cleaned = line.rstrip()
            if cleaned:
                self.log(f"  {cleaned}")

        process.wait()
        return process.returncode

    def refresh_devices_async(self):
        def worker():
            self.set_status("Scanning for ADB devices...", COLOR_WARNING)
            try:
                res = subprocess.run([ADB_PATH, "devices", "-l"], capture_output=True, text=True, timeout=5)
                lines = res.stdout.strip().splitlines()
                devices = []
                for line in lines[1:]:
                    if line.strip() and not line.startswith("*"):
                        parts = line.split()
                        if len(parts) >= 2 and parts[1] == "device":
                            dev_id = parts[0]
                            # Try to extract model
                            model = "Android Device"
                            for part in parts:
                                if part.startswith("model:"):
                                    model = part.split(":", 1)[1]
                            devices.append(f"{dev_id} ({model})")

                def update_ui():
                    self.device_combo["values"] = devices
                    if devices:
                        self.device_combo.current(0)
                        self.set_status(f"Found {len(devices)} device(s) connected", COLOR_SUCCESS)
                        self.log(f"Active device selected: {devices[0]}")
                    else:
                        self.selected_device.set("")
                        self.set_status("No devices detected. Check USB/Wireless debugging", COLOR_DANGER)
                        self.log("No devices found. Plug in phone or connect via wireless.")
                self.root.after(0, update_ui)

            except Exception as e:
                self.log(f"Error scanning devices: {e}")
                self.set_status("ADB scan failed", COLOR_DANGER)

        threading.Thread(target=worker, daemon=True).start()

    def connect_wireless_async(self):
        target = self.ip_entry.get().strip()
        if not target or target == "192.168.1.":
            messagebox.showwarning("Wireless ADB", "Please enter a valid IP and Port (e.g. 192.168.1.50:5555)")
            return

        def worker():
            self.set_status(f"Connecting to {target}...", COLOR_WARNING)
            self.log(f"Connecting to wireless ADB at {target}...")
            rc = self.run_command([ADB_PATH, "connect", target])
            if rc == 0:
                self.log(f"Successfully connected to {target}")
                self.refresh_devices_async()
            else:
                self.set_status("Wireless connection failed", COLOR_DANGER)
                self.log(f"Failed to connect to {target}. Make sure Wireless Debugging is active on phone.")

        threading.Thread(target=worker, daemon=True).start()

    def get_active_device_serial(self):
        selection = self.selected_device.get()
        if not selection:
            return None
        return selection.split()[0]

    def run_action_async(self, action_type):
        if self.is_busy:
            return

        serial = self.get_active_device_serial()
        if not serial:
            messagebox.showerror("No Device", "Please connect and select a target Android device first.")
            return

        def worker():
            self.set_busy(True)
            try:
                if action_type == "install_prebuilt":
                    self.action_install_prebuilt(serial)
                elif action_type == "build_and_install":
                    self.action_build_and_install(serial)
                elif action_type == "launch_app":
                    self.action_launch_app(serial)
            finally:
                self.set_busy(False)

        threading.Thread(target=worker, daemon=True).start()

    def find_best_apk(self):
        # Check Pixel (Un)Fold release apks
        unfold_latest = PROJECT_ROOT / "release" / "Pixel-UnFold-latest.apk"
        if unfold_latest.exists():
            return unfold_latest

        unfold_v010 = PROJECT_ROOT / "release" / "Pixel-UnFold-v0.1.0.apk"
        if unfold_v010.exists():
            return unfold_v010

        # Check legacy release apks
        release_apk = PROJECT_ROOT / "release" / "PaperFold-latest.apk"
        if release_apk.exists():
            return release_apk

        v010_apk = PROJECT_ROOT / "release" / "PaperFold-v0.1.0.apk"
        if v010_apk.exists():
            return v010_apk

        v009_apk = PROJECT_ROOT / "release" / "PaperFold-v0.0.9.apk"
        if v009_apk.exists():
            return v009_apk

        # Check build output
        build_apk = PROJECT_ROOT / "app" / "build" / "outputs" / "apk" / "debug" / "app-debug.apk"
        if build_apk.exists():
            return build_apk

        return None

    def action_install_prebuilt(self, serial):
        self.set_status("Pushing prebuilt APK...", COLOR_WARNING)
        apk_path = self.find_best_apk()
        if not apk_path:
            self.log("No prebuilt APK found! Building from source first...")
            self.action_build_and_install(serial)
            return

        self.log(f"Found APK: {apk_path.name} ({apk_path.stat().st_size // 1024} KB)")
        self.log(f"Installing onto device {serial}...")
        rc = self.run_command([ADB_PATH, "-s", serial, "install", "-r", str(apk_path)])
        if rc == 0:
            self.set_status("Installed successfully!", COLOR_SUCCESS)
            self.log("Installation completed successfully!")
            self.action_launch_app(serial)
        else:
            self.set_status("Installation failed", COLOR_DANGER)
            self.log(f"adb install failed with code {rc}")

    def action_build_and_install(self, serial):
        self.set_status("Building APK via Gradle...", COLOR_WARNING)
        self.log("Starting Gradle assembleDebug build...")

        gradle_cmd = str(PROJECT_ROOT / "gradlew.bat")
        if not os.path.exists(gradle_cmd):
            gradle_cmd = "gradlew"

        rc = self.run_command([gradle_cmd, "assembleDebug"], cwd=str(PROJECT_ROOT))
        if rc != 0:
            self.set_status("Build failed! Check log.", COLOR_DANGER)
            self.log("Gradle build failed.")
            return

        self.set_status("Build succeeded! Pushing to device...", COLOR_WARNING)
        build_apk = PROJECT_ROOT / "app" / "build" / "outputs" / "apk" / "debug" / "app-debug.apk"
        if not build_apk.exists():
            self.set_status("Build artifact not found", COLOR_DANGER)
            self.log(f"Error: {build_apk} not found.")
            return

        rc_install = self.run_command([ADB_PATH, "-s", serial, "install", "-r", str(build_apk)])
        if rc_install == 0:
            self.set_status("Build & Install completed!", COLOR_SUCCESS)
            self.log("Successfully built and installed latest Pixel (Un)Fold!")
            self.action_launch_app(serial)
        else:
            self.set_status("Install failed", COLOR_DANGER)

    def action_launch_app(self, serial):
        self.set_status("Launching Pixel (Un)Fold on phone...", COLOR_WARNING)
        self.log("Opening MainActivity...")
        self.run_command([
            ADB_PATH, "-s", serial, "shell", "am", "start",
            "-n", "com.pixel.foldpaper/.MainActivity"
        ])
        self.set_status("Ready (App running on phone)", COLOR_SUCCESS)
        self.log("App launched on Pixel Fold.")


def main():
    root = tk.Tk()
    app = PixelUnFoldInstallerApp(root)
    root.mainloop()

if __name__ == "__main__":
    main()
