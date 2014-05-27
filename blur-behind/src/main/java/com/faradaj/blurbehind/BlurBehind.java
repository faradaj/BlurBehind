package com.faradaj.blurbehind;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.view.View;
import com.faradaj.blurbehind.util.Blur;

public class BlurBehind {

	private static final String KEY_CACHE_BLURRED_BACKGROUND_IMAGE = "KEY_CACHE_BLURRED_BACKGROUND_IMAGE";
	private static final int CONSTANT_BLUR_RADIUS = 20;
	private static final int CONSTANT_DEFAULT_ALPHA = 100;

	private static final LruCache<String, Bitmap> mImageCache = new LruCache<String, Bitmap>(1);

	public static void execute(Activity activity, Runnable runnable) {
		new CacheBlurBehindAndExecuteTask(activity, runnable).execute();
	}

	public static void setBackground(Activity activity) {
		setBackground(activity, CONSTANT_DEFAULT_ALPHA);
	}

	public static void setBackground(Activity activity, int alpha) {
		Bitmap background = mImageCache.get(KEY_CACHE_BLURRED_BACKGROUND_IMAGE);
		if (background != null) {
			BitmapDrawable bd = new BitmapDrawable(activity.getResources(), background);
			bd.setAlpha(alpha);
			activity.getWindow().setBackgroundDrawable(bd);
            background.recycle();
		}
	}

	private static class CacheBlurBehindAndExecuteTask extends AsyncTask<Void, Void, Void> {

		private Activity mActivity;
		private Runnable mRunnable;

		private Bitmap mImage;

		public CacheBlurBehindAndExecuteTask(Activity activity, Runnable r) {
			mActivity = activity;
			mRunnable = r;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			View v = mActivity.getWindow().getDecorView();
			v.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
			v.setDrawingCacheEnabled(true);
			v.buildDrawingCache();

			mImage = v.getDrawingCache();
		}

		@Override
		protected Void doInBackground(Void... params) {
			Bitmap blurredBitmap = Blur.apply(mActivity, mImage, CONSTANT_BLUR_RADIUS);
			mImageCache.put(KEY_CACHE_BLURRED_BACKGROUND_IMAGE, blurredBitmap);

            blurredBitmap.recycle();

			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);

            mImage.recycle();

			mRunnable.run();
		}
	}
}
