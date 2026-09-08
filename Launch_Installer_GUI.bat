@echo off
title Pixel (Un)Fold 1-Click Installer
cd /d "%~dp0"

echo ============================================================
echo  Pixel (Un)Fold Windows 1-Click Installer
echo ============================================================
echo.
echo Launching GUI...
echo.

where python >nul 2>&1
if %ERRORLEVEL% equ 0 (
    start "" python installer_gui.py
    exit /b 0
)

where py >nul 2>&1
if %ERRORLEVEL% equ 0 (
    start "" py installer_gui.py
    exit /b 0
)

echo [ERROR] Python not found in system PATH.
echo Please install Python or ensure it is added to your PATH.
echo.
pause
