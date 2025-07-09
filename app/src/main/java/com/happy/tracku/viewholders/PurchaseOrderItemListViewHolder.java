package com.happy.tracku.viewholders;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.happy.tracku.R;

public class PurchaseOrderItemListViewHolder extends RecyclerView.ViewHolder
{
    public TextView itemTV, orderQtyTV, rackNoTV, rateTV, floorNoTV, verifiedQtyTV;
    public TextInputEditText acceptedQtyET;
    public Button acceptedQtyOkButton;
    public LinearLayout verifiedQtyLL;
    public PurchaseOrderItemListViewHolder(@NonNull View itemView)
    {
        super(itemView);
        itemTV = itemView.findViewById(R.id.itemTV);
        orderQtyTV = itemView.findViewById(R.id.orderQtyTV);
        rackNoTV = itemView.findViewById(R.id.rackNoTV);
        rateTV = itemView.findViewById(R.id.rateTV);
        floorNoTV = itemView.findViewById(R.id.floorNoTV);
        acceptedQtyET = itemView.findViewById(R.id.acceptedQtyET);
        acceptedQtyOkButton = itemView.findViewById(R.id.acceptedQtyOkButton);
        verifiedQtyTV = itemView.findViewById(R.id.verifiQtyTV);
        verifiedQtyLL = itemView.findViewById(R.id.verifiedQtyLL);

    }
}
