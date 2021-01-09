package com.example.lets_findus.ui.filter;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lets_findus.R;

public class Filter extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    String[] country = {"India", "USA", "China", "Japan", "Other"};
    /*String[] date = {"Oggi", "Ieri"};
    String[] hour = {"12:30", "16:30"};
    String[] sex = {"Maschio", "Femmina", "Altro"};
    String[] age = {"18", "30", "50", "70"};*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_popup_window);
        //Getting the instance of Spinner and applying OnItemSelectedListener on it
        Spinner spin = (Spinner) findViewById(R.id.spinner_date);
        Spinner spin_date = (Spinner) findViewById(R.id.spinner_date);
        Spinner spin_hour = (Spinner) findViewById(R.id.spinner_hour);
        Spinner spin_sex = (Spinner) findViewById(R.id.spinner_sex);
        Spinner spin_age = (Spinner) findViewById(R.id.spinner_age);

        spin.setOnItemSelectedListener(this);

        //Creating the ArrayAdapter instance having the country list
        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, country);
        /*ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, date);
        ArrayAdapter bb = new ArrayAdapter(this, android.R.layout.simple_spinner_item, hour);
        ArrayAdapter cc = new ArrayAdapter(this, android.R.layout.simple_spinner_item, sex);
        ArrayAdapter dd = new ArrayAdapter(this, android.R.layout.simple_spinner_item, age);*/
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        /*aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bb.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);*/

        //Setting the ArrayAdapter data on the Spinner
        spin.setAdapter(aa);
        /*spin_date.setAdapter((aa));
        spin_hour.setAdapter((bb));
        spin_sex.setAdapter(cc);
        spin_age.setAdapter(dd);*/

    }

    //Performing action onItemSelected and onNothing selected
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
        Toast.makeText(getApplicationContext(), country[position], Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }
}
