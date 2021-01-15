package com.example.lets_findus.utilities;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Person.class, Meeting.class}, version = 2)
@TypeConverters({DateConverter.class, SexConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract PersonDao personDao();
    public abstract MeetingDao meetingDao();
}
