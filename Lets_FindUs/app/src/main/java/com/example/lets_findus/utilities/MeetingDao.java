package com.example.lets_findus.utilities;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@Dao
public interface MeetingDao {
    @Insert
    void insert(Meeting m);

    @Update
    void update(Meeting m);

    @Delete
    void delete(Meeting m);

    @Transaction
    @Query("SELECT * FROM Meeting")
    ListenableFuture<List<MeetingPerson>> getMeetings();
}
