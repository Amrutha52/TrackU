package com.happy.ecofied.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.happy.ecofied.R;

public class DeliveryProductStatusViewHolder extends RecyclerView.ViewHolder
{
    public TextView vendorNameTV, dateTV, amountTV, detailsTV, invoiceNumberTV;
    public DeliveryProductStatusViewHolder(@NonNull View itemView)
    {
        super(itemView);
        vendorNameTV = itemView.findViewById(R.id.vendorNameTV);
        dateTV = itemView.findViewById(R.id.dateTV);
        amountTV = itemView.findViewById(R.id.amountTV);
        detailsTV = itemView.findViewById(R.id.detailsTV);
        invoiceNumberTV = itemView.findViewById(R.id.invoiceNumberTV);
    }
}