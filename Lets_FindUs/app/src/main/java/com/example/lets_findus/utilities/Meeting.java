package com.example.lets_findus.utilities;

import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Date;

//T is the object of the meeting
public class Meeting<T> {
    public static int id = 0; //identifier for the meeting
    public final T data; //the object of the meeting
    public final Location meetingLoc; //the location (including timestamp) of the meeting
    private boolean isFavourite = false; //if the meeting is set to favourite or not (default is not)

    public Meeting(T data, Location meetingLoc) {
        this.data = data;
        this.meetingLoc = meetingLoc;
        id++;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }

    public boolean getFavourite(){
        return this.isFavourite;
    }

    @Override
    public String toString() {
        return "Meeting{" +
                "data=" + data +
                ", meetingLoc=" + meetingLoc +
                ", isFavourite=" + isFavourite +
                '}';
    }
}
