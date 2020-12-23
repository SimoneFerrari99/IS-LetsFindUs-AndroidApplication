package com.example.lets_findus.utilities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

//T is the object of the meeting
@Entity(indices = @Index({"latitude", "longitude"}))
public class Meeting {
    @PrimaryKey(autoGenerate = true)
    public int id; //identifier for the meeting
    public int personId;
    @Ignore
    public Person data; //the object of the meeting
    public double latitude;
    public double longitude;
    public Date date;
    private boolean isFavourite = false; //if the meeting is set to favourite or not (default is not)

    public Meeting(Person data, double latitude, double longitude, Date date) {
        this.data = data;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
        this.personId = data.id;
    }

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
