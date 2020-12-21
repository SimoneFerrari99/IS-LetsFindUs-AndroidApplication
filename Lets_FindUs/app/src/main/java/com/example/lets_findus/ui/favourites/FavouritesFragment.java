package com.example.lets_findus.ui.favourites;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
    static View.OnClickListener myOnClickListener;
    private static ArrayList<Meeting> data;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_favourites, container, false);

        myOnClickListener = new MyOnClickListener(root.getContext());

        recyclerView = root.findViewById(R.id.rec_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(root.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        data = new ArrayList<>();
        data.add(new Meeting(new Person("", "Gazz", Person.Sex.MALE, 1999), 0, 0, null));
        data.add(new Meeting(new Person("", "Scheggia", Person.Sex.MALE, 1999), 0, 0, null));
        data.add(new Meeting(new Person("", "Tulio", Person.Sex.MALE, 1999), 0, 0, null));
        data.add(new Meeting(new Person("", "Ciullia", Person.Sex.FEMALE, 1999), 0, 0, null));
        for (int i = 0; i < 10; i++){
            data.add(new Meeting(new Person("", "Toso", Person.Sex.MALE, 1999), 0, 0, null));
        }

        adapter = new FavAdapter(data);
        recyclerView.setAdapter(adapter);
        return root;
    }

    private static class MyOnClickListener implements View.OnClickListener {

        private final Context context;

        private MyOnClickListener(Context context) {
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            removeItem(v);
        }

        private void removeItem(View v) {
            int selectedItemPosition = recyclerView.getChildAdapterPosition(v);
            Toast.makeText(v.getContext(), "Ciao "+data.get(selectedItemPosition).data.nickname, Toast.LENGTH_SHORT).show();
        }
    }
}