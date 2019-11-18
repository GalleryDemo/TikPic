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
        String path = params[0];
        is = null;
        try {
            is = hostActivity.getContentResolver().openInputStream(Uri.parse(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return decodeBitmapFromStream(is, 200, 200);
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
        Log.d(TAG, "decodeBitmapFromStream: start " + is);
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
            options.inSampleSize = getInSampleSize(width, height, reqWidth, reqHeight);
            bitmap = originalImage.decodeRegion(new Rect(0,0, width, height), options);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(bitmap == null) {
            Log.d(TAG, "decodeBitmapFromStream: bitmap is null");
        }
        Log.d(TAG, "decodeBitmapFromStream: end" + is);
        return bitmap;
    }

    private int getInSampleSize(int originalWidth, int originalHeight,
                                int reqWidth, int reqHeight) {

        int inSampleSize = 1;

        while( (originalWidth / inSampleSize) >= reqWidth &&
                (originalHeight / inSampleSize) >= reqHeight ) {
            inSampleSize *= 2;
        }
        Log.d(TAG, "getInSampleSize: inSampleSize: " + inSampleSize);
        return inSampleSize;
    }
}
