# Changelog - PaperFold v0.0.8

**Release Date:** Theming Architecture & Border Fix  
**Target Device:** Google Pixel Fold / Foldables  

### Fixed
- Fixed broken dark and AMOLED mode transitions where dark theme accidentally displayed white cards with low contrast.
- Removed unwanted white borders on cards by dynamically setting card stroke width to 0dp in AMOLED mode and 1dp in Light mode.
- Ensured unselected toggle buttons retain high-contrast readable text (#FFFFFF in Dark/AMOLED, #111827 in Light).

### Changed
- Centralized `ThemePalette` class in `MainActivity` providing unified styling across root background, cards, strokes, previews, texts, badges, and bottom sheet dialogs.
