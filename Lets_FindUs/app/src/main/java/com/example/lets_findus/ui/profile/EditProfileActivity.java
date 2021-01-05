package com.example.lets_findus.ui.profile;

import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.lets_findus.MainActivity;
import com.example.lets_findus.R;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.whiteelephant.monthpicker.MonthPickerDialog;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
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

    private CircularImageView image;

    private Menu menu;

    private boolean modifiedPhoto = false;

    private ActivityResultLauncher<Intent> takePhoto;
    private ActivityResultLauncher<Intent> pickPhoto;
    private String currentPhotoPath;

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

        image = findViewById(R.id.circularImageView);
        image.setOnClickListener(imageSelector);

        takePhoto =  registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK){
                    Uri uri = Uri.parse(currentPhotoPath);
                    launchUCrop(uri, uri);
                }
            }
        });

        pickPhoto =  registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK && result.getData() != null){
                    Uri sourceUri = result.getData().getData(); // 1
                    File file = null; // 2
                    try {
                        file = createImageFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Uri destinationUri = Uri.fromFile(file);  // 3
                    launchUCrop(sourceUri, destinationUri);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            image.setImageURI(resultUri);
            modifiedPhoto = true;
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
        }
    }

    private final View.OnClickListener imageSelector = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final CharSequence[] options = { "Fai una foto", "Scegli dalla galleria"};

            AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
            builder.setTitle("Scegli la tua foto profilo");

            builder.setItems(options, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int item) {
                    if (options[item].equals("Fai una foto")) {
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(getApplicationContext(),
                                    "com.example.android.fileprovider",
                                    photoFile);
                            takePhoto.launch(new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, photoURI));
                        }
                    } else {
                        pickPhoto.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI));
                    }
                }
            });
            builder.show();
        }
    };

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

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private void launchUCrop(Uri source, Uri destination){
        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        UCrop.of(source, destination)
                .withOptions(options)
                .withAspectRatio(1, 1)
                .start(this);
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
                startActivity(mIntent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                return true;
            case R.id.confirm:
                Bundle obbForm = getFormValues(obbligatory, null);
                Bundle data = getFormValues(other, obbForm);
                mIntent=new Intent(EditProfileActivity.this, MainActivity.class);
                mIntent.putExtra("IS_FROM_EDIT",true);
                mIntent.putExtra("FORM_DATA", data);
                if(modifiedPhoto){
                    Log.d("Modified photo", "ciao");
                    mIntent.putExtra("PROPIC_CHANGED", currentPhotoPath);
                }
                startActivity(mIntent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
