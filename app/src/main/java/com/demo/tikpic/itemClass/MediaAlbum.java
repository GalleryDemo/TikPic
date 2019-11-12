package com.demo.tikpic.itemClass;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MediaAlbum extends MediaFile {
    //stores the index in allItemList for each mediaItem in the album.
    private List<Integer> mIndexInAllItemList;
    private static final String DTAG = "DataManager";

    public MediaAlbum(String path, String name, int type){
        super(path,name,type);
    }

    public MediaAlbum(String path, String name, int type, String thumbnail){
        super(path,name,type,thumbnail);
    }

    public MediaAlbum(String path, String name, int type, int fileIndex, String thumbnailPath){

        super(path,name,type,thumbnailPath);
        Log.d(DTAG,"MAKING ALBUM: path "+path);
        mIndexInAllItemList = new ArrayList<>();
        mIndexInAllItemList.add(fileIndex);
    }

    public void addIndex(int index){
        mIndexInAllItemList.add(index);
    }

    public int getAlbumSize(){
        return mIndexInAllItemList.size();
    }

    public Integer get(int index){
        return mIndexInAllItemList.get(index);
    }

    public List getAlbum(){
        return mIndexInAllItemList;
    }
}
