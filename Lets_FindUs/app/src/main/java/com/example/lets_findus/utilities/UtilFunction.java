package com.example.lets_findus.utilities;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;

public class UtilFunction {


    public static void storeMeetingAsync(final Meeting<Person> meeting, final FileOutputStream fos, Executor executor){
        final Gson gson = new Gson();
        FutureTask<?> store = new FutureTask<>(new Callable<Object>() {
            @Override
            public Object call(){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d("PROVA", "Ehi sto runnando sul thread "+Thread.currentThread().getName());
                Type meetingType = new TypeToken<Meeting<Person>>() {}.getType(); //used because meeting has a generic type in it
                String meetingJSON = gson.toJson(meeting, meetingType);
                try {
                    fos.write(meetingJSON.getBytes()); //using getBytes the string is encoded using platform's default charset
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
        executor.execute(store);
    }
}
