package com.example.gvy.jesta;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.UUID;

/**
 * Created by Tomer on 11/08/2018.
 */

@Entity
public class Jesta {

    @ColumnInfo(name = "lastUpdate")
    private long lastUpdate;
    @ColumnInfo(name = "user_id")
    private String ownerUserID;
    @ColumnInfo(name = "desc")
    private String desc;
    @ColumnInfo(name = "category")
    private String category;
    @ColumnInfo(name = "adress")
    private String adress;
    @ColumnInfo(name = "price")
    private String price;
    @ColumnInfo(name = "id")
    private String id;
    @NonNull
    @PrimaryKey
    private String imageURL;

    public Jesta() {
    }

    public Jesta(String ownerUserID, String desc, String category,
                 String adress, String price, long time) {
        this.lastUpdate = time;
        this.ownerUserID = ownerUserID;
        this.desc = desc;
        this.category = category;
        this.adress = adress;
        this.price = price;
        this.id = String.valueOf(UUID.randomUUID());
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getOwnerUserID() {
        return ownerUserID;
    }

    public void setOwnerUserID(String ownerUserID) {
        this.ownerUserID = ownerUserID;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @NonNull
    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(@NonNull String imageURL) {
        this.imageURL = imageURL;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
