package com.happy.tracku.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.happy.tracku.R;


public class TravelLogViewHolder extends RecyclerView.ViewHolder
{

    public TextView travelDateTV, travelTimeTV, addressTV;

    public TravelLogViewHolder(@NonNull View itemView)
    {
        super(itemView);

        travelDateTV = itemView.findViewById(R.id.travel_date);
        travelTimeTV = itemView.findViewById(R.id.travel_time);
        addressTV = itemView.findViewById(R.id.address);


    }
}