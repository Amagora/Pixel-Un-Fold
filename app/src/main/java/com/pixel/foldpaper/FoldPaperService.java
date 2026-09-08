package com.pixel.foldpaper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Build;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.File;

/**
 * High-performance, zero-wake-lock Live Wallpaper service for Pixel Fold.
 * Automatically switches between cover and inner screen wallpapers with zero lag.
 */
public class FoldPaperService extends WallpaperService {
    private static final String TAG = "FoldPaperService";

    @Override
    public Engine onCreateEngine() {
        return new FoldPaperEngine();
    }

    private class FoldPaperEngine extends Engine {
        private final WallpaperRepository repository;
        private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        private final Paint placeholderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Object renderLock = new Object();

        private int surfaceWidth = 0;
        private int surfaceHeight = 0;
        private boolean isVisible = false;

        private Bitmap cachedCoverBitmap = null;
        private long cachedCoverTimestamp = 0;
        private Bitmap cachedInnerBitmap = null;
        private long cachedInnerTimestamp = 0;

        private boolean isCoverDisplay = true;
        private boolean isReceiverRegistered = false;

        private final BroadcastReceiver changeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (WallpaperRepository.ACTION_WALLPAPER_CHANGED.equals(intent.getAction())) {
                    synchronized (renderLock) {
                        // Mark timestamps as stale so bitmaps will reload from storage
                        cachedCoverTimestamp = 0;
                        cachedInnerTimestamp = 0;
                        ensureActiveBitmapLoaded();
                    }
                    if (isVisible) {
                        drawFrame();
                    }
                }
            }
        };

        FoldPaperEngine() {
            this.repository = new WallpaperRepository(FoldPaperService.this);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            IntentFilter filter = new IntentFilter(WallpaperRepository.ACTION_WALLPAPER_CHANGED);
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    registerReceiver(changeReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
                } else {
                    registerReceiver(changeReceiver, filter);
                }
                isReceiverRegistered = true;
            } catch (Exception e) {
                Log.e(TAG, "Error registering change receiver", e);
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (isReceiverRegistered) {
                try {
                    unregisterReceiver(changeReceiver);
                } catch (Exception ignored) {}
                isReceiverRegistered = false;
            }
            synchronized (renderLock) {
                recycleBitmaps();
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.isVisible = visible;
            if (visible) {
                synchronized (renderLock) {
                    ensureActiveBitmapLoaded();
                }
                drawFrame();
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            this.surfaceWidth = width;
            this.surfaceHeight = height;

            updateDisplayState(width, height);
            synchronized (renderLock) {
                ensureActiveBitmapLoaded();
            }
            drawFrame();
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            this.isVisible = false;
        }

        /**
         * Detects whether active display is the Folded Cover screen or Unfolded Inner screen.
         * Pixel Fold Cover screen: ~1080x2342 (ratio min/max ~ 0.461)
         * Pixel Fold Inner screen: ~2076x2152 (ratio min/max ~ 0.965)
         */
        private void updateDisplayState(int width, int height) {
            if (width <= 0 || height <= 0) return;

            int minDim = Math.min(width, height);
            int maxDim = Math.max(width, height);
            float ratio = (float) minDim / (float) maxDim;

            float threshold = repository.getAspectThreshold();
            this.isCoverDisplay = (ratio < threshold);

            Log.d(TAG, String.format("Surface resized: %dx%d (min/max ratio: %.3f, threshold: %.3f) -> %s",
                    width, height, ratio, threshold, (isCoverDisplay ? "COVER" : "INNER")));
        }

        /**
         * Loads or reuses pre-cached bitmaps. Guarantees 0ms transition time during fold/unfold.
         */
        private void ensureActiveBitmapLoaded() {
            if (surfaceWidth <= 0 || surfaceHeight <= 0) return;

            if (isCoverDisplay) {
                File coverFile = repository.getCoverFile();
                if (coverFile.exists() && coverFile.length() > 0) {
                    long fileTime = coverFile.lastModified();
                    if (cachedCoverBitmap == null || cachedCoverBitmap.isRecycled() || cachedCoverTimestamp != fileTime) {
                        if (cachedCoverBitmap != null && !cachedCoverBitmap.isRecycled()) {
                            cachedCoverBitmap.recycle();
                        }
                        cachedCoverBitmap = WallpaperRepository.decodeSampledBitmap(
                                coverFile, surfaceWidth, surfaceHeight);
                        cachedCoverTimestamp = fileTime;
                    }
                } else {
                    if (cachedCoverBitmap != null) {
                        cachedCoverBitmap.recycle();
                        cachedCoverBitmap = null;
                        cachedCoverTimestamp = 0;
                    }
                }
            } else {
                File innerFile = repository.getInnerFile();
                if (innerFile.exists() && innerFile.length() > 0) {
                    long fileTime = innerFile.lastModified();
                    if (cachedInnerBitmap == null || cachedInnerBitmap.isRecycled() || cachedInnerTimestamp != fileTime) {
                        if (cachedInnerBitmap != null && !cachedInnerBitmap.isRecycled()) {
                            cachedInnerBitmap.recycle();
                        }
                        cachedInnerBitmap = WallpaperRepository.decodeSampledBitmap(
                                innerFile, surfaceWidth, surfaceHeight);
                        cachedInnerTimestamp = fileTime;
                    }
                } else {
                    if (cachedInnerBitmap != null) {
                        cachedInnerBitmap.recycle();
                        cachedInnerBitmap = null;
                        cachedInnerTimestamp = 0;
                    }
                }
            }
        }

        private void recycleBitmaps() {
            if (cachedCoverBitmap != null && !cachedCoverBitmap.isRecycled()) {
                cachedCoverBitmap.recycle();
                cachedCoverBitmap = null;
                cachedCoverTimestamp = 0;
            }
            if (cachedInnerBitmap != null && !cachedInnerBitmap.isRecycled()) {
                cachedInnerBitmap.recycle();
                cachedInnerBitmap = null;
                cachedInnerTimestamp = 0;
            }
        }

        private void drawFrame() {
            final SurfaceHolder holder = getSurfaceHolder();
            if (holder == null || surfaceWidth <= 0 || surfaceHeight <= 0) return;

            Canvas canvas = null;
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        canvas = holder.lockHardwareCanvas();
                    } catch (Throwable t) {
                        // Fallback to software canvas if hardware canvas fails
                        canvas = holder.lockCanvas();
                    }
                } else {
                    canvas = holder.lockCanvas();
                }

                if (canvas != null) {
                    synchronized (renderLock) {
                        Bitmap activeBitmap = isCoverDisplay ? cachedCoverBitmap : cachedInnerBitmap;
                        int scaleMode = isCoverDisplay
                                ? repository.getCoverScaleMode()
                                : repository.getInnerScaleMode();

                        if (activeBitmap != null && !activeBitmap.isRecycled()) {
                            drawBitmapOnCanvas(canvas, activeBitmap, scaleMode);
                        } else {
                            drawDefaultPlaceholder(canvas, isCoverDisplay);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error drawing wallpaper frame", e);
            } finally {
                if (canvas != null) {
                    try {
                        holder.unlockCanvasAndPost(canvas);
                    } catch (Exception ignored) {}
                }
            }
        }

        private void drawBitmapOnCanvas(Canvas canvas, Bitmap bitmap, int scaleMode) {
            int bw = bitmap.getWidth();
            int bh = bitmap.getHeight();
            float sw = surfaceWidth;
            float sh = surfaceHeight;

            // Clear canvas to black first (prevents ghosting with transparent images)
            canvas.drawColor(Color.BLACK);

            Matrix matrix = new Matrix();

            if (scaleMode == WallpaperRepository.SCALE_MODE_FIT) {
                // Fit center with letterboxing
                float scale = Math.min(sw / (float) bw, sh / (float) bh);
                float dx = (sw - (bw * scale)) * 0.5f;
                float dy = (sh - (bh * scale)) * 0.5f;

                matrix.setScale(scale, scale);
                matrix.postTranslate(dx, dy);
            } else {
                // Center Crop (fill screen completely)
                float scale = Math.max(sw / (float) bw, sh / (float) bh);
                float dx = (sw - (bw * scale)) * 0.5f;
                float dy = (sh - (bh * scale)) * 0.5f;

                matrix.setScale(scale, scale);
                matrix.postTranslate(dx, dy);
            }

            canvas.drawBitmap(bitmap, matrix, bitmapPaint);
        }

        private void drawDefaultPlaceholder(Canvas canvas, boolean isCover) {
            // Elegant gradient placeholder when no custom wallpaper is configured yet
            int startColor = isCover ? 0xFF0D1B2A : 0xFF1A1C20;
            int endColor = isCover ? 0xFF1B263B : 0xFF2D3139;

            LinearGradient gradient = new LinearGradient(
                    0, 0, surfaceWidth, surfaceHeight,
                    startColor, endColor, Shader.TileMode.CLAMP);
            placeholderPaint.setShader(gradient);
            canvas.drawRect(0, 0, surfaceWidth, surfaceHeight, placeholderPaint);

            // Subtle indicator text
            placeholderPaint.setShader(null);
            placeholderPaint.setColor(0x88A8C7FA);
            placeholderPaint.setTextSize(surfaceWidth * 0.04f);
            placeholderPaint.setTextAlign(Paint.Align.CENTER);

            String title = isCover ? "FoldPaper: Cover Display" : "FoldPaper: Inner Display";
            String hint = "Open FoldPaper app to choose wallpaper";

            float centerY = surfaceHeight * 0.5f;
            canvas.drawText(title, surfaceWidth * 0.5f, centerY - 20, placeholderPaint);

            placeholderPaint.setTextSize(surfaceWidth * 0.025f);
            placeholderPaint.setColor(0x55E2E2E6);
            canvas.drawText(hint, surfaceWidth * 0.5f, centerY + 30, placeholderPaint);
        }
    }
}
