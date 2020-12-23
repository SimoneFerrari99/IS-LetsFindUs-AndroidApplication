package com.example.lets_findus;

import android.content.Context;
import android.util.Log;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.lets_findus.utilities.AppDatabase;
import com.example.lets_findus.utilities.Meeting;
import com.example.lets_findus.utilities.MeetingDao;
import com.example.lets_findus.utilities.MeetingPerson;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@RunWith(AndroidJUnit4.class)
public class MeetingDaoTest {
    private MeetingDao md;
    private AppDatabase db;
    private PersonDao pd;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.databaseBuilder(context, AppDatabase.class, "meeting_db").build();
        md = db.meetingDao();
        pd = db.personDao();
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void insertMeeting() throws Exception{
        ListenableFuture<List<Person>> allPerson = pd.getAllPerson();
        final Executor ex1 = Executors.newSingleThreadExecutor();
        final Executor ex2 = Executors.newSingleThreadExecutor();
        Futures.addCallback(allPerson, new FutureCallback<List<Person>>() {
            @Override
            public void onSuccess(@NullableDecl List<Person> result) {
                for(Person p : result) {
                    Meeting m1 = new Meeting(p, 45.17, 11.59, null);
                    ListenableFuture<Long> ins = md.insert(m1);
                    Futures.addCallback(ins, new FutureCallback<Long>() {
                        @Override
                        public void onSuccess(@NullableDecl Long result) {
                            Log.d("InsertMeeting", "Test success");
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            Log.d("InsertMeeting", "Test failure");
                        }
                    }, ex2);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("SelectLast", "Test failure");
            }
        }, ex1);

    }

    @Test
    public void getMeetingRegion() throws Exception{
        List<MeetingPerson> sel = md.getMeetingsBetweenRegion(50, 40, 10, 12);
        Log.d("getMeetingRegion", String.valueOf(sel.size()));
    }

    @Test
    public void getAndSetFavouriteAllMeetings() throws Exception{
        List<MeetingPerson> sel = md.getMeetingsBetweenRegion(50, 40, 10, 12);
        List<Meeting> meetings = new ArrayList<>();
        for(MeetingPerson mp : sel) {
            meetings.add(mp.meeting);
        }
        md.setFavouriteAll(meetings);
    }

    @Test
    public void deleteMeeting() throws Exception{
        List<MeetingPerson> sel = md.getMeetingsBetweenRegion(50, 40, 10, 12);
        md.delete(sel.get(0).meeting);
    }
}
