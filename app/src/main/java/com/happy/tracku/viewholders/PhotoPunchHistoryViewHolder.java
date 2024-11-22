package com.happy.tracku.viewholders;


import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.happy.tracku.R;


public class PhotoPunchHistoryViewHolder extends RecyclerView.ViewHolder
{

    public TextView punchDateTV, punchINTV;

    public PhotoPunchHistoryViewHolder(@NonNull View itemView)
    {
        super(itemView);

        punchDateTV = itemView.findViewById(R.id.punch_date);
        punchINTV = itemView.findViewById(R.id.in_time);


    }
}