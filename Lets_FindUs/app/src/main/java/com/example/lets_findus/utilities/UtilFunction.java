package com.example.lets_findus.utilities;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.LocalDate;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

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

    private void deletePicture(String path){
        File current = new File(path);
        if(current.exists()){
            current.delete();
        }
    }

    public void deleteMeetingsOlderThan(int nDays, MeetingDao md, PersonDao pd){
        Date start = subtractDays(Calendar.getInstance().getTime(), nDays);
        ListenableFuture<List<MeetingPerson>> meetingsToDelete = md.getMeetingBeforeDate(start);
        Futures.addCallback(meetingsToDelete, new FutureCallback<List<MeetingPerson>>() {
            @Override
            public void onSuccess(@NullableDecl List<MeetingPerson> result) {
                for(MeetingPerson mp : result){
                    deletePicture(mp.person.profilePath);
                    pd.deleteAll(mp.person);
                    md.delete(mp.meeting);
                }
            }

            @Override
            public void onFailure(Throwable t) {

            }
        }, Executors.newSingleThreadExecutor());
    }
}
