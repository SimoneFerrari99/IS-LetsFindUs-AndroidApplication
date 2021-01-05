package com.example.lets_findus.ui.profile;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.lets_findus.R;
import com.example.lets_findus.ui.ViewPictureActivity;
import com.example.lets_findus.utilities.Person;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import uk.co.onemandan.materialtextview.MaterialTextView;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    private Future<Person> profile;
    private String myProfileFilename = "myProfile";
    private ConstraintLayout obbligatory;
    private ConstraintLayout other;
    private View root;
    private String picPath;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Bundle formData = getArguments();
        root = inflater.inflate(R.layout.fragment_profile, container, false);

        try {
            FileInputStream fis = requireContext().openFileInput(myProfileFilename);
            profile = Person.loadPersonAsync(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        obbligatory = root.findViewById(R.id.obbligatory_data);
        other = root.findViewById(R.id.other_data);

        if(formData != null) {
            if(formData.containsKey("Nickname")){
                ((TextView)root.findViewById(R.id.nickname_card)).setText(formData.getString("Nickname"));
            }
            if(formData.containsKey("propicFilePath")){
                String propicPath = formData.getString("propicFilePath");
                ((CircularImageView)root.findViewById(R.id.circularImageView)).setImageURI(Uri.parse(propicPath));
                substituteProfilePicture(propicPath);
            }
            else {
                setProfilePicture(profile);
            }
            setFieldsValue(obbligatory, formData);
            setFieldsValue(other, formData);
        }
        else{
            fillFieldsValueOnLoad(obbligatory, profile);
            fillFieldsValueOnLoad(other, profile);
            setProfilePicture(profile);
        }

        FloatingActionButton fab = root.findViewById(R.id.floating_modify);
        fab.setOnClickListener(this);

        CardView cardView = root.findViewById(R.id.profile_card_view);
        cardView.setBackgroundResource(R.drawable.card_bottom_corner);

        final CircularImageView circularImageView = root.findViewById(R.id.circularImageView);
        circularImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), ViewPictureActivity.class);
                i.putExtra("PIC_PATH", picPath);
                View sharedView = circularImageView;
                String transitionName = getString(R.string.image_transition);

                ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(getActivity(), sharedView, transitionName);
                startActivity(i, transitionActivityOptions.toBundle());
            }
        });
        return root;
    }

    private void substituteProfilePicture(String newProfilePicturePath){
        try {
            Person myProfile = profile.get();
            if(myProfile.profilePath != null){
                File current = new File(Uri.parse(myProfile.profilePath).getPath());
                if(current.exists()){
                    current.delete();
                }
            }
            picPath = newProfilePicturePath;
            myProfile.profilePath = newProfilePicturePath;
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setProfilePicture(Future<Person> person){
        final Person myProfile;
        try {
            myProfile = profile.get();
            picPath = myProfile.profilePath;
            ((CircularImageView)root.findViewById(R.id.circularImageView)).setImageURI(Uri.parse(myProfile.profilePath));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void fillFieldsValueOnLoad(final ConstraintLayout container, final Future<Person> profile){
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.submit(new Runnable() {
            @Override
            public void run() {
                Handler mainThreadHandler = new Handler(Looper.getMainLooper());
                try {
                    final Person myProfile = profile.get();
                    final Map<String, String> profileDump = myProfile.dumpToString();

                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView)root.findViewById(R.id.nickname_card)).setText(myProfile.nickname);
                            for(int i = 0; i < container.getChildCount(); i++){
                                final View v = container.getChildAt(i);
                                if(v instanceof MaterialTextView){
                                    final String label = ((MaterialTextView) v).getLabelText().toString();
                                    if(profileDump.get(label) == null){
                                        v.setVisibility(View.GONE);
                                    }
                                    else{
                                        ((MaterialTextView) v).setContentText(profileDump.get(label), null);
                                    }
                                }
                            }
                        }
                    });


                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setFieldsValue(ConstraintLayout container, Bundle data){
        for(int i = 0; i < container.getChildCount(); i++){
            View v = container.getChildAt(i);
            if(v instanceof MaterialTextView){
                String label = ((MaterialTextView) v).getLabelText().toString();
                String value = data.getString(label);
                if(value.equals("")){
                    v.setVisibility(View.GONE);
                }
                else{
                    ((MaterialTextView) v).setContentText(value, null);
                }
                storeModifiedData(v.getId(), value);
            }
        }
        try {
            profile.get().storePersonAsync(requireContext().openFileOutput(myProfileFilename, Context.MODE_PRIVATE));
        } catch (ExecutionException | InterruptedException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void storeModifiedData(int fieldId, String value){
        try {
            Person myProfile = profile.get();
            if(value.equals("")){
                value = null;
            }
            switch (fieldId){
                case R.id.nickname_tv:
                    assert value != null;
                    myProfile.nickname = value;
                    break;
                case R.id.sex_tv:
                    assert value != null;
                    switch (value){
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
                case R.id.year_birth_tv:
                    assert value != null;
                    myProfile.yearOfBirth = Integer.parseInt(value);
                    break;
                case R.id.description_tv:
                    myProfile.description = value;
                    break;
                case R.id.facebook_tv:
                    myProfile.facebook = value;
                    break;
                case R.id.instagram_tv:
                    myProfile.instagram = value;
                    break;
                case R.id.linkedin_tv:
                    myProfile.linkedin = value;
                    break;
                case R.id.email_tv:
                    myProfile.email = value;
                    break;
                case R.id.phone_number_tv:
                    if(value == null) {
                        myProfile.phoneNumber = 0L;
                    }
                    else {
                        myProfile.phoneNumber = Long.parseLong(value);
                    }
                    break;
                case R.id.birth_date_tv:
                    if(value == null) {
                        myProfile.birthDate = null;
                    }
                    else {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ITALIAN);
                        myProfile.birthDate = sdf.parse(value);
                    }
                    break;
                case R.id.other_tv:
                    myProfile.other = value;
                    break;
            }
        } catch (ExecutionException | InterruptedException | ParseException e) {
            e.printStackTrace();
        }
    }

    private Bundle getFieldValueBundle(ConstraintLayout container, Bundle data){
        Bundle out;
        if(data == null){
            out = new Bundle();
        }
        else{
            out = new Bundle(data);
        }
        for(int i = 0; i < container.getChildCount(); i++){
            View v = container.getChildAt(i);
            if(v instanceof MaterialTextView){
                if(v.getVisibility() == View.VISIBLE) {
                    out.putString(((MaterialTextView) v).getLabelText().toString(), ((MaterialTextView) v).getContentText().toString());
                }
                else {
                    out.putString(((MaterialTextView) v).getLabelText().toString(), "");
                }
            }
        }
        return out;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getActivity(), EditProfileActivity.class);
        Bundle obbData = getFieldValueBundle(obbligatory, null);
        Bundle send = getFieldValueBundle(other, obbData);
        intent.putExtra("FIELD_VALUES", send);
        startActivity(intent);
    }
}