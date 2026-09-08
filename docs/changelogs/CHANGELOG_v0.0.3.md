# Changelog - PaperFold v0.0.3

**Release Date:** Early Iteration  
**Target Device:** Google Pixel Fold / Foldables  

### Added
- Real-time diagnostic header displaying live wallpaper service status ("Active as System Wallpaper" vs "Not currently set").
- Live aspect ratio readout showing current surface dimensions (e.g., `Ratio: 0.96 (2076x2152)` or `Ratio: 0.46 (1080x2342)`).
- Visual phone preview mockups representing the folded phone frame and unfolded inner screen with camera punch-hole styling.

### Fixed
- Prevented OutOfMemory (OOM) crashes on large multi-megapixel photos by introducing sampled bitmap decoding (`decodeSampledBitmap()`).
