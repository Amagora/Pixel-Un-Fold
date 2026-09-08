package com.pixel.foldpaper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class WallpaperRepository {
    private static final String TAG = "WallpaperRepository";
    public static final String ACTION_WALLPAPER_CHANGED = "com.pixel.foldpaper.ACTION_WALLPAPER_CHANGED";

    private static final String PREFS_NAME = "foldpaper_prefs";
    private static final String KEY_COVER_SCALE_MODE = "cover_scale_mode";
    private static final String KEY_INNER_SCALE_MODE = "inner_scale_mode";
    private static final String KEY_ASPECT_THRESHOLD = "aspect_threshold";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_AMOLED_MODE = "amoled_mode";
    private static final String KEY_DISCLOSURE_ACKNOWLEDGED = "disclosure_acknowledged";

    public static final int SCALE_MODE_CROP = 0;
    public static final int SCALE_MODE_FIT = 1;

    public static final float DEFAULT_ASPECT_THRESHOLD = 0.72f;

    private static final String FILE_COVER_WALLPAPER = "cover_wallpaper.png";
    private static final String FILE_INNER_WALLPAPER = "inner_wallpaper.png";

    private final Context context;
    private final SharedPreferences prefs;

    public WallpaperRepository(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public File getCoverFile() {
        return new File(context.getFilesDir(), FILE_COVER_WALLPAPER);
    }

    public File getInnerFile() {
        return new File(context.getFilesDir(), FILE_INNER_WALLPAPER);
    }

    public boolean hasCoverWallpaper() {
        File file = getCoverFile();
        return file.exists() && file.length() > 0;
    }

    public boolean hasInnerWallpaper() {
        File file = getInnerFile();
        return file.exists() && file.length() > 0;
    }

    public boolean saveWallpaperFromUri(Uri uri, boolean isCover) {
        File targetFile = isCover ? getCoverFile() : getInnerFile();
        try (InputStream in = context.getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(targetFile)) {
            if (in == null) return false;

            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();

            notifyWallpaperChanged();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error saving wallpaper from URI: " + uri, e);
            return false;
        }
    }

    public void removeWallpaper(boolean isCover) {
        File targetFile = isCover ? getCoverFile() : getInnerFile();
        if (targetFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            targetFile.delete();
        }
        notifyWallpaperChanged();
    }

    public int getCoverScaleMode() {
        return prefs.getInt(KEY_COVER_SCALE_MODE, SCALE_MODE_CROP);
    }

    public void setCoverScaleMode(int mode) {
        prefs.edit().putInt(KEY_COVER_SCALE_MODE, mode).apply();
        notifyWallpaperChanged();
    }

    public int getInnerScaleMode() {
        return prefs.getInt(KEY_INNER_SCALE_MODE, SCALE_MODE_CROP);
    }

    public void setInnerScaleMode(int mode) {
        prefs.edit().putInt(KEY_INNER_SCALE_MODE, mode).apply();
        notifyWallpaperChanged();
    }

    public float getAspectThreshold() {
        return prefs.getFloat(KEY_ASPECT_THRESHOLD, DEFAULT_ASPECT_THRESHOLD);
    }

    public void setAspectThreshold(float threshold) {
        prefs.edit().putFloat(KEY_ASPECT_THRESHOLD, threshold).apply();
        notifyWallpaperChanged();
    }

    public boolean isDarkMode() {
        return prefs.getBoolean(KEY_DARK_MODE, true);
    }

    public void setDarkMode(boolean enabled) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
    }

    public boolean isAmoledMode() {
        return prefs.getBoolean(KEY_AMOLED_MODE, true);
    }

    public void setAmoledMode(boolean enabled) {
        prefs.edit().putBoolean(KEY_AMOLED_MODE, enabled).apply();
    }

    public boolean isDisclosureAcknowledged() {
        return prefs.getBoolean(KEY_DISCLOSURE_ACKNOWLEDGED, false);
    }

    public void setDisclosureAcknowledged(boolean acknowledged) {
        prefs.edit().putBoolean(KEY_DISCLOSURE_ACKNOWLEDGED, acknowledged).apply();
    }

    private void notifyWallpaperChanged() {
        Intent intent = new Intent(ACTION_WALLPAPER_CHANGED);
        intent.setPackage(context.getPackageName());
        context.sendBroadcast(intent);
    }

    /**
     * Safely loads a bitmap with downsampling if required to avoid OutOfMemory errors on 4K/8K images.
     */
    public static Bitmap decodeSampledBitmap(File file, int reqWidth, int reqHeight) {
        if (file == null || !file.exists()) return null;

        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), options);

            if (options.outWidth <= 0 || options.outHeight <= 0) return null;

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        } catch (OutOfMemoryError oom) {
            Log.e(TAG, "OutOfMemoryError decoding bitmap: " + file.getName(), oom);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Exception decoding bitmap: " + file.getName(), e);
            return null;
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (reqWidth <= 0 || reqHeight <= 0) return 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return Math.max(1, inSampleSize);
    }
}
