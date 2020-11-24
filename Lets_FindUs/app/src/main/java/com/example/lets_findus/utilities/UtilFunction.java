package com.example.lets_findus.utilities;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.cert.CollectionCertStoreParameters;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

public class UtilFunction {

    //throws the IOException so that it can be handle at a higher level
    public static void storeMeeting(Meeting<Person> meeting, Gson gson, FileOutputStream fos) throws IOException {
        Type meetingType = new TypeToken<Meeting<Person>>() {}.getType(); //used because meeting has a generic type in it
        String meetingJSON = gson.toJson(meeting, meetingType);
        fos.write(meetingJSON.getBytes()); //using getBytes the string is encoded using platform's default charset
    }
}
