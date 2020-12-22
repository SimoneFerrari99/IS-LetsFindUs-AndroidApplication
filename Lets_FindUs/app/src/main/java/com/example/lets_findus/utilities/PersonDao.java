package com.example.lets_findus.utilities;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;

@Dao
public interface PersonDao {
    @Insert
    ListenableFuture<Long> insert(Person p);

    @Insert
    ListenableFuture<ArrayList<Long>> insertAll(Person... p);

    @Update
    void update(Person p);

    @Delete
    void deleteAll(Person... p);

    @Query("DELETE FROM Person WHERE Person.id = :id")
    void deletePersonById(int id);

    @Query("SELECT * FROM Person WHERE Person.id = :id")
    ListenableFuture<Person> getPersonById(int id);

    @Query("SELECT * FROM Person WHERE Person.id IN (SELECT MAX(id) FROM Person)")
    ListenableFuture<Person> getLastPersonInserted();
}
