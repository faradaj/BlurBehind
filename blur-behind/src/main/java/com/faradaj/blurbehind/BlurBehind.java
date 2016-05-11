package com.faradaj.blurbehind;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.view.View;

import com.faradaj.blurbehind.util.Blur;

public class BlurBehind {

    private static final String KEY_CACHE_BLURRED_BACKGROUND_IMAGE = "KEY_CACHE_BLURRED_BACKGROUND_IMAGE";
    private static final int CONSTANT_BLUR_RADIUS = 12;
    private static final int CONSTANT_DEFAULT_ALPHA = 100;

    private static final LruCache<String, Bitmap> mImageCache = new LruCache<String, Bitmap>(1);
    private static CacheBlurBehindAndExecuteTask cacheBlurBehindAndExecuteTask;

    private int mAlpha = CONSTANT_DEFAULT_ALPHA;
    private int mFilterColor = -1;
    private int mBlurRadius = CONSTANT_BLUR_RADIUS;

    private enum State {
        READY,
        EXECUTING
    }

    private State mState = State.READY;

    private static BlurBehind mInstance;

    public static BlurBehind getInstance() {
        if (mInstance == null) {
            mInstance = new BlurBehind();
        }
        return mInstance;
    }

    public void execute(Activity activity, OnBlurCompleteListener onBlurCompleteListener) {
        if (mState.equals(State.READY)) {
            mState = State.EXECUTING;
            cacheBlurBehindAndExecuteTask = new CacheBlurBehindAndExecuteTask(activity, onBlurCompleteListener);
            cacheBlurBehindAndExecuteTask.execute();
        }
    }

    public BlurBehind withAlpha(int alpha) {
        this.mAlpha = alpha;
        return this;
    }

    public BlurBehind withFilterColor(int filterColor) {
        this.mFilterColor = filterColor;
        return this;
    }

    public BlurBehind withBlurRadius(int blurRadius) {
        this.mBlurRadius = blurRadius;
        return this;
    }


    public void setBackground(Activity activity) {
        if (mImageCache.size() != 0) {
            setBackgroundWithoutClearCache(activity);
            clearImageCache();
        }
    }

    public void setBackgroundWithoutImageRemove(Activity activity) {
        if (mImageCache.size() != 0) {
            setBackgroundWithoutClearCache(activity);
        }
    }

    public void clearImageCache() {
        if (mImageCache.size() != 0) {
            mImageCache.remove(KEY_CACHE_BLURRED_BACKGROUND_IMAGE);
        }
    }

    public Bitmap getSavedBluredImage() {
        return mImageCache.size() != 0 ? mImageCache.get(KEY_CACHE_BLURRED_BACKGROUND_IMAGE) : null;
    }

    private void setBackgroundWithoutClearCache(Activity activity) {
        BitmapDrawable bd = new BitmapDrawable(activity.getResources(), mImageCache.get(KEY_CACHE_BLURRED_BACKGROUND_IMAGE));
        bd.setAlpha(mAlpha);
        if (mFilterColor != -1) {
            bd.setColorFilter(mFilterColor, PorterDuff.Mode.DST_ATOP);
        }
        activity.getWindow().setBackgroundDrawable(bd);
        cacheBlurBehindAndExecuteTask = null;
    }

    private class CacheBlurBehindAndExecuteTask extends AsyncTask<Void, Void, Void> {
        private Activity activity;
        private OnBlurCompleteListener onBlurCompleteListener;

        private View decorView;
        private Bitmap image;

        public CacheBlurBehindAndExecuteTask(Activity activity, OnBlurCompleteListener onBlurCompleteListener) {
            this.activity = activity;
            this.onBlurCompleteListener = onBlurCompleteListener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            decorView = activity.getWindow().getDecorView();
            decorView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
            decorView.setDrawingCacheEnabled(true);
            decorView.buildDrawingCache();

            image = decorView.getDrawingCache();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Bitmap blurredBitmap = Blur.apply(activity, image, mBlurRadius);
            mImageCache.put(KEY_CACHE_BLURRED_BACKGROUND_IMAGE, blurredBitmap);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            decorView.destroyDrawingCache();
            decorView.setDrawingCacheEnabled(false);

            activity = null;

            onBlurCompleteListener.onBlurComplete();

            mState = State.READY;
        }
    }
}
