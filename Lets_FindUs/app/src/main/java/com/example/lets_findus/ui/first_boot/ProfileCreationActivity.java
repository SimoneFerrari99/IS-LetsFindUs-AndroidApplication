package com.example.lets_findus.ui.first_boot;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lets_findus.R;
import com.example.lets_findus.utilities.Person;

import java.io.File;
import java.io.FileNotFoundException;

public class ProfileCreationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_creation);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.profile_creation_fragment);
        NavController navController = navHostFragment.getNavController();


        Person myProfile = new Person();
        String myProfileFilename = "myProfile";
        File profileFile = new File(this.getFilesDir(), myProfileFilename);
        try {
            myProfile.storePersonAsync(this.openFileOutput(myProfileFilename, Context.MODE_PRIVATE));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}