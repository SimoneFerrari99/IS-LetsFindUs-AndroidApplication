package com.example.lets_findus.utilities;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.room.Room;

public class DeleteOldMeetingService extends IntentService {

    public DeleteOldMeetingService() {
        super("delete-meeting");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "meeting_db").build();
        MeetingDao md = db.meetingDao();
        PersonDao pd = db.personDao();
        UtilFunction.deleteMeetingsOlderThan(7, md, pd);

    }
}
