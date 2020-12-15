package com.example.lets_findus.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.lets_findus.MainActivity;
import com.example.lets_findus.R;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Set;

public class EditProfileActivity extends AppCompatActivity {
    private ConstraintLayout obbligatory;
    private ConstraintLayout other;

    private String[] sex = {"Maschio", "Femmina", "Altro"};

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_mode_activity);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.sex_dropdown_menu_popup_item, sex);

        AutoCompleteTextView editTextFilledExposedDropdown = findViewById(R.id.sex_exposed_dropdown);
        editTextFilledExposedDropdown.setAdapter(adapter);

        obbligatory = findViewById(R.id.obbligatory_data);
        other = findViewById(R.id.other_data);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.edit_mode_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private Bundle getFormValues(ConstraintLayout container){
        Bundle out = new Bundle();
        for(int i = 0; i < container.getChildCount(); i++){
            View v = container.getChildAt(i);
            if(v instanceof TextInputLayout){
                System.out.println(i);
                Editable text = ((TextInputLayout) v).getEditText().getText();
                out.putString(((TextInputLayout) v).getHint().toString(), text.toString());
            }
        }

        Set<String> keySet = out.keySet();
        for (String key : keySet){
            System.out.println(key+": "+out.getString(key));
        }
        return out;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // This is the up button
            case android.R.id.home:
                Intent mIntent=new Intent(EditProfileActivity.this, MainActivity.class);
                mIntent.putExtra("IS_FROM_EDIT",true);
                startActivity(mIntent);
                return true;
            case R.id.confirm:
                Bundle data = getFormValues(obbligatory);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
