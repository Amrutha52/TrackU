package com.happy.tracku.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.happy.tracku.R;

public class DeliveryReportViewHolder extends RecyclerView.ViewHolder
{
    public TextView vendorNameTV, salesDateTV, invoiceNumberTV, itemsCountTV;
    public TextView deliveredItemsCountTV, deliveryStatusTV, allocatedToTV;
    public DeliveryReportViewHolder(@NonNull View itemView)
    {
        super(itemView);
        vendorNameTV = itemView.findViewById(R.id.vendorNameTV);
        salesDateTV = itemView.findViewById(R.id.dateTV);
        invoiceNumberTV = itemView.findViewById(R.id.invoiceNumberTV);
        itemsCountTV = itemView.findViewById(R.id.itemsCountTV);
        deliveredItemsCountTV = itemView.findViewById(R.id.deliveredItemsCountTV);
        deliveryStatusTV = itemView.findViewById(R.id.deliveryStatusTV);
        allocatedToTV = itemView.findViewById(R.id.allocatedToTV);
    }
}
