package com.demo.tikpic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

    private static final String TAG = "myAsyncTask";
    private WeakReference<ImageView> imageView;
    private MainActivity hostActivity;
    // private InputStream is = null;

    public BitmapWorkerTask(MainActivity ma, ImageView iv) {
        imageView = new WeakReference<>(iv);
        hostActivity = ma;
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(String... params) {
        String path = params[0];
        String id = path.substring(path.lastIndexOf("/") + 1);

        //File file = new File(hostActivity.getExternalCacheDir(), id + ".jpg");
        //if(!file.exists()) {
            InputStream is;
            Bitmap mBitmap = null;
            try {
                is = hostActivity.getContentResolver()
                        .openInputStream(Uri.parse(path));

                mBitmap = BitmapFactory.decodeStream(is, null, null);
                is.close();
                mBitmap = ThumbnailUtils.extractThumbnail(mBitmap, 100, 100);


                /*
                if(file.createNewFile()) {
                    FileOutputStream out = new FileOutputStream(file);
                    mBitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
                    out.flush();
                    out.close();
                } else {
                    Log.e("createCache","Cache file creation failed.");
                }
                */
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        //}

        return mBitmap;
        // return decodeBitmapFromStream(is, 100, 100);
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        /*
        if(is != null) {
            try {
                is.close();
                Log.d(TAG, "onPostExecute: InputStream closed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

         */

        if (bitmap != null) {
            Log.d(TAG, "onPostExecute: got valid bitmap, prepare to load");
            final ImageView view = imageView.get();
            if (view != null) {
                Log.d(TAG, "onPostExecute: reference is not null");
                view.setImageBitmap(bitmap);
            }
            else {
                Log.d(TAG, "onPostExecute: reference is null");
            }
        }
    }

    private Bitmap decodeBitmapFromStream(InputStream is, int reqWidth, int reqHeight) {

        if(is != null)
            Log.d(TAG, "decodeBitmapFromStream: bitmap not null");
        else
            Log.d(TAG, "decodeBitmapFromStream: bitmap null");


        // BitmapFactory.decodeStream(is, null, options);
        byte[] temp;
        Bitmap bitmap = null;
        try {

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            temp = getBytes(is);
            BitmapFactory.decodeByteArray(temp, 0, temp.length, options);


            if(options.outHeight > reqHeight || options.outWidth > reqWidth) {

                float heightScale = options.outHeight/reqHeight;
                float widthScale = options.outWidth/reqWidth;


                options.inSampleSize =
                        Math.round(heightScale > widthScale ? heightScale : widthScale);
            }
            else {
                options.inSampleSize = 1;
                Log.d(TAG, "decodeBitmapFromStream: insamplesize not set");
            }

            // options.inSampleSize = calcImage(options, reqWidth, reqHeight);
            options.inSampleSize = 8;
            Log.d(TAG, "decodeBitmapFromStream: imsamplesize: " + options.inSampleSize);
            options.inJustDecodeBounds = false;

            // bitmap = BitmapFactory.decodeStream(is, null, null);
            bitmap = BitmapFactory.decodeByteArray(temp, 0, temp.length, options);


        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = is.read(buffer)) != -1) {
            outstream.write(buffer, 0, len);
        }
        outstream.close();
        return outstream.toByteArray();
    }


    private int calcImage(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {

            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / width) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        Log.d(TAG, "decodeBitmapFromStream: inSampleSize: " + inSampleSize);
        return inSampleSize;
    }

}
