package com.example.lets_findus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
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
import com.example.lets_findus.ui.first_boot.FirstOpeningInformations;
import com.example.lets_findus.ui.matching.MatchingFragment;
import com.example.lets_findus.ui.profile.ProfileFragment;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MissingPermissionDialog.NoticeDialogListener, BottomNavigationView.OnNavigationItemSelectedListener {
    private Fragment match_frag;
    private final Fragment fav_frag = new FavouritesFragment();
    private final Fragment prof_frag = new ProfileFragment();
    private Fragment active;
    private FragmentManager fm = getSupportFragmentManager();

    private static int AUTOCOMPLETE_REQUEST_CODE = 92;

    private PlacesClient placesClient;

    private boolean isFromEdit;

    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyAFjB2Yk9GGgLqzgVMGOBlxNvSDnDUKy5w");
        }
        placesClient = Places.createClient(this);
        SharedPreferences pref = this.getSharedPreferences("com.example.lets_findus.FIRST_BOOT", MODE_PRIVATE);
        int isFirstBoot = pref.getInt("FIRST_BOOT", 0);
        if(isFirstBoot == 0) {
            Intent startFirstOpening = new Intent(this, FirstOpeningInformations.class);
            startActivity(startFirstOpening);
        }
        else {
            setContentView(R.layout.activity_main);
            BottomNavigationView navView = findViewById(R.id.nav_view);

            navView.setOnNavigationItemSelectedListener(this);

            setTitle(R.string.title_matching);

            match_frag = new MatchingFragment();
            active = match_frag;

            if(getIntent().hasExtra("FORM_DATA")){
                Bundle data = getIntent().getBundleExtra("FORM_DATA");
                if(getIntent().hasExtra("PROPIC_CHANGED")){
                    data.putString("propicFilePath", getIntent().getStringExtra("PROPIC_CHANGED"));
                }
                prof_frag.setArguments(data);
            }

            fm.beginTransaction().add(R.id.nav_host_fragment, prof_frag, "3").hide(prof_frag).commit();
            fm.beginTransaction().add(R.id.nav_host_fragment, fav_frag, "2").hide(fav_frag).commit();
            fm.beginTransaction().add(R.id.nav_host_fragment, match_frag, "1").commit();

            isFromEdit = getIntent().hasExtra("IS_FROM_EDIT");
            if (isFromEdit){
                navView.setSelectedItemId(R.id.navigation_profile);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("AutocompleteApi", "Request code: "+ requestCode + " resultCode: "+resultCode);
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getTitle().toString().compareTo("Cerca") == 0){
            List<Place.Field> fields = Arrays.asList(Place.Field.LAT_LNG);

            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(this);
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        }
        return super.onOptionsItemSelected(item);
    }
}