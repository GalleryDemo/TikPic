package com.demo.tikpic;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.demo.tikpic.itemClass.MediaAlbum;
import com.demo.tikpic.itemClass.MediaFile;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DataManager {
    private static final String DTAG = "DataManager";

    //stores all show case albums.
    private List<List<MediaAlbum>> GalleryShowCaseList;
    //all media files are stored in this list.
    private List<MediaFile> allItemList;
    private Context mContext;
    //the current show case
    private List<MediaAlbum> currentShowCase;
    private static DataManager sDataManager;

    private DataManager(Context context){
        mContext = context.getApplicationContext();
        allItemList = new ArrayList<>();
        GalleryShowCaseList = new ArrayList<>();
        currentShowCase = new ArrayList<>();
        GalleryShowCaseList.add(currentShowCase);
        Log.d(DTAG,"START ALLLIST SIZE: " + allItemList.size());


        new Thread(new Runnable() {
            @Override
            public void run() {

                scanMediaFiles();
            }
        }).start();

        Log.d(DTAG,"END OF scanfile :");
    }

    public static DataManager getInstance(Context context){
        if(sDataManager == null){
            sDataManager = new DataManager(context);
        }

        return sDataManager;
    }

    public List getCurrentShowcase(){

        return null;
    }

    public boolean scanMediaFiles(){
        allItemList.clear();
        currentShowCase.clear();
        Uri uri;
        int indexNumber;
        Cursor cursor;
        String thumbnailPath;

        String externalCacheDir = mContext.getExternalCacheDir().toString();
        ContentResolver cr = mContext.getContentResolver();

        //scan images
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;


        cursor = cr.query(uri, null, null, null, null);

        Calendar calendar = Calendar.getInstance();

        int cursorsize = cursor.getCount();
        if(cursor !=null && cursor.getCount()>0){
            //loop the cursor to save media items.
            int pathCursor = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            int idCursor = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            int nameCursor = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
            int fromCursor = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            int timeCursor = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);

            for(cursor.moveToFirst(), indexNumber = 0; !cursor.isAfterLast(); cursor.moveToNext(), indexNumber++){
                final String path, name, from, id,pathBuild;
                path = cursor.getString(pathCursor);
                id = cursor.getString(idCursor);
                pathBuild = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build().toString();
                name = cursor.getString(nameCursor);
                from = cursor.getString(fromCursor);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        createCache(pathBuild, id, 1);
                    }
                }).start();
                thumbnailPath = externalCacheDir + "/" + id + ".jpg";
                MediaFile media_file = new MediaFile(pathBuild,name,1,thumbnailPath);
                long time = cursor.getLong(timeCursor);
                //Date date = new Date(time);

                calendar.setTimeInMillis(time);
                calendar.set(Calendar.MONTH,calendar.get(Calendar.MONTH)+1);


                //Log.d(DTAG,"ALBUM Year: " + calendar.get(Calendar.YEAR) + " Month: "+ calendar.get(Calendar.MONTH)+" Day: " + calendar.get((Calendar.DAY_OF_MONTH)));

                String realDate = calendar.get(Calendar.YEAR) + "年" + calendar.get(Calendar.MONTH)+ "月" + calendar.get(Calendar.DAY_OF_MONTH) + "日";

                media_file.setDate(realDate);
                allItemList.add(media_file);

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
                if(!flag_IsInAlbum){
                    currentShowCase.add(new MediaAlbum(albumPath,from,1, indexNumber, thumbnailPath));
                    broad();
                }
            }



        }else{
            Log.e("ScanMediaFiles", "image scan result is null or 0");
            return false;
        }


        //scan videos
        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        cursor = cr.query(uri, null, null, null, null);
        cursorsize += cursor.getCount();
        if(cursor != null && cursor.getCount() > 0){
            int pathCursor = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
            int idCursor = cursor.getColumnIndex(MediaStore.Video.Media._ID);
            int nameCursor = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);
            int fromCursor = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
            int timeCursor = cursor.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN);

            for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext(),indexNumber++){
                final String path, name, from, id,pathBuild;
                path = cursor.getString(pathCursor);
                id = cursor.getString(idCursor);
                pathBuild = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build().toString();
                name = cursor.getString(nameCursor);
                from = cursor.getString(fromCursor);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        createCache(pathBuild, id, 2);
                    }
                }).start();

                thumbnailPath = externalCacheDir + "/" + id + ".jpg";
                MediaFile media_file = new MediaFile(pathBuild,name,2,thumbnailPath);

                long time = cursor.getLong(timeCursor);
                //Date date = new Date(time);

                calendar.setTimeInMillis(time);
                calendar.set(Calendar.MONTH,calendar.get(Calendar.MONTH)+1);

                String realDate = calendar.get(Calendar.YEAR) + "年" + calendar.get(Calendar.MONTH)+ "月" + calendar.get(Calendar.DAY_OF_MONTH) + "日";

                Log.d(DTAG,realDate);
                media_file.setDate(realDate);
                allItemList.add(media_file);

                String albumPath = path.substring(0, path.length() - name.length() - 1);


                boolean flag_IsInAlbum = false;
                for (MediaAlbum i : currentShowCase) {
                    if (i.getPath().compareTo(albumPath) == 0) {
                        i.addIndex(indexNumber);
                        if(i.getType() == 1){
                            i.setType(3);
                        }
                        flag_IsInAlbum = true;
                        break;
                    }
                }
                if(!flag_IsInAlbum){
                    //Log.d(DTAG,"ALBUM PATH: "+albumPath);
                    currentShowCase.add(new MediaAlbum(albumPath,from, 2, indexNumber, thumbnailPath));
                    broad();
                }
            }

        }else{
            Log.e("ScanMediaFiles", "video scan result is null or 0");
            return false;
        }

        cursor.close();
        createShowcaseList();
        Log.d(DTAG,"END OF FOR CURSOR SIZE: "+ cursorsize);
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

        Log.d(DTAG,"END3 :");
    }

    private void broad() {
        mContext.sendBroadcast(new Intent("com.demo.tikpic.Broadcast.UpdateBroadcast"));
    }

    private void createShowcaseList(){
        //showcase index 1: all picture
        new Thread(new Runnable() {
            @Override
            public void run() {
                createAllPicAlbumShowcase();
            }
        }).start();
        //showcase index 2: date list
        new Thread(new Runnable() {
            @Override
            public void run() {
                createDateAlbumShowcase();
            }
        }).start();


//        Log.d(DTAG,"DEFAULT SHOWCASE SIZE: "+currentShowCase.size());
//        Log.d(DTAG,"ALLPICS SHOWCASE SIZE: "+GalleryShowCaseList.get(1).size());
//        Log.d(DTAG,"DATE    SHOWCASE SIZE: "+GalleryShowCaseList.get(2).size());
    }

    private void createAllPicAlbumShowcase(){
        //create new album for all picture showcase;
        String name = "AllPictures";
        String path = "AllPictures";
        MediaAlbum singleAlbum = new MediaAlbum(path,name,1,0,allItemList.get(0).getThumbnailPath());
        //create a showcase that only holds one album which is the "singleAlbum"
        List<MediaAlbum> allPictureShowcase = new ArrayList<>();
        allPictureShowcase.add(singleAlbum);
        synchronized (GalleryShowCaseList){
            GalleryShowCaseList.add(allPictureShowcase);
        }

        for(int i = 0; i < allItemList.size();i++){
            singleAlbum.addIndex(i);
        }


    }

    private void createDateAlbumShowcase(){
        List<MediaAlbum> dateList = new ArrayList<>();
        synchronized (GalleryShowCaseList){
            GalleryShowCaseList.add(dateList);
        }

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
                Log.d(DTAG,"DATE SHOWCASE ALBUM: "+ i.getDate());
                dateList.add(new MediaAlbum(i.getDate(),i.getDate(),1,indexNumber,i.getThumbnailPath()));
            }
            indexNumber++;
        }
    }


    public List<MediaAlbum> getShowcaseOrAlbumOrIndex(int showcase){
        return GalleryShowCaseList.get(showcase);
    }

    public List<MediaFile> getShowcaseOrAlbumOrIndex(int showcase, int album){
        //get a particular album of a showcase base on the arguments
        //and add files into this new album.
        List<MediaFile> temp = new ArrayList<>();
        List<Integer> indexes =  GalleryShowCaseList.get(showcase).get(album).getAlbum();
        for(int i = 0; i < indexes.size();i++){
            temp.add(allItemList.get(indexes.get(i)));
        }

        return temp;
    }

    public MediaFile getShowcaseOrAlbumOrIndex(int showcase, int album, int index){

        int indexInAll = GalleryShowCaseList.get(showcase).get(album).get(index);

        return allItemList.get(indexInAll);
    }
}
