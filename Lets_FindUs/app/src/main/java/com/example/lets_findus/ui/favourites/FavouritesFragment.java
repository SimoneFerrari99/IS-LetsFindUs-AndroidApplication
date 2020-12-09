package com.example.lets_findus.ui.favourites;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lets_findus.R;
import com.example.lets_findus.utilities.Meeting;
import com.example.lets_findus.utilities.Person;

import java.util.ArrayList;

public class FavouritesFragment extends Fragment {

    private static RecyclerView.Adapter adapter; //l'adapter serve per popolare ogni riga
    private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView recyclerView;
    private static ArrayList<Meeting<Person>> data;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_favourites, container, false);

        recyclerView = root.findViewById(R.id.rec_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(root.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        data = new ArrayList<>();
        data.add(new Meeting<Person>(new Person("", "Gazz", Person.Sex.MALE, 1999), null));
        data.add(new Meeting<Person>(new Person("", "Scheggia", Person.Sex.MALE, 1999), null));
        data.add(new Meeting<Person>(new Person("", "Tulio", Person.Sex.MALE, 1999), null));
        data.add(new Meeting<Person>(new Person("", "Ciullia", Person.Sex.FEMALE, 1999), null));
        for (int i = 0; i < 10; i++){
            data.add(new Meeting<Person>(new Person("", "Toso", Person.Sex.MALE, 1999), null));
        }

        adapter = new FavAdapter(data);
        recyclerView.setAdapter(adapter);
        return root;
    }
}