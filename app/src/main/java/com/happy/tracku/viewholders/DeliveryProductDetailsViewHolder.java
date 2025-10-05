package com.happy.tracku.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.happy.tracku.R;

public class DeliveryProductDetailsViewHolder extends RecyclerView.ViewHolder
{
    public TextView vendorNameTV, dateTV, amountTV, itemNameTV, invoiceNumberTV;
    public TextInputEditText quantityET;
    public DeliveryProductDetailsViewHolder(@NonNull View itemView)
    {
        super(itemView);
        vendorNameTV = itemView.findViewById(R.id.vendorNameTV);
        dateTV = itemView.findViewById(R.id.dateTV);
        amountTV = itemView.findViewById(R.id.amountTV);
        itemNameTV = itemView.findViewById(R.id.itemNameTV);
        quantityET = itemView.findViewById(R.id.quantityET);
        invoiceNumberTV = itemView.findViewById(R.id.invoiceNumberTV);
    }
}
