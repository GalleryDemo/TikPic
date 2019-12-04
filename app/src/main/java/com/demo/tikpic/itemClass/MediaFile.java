package com.demo.tikpic.itemClass;

public class MediaFile {
    private String path;
    private String name;
    private String date;
    private int type;
    private String thumbnailPath;
    private int id;



    protected MediaFile(String path, String name, int type){
        this(path,name,type,null,-1);
    }

    public MediaFile(String path, String name, int type,int id) {
        this(name,path,type,null,id);
    }

    public MediaFile(String path, String name, int type, String thumbnailPath, int id){

        this.path = path;
        this.name = name;
        this.type = type;
        this.thumbnailPath = thumbnailPath;
        this.id = id;
    }

    public int getId() {
        return id;
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
