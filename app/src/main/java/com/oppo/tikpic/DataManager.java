package com.oppo.tikpic;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.oppo.tikpic.itemClass.Media_Album;
import com.oppo.tikpic.itemClass.Media_Item;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    //stores all show case albums.
    private List<List> GalleryShowCaseList;
    //all media files are stored in this list.
    private List<Media_Item> allItemList;


    private Context mContext;
    //the current show case
    private List<Media_Album> currentShowCase;


    public DataManager(Context context){
        this.mContext = context;
        allItemList = new ArrayList<>();
        GalleryShowCaseList = new ArrayList<>();
        GalleryShowCaseList.add(currentShowCase);

    }

    public boolean scanMediaFiles(){
        Uri uri;
        int count;
        Cursor cursor;
        String thumbnailPath;
        String externalCacheDir = mContext.getExternalCacheDir().toString();
        ContentResolver cr = mContext.getContentResolver();

        //scan images
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        cursor = cr.query(uri, null, null, null, null);

        if(cursor !=null && cursor.getCount()>0){
            //loop the cursor to save media items.
            for(cursor.moveToFirst(), count = 0; !cursor.isAfterLast(); cursor.moveToNext(), count++){
                final String path, name, from, id;

                path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                id = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                name = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                from = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));

                thumbnailPath = mContext.getExternalCacheDir() + "/" + id + ".jpg";

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        createCache(path, id, 1);
                    }
                }).start();

                Media_Item media_item = new Media_Item(path,name,1,thumbnailPath);
                allItemList.add(media_item);
            }


        }else{
            Log.e("ScanMediaFiles", "image scan result is null or 0");
            return false;
        }

        //scan videos
        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        cursor = cr.query(uri, null, null, null, null);

        if(cursor != null && cursor.getCount() > 0){


        }else{
            Log.e("ScanMediaFiles", "video scan result is null or 0");
            return false;
        }

        cursor.close();
        return true;
    }


    private void createCache(String path, String id, int type) {
        File file = new File(mContext.getExternalCacheDir(), id + ".jpg");
        if(!file.exists()){
            Bitmap mBitmap = BitmapFactory.decodeFile(path);
            if (type == 1) {
                mBitmap = ThumbnailUtils.extractThumbnail(mBitmap, 1080 / 2, 1080 / 2);
            } else {
                mBitmap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND);
            }
            try {

                if(file.createNewFile()){
                    FileOutputStream out = new FileOutputStream(file);
                    mBitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
                    out.flush();
                    out.close();
                }else{
                    Log.e("createCache","Cache file creation failed.");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
