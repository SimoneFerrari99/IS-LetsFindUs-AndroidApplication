package com.example.lets_findus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lets_findus.client.ClientActivity;
import com.example.lets_findus.server.ServerActivity;

public class MainActivity extends AppCompatActivity {

    private Button client;
    private Button server;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = findViewById(R.id.client);
        server = findViewById(R.id.server);
        client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ClientActivity.class);
                startActivity(i);
            }
        });
        server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ServerActivity.class);
                startActivity(i);
            }
        });
    }
}