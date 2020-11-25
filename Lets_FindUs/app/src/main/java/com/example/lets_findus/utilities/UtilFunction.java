package com.example.lets_findus.utilities;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static android.content.Context.MODE_PRIVATE;

public class UtilFunction {

    public static Future<Collection<Meeting<Person>>> addMeetingsAsync(final Future<Collection<Meeting<Person>>> meetingsFuture, final Meeting<Person> meeting, ExecutorService executor){
        return executor.submit(new Callable<Collection<Meeting<Person>>>() {
            @Override
            public Collection<Meeting<Person>> call() throws Exception {
                Collection<Meeting<Person>> meetings = meetingsFuture.get();
                meetings.add(meeting);
                for(Meeting<Person> meet : meetings) {
                    Log.d("addMeetingAsync", "Collection = " + meet.data.nickname);
                }
                return meetings;
            }
        });
    }

    public static Future<Collection<Meeting<Person>>> storeMeetingsAsync(final Future<Collection<Meeting<Person>>> meetingsFuture, final Context context, final String filename, ExecutorService executor){
        final Gson gson = new Gson();

        return executor.submit(new Callable<Collection<Meeting<Person>>>() {
            @Override
            public Collection<Meeting<Person>> call() {
                Collection<Meeting<Person>> meetings = null;
                FileOutputStream fos;
                try {
                    meetings = meetingsFuture.get();
                    fos = context.openFileOutput(filename, MODE_PRIVATE);
                    for(Meeting<Person> meet : meetings) {
                        Log.d("StoreMeetingAsync", "Collection = " + meet.data.nickname);
                    }
                    Type meetingType = new TypeToken<Collection<Meeting<Person>>>() {}.getType(); //used because meeting has a generic type in it
                    String meetingJSON = gson.toJson(meetings, meetingType);
                    try {
                        fos.write(meetingJSON.getBytes()); //using getBytes the string is encoded using platform's default charset
                        fos.close();
                    } catch (IOException e) {
                        Log.e("StoreMeetingAsync", "Error in writing a meeting to a file");
                        throw new RuntimeException("Error in writing a meeting to a file");
                    }
                } catch (ExecutionException | InterruptedException | FileNotFoundException e) {
                    Log.e("StoreMeetingAsync", Objects.requireNonNull(e.getMessage()));
                }
                return meetings;
            }
        });
    }

    public static Future<Collection<Meeting<Person>>> loadMeetingsAsync(final FileInputStream fis, ExecutorService executor){
        final Gson gson = new Gson();

        return executor.submit(new Callable<Collection<Meeting<Person>>>() {
            @Override
            public Collection<Meeting<Person>> call(){
                Type meetingType = new TypeToken<Collection<Meeting<Person>>>() {}.getType(); //used because meeting has a generic type in it
                InputStreamReader inputStreamReader = new InputStreamReader(fis);
                StringBuilder stringBuilder = new StringBuilder();
                String contents;
                try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                    String line = reader.readLine();
                    Log.d("loadMeetingAsync", "Linea = " + line);
                    while (line != null) {
                        stringBuilder.append(line);
                        line = reader.readLine();
                    }
                } catch (IOException e) {
                    Log.e("Scrittura sul file", "Errore nello scrivere nel file");
                    throw new RuntimeException("Error in reading a meeting list from a file");
                } finally {
                    contents = stringBuilder.toString();
                    Log.d("loadMeetingAsync", "Stringa = " + contents);
                }
                Collection<Meeting<Person>> meetings = gson.fromJson(contents, meetingType);

                Log.d("loadMeetingAsync", "Collection = " + meetings);

                if(meetings == null){
                    meetings = new ArrayList<>();
                }

                return meetings;
            }
        });
    }

    public static void setMarkerAsync(final Future<Collection<Meeting<Person>>> meetingsFuture, ExecutorService executor, final GoogleMap map){
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Collection<Meeting<Person>> meetings = meetingsFuture.get();
                    Handler mainThreadHandler = new Handler(Looper.getMainLooper());
                    for(Meeting<Person> meeting: meetings){
                        final LatLng meetLoc = new LatLng(meeting.meetingLoc.getLatitude(), meeting.meetingLoc.getLongitude());
                        final String markerTitle = meeting.data.nickname;
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                map.addMarker(new MarkerOptions()
                                        .position(meetLoc)
                                        .title(markerTitle));
                            }
                        });
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
