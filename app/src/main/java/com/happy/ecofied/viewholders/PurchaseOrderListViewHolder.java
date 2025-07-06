package com.happy.ecofied.viewholders;


import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.happy.ecofied.R;

public class PurchaseOrderListViewHolder extends RecyclerView.ViewHolder
{
    public TextView purchaseOrderNoTV, purchaseOrderDateTV, amountTV, vendorNameTV, viewTV;
    public PurchaseOrderListViewHolder(@NonNull View itemView)
    {
        super(itemView);
        purchaseOrderNoTV = itemView.findViewById(R.id.purchase_order_noTV);
        purchaseOrderDateTV = itemView.findViewById(R.id.purchase_order_dateTV);
        amountTV = itemView.findViewById(R.id.purchase_order_amountTV);
        vendorNameTV = itemView.findViewById(R.id.purchase_order_vendor_name);
        viewTV = itemView.findViewById(R.id.order_view_link);
    }
}
