# Changelog - PaperFold v0.0.1

**Release Date:** Initial Development Iteration  
**Target Device:** Google Pixel Fold / Foldables  

### Added
- Initial project scaffolding for dual-screen wallpaper management on foldable Android devices.
- `FoldPaperService` implementation extending Android's standard `WallpaperService`.
- Basic dual-screen aspect ratio calculation comparing min dimension to max dimension.
- Local internal storage handling for Cover screen wallpaper (`cover_wallpaper.png`) and Inner screen wallpaper (`inner_wallpaper.png`).
- Core activity UI with basic buttons to select and assign images.
