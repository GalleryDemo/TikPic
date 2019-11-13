package com.demo.tikpic.timeline;

import android.util.Log;

import com.demo.tikpic.DataManager;
import com.demo.tikpic.MainActivity;
import com.demo.tikpic.itemClass.MediaAlbum;

import java.util.ArrayList;
import java.util.List;

final class LoadPhotos {

    private static final String TAG = "LoadPhotos";
    private MainActivity hostActivity;
    private DataManager dataManager;
    // private Map<String, List<String>> albumMap;
    private List<MediaAlbum> albumList;

    LoadPhotos(MainActivity activity) {
        this.hostActivity = activity;
        dataManager = DataManager.getInstance(hostActivity);
        // albumMap = getAlbumMap();
        albumList = dataManager.getShowcaseOrAlbumOrIndex(2);
    }

    List<Photo> getPhotoListInAlbum(int index) {
        final List<Photo> photoList = new ArrayList<>();

        for (int i = 0; i < albumList.get(index).getAlbum().size(); i++) {
            String ThumbnailPath =
                    dataManager.getShowcaseOrAlbumOrIndex(2, index, i).getThumbnailPath();
            Log.d(TAG, "getPhotoListInAlbum: ThumbnailPath: " + ThumbnailPath);
            photoList.add(new Photo(ThumbnailPath));
        }
        return photoList;
    }

    /*
    private Map<String, List<String>> getAlbumMap() {

        Map<String, List<String>> albumMap = new LinkedHashMap<>();

        Cursor cursor = hostActivity.getContentResolver().query(
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
    */

    public int getAlbumListSize() {
        return albumList.size();
    }

    public String getAlbumName(int i) {
        return albumList.get(i).getName();
    }

}
