package com.example.lets_findus.ui.first_boot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lets_findus.MainActivity;
import com.example.lets_findus.R;

import java.util.ArrayList;
import java.util.List;

public class ResponsibilityActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_responsibility);

        final List<CheckBox> checkBoxes = new ArrayList<>();
        checkBoxes.add((CheckBox)findViewById(R.id.checkBox));
        checkBoxes.add((CheckBox)findViewById(R.id.checkBox2));
        checkBoxes.add((CheckBox)findViewById(R.id.checkBox3));

        final Button end = findViewById(R.id.responsibility_end_button);
        end.setVisibility(View.GONE);
        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences pref = getSharedPreferences("com.example.lets_findus.FIRST_BOOT", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("FIRST_BOOT", 1);
                editor.apply();
                finish();
                startActivity(new Intent(ResponsibilityActivity.this, MainActivity.class));
            }
        });

        for(CheckBox c : checkBoxes){
            c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(allChecked(checkBoxes)){
                        end.setVisibility(View.VISIBLE);
                    }
                    else{
                        end.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    public boolean allChecked(List<CheckBox> l){
        boolean allChecked = true;
        for(CheckBox c : l){
            if(!c.isChecked()){
                allChecked = false;
            }
        }
        return allChecked;
    }
}