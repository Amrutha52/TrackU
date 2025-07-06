package com.happy.ecofied.viewholders;


import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.happy.ecofied.R;


public class PhotoPunchHistoryViewHolder extends RecyclerView.ViewHolder
{

    public TextView punchDateTV, punchINTV, employeeCodeTV;

    public PhotoPunchHistoryViewHolder(@NonNull View itemView)
    {
        super(itemView);

        punchDateTV = itemView.findViewById(R.id.punch_date);
        punchINTV = itemView.findViewById(R.id.in_time);
        employeeCodeTV = itemView.findViewById(R.id.employeeCode);
    }
}