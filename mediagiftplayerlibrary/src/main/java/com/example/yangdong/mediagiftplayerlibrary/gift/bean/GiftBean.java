package com.example.yangdong.mediagiftplayerlibrary.gift.bean;

import java.io.Serializable;

public class GiftBean implements Serializable {
    public String path;
    public int type;
    public boolean isResource;
    public boolean isMediaPlayer;

    public GiftBean(String path, int type, boolean isResource, boolean isMediaPlayer) {
        this.path = path;
        this.type = type;
        this.isResource = isResource;
        this.isMediaPlayer = isMediaPlayer;
    }

    @Override
    public String toString() {
        return "GiftBean{" +
                "path='" + path + '\'' +
                ", type=" + type +
                ", isResource=" + isResource +
                ", isMediaPlayer=" + isMediaPlayer +
                '}';
    }

}
