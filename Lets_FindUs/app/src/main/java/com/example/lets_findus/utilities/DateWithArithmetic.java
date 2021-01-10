package com.example.lets_findus.utilities;

import android.util.Log;

import java.util.Calendar;
import java.util.Date;

public class DateWithArithmetic{

    public static Date subtractDays(Date d, int numDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        calendar.add(Calendar.DAY_OF_MONTH, -numDays);
        Log.d("Oggi -"+numDays, calendar.getTime().toString());
        return calendar.getTime();
    }
}
