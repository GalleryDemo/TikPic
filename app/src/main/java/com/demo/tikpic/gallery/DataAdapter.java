package com.demo.tikpic.gallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.disklrucache.DiskLruCache;
import com.demo.tikpic.DataManager;
import com.demo.tikpic.MainActivity;
import com.demo.tikpic.R;
import com.demo.tikpic.ViewPager.ViewPagerFragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;

import static android.os.Environment.isExternalStorageRemovable;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

    private static final String TAG = "myDataAdapter";
    private MainActivity hostActivity;
    private DataManager dataManager;
    private List<String> imageUrlList;

    private LruCache<String, Bitmap> memoryCache;

    private DiskLruCache diskLruCache;
    private final Object diskCacheLock = new Object();
    private boolean diskCacheStarting = true;
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 50; // 50MB
    private static final String DISK_CACHE_SUBDIR = "thumbnails";

    DataAdapter(MainActivity activity) {
        hostActivity = activity;
        dataManager = DataManager.getInstance(hostActivity);
        imageUrlList = dataManager.getImagePaths();

        // initialize memory cache
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024); // in KiloBytes
        final int cacheSize = maxMemory / 8; // in KiloBytes
        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024; // in KiloBytes
            }
        };

        // initialize disk cache on background thread
        File cacheDir = getDiskCacheDir(hostActivity, DISK_CACHE_SUBDIR);
        new InitDiskCacheTask().execute(cacheDir);

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: position " + position);
        loadBitmap(position, holder.mImageView);
    }

    private void loadBitmap(int position, ImageView imageView) {

        // check if bitmap is cached in memory
        Bitmap bitmap = getBitmapFromMemCache(String.valueOf(position));
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            Log.d(TAG, "loadBitmap: position " + position + " memory hit");
        }
        else { // // check if bitmap is cached in disk
            bitmap = getBitmapFromDiskCache(String.valueOf(position));
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                memoryCache.put(String.valueOf(position), bitmap);
                Log.d(TAG, "loadBitmap: position " + position + " disk hit");
            }
            else {
                // imageView.setImageResource(R.drawable.image_placeholder);
                BitmapWorkerTask workerTask = new BitmapWorkerTask(imageView);
                workerTask.execute(String.valueOf(position));
                Log.d(TAG, "loadBitmap: position " + position + " miss, reading asynchronously");
            }
        }
    }

    @Override
    public int getItemCount() {
        return imageUrlList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder
                        implements View.OnClickListener {

        private ImageView mImageView;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.itemImageView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            hostActivity.replaceFragment(new ViewPagerFragment());
        }
    }

    private void addBitmapToCache(String key, Bitmap bitmap) {
        // Add to memory cache
        if (getBitmapFromMemCache(key) == null) {
            memoryCache.put(key, bitmap);
        }

        // Also add to disk cache
        synchronized (diskCacheLock) {
            try {
                if (diskLruCache != null && diskLruCache.get(key) == null) {
                    DiskLruCache.Editor editor = diskLruCache.edit(key);

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                    byte[]bytes = byteArrayOutputStream.toByteArray();
                    String encodedBitmap = Base64.encodeToString(bytes,Base64.DEFAULT);

                    editor.set(0, encodedBitmap);
                    editor.commit();

                    diskLruCache.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Bitmap getBitmapFromMemCache(String key) {
        return memoryCache.get(key);
    }

    private Bitmap getBitmapFromDiskCache(String key) {

        synchronized (diskCacheLock) {
            // Wait while disk cache is started from background thread
            while (diskCacheStarting) {
                try {
                    diskCacheLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (diskLruCache != null) {
                try {
                    DiskLruCache.Value value = diskLruCache.get(key);
                    if(value != null) {
                        String encodedBitmap = value.getString(0);
                        byte[] bitmapArray = Base64.decode(encodedBitmap, Base64.DEFAULT);
                        return BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    // Creates a unique subdirectory of the designated app cache directory. Tries to use external
    // but if not mounted, falls back on internal storage.
    private static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !isExternalStorageRemovable() ?
                        context.getExternalCacheDir().getPath() : context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }

    class InitDiskCacheTask extends AsyncTask<File, Void, Void> {
        @Override
        protected Void doInBackground(File... params) {

            synchronized (diskCacheLock) {
                File cacheDir = params[0];
                try {
                    diskLruCache = DiskLruCache.open(cacheDir, 1, 1, DISK_CACHE_SIZE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                diskCacheStarting = false; // Finished initialization
                diskCacheLock.notifyAll(); // Wake any waiting threads
            }

            return null;
        }
    }

    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

        private static final String TAG = "myAsyncTask";
        private WeakReference<ImageView> imageView;
        private InputStream is = null;

        BitmapWorkerTask(ImageView iv) {
            imageView = new WeakReference<>(iv);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {

            try {
                int position = Integer.valueOf(params[0]);
                String path = imageUrlList.get(position);
                is = hostActivity.getContentResolver().openInputStream(Uri.parse(path));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Bitmap bitmap = decodeBitmapFromStream(is, 200, 200);
            Log.d(TAG, "loadBitmap: START***" + params[0]);
            addBitmapToCache(params[0], bitmap); // params[1]: String.valueOf(position)
            Log.d(TAG, "loadBitmap: END***" + params[0]);
            return bitmap;
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bitmap != null) { imageView.get().setImageBitmap(bitmap); }
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
}


