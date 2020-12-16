package com.example.lets_findus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

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

public class MainActivity extends AppCompatActivity implements MissingPermissionDialog.NoticeDialogListener, BottomNavigationView.OnNavigationItemSelectedListener {
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<Collection<Meeting<Person>>> allMeetings;
    private String filename = "incontri";

    private Fragment match_frag;
    private final Fragment fav_frag = new FavouritesFragment();
    private final Fragment prof_frag = new ProfileFragment();
    private Fragment active;
    private FragmentManager fm = getSupportFragmentManager();

    private boolean isFromEdit;

    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);

        navView.setOnNavigationItemSelectedListener(this);

        setTitle(R.string.title_matching);

        match_frag = new MatchingFragment();
        active = match_frag;

        if(getIntent().hasExtra("FORM_DATA")){
            prof_frag.setArguments(getIntent().getBundleExtra("FORM_DATA"));
        }

        fm.beginTransaction().add(R.id.nav_host_fragment, prof_frag, "3").hide(prof_frag).commit();
        fm.beginTransaction().add(R.id.nav_host_fragment, fav_frag, "2").hide(fav_frag).commit();
        fm.beginTransaction().add(R.id.nav_host_fragment, match_frag, "1").commit();

        isFromEdit = getIntent().hasExtra("IS_FROM_EDIT");
        if (isFromEdit){
            navView.setSelectedItemId(R.id.navigation_profile);
        }

        /*Meeting<Person> firstMeeting = new Meeting<>(new Person("pathProva", "gazz", Person.Sex.MALE, 1999), new Location(""));

        try {
            allMeetings = UtilFunction.loadMeetingsAsync(MainActivity.this.openFileInput(filename), executor);
            allMeetings = UtilFunction.addMeetingsAsync(allMeetings, firstMeeting, executor);
            UtilFunction.storeMeetingsAsync(allMeetings, MainActivity.this, filename, executor);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_matching:
                if(menu != null){
                    menu.setGroupVisible(R.id.match_menu, true);
                    menu.setGroupVisible(R.id.fav_menu, false);
                    menu.setGroupVisible(R.id.prof_menu, false);
                }
                setTitle(R.string.title_matching);
                fm.beginTransaction().hide(active).show(match_frag).commit();
                active = match_frag;
                return true;

            case R.id.navigation_favorites:
                if(menu != null){
                    menu.setGroupVisible(R.id.match_menu, false);
                    menu.setGroupVisible(R.id.fav_menu, true);
                    menu.setGroupVisible(R.id.prof_menu, false);
                }
                setTitle(R.string.title_favorites);
                fm.beginTransaction().hide(active).show(fav_frag).commit();
                active = fav_frag;
                return true;

            case R.id.navigation_profile:
                if(menu != null){
                    menu.setGroupVisible(R.id.match_menu, false);
                    menu.setGroupVisible(R.id.fav_menu, false);
                    menu.setGroupVisible(R.id.prof_menu, true);
                }
                setTitle(R.string.title_profile);
                fm.beginTransaction().hide(active).show(prof_frag).commit();
                active = prof_frag;
                return true;
        }
        return false;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.main_act_menu, menu);
        if(isFromEdit){
            menu.setGroupVisible(R.id.match_menu, false);
            menu.setGroupVisible(R.id.fav_menu, false);
            menu.setGroupVisible(R.id.prof_menu, true);
        }
        else {
            menu.setGroupVisible(R.id.match_menu, true);
            menu.setGroupVisible(R.id.fav_menu, false);
            menu.setGroupVisible(R.id.prof_menu, false);
        }
        return super.onCreateOptionsMenu(menu);

    }

}