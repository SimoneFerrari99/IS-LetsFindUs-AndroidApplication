package com.example.lets_findus.utilities;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static android.content.Context.MODE_PRIVATE;

public class UtilFunction {

    public static Future<Collection<Meeting>> addMeetingsAsync(final Future<Collection<Meeting>> meetingsFuture, final Meeting meeting, ExecutorService executor){
        return executor.submit(new Callable<Collection<Meeting>>() {
            @Override
            public Collection<Meeting> call() throws Exception {
                Collection<Meeting> meetings = meetingsFuture.get();
                meetings.add(meeting);
                for(Meeting meet : meetings) {
                    Log.d("addMeetingAsync", "Collection = " + meet.data.nickname);
                }
                return meetings;
            }
        });
    }

    public static Future<Collection<Meeting>> storeMeetingsAsync(final Future<Collection<Meeting>> meetingsFuture, final Context context, final String filename, ExecutorService executor){
        final Gson gson = new Gson();

        return executor.submit(new Callable<Collection<Meeting>>() {
            @Override
            public Collection<Meeting> call() {
                Collection<Meeting> meetings = null;
                FileOutputStream fos;
                try {
                    meetings = meetingsFuture.get();
                    fos = context.openFileOutput(filename, MODE_PRIVATE);
                    for(Meeting meet : meetings) {
                        Log.d("StoreMeetingAsync", "Collection = " + meet.data.nickname);
                    }
                    Type meetingType = new TypeToken<Collection<Meeting>>() {}.getType(); //used because meeting has a generic type in it
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

    public static Future<Collection<Meeting>> loadMeetingsAsync(final FileInputStream fis, ExecutorService executor){
        final Gson gson = new Gson();

        return executor.submit(new Callable<Collection<Meeting>>() {
            @Override
            public Collection<Meeting> call(){
                Type meetingType = new TypeToken<Collection<Meeting>>() {}.getType(); //used because meeting has a generic type in it
                InputStreamReader inputStreamReader = new InputStreamReader(fis);
                StringBuilder stringBuilder = new StringBuilder();
                String contents;
                try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                    String line = reader.readLine();
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
                Collection<Meeting> meetings = gson.fromJson(contents, meetingType);

                Log.d("loadMeetingAsync", "Collection = " + meetings);

                if(meetings == null){
                    meetings = new ArrayList<>();
                }

                return meetings;
            }
        });
    }

    public static void setMarkerAsync(final Future<Collection<Meeting>> meetingsFuture, ExecutorService executor, final GoogleMap map){
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Collection<Meeting> meetings = meetingsFuture.get();
                    Handler mainThreadHandler = new Handler(Looper.getMainLooper());
                    for(Meeting meeting: meetings){
                        final LatLng meetLoc = new LatLng(meeting.latitude, meeting.longitude);
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

    public static void setMAsync(ExecutorService executor, final GoogleMap map){
        executor.submit(new Runnable() {
            @Override
            public void run() {
                Handler mainThreadHandler = new Handler(Looper.getMainLooper());
                final float[] hue = {BitmapDescriptorFactory.HUE_AZURE, BitmapDescriptorFactory.HUE_ORANGE, BitmapDescriptorFactory.HUE_VIOLET, BitmapDescriptorFactory.HUE_GREEN, BitmapDescriptorFactory.HUE_MAGENTA, BitmapDescriptorFactory.HUE_RED};
                final Random rnd = new Random();
                /*try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                final MarkerOptions mark = new MarkerOptions();
                for(int i = 0; i < 10000; i++) {
                    mark.position(new LatLng(0,0))
                            .icon(BitmapDescriptorFactory.defaultMarker(hue[rnd.nextInt(hue.length)]))
                            .title("prova");
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            map.addMarker(mark);
                        }
                    });
                }

            }
        });
    }
}
