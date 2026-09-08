# Changelog - PaperFold v0.0.2

**Release Date:** Early Iteration  
**Target Device:** Google Pixel Fold / Foldables  

### Added
- Independent scaling mode toggles for both screens: **Crop (Fill)** vs **Fit (Center)** with letterboxing.
- `WallpaperRepository` persistent preferences using Android `SharedPreferences`.
- Broadcast receiver mechanism (`ACTION_WALLPAPER_CHANGED`) to trigger instant wallpaper redraws when new images are picked without requiring service restart.

### Changed
- Refined aspect ratio detection threshold to 0.72 to accurately differentiate Pixel Fold's outer cover screen (~9:20 aspect ratio) from the unfolded inner display (~1:1 aspect ratio).
