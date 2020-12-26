package com.example.lets_findus.utilities;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.google.android.gms.maps.model.VisibleRegion;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;

@Dao
public abstract class MeetingDao {
    @Insert
    public abstract ListenableFuture<Long> insert(Meeting m);

    // TODO: 23/12/2020 test
    @Insert
    public abstract ListenableFuture<Long[]> insertAll(Meeting... m);

    @Query("UPDATE Meeting SET isFavourite = 1 WHERE id IN (:ids)")
    abstract void setFavouriteAllId(List<Integer> ids);

    public void setFavouriteAll(List<Meeting> meetings){
        List<Integer> ids = new ArrayList<>();
        for(Meeting m : meetings){
            ids.add(m.id);
        }
        setFavouriteAllId(ids);
    }

    @Delete
    public abstract void delete(Meeting m);

    @Transaction
    @Query("SELECT * FROM Meeting INNER JOIN Person ON Meeting.personId = Person.id WHERE (Meeting.latitude BETWEEN :latDown AND :latUp) AND (Meeting.longitude BETWEEN :longLeft AND :longRight)")
    public abstract ListenableFuture<List<MeetingPerson>> getMeetingsBetweenRegion(double latUp, double latDown, double longLeft, double longRight);

    // TODO: 23/12/2020 test
    public ListenableFuture<List<MeetingPerson>> getMeetingsBetweenVisibleRegion(VisibleRegion vr){
        return getMeetingsBetweenRegion(vr.farLeft.latitude, vr.nearLeft.latitude, vr.farLeft.longitude, vr.farRight.longitude);
    }

    @Transaction
    @Query("SELECT * FROM Meeting INNER JOIN Person ON Meeting.personId = Person.id WHERE Meeting.isFavourite = 1")
    public abstract ListenableFuture<List<MeetingPerson>> getFavouriteMeetings();

}
