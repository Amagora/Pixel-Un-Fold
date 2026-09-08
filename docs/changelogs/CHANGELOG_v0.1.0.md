# Changelog - PaperFold v0.1.0

**Release Date:** Current Release  
**Target Device:** Google Pixel Fold / Foldables (Google Pixel 9/10/11 Pro Fold)  

### Fixed (4 UI / BottomSheet Theming & Layout Bugs)
- **Folded Settings Navigation Bar Theming**:
  - Fixed issue where opening the "Appearance & Settings" bottom sheet on the folded cover screen caused the system navigation bar to flip to stark white.
  - Implemented `applyBottomSheetWindowTheme()` which explicitly styles the dialog window's `navigationBarColor` to match `palette.dialogBg`, disables Android's forced contrast scrim (`setNavigationBarContrastEnforced(false)`), and uses `WindowInsetsControllerCompat.setAppearanceLightNavigationBars(!palette.isDark)`.
  - In Dark and AMOLED modes, the space around the gesture bar is dark or AMOLED pure black (`#000000`), and the gesture bar pill is crisp white. In Light mode, the space is clean white and the gesture pill is dark.
- **Folded A.I. Disclosure Navigation Bar Theming**:
  - Applied the exact same window and navigation bar theming to the A.I. & Privacy Disclosure dialog, ensuring consistent theme fidelity across both modals.
- **Unfolded Settings Gesture Bar Overlap**:
  - Fixed issue where opening the "Appearance & Settings" dialog on the unfolded inner screen placed the "Done" action button directly underneath the system gesture bar pill.
  - Implemented `setupBottomSheetDialog()` with dynamic window insets listeners and container padding (`dialogRoot.setPadding(..., basePaddingBottom + navInsets.bottom)`), lifting the action button cleanly above the system gesture bar with comfortable spacing.
  - Overrode default `design_bottom_sheet` background to transparent and cleared redundant internal padding to ensure pixel-perfect layout and theme continuity.
- **Unfolded Disclosure Policies Gesture Bar Overlap**:
  - Applied the same dynamic insets clearance to the Notice & Disclosure dialog, lifting the "I Understand" action button cleanly above the system gesture bar.

### Updated
- **Dynamic Real-Time Theme Updates in Open Dialogs**:
  - Toggling between Light, Dark, and AMOLED True Black inside the open Settings bottom sheet immediately updates the dialog window's navigation bar color, window flags, and gesture pill icon appearance in real time.
- **Windows 1-Click Installer GUI (`installer_gui.py`)**:
  - Added detection and deployment support for `release/PaperFold-v0.1.0.apk`.
- **Release Packaging**:
  - Packaged standalone APKs to `release/PaperFold-v0.1.0.apk` and updated `release/PaperFold-latest.apk`.
