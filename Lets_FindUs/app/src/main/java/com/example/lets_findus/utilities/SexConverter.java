package com.example.lets_findus.utilities;

import androidx.room.TypeConverter;
//converter per l'enum Sex per salvarlo correttamente nel room database
public class SexConverter {
    @TypeConverter
    public static Person.Sex fromString(String value) {
        switch (value){
            case "male":
                return Person.Sex.MALE;
            case "female":
                return Person.Sex.FEMALE;
            case "other":
                return Person.Sex.OTHER;
            default:
                return null;
        }
    }

    @TypeConverter
    public static String sexToString(Person.Sex sex) {
        switch (sex){
            case MALE:
                return "male";
            case FEMALE:
                return "female";
            case OTHER:
                return "other";
            default:
                return null;
        }
    }
}
