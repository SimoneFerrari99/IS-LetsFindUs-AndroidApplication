package com.example.lets_findus.ui.profile;

import android.content.Intent;
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
import com.example.lets_findus.utilities.Person;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import uk.co.onemandan.materialtextview.MaterialTextView;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    private Future<Person> profile;
    private Person myProfile;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Bundle formData = getArguments();
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        try {
            FileInputStream fis = requireContext().openFileInput("myProfile");
            profile = Person.loadPersonAsync(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ConstraintLayout obbligatory = root.findViewById(R.id.obbligatory_data);
        ConstraintLayout other = root.findViewById(R.id.other_data);

        if(formData != null) {
            if(formData.containsKey("Nickname")){
                ((TextView)root.findViewById(R.id.nickname_card)).setText(formData.getString("Nickname"));
            }
            setFieldsValue(obbligatory, formData);
            setFieldsValue(other, formData);
        }
        else{
            fillFieldsValueOnLoad(obbligatory, profile);
            fillFieldsValueOnLoad(other, profile);
        }

        FloatingActionButton fab = root.findViewById(R.id.floating_modify);
        fab.setOnClickListener(this);

        CardView cardView = root.findViewById(R.id.profile_card_view);
        cardView.setBackgroundResource(R.drawable.card_bottom_corner);
        return root;
    }

    private void fillFieldsValueOnLoad(final ConstraintLayout container, final Future<Person> profile){
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.submit(new Runnable() {
            @Override
            public void run() {
                Handler mainThreadHandler = new Handler(Looper.getMainLooper());
                try {
                    while(!profile.isDone());
                    myProfile = profile.get();
                    final Map<String, String> profileDump = myProfile.dumpToString();

                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            for(int i = 0; i < container.getChildCount(); i++){
                                final View v = container.getChildAt(i);
                                if(v instanceof MaterialTextView){
                                    final String label = ((MaterialTextView) v).getLabelText().toString();
                                    if(profileDump.get(label).equals("null")){
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
                if(data.getString(label).equals("")){
                    v.setVisibility(View.GONE);
                }
                else{
                    ((MaterialTextView) v).setContentText(data.getString(label), null);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getActivity(), EditProfileActivity.class);
        startActivity(intent);
    }
}