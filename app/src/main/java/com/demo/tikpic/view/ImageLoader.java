package com.demo.tikpic.view;

import android.graphics.Bitmap;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ImageButton;

import androidx.collection.LruCache;

public  class ImageLoader {

    private static ImageLoader instance;
    private String TAG = "ImageLoader";
    private LruCache<String, Bitmap> lruCache;

    public static synchronized ImageLoader getInstance(){
        if(instance==null){
            instance= new ImageLoader();
        }
        return instance;
    }

    private ImageLoader() {
        int maxMemory = (int)(Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 2;
        //Log.d(TAG, "ImageLoader: maxMemory"+maxMemory+"//cachesize :"+cacheSize);
        lruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount()/1024;
            }
        };
    }

    public synchronized void addBitmap(String key, Bitmap bitmap) {
        if (getBitmap(key) == null) {
            lruCache.put(key, bitmap);
        }
    }

    public synchronized Bitmap getBitmap(String key) {
        return lruCache.get(key);
    }
}