package com.example.lets_findus.ui.first_boot;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.lets_findus.R;
import com.example.lets_findus.utilities.Person;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class InsertNonObbligatoryDataFragment extends Fragment {

    private String myProfileFilename = "myProfile";
    private Future<Person> profile;

    private MaterialDatePicker.Builder<Long> materialDateBuilder;
    private MaterialDatePicker<Long> materialDatePicker;

    private FloatingActionButton nextFab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_insert_non_obbligatory_data, container, false);

        try {
            FileInputStream fis = requireContext().openFileInput(myProfileFilename);
            profile = Person.loadPersonAsync(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        final TextInputEditText birthDate = (TextInputEditText) ((TextInputLayout)root.findViewById(R.id.birth_date_tv)).getEditText();
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

        birthDate.setInputType(InputType.TYPE_NULL);
        birthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDatePicker.show(getParentFragmentManager(), "MATERIAL_DATE_PICKER");
            }
        });
        birthDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    materialDatePicker.show(getParentFragmentManager(), "MATERIAL_DATE_PICKER");
                }
            }
        });

        final ConstraintLayout nonobbContainer = root.findViewById(R.id.other_data);

        nextFab = root.findViewById(R.id.nonobbligatory_data_next_fab);
        nextFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storeFormValues(nonobbContainer, profile);
                Navigation.findNavController(v).navigate(R.id.action_insertNonObbligatoryDataFragment_to_profileFragment);
            }
        });

        return root;
    }

    private void storeFormValues(ConstraintLayout container, Future<Person> person){
        try {
            Person myProfile = person.get();
            String value;
            for(int i = 0; i < container.getChildCount(); i++){
                View v = container.getChildAt(i);
                value = ((TextInputLayout) v).getEditText().getText().toString();
                switch (v.getId()){
                    case R.id.name_tv:
                        myProfile.name = value.compareTo("") == 0 ? null : value;
                        break;
                    case R.id.surname_tv:
                        myProfile.surname = value.compareTo("") == 0 ? null : value;
                        break;
                    case R.id.description_tv:
                        myProfile.description = value.compareTo("") == 0 ? null : value;
                        break;
                    case R.id.facebook_tv:
                        myProfile.facebook = value.compareTo("") == 0 ? null : value;
                        break;
                    case R.id.instagram_tv:
                        myProfile.instagram = value.compareTo("") == 0 ? null : value;
                        break;
                    case R.id.linkedin_tv:
                        myProfile.linkedin = value.compareTo("") == 0 ? null : value;
                        break;
                    case R.id.email_tv:
                        myProfile.email = value.compareTo("") == 0 ? null : value;
                        break;
                    case R.id.phone_number_tv:
                        myProfile.phoneNumber = value.compareTo("") == 0 ? 0L : Long.parseLong(value);
                        break;
                    case R.id.birth_date_tv:
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ITALIAN);
                        myProfile.birthDate = value.compareTo("") == 0 ? null : sdf.parse(value);
                        break;
                    case R.id.other_tv:
                        myProfile.other = value.compareTo("") == 0 ? null : value;
                        break;
                }
            }
            myProfile.storePersonAsync(getContext().openFileOutput(myProfileFilename, Context.MODE_PRIVATE));
        } catch (ExecutionException | InterruptedException | FileNotFoundException | ParseException e) {
            e.printStackTrace();
        }
    }
}