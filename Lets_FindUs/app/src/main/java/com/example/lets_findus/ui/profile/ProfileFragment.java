package com.example.lets_findus.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.lets_findus.R;

public class ProfileFragment extends Fragment {


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        setHasOptionsMenu(true);

        CardView cardView = root.findViewById(R.id.profile_card_view);
        cardView.setBackgroundResource(R.drawable.card_bottom_corner);
        return root;
    }

}