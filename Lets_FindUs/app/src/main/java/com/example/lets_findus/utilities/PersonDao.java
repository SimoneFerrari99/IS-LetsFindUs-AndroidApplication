package com.example.lets_findus.utilities;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@Dao
public abstract class PersonDao {
    @Insert
    public abstract ListenableFuture<Long> insert(Person p);

    @Insert
    public abstract ListenableFuture<Long[]> insertAll(Person... p);

    @Update
    public abstract void update(Person p);

    @Delete
    public abstract void deleteAll(Person... p);

    @Query("DELETE FROM Person WHERE Person.id = :id")
    public abstract void deletePersonById(int id);

    @Query("SELECT * FROM Person WHERE Person.id = :id")
    public abstract ListenableFuture<Person> getPersonById(int id);

    @Query("SELECT * FROM Person WHERE Person.id IN (SELECT MAX(id) FROM Person)")
    public abstract ListenableFuture<Person> getLastPersonInserted();

    @Query("SELECT * FROM Person")
    public abstract ListenableFuture<List<Person>> getAllPerson();
}
