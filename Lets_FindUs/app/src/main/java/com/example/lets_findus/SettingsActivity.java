package com.example.lets_findus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.lets_findus.ui.first_boot.FirstOpeningInformations;
import com.google.gson.internal.$Gson$Preconditions;

import java.io.File;
import java.util.concurrent.Executors;
import android.os.Build.*;

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
            public void onClick(View v) {
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
                /*
                new AlertDialog.Builder(v.getContext())
                        .setTitle(R.string.clear_cache_confirm_title)
                        .setMessage(R.string.clear_cache_confirm_label)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                deleteCache(v.getContext());

                            }})
                        .setNegativeButton(R.string.no, null).show();*/
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

        // Apertura invio feedback
        TextView feedback = findViewById(R.id.feedback_label);
        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startFeedback= new Intent("android.intent.action.VIEW", Uri.parse("https://forms.gle/uEYMHAPhgmg2KBVY7"));
                startActivity(startFeedback);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent mIntent = new Intent(this, MainActivity.class);

        mIntent.putExtra("IS_FROM_PROFILE", true);
        startActivity(mIntent);

        return super.onOptionsItemSelected(item);
    }


/*
    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {}
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }*/
}