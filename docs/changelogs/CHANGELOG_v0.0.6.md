# Changelog - PaperFold v0.0.6

**Release Date:** Layout Overhaul Iteration  
**Target Device:** Google Pixel Fold / Foldables  

### Added
- Fold-aware adaptive layout architecture:
  - **Cover Display (Folded)**: Single-column compact vertical layout.
  - **Inner Display (Unfolded)**: Side-by-side dual-pane layout (`layout-sw600dp`).
- Dynamic window insets handling for Pixel gesture navigation bar and status bar cutout.

### Changed
- Eliminated unnecessary vertical scrolling on unfolded display: all controls, previews, and actions fit seamlessly on one screen.
- Button sizes and touch targets expanded to take advantage of available screen width and improve accessibility.
