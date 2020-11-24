package com.example.lets_findus.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.lets_findus.R;

import org.jetbrains.annotations.NotNull;

import java.io.InvalidObjectException;

public class Person {
    enum Sex{
        MALE,
        FEMALE,
        OTHER
    }
    //these are all obligatory fields
    @NotNull
    public String profilePath;
    @NotNull
    public String nickname;
    @NotNull
    public Sex sex;
    @NotNull
    public int yearOfBirth;

    // TODO: 24/11/2020 aggiungere un'implementazione comoda per tutti i campi non obbligatori

    public Person(@NotNull String pathName, @NotNull String nickname, @NotNull Sex sex, @NotNull int yearOfBirth) {
        this.profilePath = pathName;
        this.nickname = nickname;
        this.sex = sex;
        this.yearOfBirth = yearOfBirth;
    }

    //return the Bitmap loaded from the path, if the path is valid, an exception if not
    @NotNull
    public Bitmap getProfilePicture() throws InvalidObjectException {
        Bitmap image = BitmapFactory.decodeFile(this.profilePath);
        if (image != null)
            return image;
        else
            throw new InvalidObjectException("The specified file name cannot be decoded into a bitmap");
    }

}
