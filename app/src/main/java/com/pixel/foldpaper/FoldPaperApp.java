package com.pixel.foldpaper;

import android.app.Application;
import com.google.android.material.color.DynamicColors;

public class FoldPaperApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Enable Material You Dynamic Colors (Monet palette extracted from wallpaper)
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}

