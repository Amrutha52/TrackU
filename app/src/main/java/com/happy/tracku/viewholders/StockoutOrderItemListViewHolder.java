package com.happy.tracku.viewholders;

import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.happy.tracku.R;

public class StockoutOrderItemListViewHolder extends RecyclerView.ViewHolder
{
    public TextView itemTV, orderQtyTV, rackNoTV, rateTV, floorNoTV, verifiedQtyTV, unitTv;
    public TextInputEditText orderQtyET;
    public Button orderQtyOkButton;
    public LinearLayout verifiedQtyLL;
    public MaterialAutoCompleteTextView employeeCodeMATV, vehicleMATV;
    public TextWatcher qtyTextWatcher;
    public StockoutOrderItemListViewHolder(@NonNull View itemView)
    {
        super(itemView);
        itemTV = itemView.findViewById(R.id.itemTV);
        orderQtyTV = itemView.findViewById(R.id.orderQtyTV);
       // rackNoTV = itemView.findViewById(R.id.rackNoTV);
        rateTV = itemView.findViewById(R.id.rateTV);
       // floorNoTV = itemView.findViewById(R.id.floorNoTV);
        orderQtyET = itemView.findViewById(R.id.orderQtyET);
        orderQtyOkButton = itemView.findViewById(R.id.orderQtyOkButton);
        verifiedQtyTV = itemView.findViewById(R.id.verifiQtyTV);
        verifiedQtyLL = itemView.findViewById(R.id.verifiedQtyLL);
        employeeCodeMATV = itemView.findViewById(R.id.employeecodeMATV);
        vehicleMATV = itemView.findViewById(R.id.vehicleModelMATV);
        unitTv = itemView.findViewById(R.id.unitTV);


    }
}