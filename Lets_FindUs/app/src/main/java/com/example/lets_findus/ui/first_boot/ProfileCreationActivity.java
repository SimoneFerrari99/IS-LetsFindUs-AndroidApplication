package com.example.lets_findus.ui.first_boot;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lets_findus.R;
import com.example.lets_findus.utilities.Person;

import java.io.File;
import java.io.FileNotFoundException;

public class ProfileCreationActivity extends AppCompatActivity {
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_creation);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.profile_creation_fragment);
        navController = navHostFragment.getNavController();

        if(getIntent().hasExtra("IS_FROM_EDIT")) {
            Bundle data = null;
            if (getIntent().hasExtra("FORM_DATA")) {
                data = getIntent().getBundleExtra("FORM_DATA");
                if (getIntent().hasExtra("PROPIC_CHANGED")) {
                    data.putString("propicFilePath", getIntent().getStringExtra("PROPIC_CHANGED"));
                }
            }
            navController.navigate(R.id.profileFragment, data);
        }
        else {
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        startActivity(new Intent(this, ResponsibilityActivity.class));
        finish();
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.first_boot_menu, menu);
        menu.setGroupVisible(R.id.confirm_menu, false);
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                if(controller.getCurrentDestination().getLabel().toString().compareTo("ProfileFragment") == 0){
                    menu.setGroupVisible(R.id.confirm_menu, true);
                }
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}