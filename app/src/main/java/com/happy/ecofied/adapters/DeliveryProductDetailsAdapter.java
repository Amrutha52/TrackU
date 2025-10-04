package com.happy.ecofied.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.happy.ecofied.R;
import com.happy.ecofied.gson.deliverypendinglist.DeliveryPending;
import com.happy.ecofied.viewes.DeliveryProductDetailsActivity;
import com.happy.ecofied.viewholders.DeliveryProductDetailsViewHolder;

import java.util.List;

public class DeliveryProductDetailsAdapter extends RecyclerView.Adapter<DeliveryProductDetailsViewHolder>
{
    Context context;
    List<DeliveryPending> deliveryPendingList;
    public DeliveryProductDetailsAdapter(DeliveryProductDetailsActivity context, List<DeliveryPending> deliveryPendingList)
    {
        this.context = context;
        this.deliveryPendingList = deliveryPendingList;
        Log.e("Log", "deliveryPendingList" + deliveryPendingList);
    }

    @NonNull
    @Override
    public DeliveryProductDetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_delivery_product_details, parent, false);
        return new DeliveryProductDetailsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeliveryProductDetailsViewHolder holder, int position)
    {

        DeliveryPending deliveryPending = deliveryPendingList.get(position);

        holder.vendorNameTV.setText(deliveryPending.getVendorName());
        holder.dateTV.setText(deliveryPending.getSalesDate());
        holder.amountTV.setText(deliveryPending.getGrandTotal().toString());
        holder.itemNameTV.setText(deliveryPending.getItemName());
        holder.quantityET.setText(deliveryPending.getQuantity().toString());
    }



    @Override
    public int getItemCount() {
        return deliveryPendingList.size();
    }


}
