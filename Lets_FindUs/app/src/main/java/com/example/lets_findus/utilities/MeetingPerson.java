package com.example.lets_findus.utilities;

import androidx.room.Embedded;
import androidx.room.Relation;
//classe per la relazione 1 a 1 tra meeting e person nel database
public class MeetingPerson {
    @Embedded public Person person;
    @Relation(
            parentColumn = "id",
            entityColumn =  "personId"
    )
    public Meeting meeting;
}
