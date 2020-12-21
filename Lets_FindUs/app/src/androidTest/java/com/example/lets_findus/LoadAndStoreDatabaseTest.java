package com.example.lets_findus;

import android.content.Context;
import android.util.Log;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.lets_findus.utilities.AppDatabase;
import com.example.lets_findus.utilities.MeetingDao;
import com.example.lets_findus.utilities.Person;
import com.example.lets_findus.utilities.PersonDao;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.Executors;

@RunWith(AndroidJUnit4.class)
public class LoadAndStoreDatabaseTest {
    private PersonDao pd;
    private MeetingDao md;
    private AppDatabase db;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.databaseBuilder(context, AppDatabase.class, "meeting_db").build();
        pd = db.personDao();
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void storeAndUpdatePerson() throws Exception {
        final Person p = new Person("", "Gazz", Person.Sex.MALE, 1999);
        Log.d("PersonId", String.valueOf(p.id));
        ListenableFuture<Long> ins = pd.insert(p);
        Futures.addCallback(ins, new FutureCallback<Long>() {
            @Override
            public void onSuccess(@NullableDecl Long result) {
                p.id = 1;
                p.nickname = "tuamadre";
                pd.update(p);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("TestFailure", "Insert failure");
            }
        }, Executors.newSingleThreadExecutor());
    }

    @Test
    public void loadPersonFromId() throws Exception{
        ListenableFuture<Person> getPerson = pd.getPersonById(1);
        Futures.addCallback(getPerson, new FutureCallback<Person>() {
            @Override
            public void onSuccess(@NullableDecl Person result) {
                assert result != null;
                Log.d("TestSuccess", "Nickname: "+result.nickname);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("TestFailure", "Select failure");
            }

        }, Executors.newSingleThreadExecutor());
    }
}
