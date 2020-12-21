package com.example.lets_findus.utilities;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.google.common.util.concurrent.ListenableFuture;

@Dao
public interface PersonDao {
    @Insert
    ListenableFuture<Long> insert(Person p);

    @Update
    void update(Person p);

    @Delete
    void delete(Person... p);

    @Query("SELECT * FROM Person WHERE Person.id = :id")
    ListenableFuture<Person> getPersonById(int id);
}
