@echo off
title Pixel (Un)Fold APK Deployer
cd /d "%~dp0"
powershell -ExecutionPolicy Bypass -File "%~dp0deployer.ps1"
