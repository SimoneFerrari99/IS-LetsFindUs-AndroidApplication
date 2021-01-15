package com.example.lets_findus;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lets_findus.ui.first_boot.FirstOpeningInformations;
//activity per i settings dell'app
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle(R.string.title_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Eliminazione dei dati dal menu impostazioni
        TextView deleteData = findViewById(R.id.delete_data_label);
        deleteData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                //TODO: AGGIUNGI SI O NO

                new AlertDialog.Builder(v.getContext())
                        .setTitle(R.string.delete_data_confirm_title)
                        .setMessage(R.string.delete_data_confirm_label)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (VERSION_CODES.KITKAT <= VERSION.SDK_INT) {
                                    ((ActivityManager)v.getContext().getSystemService(ACTIVITY_SERVICE))
                                            .clearApplicationUserData();
                                } else {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivity(intent);
                                }
                            }})
                        .setNegativeButton(R.string.no, null).show();
            }
        });


        // Pulizia della cache dal menu impostazioni
        TextView clearCache = findViewById(R.id.clear_cache_label);
        clearCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });


        // Apertura informazioni APP
        TextView informations = findViewById(R.id.informations_label);
        informations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startFirstOpening = new Intent(v.getContext(), FirstOpeningInformations.class);
                startActivity(startFirstOpening);
            }
        });

        // Apertura invio feedback tramite un apposito form esterno
        TextView feedback = findViewById(R.id.feedback_label);
        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startFeedback= new Intent("android.intent.action.VIEW", Uri.parse("https://forms.gle/uEYMHAPhgmg2KBVY7"));
                startActivity(startFeedback);
            }
        });
    }
    //gestione del pulsante back
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent mIntent = new Intent(this, MainActivity.class);

        mIntent.putExtra("IS_FROM_PROFILE", true);
        finish();
        startActivity(mIntent);
        return super.onOptionsItemSelected(item);
    }
}