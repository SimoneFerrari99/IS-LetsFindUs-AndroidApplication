package com.example.lets_findus;

import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScanResultAdapter extends RecyclerView.Adapter<ScanResultAdapter.ViewHolder> {

    private List<ScanResult> items;


    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView name;
        private final TextView mac;
        private final TextView rssi;

        public ViewHolder(View view) {
            super(view);
            this.name = view.findViewById(R.id.device_name);
            this.mac = view.findViewById(R.id.mac_address);
            this.rssi = view.findViewById(R.id.signal_strength);

        }

    }

    public ScanResultAdapter(List<ScanResult> dataSet) {
        items = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_scan_result, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        TextView name = viewHolder.name;
        TextView mac = viewHolder.mac;
        TextView rssi = viewHolder.rssi;

        name.setText("Device name: " + items.get(position).getDevice().getName());
        name.setText("MAC: " + items.get(position).getDevice().getAddress());
        name.setText("Device name: " + items.get(position).getRssi());

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return items.size();
    }
}


