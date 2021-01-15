package com.example.lets_findus.ui;

import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lets_findus.R;
//activity per la visualizzazione della foto profilo
public class ViewPictureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_picture);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //carico l'immagine sulla view
        if(getIntent().hasExtra("PIC_PATH")){
            String picPath = getIntent().getStringExtra("PIC_PATH");
            ImageView iv = findViewById(R.id.imageView2);
            iv.setImageURI(Uri.parse(picPath));
        }
    }
    //quando clicco il pulsante indietro torno indietro nel backstack
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}