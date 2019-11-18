package com.demo.tikpic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

    private static final String TAG = "myAsyncTask";
    private WeakReference<ImageView> imageView;
    private MainActivity hostActivity;
    private InputStream is = null;

    public BitmapWorkerTask(MainActivity ma, ImageView iv) {
        imageView = new WeakReference<>(iv);
        hostActivity = ma;
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(String... params) {
        try {
            is = hostActivity.getContentResolver().openInputStream(Uri.parse(params[0]));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = decodeBitmapFromStream(is, 200, 200);
        return bitmap;
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {

        if(is != null) {
            try {
                is.close();
                Log.d(TAG, "onPostExecute: InputStream closed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (bitmap != null) {
            Log.d(TAG, "onPostExecute: got valid bitmap, prepare to load");
            imageView.get().setImageBitmap(bitmap);
        }
    }

    private Bitmap decodeBitmapFromStream(InputStream is, int reqWidth, int reqHeight) {

        Bitmap bitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;

        BitmapRegionDecoder originalImage = null;
        int height = 0;
        int width = 0;
        try {
            originalImage = BitmapRegionDecoder.newInstance(is, false);
            height = originalImage.getHeight();
            width = originalImage.getWidth();
            options.inSampleSize = calculateInSampleSize(width, height, reqWidth, reqHeight);
            // Log.d(TAG, "decodeBitmapFromStream: inSampleSize: " + options.inSampleSize);

            bitmap = originalImage.decodeRegion(new Rect(0,0, width, height), options);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private int calculateInSampleSize(int originalWidth, int originalHeight,
                                int reqWidth, int reqHeight) {

        Log.d(TAG, "getInSampleSize: width: " + originalWidth);
        Log.d(TAG, "getInSampleSize: height: " + originalHeight);

        int inSampleSize = 1;

        if (originalHeight > reqHeight || originalWidth > reqWidth) {

            final int halfHeight = originalHeight / 2;
            final int halfWidth = originalWidth / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
