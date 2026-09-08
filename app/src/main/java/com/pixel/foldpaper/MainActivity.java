package com.pixel.foldpaper;

import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private WallpaperRepository repository;
    private int systemNavBottomInset = 0;

    private View coordinatorRoot;
    private View appbar;
    private View bottomBarInner;

    private TextView tvAppTitle;
    private TextView tvAppVersion;
    private ImageButton btnSettings;

    private MaterialCardView cardStatus;
    private MaterialCardView cardCover;
    private MaterialCardView cardInner;
    private MaterialCardView bottomBarContainer;

    private MaterialCardView cardPreviewCover;
    private MaterialCardView cardPreviewInner;

    private ImageView ivStatusIcon;
    private TextView tvServiceStatus;
    private View dividerStatus;
    private ImageView ivDeviceStateIcon;
    private TextView tvDeviceState;
    private TextView tvAspectRatioDiag;

    private ImageView ivCoverIcon;
    private TextView tvCoverTitle;
    private TextView badgeCoverRatio;
    private TextView tvCoverDesc;
    private MaterialButton btnPickCover;
    private MaterialButton btnResetCover;
    private TextView tvCoverScaleTitle;
    private MaterialButtonToggleGroup toggleCoverScale;
    private MaterialButton btnCoverCrop;
    private MaterialButton btnCoverFit;
    private View containerCoverStatusDetail;
    private ImageView ivCoverStatusDot;
    private TextView tvStatusCoverDetail;

    private ImageView ivInnerIcon;
    private TextView tvInnerTitle;
    private TextView badgeInnerRatio;
    private TextView tvInnerDesc;
    private MaterialButton btnPickInner;
    private MaterialButton btnResetInner;
    private TextView tvInnerScaleTitle;
    private MaterialButtonToggleGroup toggleInnerScale;
    private MaterialButton btnInnerCrop;
    private MaterialButton btnInnerFit;
    private View containerInnerStatusDetail;
    private ImageView ivInnerStatusDot;
    private TextView tvStatusInnerDetail;

    private MaterialButton btnApplyWallpaper;

    private ImageView ivPreviewCover;
    private TextView tvPlaceholderCover;
    private ImageView ivPreviewInner;
    private TextView tvPlaceholderInner;

    private ActivityResultLauncher<String> coverImagePickerLauncher;
    private ActivityResultLauncher<String> innerImagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        repository = new WallpaperRepository(this);

        // Apply Material You dynamic colors before layout inflation
        DynamicColors.applyIfAvailable(this);

        // Modern Edge-To-Edge
        EdgeToEdge.enable(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupInsets();
        setupPickers();
        setupListeners();
        applyThemeMode();

        // Check one-time AI, experimental & privacy disclosure
        if (!repository.isDisclosureAcknowledged()) {
            showDisclosureDialog(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
        updatePreviews();
        updateDeviceStateDiagnostics();
        applyThemeMode();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Automatically switch between compact single-column (folded) and dual-pane (unfolded)
        recreate();
    }

    private void initViews() {
        coordinatorRoot = findViewById(R.id.coordinator_root);
        appbar = findViewById(R.id.appbar);
        bottomBarInner = findViewById(R.id.bottom_bar_inner);

        tvAppTitle = findViewById(R.id.tv_app_title);
        tvAppVersion = findViewById(R.id.tv_app_version);
        btnSettings = findViewById(R.id.btn_settings);

        cardStatus = findViewById(R.id.card_status);
        cardCover = findViewById(R.id.card_cover);
        cardInner = findViewById(R.id.card_inner);
        bottomBarContainer = findViewById(R.id.bottom_bar_container);

        cardPreviewCover = findViewById(R.id.card_preview_cover);
        cardPreviewInner = findViewById(R.id.card_preview_inner);

        ivStatusIcon = findViewById(R.id.iv_status_icon);
        tvServiceStatus = findViewById(R.id.tv_service_status);
        dividerStatus = findViewById(R.id.divider_status);
        ivDeviceStateIcon = findViewById(R.id.iv_device_state_icon);
        tvDeviceState = findViewById(R.id.tv_device_state);
        tvAspectRatioDiag = findViewById(R.id.tv_aspect_ratio_diag);

        // Cover Screen Elements
        ivCoverIcon = findViewById(R.id.iv_cover_icon);
        tvCoverTitle = findViewById(R.id.tv_cover_title);
        badgeCoverRatio = findViewById(R.id.badge_cover_ratio);
        tvCoverDesc = findViewById(R.id.tv_cover_desc);
        btnPickCover = findViewById(R.id.btn_pick_cover);
        btnResetCover = findViewById(R.id.btn_reset_cover);
        tvCoverScaleTitle = findViewById(R.id.tv_cover_scale_title);
        toggleCoverScale = findViewById(R.id.toggle_cover_scale);
        btnCoverCrop = findViewById(R.id.btn_cover_crop);
        btnCoverFit = findViewById(R.id.btn_cover_fit);
        containerCoverStatusDetail = findViewById(R.id.container_cover_status_detail);
        ivCoverStatusDot = findViewById(R.id.iv_cover_status_dot);
        tvStatusCoverDetail = findViewById(R.id.tv_status_cover_detail);

        // Inner Screen Elements
        ivInnerIcon = findViewById(R.id.iv_inner_icon);
        tvInnerTitle = findViewById(R.id.tv_inner_title);
        badgeInnerRatio = findViewById(R.id.badge_inner_ratio);
        tvInnerDesc = findViewById(R.id.tv_inner_desc);
        btnPickInner = findViewById(R.id.btn_pick_inner);
        btnResetInner = findViewById(R.id.btn_reset_inner);
        tvInnerScaleTitle = findViewById(R.id.tv_inner_scale_title);
        toggleInnerScale = findViewById(R.id.toggle_inner_scale);
        btnInnerCrop = findViewById(R.id.btn_inner_crop);
        btnInnerFit = findViewById(R.id.btn_inner_fit);
        containerInnerStatusDetail = findViewById(R.id.container_inner_status_detail);
        ivInnerStatusDot = findViewById(R.id.iv_inner_status_dot);
        tvStatusInnerDetail = findViewById(R.id.tv_status_inner_detail);

        btnApplyWallpaper = findViewById(R.id.btn_apply_wallpaper);

        ivPreviewCover = findViewById(R.id.iv_preview_cover);
        tvPlaceholderCover = findViewById(R.id.tv_placeholder_cover);
        ivPreviewInner = findViewById(R.id.iv_preview_inner);
        tvPlaceholderInner = findViewById(R.id.tv_placeholder_inner);

        // Initialize Scale Toggles
        if (repository.getCoverScaleMode() == WallpaperRepository.SCALE_MODE_FIT) {
            toggleCoverScale.check(R.id.btn_cover_fit);
        } else {
            toggleCoverScale.check(R.id.btn_cover_crop);
        }

        if (repository.getInnerScaleMode() == WallpaperRepository.SCALE_MODE_FIT) {
            toggleInnerScale.check(R.id.btn_inner_fit);
        } else {
            toggleInnerScale.check(R.id.btn_inner_crop);
        }
    }

    private static class ThemePalette {
        final boolean isDark;
        final boolean isAmoled;
        final int rootBg;
        final int cardBg;
        final int cardStroke;
        final int cardStrokeWidthPx;
        final int previewBorder;
        final int previewCardBg;
        final int textPrimary;
        final int textSecondary;
        final int textTertiary;
        final int accentColor;
        final int onAccentColor;
        final int outlineButtonStroke;
        final int toggleUnselectedStroke;
        final int toggleUnselectedText;
        final int badgeBg;
        final int badgeTextColor;
        final int dialogBg;
        final int dialogCardBg;
        final int dialogStroke;

        ThemePalette(boolean isDark, boolean isAmoled, int rootBg, int cardBg, int cardStroke, int cardStrokeWidthPx,
                     int previewBorder, int previewCardBg, int textPrimary, int textSecondary, int textTertiary,
                     int accentColor, int onAccentColor, int outlineButtonStroke, int toggleUnselectedStroke,
                     int toggleUnselectedText, int badgeBg, int badgeTextColor, int dialogBg, int dialogCardBg, int dialogStroke) {
            this.isDark = isDark;
            this.isAmoled = isAmoled;
            this.rootBg = rootBg;
            this.cardBg = cardBg;
            this.cardStroke = cardStroke;
            this.cardStrokeWidthPx = cardStrokeWidthPx;
            this.previewBorder = previewBorder;
            this.previewCardBg = previewCardBg;
            this.textPrimary = textPrimary;
            this.textSecondary = textSecondary;
            this.textTertiary = textTertiary;
            this.accentColor = accentColor;
            this.onAccentColor = onAccentColor;
            this.outlineButtonStroke = outlineButtonStroke;
            this.toggleUnselectedStroke = toggleUnselectedStroke;
            this.toggleUnselectedText = toggleUnselectedText;
            this.badgeBg = badgeBg;
            this.badgeTextColor = badgeTextColor;
            this.dialogBg = dialogBg;
            this.dialogCardBg = dialogCardBg;
            this.dialogStroke = dialogStroke;
        }
    }

    private ThemePalette buildThemePalette() {
        boolean isDark = repository.isDarkMode();
        boolean isAmoled = repository.isAmoledMode();
        float density = getResources().getDisplayMetrics().density;
        int oneDp = Math.round(1 * density);

        // Dynamically resolve Monet system colors if available (Android 12+ API 31+)
        int monetAccentDark = 0xFFA8C7FA;
        int monetOnAccentDark = 0xFF062E6F;
        int monetBadgeBgDark = 0xFF1E2D3D;
        int monetBadgeTextDark = 0xFFC5E7FF;

        int monetAccentLight = 0xFF1A73E8;
        int monetOnAccentLight = 0xFFFFFFFF;
        int monetBadgeBgLight = 0xFFD3E3FD;
        int monetBadgeTextLight = 0xFF041E49;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            try {
                monetAccentDark = getColor(android.R.color.system_accent1_300);
                monetOnAccentDark = getColor(android.R.color.system_accent1_900);
                monetBadgeBgDark = getColor(android.R.color.system_accent2_800);
                monetBadgeTextDark = getColor(android.R.color.system_accent1_100);

                monetAccentLight = getColor(android.R.color.system_accent1_600);
                monetOnAccentLight = 0xFFFFFFFF;
                monetBadgeBgLight = getColor(android.R.color.system_accent1_100);
                monetBadgeTextLight = getColor(android.R.color.system_accent1_900);
            } catch (Exception ignored) {
            }
        }

        if (!isDark) {
            // Mode 3: Clean High-Contrast Light Mode
            return new ThemePalette(
                    false, false,
                    0xFFF5F6F8,             // rootBg: clean light grey/off-white
                    0xFFFFFFFF,             // cardBg: pure crisp white
                    0xFFE5E7EB,             // cardStroke: soft hairline border (NO harsh dark lines)
                    oneDp,                  // cardStrokeWidthPx: 1dp
                    0xFFD1D5DB,             // previewBorder: soft phone frame
                    0xFFECEEF2,             // previewCardBg
                    0xFF111827,             // textPrimary: deep slate (100% readable)
                    0xFF4B5563,             // textSecondary: neutral dark grey
                    0xFF9CA3AF,             // textTertiary
                    monetAccentLight,       // accentColor
                    monetOnAccentLight,     // onAccentColor
                    0xFFD1D5DB,             // outlineButtonStroke: soft outline
                    0xFFD1D5DB,             // toggleUnselectedStroke
                    0xFF111827,             // toggleUnselectedText: dark readable text!
                    monetBadgeBgLight,      // badgeBg
                    monetBadgeTextLight,    // badgeTextColor
                    0xFFFFFFFF,             // dialogBg
                    0xFFF8F9FA,             // dialogCardBg
                    0xFFE5E7EB              // dialogStroke
            );
        } else if (isAmoled) {
            // Mode 1: True AMOLED Mode (Pure Black)
            return new ThemePalette(
                    true, true,
                    0xFF000000,             // rootBg: pure OLED black
                    0xFF101114,             // cardBg: deep OLED charcoal surface
                    0x00000000,             // cardStroke: transparent (ZERO WHITE BORDERS!)
                    0,                      // cardStrokeWidthPx: 0dp
                    0xFF22252C,             // previewBorder: subtle dark phone bezel
                    0xFF000000,             // previewCardBg: black
                    0xFFFFFFFF,             // textPrimary: crisp 100% white
                    0xFFA0A4B0,             // textSecondary: soft cool grey
                    0xFF656A76,             // textTertiary: muted caption grey
                    monetAccentDark,        // accentColor: luminous Monet dynamic accent
                    monetOnAccentDark,      // onAccentColor
                    0xFF282B33,             // outlineButtonStroke: dark outline
                    0xFF282B33,             // toggleUnselectedStroke
                    0xFFFFFFFF,             // toggleUnselectedText: crisp white
                    0xFF16181D,             // badgeBg: subtle dark container
                    monetBadgeTextDark,     // badgeTextColor
                    0xFF000000,             // dialogBg: pure black
                    0xFF121418,             // dialogCardBg
                    0x00000000              // dialogStroke: transparent
            );
        } else {
            // Mode 2: Standard Grey Dark Mode
            return new ThemePalette(
                    true, false,
                    0xFF14161B,             // rootBg: dark charcoal grey (NEVER WHITE!)
                    0xFF1F2228,             // cardBg: elevated dark card surface
                    0xFF2C303A,             // cardStroke: subtle dark stroke (NEVER WHITE!)
                    oneDp,                  // cardStrokeWidthPx: 1dp
                    0xFF2C303A,             // previewBorder: subtle dark phone frame
                    0xFF14161B,             // previewCardBg
                    0xFFFFFFFF,             // textPrimary: crisp 100% white
                    0xFFA8ADB8,             // textSecondary: soft light grey
                    0xFF707684,             // textTertiary: muted grey
                    monetAccentDark,        // accentColor: luminous Monet dynamic accent
                    monetOnAccentDark,      // onAccentColor
                    0xFF353A45,             // outlineButtonStroke: subtle dark outline
                    0xFF353A45,             // toggleUnselectedStroke
                    0xFFFFFFFF,             // toggleUnselectedText: crisp white
                    0xFF252932,             // badgeBg: elevated badge container
                    monetBadgeTextDark,     // badgeTextColor
                    0xFF181A1F,             // dialogBg: dark grey
                    0xFF22252C,             // dialogCardBg
                    0xFF2C303A              // dialogStroke: subtle dark stroke
            );
        }
    }

    private void applyThemeMode() {
        ThemePalette palette = buildThemePalette();

        // 1. Root & App Bar Backgrounds
        coordinatorRoot.setBackgroundColor(palette.rootBg);
        appbar.setBackgroundColor(palette.rootBg);

        // 2. Cards (Background, Stroke Width, Stroke Color)
        applyCardTheme(cardStatus, palette);
        applyCardTheme(cardCover, palette);
        applyCardTheme(cardInner, palette);
        applyCardTheme(bottomBarContainer, palette);

        // 3. Wallpaper Preview Card Frames
        if (cardPreviewCover != null) {
            cardPreviewCover.setCardBackgroundColor(palette.previewCardBg);
            cardPreviewCover.setStrokeColor(ColorStateList.valueOf(palette.previewBorder));
            cardPreviewCover.setStrokeWidth(Math.round(1.5f * getResources().getDisplayMetrics().density));
        }
        if (cardPreviewInner != null) {
            cardPreviewInner.setCardBackgroundColor(palette.previewCardBg);
            cardPreviewInner.setStrokeColor(ColorStateList.valueOf(palette.previewBorder));
            cardPreviewInner.setStrokeWidth(Math.round(1.5f * getResources().getDisplayMetrics().density));
        }

        // 4. Typography
        tvAppTitle.setTextColor(palette.textPrimary);
        btnSettings.setImageTintList(ColorStateList.valueOf(palette.textPrimary));
        tvServiceStatus.setTextColor(palette.textPrimary);
        if (dividerStatus != null) {
            dividerStatus.setBackgroundColor(palette.isAmoled ? 0xFF22252C : palette.cardStroke);
        }
        tvAspectRatioDiag.setTextColor(palette.textTertiary);

        // Badges
        if (tvAppVersion != null) {
            tvAppVersion.setBackgroundTintList(ColorStateList.valueOf(palette.badgeBg));
            tvAppVersion.setTextColor(palette.badgeTextColor);
        }
        if (badgeCoverRatio != null) {
            badgeCoverRatio.setBackgroundTintList(ColorStateList.valueOf(palette.badgeBg));
            badgeCoverRatio.setTextColor(palette.badgeTextColor);
        }
        if (badgeInnerRatio != null) {
            badgeInnerRatio.setBackgroundTintList(ColorStateList.valueOf(palette.badgeBg));
            badgeInnerRatio.setTextColor(palette.badgeTextColor);
        }

        // Device state
        if (tvDeviceState != null) {
            tvDeviceState.setTextColor(palette.accentColor);
        }
        if (ivDeviceStateIcon != null) {
            ivDeviceStateIcon.setImageTintList(ColorStateList.valueOf(palette.accentColor));
        }

        tvCoverTitle.setTextColor(palette.textPrimary);
        if (ivCoverIcon != null) {
            ivCoverIcon.setImageTintList(ColorStateList.valueOf(palette.accentColor));
        }
        tvCoverDesc.setTextColor(palette.textSecondary);
        tvCoverScaleTitle.setTextColor(palette.textSecondary);

        tvInnerTitle.setTextColor(palette.textPrimary);
        if (ivInnerIcon != null) {
            ivInnerIcon.setImageTintList(ColorStateList.valueOf(palette.accentColor));
        }
        tvInnerDesc.setTextColor(palette.textSecondary);
        tvInnerScaleTitle.setTextColor(palette.textSecondary);

        tvPlaceholderCover.setTextColor(palette.textTertiary);
        tvPlaceholderInner.setTextColor(palette.textTertiary);

        // 5. Action Buttons (Material You dynamic filled color)
        if (btnPickCover != null) {
            btnPickCover.setBackgroundTintList(ColorStateList.valueOf(palette.accentColor));
            btnPickCover.setTextColor(palette.onAccentColor);
            btnPickCover.setIconTint(ColorStateList.valueOf(palette.onAccentColor));
        }
        if (btnPickInner != null) {
            btnPickInner.setBackgroundTintList(ColorStateList.valueOf(palette.accentColor));
            btnPickInner.setTextColor(palette.onAccentColor);
            btnPickInner.setIconTint(ColorStateList.valueOf(palette.onAccentColor));
        }

        // Secondary Outlined Buttons
        if (btnResetCover != null) {
            btnResetCover.setStrokeColor(ColorStateList.valueOf(palette.outlineButtonStroke));
            btnResetCover.setTextColor(palette.accentColor);
            btnResetCover.setIconTint(ColorStateList.valueOf(palette.accentColor));
        }
        if (btnResetInner != null) {
            btnResetInner.setStrokeColor(ColorStateList.valueOf(palette.outlineButtonStroke));
            btnResetInner.setTextColor(palette.accentColor);
            btnResetInner.setIconTint(ColorStateList.valueOf(palette.accentColor));
        }

        // Apply Wallpaper Sticky Button
        if (btnApplyWallpaper != null) {
            btnApplyWallpaper.setBackgroundTintList(ColorStateList.valueOf(palette.accentColor));
            btnApplyWallpaper.setTextColor(palette.onAccentColor);
            btnApplyWallpaper.setIconTint(ColorStateList.valueOf(palette.onAccentColor));
        }

        // Footer Detail Strips
        if (containerCoverStatusDetail != null) {
            containerCoverStatusDetail.setBackgroundTintList(ColorStateList.valueOf(palette.badgeBg));
            tvStatusCoverDetail.setTextColor(palette.badgeTextColor);
        }
        if (containerInnerStatusDetail != null) {
            containerInnerStatusDetail.setBackgroundTintList(ColorStateList.valueOf(palette.badgeBg));
            tvStatusInnerDetail.setTextColor(palette.badgeTextColor);
        }

        // Style Toggle Buttons in MaterialButtonToggleGroup
        boolean coverIsFit = (repository.getCoverScaleMode() == WallpaperRepository.SCALE_MODE_FIT);
        styleToggleButton(btnCoverCrop, !coverIsFit, palette.accentColor, palette.onAccentColor, palette.toggleUnselectedText, palette.toggleUnselectedStroke);
        styleToggleButton(btnCoverFit, coverIsFit, palette.accentColor, palette.onAccentColor, palette.toggleUnselectedText, palette.toggleUnselectedStroke);

        boolean innerIsFit = (repository.getInnerScaleMode() == WallpaperRepository.SCALE_MODE_FIT);
        styleToggleButton(btnInnerCrop, !innerIsFit, palette.accentColor, palette.onAccentColor, palette.toggleUnselectedText, palette.toggleUnselectedStroke);
        styleToggleButton(btnInnerFit, innerIsFit, palette.accentColor, palette.onAccentColor, palette.toggleUnselectedText, palette.toggleUnselectedStroke);

        // System Bars
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(!palette.isDark);
        controller.setAppearanceLightNavigationBars(!palette.isDark);
    }

    private void applyCardTheme(MaterialCardView card, ThemePalette palette) {
        if (card == null) return;
        card.setCardBackgroundColor(palette.cardBg);
        card.setStrokeWidth(palette.cardStrokeWidthPx);
        card.setStrokeColor(ColorStateList.valueOf(palette.cardStroke));
    }

    private void styleToggleButton(MaterialButton button, boolean isChecked, int accentColor, int onAccentColor, int unselectedText, int strokeColor) {
        if (button == null) return;
        if (isChecked) {
            button.setBackgroundTintList(ColorStateList.valueOf(accentColor));
            button.setTextColor(onAccentColor);
            button.setStrokeColor(ColorStateList.valueOf(accentColor));
        } else {
            button.setBackgroundTintList(ColorStateList.valueOf(0x00000000));
            button.setTextColor(unselectedText);
            button.setStrokeColor(ColorStateList.valueOf(strokeColor));
        }
    }

    private void applyBottomSheetWindowTheme(BottomSheetDialog dialog, ThemePalette palette) {
        if (dialog == null) return;
        android.view.Window window = dialog.getWindow();
        if (window == null) return;

        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowCompat.setDecorFitsSystemWindows(window, false);

        // Keep navigation bar color identical to the dialog background
        window.setNavigationBarColor(palette.dialogBg);
        window.setStatusBarColor(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setNavigationBarContrastEnforced(false);
            window.setStatusBarContrastEnforced(false);
        }

        // Apply appearance light/dark to gesture navigation bar & status bar icons
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
        if (controller != null) {
            // Dark / AMOLED mode -> gesture pill is white (light=false)
            // Light mode -> gesture pill is dark (light=true)
            controller.setAppearanceLightNavigationBars(!palette.isDark);
            controller.setAppearanceLightStatusBars(!palette.isDark);
        }
    }

    private void setupBottomSheetDialog(BottomSheetDialog dialog, View dialogRoot, Runnable updateColors) {
        ThemePalette palette = buildThemePalette();
        applyBottomSheetWindowTheme(dialog, palette);

        final int basePaddingLeft = dialogRoot.getPaddingLeft();
        final int basePaddingTop = dialogRoot.getPaddingTop();
        final int basePaddingRight = dialogRoot.getPaddingRight();
        final int basePaddingBottom = dialogRoot.getPaddingBottom();

        // Calculate initial bottom navigation inset to guarantee immediate clearance
        int initialInset = systemNavBottomInset;
        if (initialInset <= 0 && coordinatorRoot != null) {
            WindowInsetsCompat rootInsets = ViewCompat.getRootWindowInsets(coordinatorRoot);
            if (rootInsets != null) {
                initialInset = rootInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            }
        }
        if (initialInset <= 0) {
            initialInset = Math.round(28 * getResources().getDisplayMetrics().density);
        }

        dialogRoot.setPadding(basePaddingLeft, basePaddingTop, basePaddingRight, basePaddingBottom + initialInset);

        ViewCompat.setOnApplyWindowInsetsListener(dialogRoot, (v, windowInsets) -> {
            Insets navInsets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.navigationBars() | WindowInsetsCompat.Type.displayCutout());
            int bottomInset = Math.max(navInsets.bottom, systemNavBottomInset);
            if (bottomInset > 0) {
                v.setPadding(basePaddingLeft, basePaddingTop, basePaddingRight, basePaddingBottom + bottomInset);
            }
            return windowInsets;
        });

        dialog.setOnShowListener(dialogInterface -> {
            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundColor(Color.TRANSPARENT);
                bottomSheet.setPadding(0, 0, 0, 0);
                ViewCompat.setOnApplyWindowInsetsListener(bottomSheet, (v, insets) -> insets);
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }

            android.view.Window win = dialog.getWindow();
            if (win != null) {
                WindowInsetsCompat decorInsets = ViewCompat.getRootWindowInsets(win.getDecorView());
                if (decorInsets != null) {
                    int navBottom = decorInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
                    if (navBottom > 0) {
                        systemNavBottomInset = Math.max(systemNavBottomInset, navBottom);
                        dialogRoot.setPadding(basePaddingLeft, basePaddingTop, basePaddingRight, basePaddingBottom + navBottom);
                    }
                }
            }

            if (updateColors != null) {
                updateColors.run();
            }
        });
    }

    private void showSettingsDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_settings, null);
        dialog.setContentView(view);

        View dialogRoot = view.findViewById(R.id.settings_dialog_root);
        View dragHandle = view.findViewById(R.id.settings_drag_handle);
        TextView tvTitle = view.findViewById(R.id.tv_settings_title);
        TextView tvSubtitle = view.findViewById(R.id.tv_settings_subtitle);
        TextView tvVersionBadge = view.findViewById(R.id.tv_settings_version_badge);
        MaterialCardView cardDark = view.findViewById(R.id.card_setting_dark);
        MaterialCardView cardAmoled = view.findViewById(R.id.card_setting_amoled);
        MaterialCardView cardDisclosure = view.findViewById(R.id.card_setting_disclosure);
        TextView tvDarkTitle = view.findViewById(R.id.tv_dark_theme_title);
        TextView tvDarkDesc = view.findViewById(R.id.tv_dark_theme_desc);
        TextView tvAmoledTitle = view.findViewById(R.id.tv_amoled_mode_title);
        TextView tvAmoledDesc = view.findViewById(R.id.tv_amoled_mode_desc);
        TextView tvDisclosureTitle = view.findViewById(R.id.tv_disclosure_setting_title);
        TextView tvDisclosureDesc = view.findViewById(R.id.tv_disclosure_setting_desc);
        ImageView ivDisclosureIcon = view.findViewById(R.id.iv_disclosure_icon);
        ImageView ivDisclosureChevron = view.findViewById(R.id.iv_disclosure_chevron);
        MaterialSwitch switchDark = view.findViewById(R.id.switch_dark_theme);
        MaterialSwitch switchAmoled = view.findViewById(R.id.switch_dialog_amoled);
        MaterialButton btnDone = view.findViewById(R.id.btn_settings_done);

        switchDark.setChecked(repository.isDarkMode());
        switchAmoled.setChecked(repository.isAmoledMode());

        Runnable updateDialogColors = () -> {
            ThemePalette palette = buildThemePalette();

            applyBottomSheetWindowTheme(dialog, palette);

            if (dialogRoot.getBackground() != null) {
                dialogRoot.getBackground().setTint(palette.dialogBg);
            } else {
                dialogRoot.setBackgroundColor(palette.dialogBg);
            }

            if (dragHandle != null) {
                dragHandle.setBackgroundTintList(ColorStateList.valueOf(palette.isDark ? 0xFF484C56 : 0xFFD1D5DB));
            }

            tvTitle.setTextColor(palette.textPrimary);
            tvSubtitle.setTextColor(palette.textSecondary);

            if (tvVersionBadge != null) {
                tvVersionBadge.setBackgroundTintList(ColorStateList.valueOf(palette.badgeBg));
                tvVersionBadge.setTextColor(palette.badgeTextColor);
            }

            cardDark.setCardBackgroundColor(palette.dialogCardBg);
            cardDark.setStrokeWidth(palette.cardStrokeWidthPx);
            cardDark.setStrokeColor(ColorStateList.valueOf(palette.dialogStroke));
            tvDarkTitle.setTextColor(palette.textPrimary);
            tvDarkDesc.setTextColor(palette.textSecondary);

            cardAmoled.setCardBackgroundColor(palette.dialogCardBg);
            cardAmoled.setStrokeWidth(palette.cardStrokeWidthPx);
            cardAmoled.setStrokeColor(ColorStateList.valueOf(palette.dialogStroke));
            tvAmoledTitle.setTextColor(palette.textPrimary);
            tvAmoledDesc.setTextColor(palette.textSecondary);

            if (cardDisclosure != null) {
                cardDisclosure.setCardBackgroundColor(palette.dialogCardBg);
                cardDisclosure.setStrokeWidth(palette.cardStrokeWidthPx);
                cardDisclosure.setStrokeColor(ColorStateList.valueOf(palette.dialogStroke));
            }
            if (tvDisclosureTitle != null) tvDisclosureTitle.setTextColor(palette.textPrimary);
            if (tvDisclosureDesc != null) tvDisclosureDesc.setTextColor(palette.textSecondary);
            if (ivDisclosureIcon != null) ivDisclosureIcon.setImageTintList(ColorStateList.valueOf(palette.accentColor));
            if (ivDisclosureChevron != null) ivDisclosureChevron.setImageTintList(ColorStateList.valueOf(palette.textTertiary));

            // Respect rule: Light mode disables AMOLED switch visually, but keeps saved choice intact
            boolean isDark = repository.isDarkMode();
            switchAmoled.setEnabled(isDark);
            cardAmoled.setAlpha(isDark ? 1.0f : 0.45f);

            // Material Switches tinting
            ColorStateList switchTrack = new ColorStateList(
                    new int[][]{
                            new int[]{android.R.attr.state_checked},
                            new int[]{-android.R.attr.state_checked}
                    },
                    new int[]{
                            palette.accentColor,
                            palette.isDark ? 0xFF353A45 : 0xFFD1D5DB
                    }
            );
            ColorStateList switchThumb = new ColorStateList(
                    new int[][]{
                            new int[]{android.R.attr.state_checked},
                            new int[]{-android.R.attr.state_checked}
                    },
                    new int[]{
                            palette.onAccentColor,
                            palette.isDark ? 0xFFA0A4B0 : 0xFFFFFFFF
                    }
            );
            switchDark.setThumbTintList(switchThumb);
            switchDark.setTrackTintList(switchTrack);
            switchAmoled.setThumbTintList(switchThumb);
            switchAmoled.setTrackTintList(switchTrack);

            if (btnDone != null) {
                btnDone.setBackgroundTintList(ColorStateList.valueOf(palette.accentColor));
                btnDone.setTextColor(palette.onAccentColor);
            }
        };

        setupBottomSheetDialog(dialog, dialogRoot, updateDialogColors);
        updateDialogColors.run();

        switchDark.setOnCheckedChangeListener((buttonView, isChecked) -> {
            repository.setDarkMode(isChecked);
            applyThemeMode();
            updateDialogColors.run();
        });

        switchAmoled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            repository.setAmoledMode(isChecked);
            applyThemeMode();
            updateDialogColors.run();
        });

        if (cardDisclosure != null) {
            cardDisclosure.setOnClickListener(v -> {
                dialog.dismiss();
                showDisclosureDialog(false);
            });
        }

        btnDone.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showDisclosureDialog(boolean isFirstLaunch) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_disclosure, null);
        dialog.setContentView(view);

        if (isFirstLaunch) {
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
        }

        View root = view.findViewById(R.id.disclosure_dialog_root);
        View dragHandle = view.findViewById(R.id.disclosure_drag_handle);
        TextView tvTitle = view.findViewById(R.id.tv_disclosure_title);
        TextView tvBadge = view.findViewById(R.id.tv_disclosure_badge);

        MaterialCardView cardAi = view.findViewById(R.id.card_disclosure_ai);
        TextView tvAiTitle = view.findViewById(R.id.tv_disclosure_ai_title);
        TextView tvAiBody = view.findViewById(R.id.tv_disclosure_ai_body);

        MaterialCardView cardExp = view.findViewById(R.id.card_disclosure_experimental);
        TextView tvExpTitle = view.findViewById(R.id.tv_disclosure_experimental_title);
        TextView tvExpBody = view.findViewById(R.id.tv_disclosure_experimental_body);

        MaterialCardView cardPriv = view.findViewById(R.id.card_disclosure_privacy);
        TextView tvPrivTitle = view.findViewById(R.id.tv_disclosure_privacy_title);
        TextView tvPrivBody = view.findViewById(R.id.tv_disclosure_privacy_body);

        MaterialButton btnAccept = view.findViewById(R.id.btn_disclosure_accept);

        ThemePalette palette = buildThemePalette();

        setupBottomSheetDialog(dialog, root, () -> {
            ThemePalette p = buildThemePalette();
            applyBottomSheetWindowTheme(dialog, p);
        });
        applyBottomSheetWindowTheme(dialog, palette);

        if (root.getBackground() != null) {
            root.getBackground().setTint(palette.dialogBg);
        } else {
            root.setBackgroundColor(palette.dialogBg);
        }

        if (dragHandle != null) {
            dragHandle.setBackgroundTintList(ColorStateList.valueOf(palette.isDark ? 0xFF484C56 : 0xFFD1D5DB));
        }

        if (tvTitle != null) tvTitle.setTextColor(palette.textPrimary);

        if (tvBadge != null) {
            tvBadge.setBackgroundTintList(ColorStateList.valueOf(palette.badgeBg));
            tvBadge.setTextColor(palette.badgeTextColor);
        }

        if (cardAi != null) {
            cardAi.setCardBackgroundColor(palette.dialogCardBg);
            cardAi.setStrokeWidth(palette.cardStrokeWidthPx);
            cardAi.setStrokeColor(ColorStateList.valueOf(palette.dialogStroke));
        }
        if (cardExp != null) {
            cardExp.setCardBackgroundColor(palette.dialogCardBg);
            cardExp.setStrokeWidth(palette.cardStrokeWidthPx);
            cardExp.setStrokeColor(ColorStateList.valueOf(palette.dialogStroke));
        }
        if (cardPriv != null) {
            cardPriv.setCardBackgroundColor(palette.dialogCardBg);
            cardPriv.setStrokeWidth(palette.cardStrokeWidthPx);
            cardPriv.setStrokeColor(ColorStateList.valueOf(palette.dialogStroke));
        }

        if (tvAiTitle != null) tvAiTitle.setTextColor(palette.accentColor);
        if (tvAiBody != null) tvAiBody.setTextColor(palette.textPrimary);

        if (tvExpTitle != null) tvExpTitle.setTextColor(palette.isDark ? 0xFFFFB4AB : 0xFFBA1A1A);
        if (tvExpBody != null) tvExpBody.setTextColor(palette.textPrimary);

        if (tvPrivTitle != null) tvPrivTitle.setTextColor(palette.isDark ? 0xFF8AE99C : 0xFF146C2E);
        if (tvPrivBody != null) tvPrivBody.setTextColor(palette.textPrimary);

        if (btnAccept != null) {
            btnAccept.setBackgroundTintList(ColorStateList.valueOf(palette.accentColor));
            btnAccept.setTextColor(palette.onAccentColor);
            btnAccept.setOnClickListener(v -> {
                if (isFirstLaunch) {
                    repository.setDisclosureAcknowledged(true);
                }
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void setupInsets() {
        final int defaultBottomPadding = bottomBarInner.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(coordinatorRoot, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());

            systemNavBottomInset = insets.bottom;

            // 1. Pad AppBar for the status bar / notification shade
            appbar.setPadding(
                    appbar.getPaddingLeft(),
                    insets.top,
                    appbar.getPaddingRight(),
                    appbar.getPaddingBottom()
            );

            // 2. Pad Bottom Bar for the Pixel gesture navigation bar
            bottomBarInner.setPadding(
                    bottomBarInner.getPaddingLeft(),
                    bottomBarInner.getPaddingTop(),
                    bottomBarInner.getPaddingRight(),
                    defaultBottomPadding + insets.bottom
            );

            return windowInsets;
        });
    }

    private void setupPickers() {
        coverImagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        handleWallpaperSelected(uri, true);
                    }
                });

        innerImagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        handleWallpaperSelected(uri, false);
                    }
                });
    }

    private void setupListeners() {
        btnSettings.setOnClickListener(v -> showSettingsDialog());

        btnPickCover.setOnClickListener(v ->
                coverImagePickerLauncher.launch("image/*"));

        btnResetCover.setOnClickListener(v -> {
            repository.removeWallpaper(true);
            updatePreviews();
        });

        btnPickInner.setOnClickListener(v ->
                innerImagePickerLauncher.launch("image/*"));

        btnResetInner.setOnClickListener(v -> {
            repository.removeWallpaper(false);
            updatePreviews();
        });

        // Toggle Group for Cover Scale Mode
        toggleCoverScale.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                boolean isFit = (checkedId == R.id.btn_cover_fit);
                repository.setCoverScaleMode(isFit ? WallpaperRepository.SCALE_MODE_FIT : WallpaperRepository.SCALE_MODE_CROP);
                ivPreviewCover.setScaleType(isFit ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP);
                applyThemeMode();
            }
        });

        // Toggle Group for Inner Scale Mode
        toggleInnerScale.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                boolean isFit = (checkedId == R.id.btn_inner_fit);
                repository.setInnerScaleMode(isFit ? WallpaperRepository.SCALE_MODE_FIT : WallpaperRepository.SCALE_MODE_CROP);
                ivPreviewInner.setScaleType(isFit ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP);
                applyThemeMode();
            }
        });

        btnApplyWallpaper.setOnClickListener(v -> openLiveWallpaperChooser());
    }

    private void handleWallpaperSelected(Uri uri, boolean isCover) {
        boolean success = repository.saveWallpaperFromUri(uri, isCover);
        if (success) {
            updatePreviews();
        }
    }

    private void updateStatus() {
        WallpaperManager wm = WallpaperManager.getInstance(this);
        WallpaperInfo info = wm.getWallpaperInfo();

        boolean isActive = false;
        if (info != null && info.getComponent() != null) {
            isActive = info.getComponent().getClassName().equals(FoldPaperService.class.getName());
        }

        if (isActive) {
            ivStatusIcon.setImageResource(R.drawable.ic_check_circle);
            ivStatusIcon.setColorFilter(null);
            tvServiceStatus.setText(R.string.status_wallpaper_active);
        } else {
            ivStatusIcon.setImageResource(R.drawable.ic_wallpaper_thumb);
            ivStatusIcon.setColorFilter(null);
            tvServiceStatus.setText(R.string.status_wallpaper_inactive);
        }
    }

    private void updatePreviews() {
        // Update Cover Preview
        if (repository.hasCoverWallpaper()) {
            Bitmap coverBmp = WallpaperRepository.decodeSampledBitmap(
                    repository.getCoverFile(), 200, 420);
            if (coverBmp != null) {
                ivPreviewCover.setImageBitmap(coverBmp);
                ivPreviewCover.setVisibility(View.VISIBLE);
                tvPlaceholderCover.setVisibility(View.GONE);
                ivPreviewCover.setScaleType(repository.getCoverScaleMode() == WallpaperRepository.SCALE_MODE_FIT
                        ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP);
            }
            if (tvStatusCoverDetail != null) {
                tvStatusCoverDetail.setText(R.string.status_custom_image);
                ivCoverStatusDot.setImageResource(R.drawable.ic_check_circle);
                ivCoverStatusDot.setImageTintList(ColorStateList.valueOf(0xFF22C55E));
            }
            if (btnResetCover != null) {
                btnResetCover.setEnabled(true);
                btnResetCover.setAlpha(1.0f);
            }
        } else {
            ivPreviewCover.setImageDrawable(null);
            ivPreviewCover.setVisibility(View.GONE);
            tvPlaceholderCover.setVisibility(View.VISIBLE);
            if (tvStatusCoverDetail != null) {
                tvStatusCoverDetail.setText(R.string.status_default_image);
                ivCoverStatusDot.setImageResource(R.drawable.ic_wallpaper_thumb);
                ivCoverStatusDot.setImageTintList(ColorStateList.valueOf(0xFF94A3B8));
            }
            if (btnResetCover != null) {
                btnResetCover.setEnabled(false);
                btnResetCover.setAlpha(0.5f);
            }
        }

        // Update Inner Preview
        if (repository.hasInnerWallpaper()) {
            Bitmap innerBmp = WallpaperRepository.decodeSampledBitmap(
                    repository.getInnerFile(), 300, 300);
            if (innerBmp != null) {
                ivPreviewInner.setImageBitmap(innerBmp);
                ivPreviewInner.setVisibility(View.VISIBLE);
                tvPlaceholderInner.setVisibility(View.GONE);
                ivPreviewInner.setScaleType(repository.getInnerScaleMode() == WallpaperRepository.SCALE_MODE_FIT
                        ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP);
            }
            if (tvStatusInnerDetail != null) {
                tvStatusInnerDetail.setText(R.string.status_custom_image);
                ivInnerStatusDot.setImageResource(R.drawable.ic_check_circle);
                ivInnerStatusDot.setImageTintList(ColorStateList.valueOf(0xFF22C55E));
            }
            if (btnResetInner != null) {
                btnResetInner.setEnabled(true);
                btnResetInner.setAlpha(1.0f);
            }
        } else {
            ivPreviewInner.setImageDrawable(null);
            ivPreviewInner.setVisibility(View.GONE);
            tvPlaceholderInner.setVisibility(View.VISIBLE);
            if (tvStatusInnerDetail != null) {
                tvStatusInnerDetail.setText(R.string.status_default_image);
                ivInnerStatusDot.setImageResource(R.drawable.ic_wallpaper_thumb);
                ivInnerStatusDot.setImageTintList(ColorStateList.valueOf(0xFF94A3B8));
            }
            if (btnResetInner != null) {
                btnResetInner.setEnabled(false);
                btnResetInner.setAlpha(0.5f);
            }
        }
    }

    private void updateDeviceStateDiagnostics() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        int minDim = Math.min(width, height);
        int maxDim = Math.max(width, height);
        float ratio = (maxDim > 0) ? ((float) minDim / (float) maxDim) : 0f;

        float threshold = repository.getAspectThreshold();
        boolean isCover = (ratio < threshold);

        if (isCover) {
            ivDeviceStateIcon.setImageResource(R.drawable.ic_phone_cover);
            tvDeviceState.setText(R.string.device_state_folded);
        } else {
            ivDeviceStateIcon.setImageResource(R.drawable.ic_phone_inner);
            tvDeviceState.setText(R.string.device_state_unfolded);
        }

        tvAspectRatioDiag.setText(String.format(Locale.US, "Ratio: %.2f (%dx%d)", ratio, width, height));
    }

    private void openLiveWallpaperChooser() {
        Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(this, FoldPaperService.class));
        try {
            startActivity(intent);
        } catch (Exception e) {
            Intent fallback = new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
            try {
                startActivity(fallback);
            } catch (Exception ignored) {
            }
        }
    }
}
