package com.example.lets_findus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.lets_findus.ui.MissingBluetoothDialog;
import com.example.lets_findus.ui.MissingPermissionDialog;
import com.example.lets_findus.ui.favourites.FavouritesFragment;
import com.example.lets_findus.ui.first_boot.FirstOpeningInformations;
import com.example.lets_findus.ui.matching.MatchingFragment;
import com.example.lets_findus.ui.profile.ProfileFragment;
import com.example.lets_findus.utilities.DeleteBroadcastReceiver;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements MissingBluetoothDialog.NoticeDialogListener, MissingPermissionDialog.NoticeDialogListener, BottomNavigationView.OnNavigationItemSelectedListener {
    private MatchingFragment match_frag;
    private final FavouritesFragment fav_frag = new FavouritesFragment();
    private final ProfileFragment prof_frag = new ProfileFragment();
    private Fragment active;
    private FragmentManager fm = getSupportFragmentManager();

    private BluetoothAdapter bluetoothAdapter;

    public static ActivityResultLauncher<String> requestPermissionLauncher;

    private static int AUTOCOMPLETE_REQUEST_CODE = 3;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int ACCESS_LOCATION = 2;

    private PlacesClient placesClient;

    private boolean isFromEdit;

    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
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
            finish();
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

            requestPermissionLauncher =
                    registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public void onActivityResult(Boolean isGranted) {
                            if (isGranted) {
                                fm.beginTransaction().remove(match_frag).commit();
                                match_frag = new MatchingFragment();
                                fm.beginTransaction().add(R.id.nav_host_fragment, match_frag, "1").commit();
                            } else {
                                DialogFragment newFragment = new MissingPermissionDialog();
                                newFragment.show(getSupportFragmentManager(), "Location permission");
                            }
                        }
                    });
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            else{
                askLocationPermission();
            }
            if(!(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED)){
                //requestPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            }

            isFromEdit = getIntent().hasExtra("IS_FROM_EDIT");
            if (isFromEdit || getIntent().hasExtra("IS_FROM_PROFILE")){
                navView.setSelectedItemId(R.id.navigation_profile);
            }

            if(getIntent().hasExtra("IS_FROM_FAV")){
                navView.setSelectedItemId(R.id.navigation_favorites);
            }
            scheduleDeleteMeeting();
        }
    }

    private void askLocationPermission(){
        if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void scheduleDeleteMeeting(){
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), DeleteBroadcastReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, DeleteBroadcastReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every every half hour from this point onwards
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, AlarmManager.INTERVAL_DAY, pIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ENABLE_BT){
            if(resultCode == RESULT_CANCELED) {
                DialogFragment newFragment = new MissingBluetoothDialog();
                newFragment.show(getSupportFragmentManager(), "Missing bluetooth");
            }
            else{
                askLocationPermission();
            }
        }
        if(requestCode == ACCESS_LOCATION){
            askLocationPermission();
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ){
                fm.beginTransaction().remove(match_frag).commit();
                match_frag = new MatchingFragment();
                fm.beginTransaction().add(R.id.nav_host_fragment, match_frag, "1").commit();
            }
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        Intent intent = new Intent();
        if(dialog.getClass().getSimpleName().compareTo("MissingBluetoothDialog") == 0){
            bluetoothAdapter.enable();
            askLocationPermission();
        }
        else{
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package",this.getPackageName(), null);
            intent.setData(uri);
            startActivityForResult(intent, ACCESS_LOCATION);
        }
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
        if(isFromEdit || getIntent().hasExtra("IS_FROM_PROFILE")){
            menu.setGroupVisible(R.id.match_menu, false);
            menu.setGroupVisible(R.id.fav_menu, false);
            menu.setGroupVisible(R.id.prof_menu, true);
        }
        else {
            if(getIntent().hasExtra("IS_FROM_FAV")){
                menu.setGroupVisible(R.id.match_menu, false);
                menu.setGroupVisible(R.id.fav_menu, true);
                menu.setGroupVisible(R.id.prof_menu, false);
            }
            else {
                menu.setGroupVisible(R.id.match_menu, true);
                menu.setGroupVisible(R.id.fav_menu, false);
                menu.setGroupVisible(R.id.prof_menu, false);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getTitle().toString().compareTo("Cerca") == 0){
            try {
                List<Place.Field> fields = Arrays.asList(Place.Field.LAT_LNG);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(this);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
            }
            catch (RuntimeException e){
                e.printStackTrace();
            }
        }
        if(item.getTitle().toString().compareTo("Impostazioni") == 0){
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
        }
        else if(item.getTitle().toString().compareTo("Filtri") == 0){
            showFilterPopup();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showFilterPopup(){
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.filter_popup_window, null);
        popupView.setBackgroundResource(android.R.color.white);
        popupView.setElevation(32);

        final Spinner age = popupView.findViewById(R.id.spinner_age);
        final Spinner date = popupView.findViewById(R.id.spinner_date);
        final Spinner hour = popupView.findViewById(R.id.spinner_hour);
        final Spinner sex = popupView.findViewById(R.id.spinner_sex);

        ArrayAdapter<CharSequence> ageAdapter = ArrayAdapter.createFromResource(this, R.array.age_spinner_options, android.R.layout.simple_spinner_item);
        ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        age.setAdapter(ageAdapter);

        ArrayAdapter<CharSequence> dateAdapter = ArrayAdapter.createFromResource(this, R.array.date_spinner_options, android.R.layout.simple_spinner_item);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        date.setAdapter(dateAdapter);

        ArrayAdapter<CharSequence> hourAdapter = ArrayAdapter.createFromResource(this, R.array.hour_spinner_options, android.R.layout.simple_spinner_item);
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hour.setAdapter(hourAdapter);

        ArrayAdapter<CharSequence> sexAdapter = ArrayAdapter.createFromResource(this, R.array.sex_spinner_options, android.R.layout.simple_spinner_item);
        sexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sex.setAdapter(sexAdapter);

        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.setElevation(10);

        Button filter = popupView.findViewById(R.id.button2);
        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> options = new HashMap<>();
                options.put("age", age.getSelectedItem().toString());
                options.put("date", date.getSelectedItem().toString());
                options.put("hour", hour.getSelectedItem().toString());
                options.put("sex", sex.getSelectedItem().toString());
                if(active == match_frag){
                    match_frag.filterItems(options);
                }
                if(active == fav_frag){
                    fav_frag.filterItems(options);
                }
                popupWindow.dismiss();
            }
        });

        popupWindow.showAtLocation(new LinearLayout(this), Gravity.CENTER, 0, 0);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        super.onBackPressed();
    }
}