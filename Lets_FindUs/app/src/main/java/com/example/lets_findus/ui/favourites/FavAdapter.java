package com.example.lets_findus.ui.favourites;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lets_findus.R;
import com.example.lets_findus.utilities.Meeting;
import com.example.lets_findus.utilities.Person;

import java.util.ArrayList;

public class FavAdapter extends RecyclerView.Adapter<FavAdapter.MyViewHolder>{

    private ArrayList<Meeting<Person>> dataSet;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName;
        TextView textViewVersion;
        ImageView imageViewIcon;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.textViewName = itemView.findViewById(R.id.textViewName);
            this.textViewVersion = itemView.findViewById(R.id.textViewVersion);
            this.imageViewIcon = itemView.findViewById(R.id.imageView);
        }
    }

    public FavAdapter(ArrayList<Meeting<Person>> data) {
        this.dataSet = data;
    }

    @NonNull
    @Override
    public FavAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cards_layout, parent, false);

        view.setOnClickListener(FavouritesFragment.myOnClickListener);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavAdapter.MyViewHolder holder, int position) {
        TextView textViewName = holder.textViewName;
        TextView textViewVersion = holder.textViewVersion;
        ImageView imageView = holder.imageViewIcon;
        textViewName.setText("Nickname: "+dataSet.get(position).data.nickname);
        textViewVersion.setText("Anno di nascita: "+String.valueOf(dataSet.get(position).data.yearOfBirth));
        //imageView.setImageResource(dataSet.get(position).getImage());
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
