package com.example.lets_findus.utilities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

//indici per velocizzare la ricerca nel database
@Entity(indices = @Index({"latitude", "longitude"}))
public class Meeting {
    @PrimaryKey(autoGenerate = true)
    public int id; 
    public int personId;
    @Ignore
    public Person data; 
    public double latitude;
    public double longitude;
    public Date date;
    private boolean isFavourite = false; 

    public Meeting(int personId, double latitude, double longitude, Date date) {
        this.personId = personId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
    }

    public Meeting(Person data, double latitude, double longitude, Date date) {
        this.data = data;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
        this.personId = data.id;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }

    public boolean getFavourite(){
        return this.isFavourite;
    }

}
