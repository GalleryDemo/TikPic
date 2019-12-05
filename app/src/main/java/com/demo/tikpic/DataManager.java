package com.demo.tikpic;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.disklrucache.DiskLruCache;
import com.demo.tikpic.albums.AlbumsAdapter;
import com.demo.tikpic.gallery.DataAdapter;
import com.demo.tikpic.itemClass.MediaAlbum;
import com.demo.tikpic.itemClass.MediaFile;
import com.demo.tikpic.timeline.ItemViewHolder;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;
import static android.os.Environment.isExternalStorageRemovable;


public class DataManager {
    private static final String TAG = "DataManager";

    //stores all show case albums.
    private List<List<MediaAlbum>> GalleryShowCaseList;



    //all media files are stored in this list.
    private static List<MediaFile> allItemList;
    private List<String> imagePaths;
    private Map<String, List<Integer>> albumMap;


    private static Context mContext;
    //the current show case
    private List<MediaAlbum> currentShowCase;
    private static DataManager sDataManager;

    // Memory Cache
    private static LruCache<String, Bitmap> memoryCache;
    private static LruCache<String, Bitmap> albumMemoryCache;


    // Disk Cache
    private static DiskLruCache diskLruCache;
    private static final Object diskCacheLock = new Object();
    private static boolean diskCacheStarting = true;
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 100; // 100MB
    private static final String DISK_CACHE_SUBDIR = "thumbnails";
    //private static final String DISK_CACHE_SUBDIR_ALBUM = "albumThumbnails";

    private static ExecutorService cachedThreadPool;

    static final Executor THREAD_POOL_EXECUTOR1;
    private static final int CORE_POOL_SIZE =  1;
    private static final int MAXIMUM_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2 +1;
    private static final int BACKUP_POOL_SIZE = 5;
    private static final int KEEP_ALIVE_SECONDS = 1;

    private static BlockingQueue<Runnable> queue = new LinkedBlockingDeque<Runnable>();
    private static SynchronousQueue<Runnable> sQueue = new SynchronousQueue<>(false);
    private static ThreadPoolExecutor sBackupExecutor;
    private static LinkedBlockingQueue<Runnable> sBackupExecutorQueue;


    private static final RejectedExecutionHandler sRunOnSerialPolicy =
            new RejectedExecutionHandler() {
                public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                    // As a last ditch fallback, run it on an executor with an unbounded queue.
                    // Create this executor lazily, hopefully almost never.
                    synchronized (this) {
                        if (sBackupExecutor == null) {
                            sBackupExecutorQueue = new LinkedBlockingQueue<Runnable>();
                            sBackupExecutor = new ThreadPoolExecutor(
                                    BACKUP_POOL_SIZE, BACKUP_POOL_SIZE, KEEP_ALIVE_SECONDS,
                                    TimeUnit.SECONDS, sBackupExecutorQueue, sThreadFactory);
                            sBackupExecutor.allowCoreThreadTimeOut(true);
                            sBackupExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
                        }
                    }
                    sBackupExecutor.execute(r);
                }
            };

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
        }
    };
    static {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
                sQueue, sThreadFactory);
        threadPoolExecutor.setRejectedExecutionHandler(sRunOnSerialPolicy);
        THREAD_POOL_EXECUTOR1 = threadPoolExecutor;

    }

    private DataManager(Context context) {
        mContext = context.getApplicationContext();
        allItemList = new ArrayList<>();
        GalleryShowCaseList = new ArrayList<>();
        currentShowCase = new ArrayList<>();
        imagePaths = new ArrayList<>();
        albumMap = new LinkedHashMap<>();
        GalleryShowCaseList.add(currentShowCase);


        // Log.d(TAG,"START ALLLIST SIZE: " + allItemList.size());
        //Log.d(TAG, "DataManager: CORE_POOL_SIZE" + CORE_POOL_SIZE);
        scanMediaFiles();

        // initialize memory cache
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024); // in KiloBytes
        final int cacheSize = maxMemory / 8; // in KiloBytes
        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024; // in KiloBytes
            }
        };
        albumMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024; // in KiloBytes
            }
        };

        // initialize disk cache on background thread
        File cacheDir = getDiskCacheDir(mContext, DISK_CACHE_SUBDIR);
        new InitDiskCacheTask().execute(cacheDir);

        // cached thread pool for BitmapWorkerTask
        cachedThreadPool = Executors.newCachedThreadPool();
    }
    public static DataManager getInstance() {
        return sDataManager;
    }

    public static DataManager getInstance(Context context) {
        if(sDataManager == null) {
            sDataManager = new DataManager(context);
        }
        return sDataManager;
    }

    private boolean scanMediaFiles() {
        allItemList.clear();
        currentShowCase.clear();
        imagePaths.clear();


        int indexNumber;
        Cursor cursor;
        String thumbnailPath;
        String externalCacheDir = mContext.getExternalCacheDir().toString();
        ContentResolver cr = mContext.getContentResolver();

        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATE_TAKEN,
        };

        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

        Uri queryUri = MediaStore.Files.getContentUri("external");

        //scan images
        cursor = cr.query(queryUri,
                projection,
                selection,
                null,
                MediaStore.Files.FileColumns.DATE_TAKEN + " DESC");

        if(cursor != null && cursor.getCount()>0) {
            //loop the cursor to save media items.
            for(cursor.moveToFirst(), indexNumber = 0; !cursor.isAfterLast(); cursor.moveToNext(), indexNumber++){

                int id;
                String path, name, from, contentUri;
                String month, realDate ;
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                id = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID));
                contentUri = queryUri.buildUpon().appendPath(String.valueOf(id)).build().toString();
                int type = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE));//either 1(image) or 3(video), since we don't have 2(audio) yet.
                name = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME));
                from = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME));

                thumbnailPath = externalCacheDir + "/" + id + ".jpg";
                MediaFile media_file = new MediaFile(contentUri,name,type,thumbnailPath,id);

                Log.d(TAG, "scanMediaFiles: "+contentUri);
                long time = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_TAKEN));
                Calendar calendar = Calendar.getInstance();

                calendar.setTimeInMillis(time);

                //calendar month value is from 0(Jan) - 11(Dec)
                if(calendar.get(Calendar.MONTH) == Calendar.JANUARY){
                    realDate = calendar.get(Calendar.YEAR) + "年" +
                            1 + "月" +
                            calendar.get(Calendar.DAY_OF_MONTH) + "日";
                    media_file.setDate(realDate);
                    Log.d(TAG, "scanMediaFiles: "+ realDate );
                    month = String.valueOf(1);

                }else if(calendar.get(Calendar.MONTH) == Calendar.DECEMBER){
                    realDate = calendar.get(Calendar.YEAR) + "年" +
                            12 + "月" +
                            calendar.get(Calendar.DAY_OF_MONTH) + "日";
                    media_file.setDate(realDate);
                    Log.d(TAG, "scanMediaFiles: "+ realDate );
                    month = String.valueOf(12);

                }else{
                    calendar.set(Calendar.MONTH,calendar.get(Calendar.MONTH) + 1);
                    realDate = calendar.get(Calendar.YEAR) + "年" +
                            calendar.get(Calendar.MONTH)+ "月" +
                            calendar.get(Calendar.DAY_OF_MONTH) + "日";
                    media_file.setDate(realDate);
                    month = String.valueOf(calendar.get(Calendar.MONTH));

                }

                allItemList.add(media_file);

                String year = String.valueOf(calendar.get(Calendar.YEAR));
                String day = String.valueOf(calendar.get(Calendar.DATE));

                String yyyyMM = year + "年" + month + "月" + day + "日";
                // add record
                if(albumMap.get(yyyyMM) == null) {
                    //Log.d(TAG, "scanMediaFiles - AlbumMap: " + albumMap.get(yyyyMM) );
                    albumMap.put(yyyyMM, new ArrayList<>());
                }

                albumMap.get(yyyyMM).add(indexNumber);
                //creating albums base on the files' folder
                String albumPath = path.substring(0, path.length() - name.length() - 1);


                boolean flag_IsInAlbum = false;
                for (MediaAlbum i : currentShowCase) {
                    //loop the album folder, if the current image have the same parent folder
                    if (i.getPath().compareTo(albumPath) == 0) {
                        i.addIndex(indexNumber);
                        flag_IsInAlbum = true;
                        break;
                    }
                }
                //the current show case is one of the showcases, which will display all the default folders(screenshot/camera/example) from phone.
                //here we are only adding these default sub-albums to the current showcase, which maybe the initial showcase.
                if(!flag_IsInAlbum) {
                    currentShowCase.add(new MediaAlbum(albumPath,from,1, indexNumber, thumbnailPath));
                }
            }

        } else {
            Log.e("ScanMediaFiles", "file scan result is null or 0");
            return false;
        }

        cursor.close();
        createShowcaseList();
        return true;
    }

    private void createShowcaseList() {
        //showcase index 1: all picture
        Log.d(TAG,"DEFAULT SHOWCASE SIZE: "+currentShowCase.size());
        createAllPicAlbumShowcase();
        Log.d(TAG,"ALLPICS SHOWCASE SIZE: "+GalleryShowCaseList.get(1).size());
        //showcase index 2: date list
        createDateAlbumShowcase();
        Log.d(TAG,"DATE    SHOWCASE SIZE: "+GalleryShowCaseList.get(2).size());
    }

    private void createAllPicAlbumShowcase() {
        //create new album for all picture showcase;
        String name = "AllPictures";
        String path = "AllPictures";
        MediaAlbum singleAlbum = new MediaAlbum(path,name,1);
        //create a showcase that only holds one album which is the "singleAlbum"
        List<MediaAlbum> allPictureShowcase = new ArrayList<>();
        allPictureShowcase.add(singleAlbum);
        GalleryShowCaseList.add(allPictureShowcase);
        for(int i = 0; i < allItemList.size();i++){
            singleAlbum.addIndex(i);
        }
    }

    private void createDateAlbumShowcase() {
        List<MediaAlbum> dateList = new ArrayList<>();
        GalleryShowCaseList.add(dateList);
        int indexNumber = 0;
        for(MediaFile i : allItemList){

            boolean haveDateAlbum = false;
            for(MediaAlbum j: dateList){
                //Path is actually the date of the album,we did not save the data in the date but in its path.
                if(j.getPath().compareTo(i.getDate()) == 0){
                    j.addIndex(indexNumber);
                    haveDateAlbum = true;
                    break;
                }
            }
            if(!haveDateAlbum){
                Log.d(TAG,"DATE SHOWCASE ALBUM: "+ i.getDate());
                dateList.add(new MediaAlbum(i.getDate(),i.getDate(),1,indexNumber,i.getThumbnailPath()));
            }
            indexNumber++;
        }
    }

    public List<MediaAlbum> getShowcaseOrAlbumOrIndex(int showcase) {
        return GalleryShowCaseList.get(showcase);
    }

    public List<MediaFile> getShowcaseOrAlbumOrIndex(int showcase, int album) {
        //get a particular album of a showcase base on the arguments
        //and add files into this new album.
        List<MediaFile> temp = new ArrayList<>();
        List<Integer> indexes =  GalleryShowCaseList.get(showcase).get(album).getAlbum();
        for(int i = 0; i < indexes.size();i++){
            temp.add(allItemList.get(indexes.get(i)));
        }

        return temp;
    }

    public MediaFile getShowcaseOrAlbumOrIndex(int showcase, int album, int index) {

        int indexInAll = GalleryShowCaseList.get(showcase).get(album).get(index);

        return allItemList.get(indexInAll);
    }

    public List<Integer> getShowcaseOrAlbumOrIndexInt(int showcase, int album) {
        //get a particular album of a showcase base on the arguments
        //and add files into this new album.
        return GalleryShowCaseList.get(showcase).get(album).getAlbum();
    }


    public List<String> getImagePaths() {
        return imagePaths;
    }
    public List<MediaFile> getAllItemList() {
        return allItemList;
    }

    private Map<String, List<String>> getDateAlbumMap() {

        Map<String, List<String>> albumMap = new LinkedHashMap<>();

        Cursor cursor = mContext.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.DATE_TAKEN },
                null,
                null,
                MediaStore.MediaColumns.DATE_TAKEN + " DESC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                // get URI
                String id = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                String uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        .buildUpon().appendPath(String.valueOf(id)).build().toString();

                // get Time Tag
                long millisSinceEpoch = cursor.getLong(
                        cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));
                Calendar calendar = new GregorianCalendar();
                calendar.setTimeInMillis(millisSinceEpoch);
                String year = String.valueOf(calendar.get(Calendar.YEAR));
                String month = String.valueOf(calendar.get(Calendar.MONTH));

                if(month.length() == 1) {
                    month = "0" + month;
                }

                String yyyyMM = year + "/" + month;
                // add record
                if(albumMap.get(yyyyMM) == null) {
                    albumMap.put(yyyyMM, new ArrayList<>());
                }

                albumMap.get(yyyyMM).add(uri);
            }
            cursor.close();
        }

        return albumMap;
    }

    public Set<String> getAlbumKeySet() {
        return albumMap.keySet();
    }

    public List<Integer> getPhotoListInAlbum(String key) {
        final List<Integer> photoList = new ArrayList<>();
        /*
        for (int i = 0; i < albumList.get(index).getAlbum().size(); i++) {
            String ThumbnailPath = dataManager.getShowcaseOrAlbumOrIndex(2, index, i).getThumbnailPath();
            String ThumbnailPath =
            Log.d(TAG, "getPhotoListInAlbum: ThumbnailPath: " + ThumbnailPath);
            photoList.add(new Photo(ThumbnailPath));
        }
         */
        for(int index : albumMap.get(key)) {
            photoList.add(index);
        }
        return photoList;
    }



    public void loadBitmap(int position, DataAdapter.ViewHolder viewHolder, int thumbNailWidth) {

        ImageView imageView = viewHolder.mImageView;

        int idKey = allItemList.get(position).getId();

        // check if bitmap is cached in memory
        Bitmap bitmap = getBitmapFromMemCache(String.valueOf(idKey));
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            Log.d(TAG, "loadBitmap: ID " + idKey + " memory hit");
        }
        else { // // check if bitmap is cached in disk

            bitmap = getBitmapFromDiskCache(String.valueOf(idKey));
            Log.d(TAG, "loadBitmap - Bitmap: "+ bitmap);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                memoryCache.put(String.valueOf(idKey), bitmap);
                Log.d(TAG, "loadBitmap: ID " + idKey + " disk hit");
            }
            else {
                if(!viewHolder.isLoading()) {
                    // change viewholder's loading state from false to true
                    viewHolder.switchLoadState();
                    BitmapWorkerTask workerTask = new BitmapWorkerTask(viewHolder);
                    imageView.setTag(workerTask);

                    workerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, position, thumbNailWidth);
                    // workerTask.execute(position, mediaType);
                    Log.d(TAG, "loadBitmap: ID " + idKey + " miss, reading asynchronously");
                }
            }
        }
    }

    public void loadBitmap(int position, ItemViewHolder viewHolder, int thumbNailWidth) {

        ImageView imageView = viewHolder.imageView;

        int idKey = allItemList.get(position).getId();
        // check if bitmap is cached in memory
        Bitmap bitmap = getBitmapFromMemCache(String.valueOf(idKey));
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            Log.d(TAG, "loadBitmap: idKey " + idKey + " memory hit");
        }
        else { // // check if bitmap is cached in disk
            bitmap = getBitmapFromDiskCache(String.valueOf(idKey));
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                memoryCache.put(String.valueOf(idKey), bitmap);
                Log.d(TAG, "loadBitmap: idKey " + idKey + " disk hit");
            }
            else {
                if(!viewHolder.isLoading()) {
                    // change viewholder's loading state from false to true
                    viewHolder.switchLoadState();

                    BitmapWorkerTask workerTask = new BitmapWorkerTask(viewHolder);
                    imageView.setTag(workerTask);
                    workerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, position, thumbNailWidth);
                    // workerTask.execute(position, mediaType);
                    Log.d(TAG, "loadBitmap: idKey " + idKey + " miss, reading asynchronously");
                }
            }
        }
    }

    public void loadBitmap(int position, AlbumsAdapter.ViewHolder viewHolder, int adapterType) {

        ImageView imageView = viewHolder.albumCoverImage;

        int width = viewHolder.getCoverSize();
        Log.d("ayayaya", "loadBitmap - width 1:" + viewHolder.getCoverSize());


        //change the position because position as key might already have a bitmap stored in memcache, but the bitmap have lower quality.
        //so we change the position(key) to add a new higher quality bitmap as album cover.
        int idKey = allItemList.get(position).getId();

        if(position == 0){
            position = -1;
        }else{
            position = position * (-2);
        }


        // check if bitmap is cached in memory
        Bitmap bitmap = getBitmapFromAlbumMemCache(String.valueOf(idKey));
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            Log.d(TAG, "loadBitmap: idKey " + idKey + " memory hit");

        }
        else { // // check if bitmap is cached in disk

            bitmap = getBitmapFromDiskCache(String.valueOf(idKey));
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                albumMemoryCache.put(String.valueOf(idKey), bitmap);
                //Log.d(TAG, "loadBitmap: position " + idKey + " disk hit. albumMemSize: "+ albumMemoryCache.size()+ " MY Albums count: "+ getShowcaseOrAlbumOrIndex(0).size());
            }
            else {
                if(!viewHolder.isLoading()) {
                    // change viewholder's loading state from false to true
                    viewHolder.switchLoadState();

                    BitmapWorkerTask workerTask = new BitmapWorkerTask(viewHolder);
                    imageView.setTag(workerTask);
                    workerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, position, adapterType, width);
                    // workerTask.execute(position, mediaType);
                    Log.d(TAG, "loadBitmap: idKey " + idKey + " miss, reading asynchronously");
                }
            }
        }
    }

    // Creates a unique subdirectory of the designated app cache directory.
    // Tries to use external but if not mounted, falls back on internal storage.
    private static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so,
        // try and use external cache dir otherwise use internal cache dir

        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !isExternalStorageRemovable() ?
                        context.getExternalCacheDir().getPath() : context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }

    private static void addBitmapToCache(String key, Bitmap bitmap,int style) {
        if(style == 1){
            // Add to memory cache
            if (getBitmapFromMemCache(key) == null) {

                memoryCache.put(key, bitmap);
            }
        }else{
            if (getBitmapFromAlbumMemCache(key) == null) {

                albumMemoryCache.put(key, bitmap);

            }
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

    private static Bitmap getBitmapFromMemCache(String key) {
        return memoryCache.get(key);
    }
    private static Bitmap getBitmapFromAlbumMemCache(String key) {
        return albumMemoryCache.get(key);
    }
    class MyCallable implements Callable<Bitmap> {
        private String key;

        public MyCallable(String key){
            this.key = key;
        }

        @Override
        public Bitmap call() {
            Log.d(TAG, "call: " + Thread.currentThread().getName());
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

    static class InitDiskCacheTask extends AsyncTask<File, Void, Void> {
        @Override
        protected Void doInBackground(File... params) {

            synchronized (diskCacheLock) {
                File cacheDir = params[0];
                try {
                    diskLruCache = DiskLruCache.open(
                            cacheDir, 1, 1, DISK_CACHE_SIZE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                diskCacheStarting = false; // Finished initialization
                diskCacheLock.notifyAll(); // Wake any waiting threads
            }
            return null;
        }
    }

    private static class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {

        private WeakReference<RecyclerView.ViewHolder> viewHolderWeakRef;

        private WeakReference<ImageView> imageViewWeakRef;
        private InputStream is = null;
        private boolean cancelled;


        BitmapWorkerTask(DataAdapter.ViewHolder viewHolder) {

            viewHolderWeakRef = new WeakReference<>(viewHolder);
            imageViewWeakRef = new WeakReference<>(viewHolder.mImageView);
        }

        BitmapWorkerTask(ItemViewHolder viewHolder) {

            viewHolderWeakRef = new WeakReference<>(viewHolder);
            imageViewWeakRef = new WeakReference<>(viewHolder.imageView);
        }

        BitmapWorkerTask(AlbumsAdapter.ViewHolder viewHolder) {

            viewHolderWeakRef = new WeakReference<>(viewHolder);
            imageViewWeakRef = new WeakReference<>(viewHolder.albumCoverImage);
        }




        @Override
        protected Bitmap doInBackground(Integer... params) {
            cancelled = isCancelled();

            if(!cancelled){
                int position = params[0];
                int width;
                MediaFile file;

                //means we are decoding a bitmap for album cover, which needs higher quality.
                if(params.length == 3){
                    width = params[2];

                    if(position == -1){
                        file = allItemList.get(position + 1);
                    }else{
                        file = allItemList.get(position/(-2));
                    }
                }else{
                    //normal size, width = screen/4.
                    width =  params[1];
                    file = allItemList.get(position);
                }


                //String path = imageUrlList.get(position);

                final Bitmap bitmap;

                Log.d(TAG, "doInBackground: Thread Name: "+ Thread.currentThread());
                // Video
                if(file.getType() == 3) {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(mContext, Uri.parse(file.getPath()));
                    bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                    cancelled = isCancelled();
                }
                // Image
                else {
                    cancelled = isCancelled();
                    try {
                        is = mContext.getContentResolver().openInputStream(Uri.parse(file.getPath()));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    bitmap = decodeBitmapFromStream(is, width, width);
                    Log.d(TAG,"IS： "+ is);
                }

                if(params.length == 3){
                    cachedThreadPool.submit(() -> addBitmapToCache(String.valueOf(file.getId()), bitmap,3));
                }else{
                    cachedThreadPool.submit(() -> addBitmapToCache(String.valueOf(file.getId()), bitmap,1));
                    Log.d(TAG, "doInBackground - style : "+ params.length );
                }
                return bitmap;
            }else{

                return null;
            }
            //Log.d(TAG, "doInBackground: TIMELAP - ENTERED" );




            //Log.d(TAG, "doInBackground: TIMELAP - LEAVEING" );

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

            if (bitmap != null && imageViewWeakRef != null) {
                final ImageView imageView = imageViewWeakRef.get();

                if(imageView != null) {
                    imageView.setImageBitmap(bitmap);
                    if(viewHolderWeakRef.get() instanceof DataAdapter.ViewHolder){
                        DataAdapter.ViewHolder holder = (DataAdapter.ViewHolder)viewHolderWeakRef.get();
                        holder.switchLoadState();
                    }
                    if(viewHolderWeakRef.get() instanceof ItemViewHolder){
                        ItemViewHolder holder = (ItemViewHolder)viewHolderWeakRef.get();
                        holder.switchLoadState();
                    }
                    if(viewHolderWeakRef.get() instanceof AlbumsAdapter.ViewHolder){
                        AlbumsAdapter.ViewHolder holder = (AlbumsAdapter.ViewHolder)viewHolderWeakRef.get();
                        holder.switchLoadState();
                    }


                }
            }
        }

        private Bitmap decodeBitmapFromStream(InputStream is, int reqWidth, int reqHeight) {

            Bitmap bitmap = null;

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;

            BitmapRegionDecoder originalImage;
            int width, height;
            try {
                originalImage = BitmapRegionDecoder.newInstance(is, false);
                height = originalImage.getHeight();
                width = originalImage.getWidth();
                options.inSampleSize = calculateInSampleSize(width, height, reqWidth, reqHeight);


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
