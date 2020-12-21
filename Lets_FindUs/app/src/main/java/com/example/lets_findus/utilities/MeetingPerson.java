package com.example.lets_findus.utilities;

import androidx.room.Embedded;
import androidx.room.Relation;

public class MeetingPerson {
    @Embedded public Person person;
    @Relation(
            parentColumn = "id",
            entityColumn =  "personId"
    )
    public Meeting meeting;
}
