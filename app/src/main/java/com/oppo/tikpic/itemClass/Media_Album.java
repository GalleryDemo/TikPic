package com.oppo.tikpic.itemClass;

import java.util.List;

public class Media_Album extends Media_Item{
    //stores the index in allItemList for each mediaItem in the album.
    private List<Integer> mIndexInAllItemList;

    public Media_Album( String path,String name, int type){
        super(path,name,type);
    }

    public Media_Album( String path,String name,int type, String thumbnail){
        super(path,name,type,thumbnail);
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
