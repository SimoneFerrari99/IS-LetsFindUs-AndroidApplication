package com.example.lets_findus.utilities;

import androidx.room.TypeConverter;

import java.util.Date;
//converter per il tipo data nella classe meeting, utile per il salvataggio in maniera corretta nel database
public class DateConverter {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
