package com.example.lets_findus.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InvalidObjectException;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Person {
    public enum Sex{
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

    public String description;
    public String facebook;
    public String instagram;
    public String linkedin;
    public String email;
    public int phoneNumber;
    public Date birthDate;
    public String other;

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

    public void storePersonAsync(final FileOutputStream fos){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        final Gson gson = new Gson();

        executor.submit(new Runnable() {
            @Override
            public void run() {
                String personJson = gson.toJson(Person.this);
                try {
                    fos.write(personJson.getBytes());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static Future<Person> loadPersonAsync(final FileInputStream fis){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        final Gson gson = new Gson();

        return executor.submit(new Callable<Person>() {
            @Override
            public Person call() throws Exception {
                Type personType = new TypeToken<Person>() {}.getType();
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
                    e.printStackTrace();
                } finally {
                    contents = stringBuilder.toString();
                }

                return gson.fromJson(contents, personType);
            }
        });
    }

    public Map<String, String> dumpToString(){
        Map<String, String> dumper = new HashMap<>();
        dumper.put("Nickname", nickname);
        switch (sex){
            case MALE:
                dumper.put("Sesso", "Maschio");
                break;
            case FEMALE:
                dumper.put("Sesso", "Femmina");
                break;
            case OTHER:
                dumper.put("Sesso", "Altro");
                break;
        }
        dumper.put("Anno di nascita", String.valueOf(yearOfBirth));
        dumper.put("Descrizione", description);
        dumper.put("Facebook", facebook);
        dumper.put("Instagram", instagram);
        dumper.put("Linkedin", linkedin);
        dumper.put("Email", email);
        dumper.put("Telefono", (phoneNumber != 0) ? String.valueOf(phoneNumber) : null);
        dumper.put("Data di nascita", (birthDate != null) ? birthDate.toString() : null);
        dumper.put("Altro", other);

        return dumper;
    }

}
