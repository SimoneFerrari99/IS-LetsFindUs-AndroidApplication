package com.example.lets_findus.utilities;

import android.util.Log;

import org.joda.time.DateTime;

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

    public static boolean isInHourRange(Date d, double lowerBound, double upperBound){
        DateTime date = new DateTime(d);
        int hour = date.getHourOfDay();
        int minutes = date.getMinuteOfHour();
        double hourAndMinutes = hour + (double)minutes/100;
        Log.d("Ora e minuti: ", String.valueOf(hourAndMinutes));
        return (hourAndMinutes >= lowerBound && hourAndMinutes <= upperBound);
    }
}
