package com.example.lets_findus.utilities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Entity
public class Person {
    @PrimaryKey(autoGenerate = true)
    public int id;
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
    public Integer yearOfBirth;

    public String description;
    public String name;
    public String surname;
    public String facebook;
    public String instagram;
    public String linkedin;
    public String email;
    public Long phoneNumber;
    public Date birthDate;
    public String other;

    public Person(){
        super();
    }

    public Person(@NotNull String profilePath, @NotNull String nickname, @NotNull Sex sex, @NotNull Integer yearOfBirth) {
        this.profilePath = profilePath;
        this.nickname = nickname;
        this.sex = sex;
        this.yearOfBirth = yearOfBirth;
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
        dumper.put("Nome", name);
        dumper.put("Cognome", surname);
        dumper.put("Facebook", facebook);
        dumper.put("Instagram", instagram);
        dumper.put("Linkedin", linkedin);
        dumper.put("Email", email);
        dumper.put("Telefono", (phoneNumber != 0) ? String.valueOf(phoneNumber) : null);
        if(birthDate != null){
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ITALIAN);
            dumper.put("Data di nascita", sdf.format(birthDate));
        }
        else{
            dumper.put("Data di nascita", null);
        }
        dumper.put("Altro", other);

        return dumper;
    }

}
