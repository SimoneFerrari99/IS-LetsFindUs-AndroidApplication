package com.example.lets_findus.ui.first_boot;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.lets_findus.R;
import com.example.lets_findus.utilities.Person;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.whiteelephant.monthpicker.MonthPickerDialog;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
//sezione dell'inserimento dei dati obbligatori
public class InsertObbligatoryDataFragment extends Fragment {

    private final String myProfileFilename = "myProfile";
    private Future<Person> profile;

    private String[] sex = {"Maschio", "Femmina", "Altro"};
    private AutoCompleteTextView editTextFilledExposedDropdown;

    private FloatingActionButton nextFab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_insert_obbligatory_data, container, false);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.sex_dropdown_menu_popup_item, sex); //adapter per il menù a tendina con le varie opzioni per il sesso

        editTextFilledExposedDropdown = root.findViewById(R.id.creation_sex_exposed_dropdown);
        editTextFilledExposedDropdown.setAdapter(adapter);
        editTextFilledExposedDropdown.setText(getString(R.string.maschio), false); //setto il valore di default a maschio

        //inizializzo il picker per l'anno
        final TextInputEditText yearBirth = (TextInputEditText) ((TextInputLayout)root.findViewById(R.id.creation_year_birth_tv)).getEditText();
        Calendar today = Calendar.getInstance();
        MonthPickerDialog.Builder materialYearBuilder = new MonthPickerDialog.Builder(getContext(), new MonthPickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(int selectedMonth, int selectedYear) {
                yearBirth.setText(String.valueOf(selectedYear));
            }
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH));
        final MonthPickerDialog yearPicker = materialYearBuilder.setMinYear(1950)
                .setMaxYear(today.get(Calendar.YEAR))
                .setTitle("Scegli il tuo anno di nascita")
                .showYearOnly().build();

        yearBirth.setInputType(InputType.TYPE_NULL);
        yearBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                View view = getActivity().getCurrentFocus();
                if (view == null) {
                    view = new View(getActivity());
                }
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                yearPicker.show();
            }
        });
        yearBirth.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    View view = getActivity().getCurrentFocus();
                    if (view == null) {
                        view = new View(getActivity());
                    }
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    yearPicker.show();
                }
            }
        });

        try {
            //mi prendo il mio profilo
            FileInputStream fis = requireContext().openFileInput(myProfileFilename);
            profile = Person.loadPersonAsync(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        final ConstraintLayout obbContainer = root.findViewById(R.id.obb_container);

        //quando clicco sul bottone avanti salvo i dati e vado avanti
        nextFab = root.findViewById(R.id.obbligatory_data_next_fab);
        nextFab.hide();
        nextFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storeFormValues(obbContainer, profile);
                Navigation.findNavController(v).navigate(R.id.action_insertObbligatoryDataFragment_to_insertNonObbligatoryDataFragment);
            }
        });

        setObbligatoryFieldsError(obbContainer);

        return root;
    }
    //controlla se ho campi vuoti
    private boolean checkEmptyFields(ConstraintLayout container){
        boolean empty = false;
        for(int i = 0; i < container.getChildCount() && !empty; i++){
            final View v = container.getChildAt(i);
            if(v instanceof TextInputLayout && ((TextInputLayout) v).getEditText() instanceof TextInputEditText){
                final TextInputEditText te = (TextInputEditText) ((TextInputLayout) v).getEditText();
                if(te.getText().toString().compareTo("") == 0){
                    empty = true;
                }
            }
        }
        return empty;
    }
    //salvo i valori contenuti nei campi nell'apposito campo del profilo
    private void storeFormValues(ConstraintLayout container, Future<Person> person){
        try {
            Person myProfile = person.get();
            for(int i = 0; i < container.getChildCount(); i++){
                View v = container.getChildAt(i);
                switch (v.getId()){
                    case R.id.creation_nickname_tv:
                        myProfile.nickname = ((TextInputLayout) v).getEditText().getText().toString();
                        break;
                    case R.id.creation_sex_tv:
                        switch (editTextFilledExposedDropdown.getText().toString()){
                            case "Maschio":
                                myProfile.sex = Person.Sex.MALE;
                                break;
                            case "Femmina":
                                myProfile.sex = Person.Sex.FEMALE;
                                break;
                            case "Altro":
                                myProfile.sex = Person.Sex.OTHER;
                                break;
                        }
                        break;
                    case R.id.creation_year_birth_tv:
                        myProfile.yearOfBirth = Integer.valueOf(((TextInputLayout) v).getEditText().getText().toString());
                        break;
                }
            }
            myProfile.storePersonAsync(getContext().openFileOutput(myProfileFilename, Context.MODE_PRIVATE));
        } catch (ExecutionException | InterruptedException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    //funzione che setta un errore qualora uno dei campi obbligatori fosse vuoto
    private void setObbligatoryFieldsError(final ConstraintLayout container){
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
                            nextFab.hide();
                            ((TextInputLayout) v).setErrorEnabled(true);
                            ((TextInputLayout) v).setError("Questo campo non può essere vuoto");
                        }
                        else{
                            ((TextInputLayout) v).setError(null);
                            ((TextInputLayout) v).setErrorEnabled(false);
                            if(!checkEmptyFields(container)){
                                nextFab.show();
                            }
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
            }
        }
    }
}