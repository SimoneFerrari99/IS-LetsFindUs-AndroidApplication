package com.example.lets_findus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.lets_findus.ui.MissingPermissionDialog;
import com.example.lets_findus.ui.favourites.FavouritesFragment;
import com.example.lets_findus.ui.matching.MatchingFragment;
import com.example.lets_findus.ui.profile.ProfileFragment;
import com.example.lets_findus.utilities.Meeting;
import com.example.lets_findus.utilities.Person;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity implements MissingPermissionDialog.NoticeDialogListener {
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<Collection<Meeting<Person>>> allMeetings;
    private String filename = "incontri";

    private MatchingFragment match_frag;
    private FavouritesFragment fav_frag;
    private ProfileFragment prof_frag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_mathcing, R.id.navigation_favouirtes, R.id.navigation_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        /*Meeting<Person> firstMeeting = new Meeting<>(new Person("pathProva", "tomare", Person.Sex.MALE, 1999), new Location(""));

        try {
            allMeetings = UtilFunction.loadMeetingsAsync(MainActivity.this.openFileInput(filename), executor);
            allMeetings = UtilFunction.addMeetingsAsync(allMeetings, firstMeeting, executor);
            UtilFunction.storeMeetingsAsync(allMeetings, MainActivity.this, filename, executor);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",this.getPackageName(), null);
        intent.setData(uri);
        this.startActivity(intent);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_UNINSTALL_PACKAGE);
        Uri uri = Uri.fromParts("package",this.getPackageName(), null);
        intent.setData(uri);
        this.startActivity(intent);
    }
}