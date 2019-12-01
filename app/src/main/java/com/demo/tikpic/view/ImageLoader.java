package com.demo.tikpic.view;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.collection.LruCache;

public class ImageLoader {

    private String TAG = "ImageLoader";
    private LruCache<String, Bitmap> lruCache;

    public ImageLoader() {
        int maxMemory = (int)(Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 3;
        Log.d(TAG, "ImageLoader: maxMemory"+maxMemory+"//cachesize :"+cacheSize);
        lruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }
        };
    }

    public void addBitmap(String key, Bitmap bitmap) {
        if (getBitmap(key) == null) {
            lruCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmap(String key) {
        return lruCache.get(key);
    }

    public int getCount(){
        return lruCache.putCount();
    }

}