package com.example.lets_findus.ui.favourites;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.lets_findus.PersonProfileActivity;
import com.example.lets_findus.R;
import com.example.lets_findus.utilities.AppDatabase;
import com.example.lets_findus.utilities.MeetingDao;
import com.example.lets_findus.utilities.MeetingPerson;
import com.example.lets_findus.utilities.PersonDao;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class FavouritesFragment extends Fragment {

    private static RecyclerView.Adapter adapter; //l'adapter serve per popolare ogni riga
    private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView recyclerView;
    static View.OnClickListener myOnClickListener;
    private static List<MeetingPerson> favouriteMeetings;

    private static AppDatabase db;
    private static MeetingDao md;
    private static PersonDao pd;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_favourites, container, false);

        myOnClickListener = new MyOnClickListener(root.getContext());

        recyclerView = root.findViewById(R.id.rec_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(root.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        if(db == null){
            db = Room.databaseBuilder(getContext(), AppDatabase.class, "meeting_db").build();
            md = db.meetingDao();
            pd = db.personDao();
        }

        Futures.addCallback(md.getFavouriteMeetings(), new FutureCallback<List<MeetingPerson>>() {
            @Override
            public void onSuccess(@NullableDecl List<MeetingPerson> result) {
                favouriteMeetings = result;
                adapter = new FavAdapter(favouriteMeetings);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onFailure(Throwable t) {
                favouriteMeetings = new ArrayList<>();
                adapter = new FavAdapter(favouriteMeetings);
                recyclerView.setAdapter(adapter);
            }
        }, Executors.newSingleThreadExecutor());

        return root;
    }

    private class MyOnClickListener implements View.OnClickListener {

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
            //Toast.makeText(v.getContext(), "Ciao "+data.get(selectedItemPosition).data.nickname, Toast.LENGTH_SHORT).show();
            Intent startPersonProfile = new Intent(getContext(), PersonProfileActivity.class);
            startPersonProfile.putExtra("MEETING_ID", favouriteMeetings.get(selectedItemPosition).meeting.id);
            startActivity(startPersonProfile);
        }
    }
}