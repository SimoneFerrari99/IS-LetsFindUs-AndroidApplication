package com.example.lets_findus.utilities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

//indici per velocizzare la ricerca nel database
@Entity(indices = @Index({"latitude", "longitude"}))
public class Meeting {
    @PrimaryKey(autoGenerate = true)
    public int id; 
    public int personId;
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

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }

    public boolean getFavourite(){
        return this.isFavourite;
    }

}
