package com.oppo.tikpic.itemClass;

public class Media_File {
    private String path;
    private String name;
    private String date;
    private int type;
    private String thumbnailPath;


    public Media_File(String path, String name, int type){
        this(name,path,type,null);
    }

    public Media_File(String path, String name, int type, String thumbnailPath){

        this.path = path;
        this.name = name;
        this.type = type;
        this.thumbnailPath = thumbnailPath;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }




}
