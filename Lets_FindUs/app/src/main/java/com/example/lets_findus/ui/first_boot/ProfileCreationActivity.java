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
//activity principale in cui viene creato il profilo
public class ProfileCreationActivity extends AppCompatActivity {
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_creation);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.profile_creation_fragment);
        navController = navHostFragment.getNavController();
        //controllo per capire se vengo dall'activity dell'editing dei dati o dalla activity principale
        if(getIntent().hasExtra("IS_FROM_EDIT")) {
            Bundle data = null;
            //se vengo dall'activity dell'editing devo prendermi i dati appena inseriti
            if (getIntent().hasExtra("FORM_DATA")) {
                data = getIntent().getBundleExtra("FORM_DATA");
                if (getIntent().hasExtra("PROPIC_CHANGED")) {
                    data.putString("propicFilePath", getIntent().getStringExtra("PROPIC_CHANGED"));
                }
            }
            //se vengo dall'activity dell'editing navigo alla pagina finale
            navController.navigate(R.id.profileFragment, data);
        }
        else {
            //altrimenti creo il file in cui viene salvato il profilo e creo un'oggetto person dummy
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
    //creo il tick nella appbar e lo visualizzo solo se sono nella pagina di visualizzazione del profilo
    @Override
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
    //se viene selezionato il tick nella appbar vado avanti
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        startActivity(new Intent(this, ResponsibilityActivity.class));
        finish();
        return super.onOptionsItemSelected(item);
    }

}