package com.example.lets_findus.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.lets_findus.MainActivity;
import com.example.lets_findus.R;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.whiteelephant.monthpicker.MonthPickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditProfileActivity extends AppCompatActivity {

    private ConstraintLayout obbligatory;
    private ConstraintLayout other;

    private MaterialDatePicker.Builder<Long> materialDateBuilder;
    private MaterialDatePicker<Long> materialDatePicker;

    private MonthPickerDialog.Builder materialYearBuilder;

    private Menu menu;

    private String[] sex = {"Maschio", "Femmina", "Altro"};

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_mode_activity);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.sex_dropdown_menu_popup_item, sex);

        AutoCompleteTextView editTextFilledExposedDropdown = findViewById(R.id.sex_exposed_dropdown);
        editTextFilledExposedDropdown.setAdapter(adapter);

        final TextInputEditText birthDate = (TextInputEditText) ((TextInputLayout)findViewById(R.id.birth_date_tv)).getEditText();
        final TextInputEditText yearBirth = (TextInputEditText) ((TextInputLayout)findViewById(R.id.year_birth_tv)).getEditText();

        materialDateBuilder = MaterialDatePicker.Builder.datePicker();
        materialDateBuilder.setTitleText("Scegli la tua data di nascita");
        materialDatePicker = materialDateBuilder.build();
        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
            @Override
            public void onPositiveButtonClick(Long selection) {
                SimpleDateFormat simpleFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ITALIAN);
                Date date = new Date(selection);
                birthDate.setText(simpleFormat.format(date));
            }
        });

        Calendar today = Calendar.getInstance();
        materialYearBuilder = new MonthPickerDialog.Builder(this, new MonthPickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(int selectedMonth, int selectedYear) {
                yearBirth.setText(String.valueOf(selectedYear));
            }
        },  today.get(Calendar.YEAR), today.get(Calendar.MONTH));
        final MonthPickerDialog yearPicker = materialYearBuilder.setMinYear(1950)
                .setMaxYear(today.get(Calendar.YEAR))
                .setTitle("Scegli il tuo anno di nascita")
                .showYearOnly().build();

        yearBirth.setInputType(InputType.TYPE_NULL);
        yearBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                yearPicker.show();
            }
        });
        yearBirth.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    yearPicker.show();
                }
            }
        });

        birthDate.setInputType(InputType.TYPE_NULL);
        birthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDatePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
            }
        });
        birthDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    materialDatePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
                }
            }
        });

        obbligatory = findViewById(R.id.obbligatory_data);
        other = findViewById(R.id.other_data);

        fillFormValue(obbligatory, getIntent().getBundleExtra("FIELD_VALUES"));
        fillFormValue(other, getIntent().getBundleExtra("FIELD_VALUES"));

        setObbligatoryFieldsError(obbligatory);
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        this.menu = menu;
        inflater.inflate(R.menu.edit_mode_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private Bundle getFormValues(ConstraintLayout container, Bundle attachBundle){
        Bundle out;
        if(attachBundle == null){
            out = new Bundle();
        }
        else{
            out = new Bundle(attachBundle);
        }
        for(int i = 0; i < container.getChildCount(); i++){
            View v = container.getChildAt(i);
            if(v instanceof TextInputLayout){
                Editable text = ((TextInputLayout) v).getEditText().getText();
                out.putString(((TextInputLayout) v).getHint().toString(), text.toString());
            }
        }
        return out;
    }


    private void fillFormValue(ConstraintLayout container, Bundle data){
        ((TextView)findViewById(R.id.nickname_card)).setText(data.getString("Nickname"));
        for(int i = 0; i < container.getChildCount(); i++){
            View v = container.getChildAt(i);
            if(v instanceof TextInputLayout){
                if (((TextInputLayout) v).getEditText() instanceof TextInputEditText) {
                    final TextInputEditText te = (TextInputEditText) ((TextInputLayout) v).getEditText();
                    switch (v.getId()) {
                        case R.id.facebook_tv:
                        case R.id.instagram_tv:
                        case R.id.linkedin_tv:
                            te.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                @Override
                                public void onFocusChange(View v, boolean hasFocus) {
                                    if (hasFocus) {
                                        te.setHint(R.string.social_hint);
                                    } else {
                                        te.setHint("");
                                    }
                                }
                            });
                            break;
                        case R.id.email_tv:
                            te.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                @Override
                                public void onFocusChange(View v, boolean hasFocus) {
                                    if (hasFocus) {
                                        te.setHint(R.string.mail_hint);
                                    } else {
                                        te.setHint("");
                                    }
                                }
                            });
                    }
                }
                String label = ((TextInputLayout) v).getHint().toString();
                if(!data.getString(label).equals("")){
                    String value = data.getString(label);
                    if(((TextInputLayout) v).getEditText() instanceof AutoCompleteTextView){
                        AutoCompleteTextView al = (AutoCompleteTextView)((TextInputLayout) v).getEditText();
                        al.setText(value, false);
                    }
                    else {
                        ((TextInputLayout) v).getEditText().setText(value);
                    }
                }
            }
        }
    }

    private void setObbligatoryFieldsError(ConstraintLayout container){
        for(int i = 0; i < container.getChildCount(); i++){
            final View v = container.getChildAt(i);
            if(v instanceof TextInputLayout && ((TextInputLayout) v).getEditText() instanceof TextInputEditText){
                final TextInputEditText te = (TextInputEditText) ((TextInputLayout) v).getEditText();
                te.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if(count == 0){
                            menu.findItem(R.id.confirm).setEnabled(false).setVisible(false);
                            ((TextInputLayout) v).setErrorEnabled(true);
                            ((TextInputLayout) v).setError("Questo campo non può essere vuoto");
                        }
                        else{
                            menu.findItem(R.id.confirm).setEnabled(true).setVisible(true);
                            ((TextInputLayout) v).setError(null);
                            ((TextInputLayout) v).setErrorEnabled(false);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent mIntent;
        switch (item.getItemId()) {
            // This is the up button
            case android.R.id.home:
                mIntent=new Intent(EditProfileActivity.this, MainActivity.class);
                mIntent.putExtra("IS_FROM_EDIT",true);
                startActivity(mIntent);
                return true;
            case R.id.confirm:
                Bundle obbForm = getFormValues(obbligatory, null);
                Bundle data = getFormValues(other, obbForm);
                mIntent=new Intent(EditProfileActivity.this, MainActivity.class);
                mIntent.putExtra("IS_FROM_EDIT",true);
                mIntent.putExtra("FORM_DATA", data);
                startActivity(mIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
