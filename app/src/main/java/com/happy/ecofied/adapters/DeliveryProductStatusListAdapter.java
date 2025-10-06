package com.happy.ecofied.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.happy.ecofied.R;
import com.happy.ecofied.gson.deliverypendinglist.DeliveryPending;
import com.happy.ecofied.viewes.DeliveryProductDetailsActivity;
import com.happy.ecofied.viewes.DeliveryStatusActivity;
import com.happy.ecofied.viewholders.DeliveryProductStatusViewHolder;

import java.util.List;

public class DeliveryProductStatusListAdapter extends RecyclerView.Adapter<DeliveryProductStatusViewHolder> implements View.OnClickListener
{
    Context context;
    List<DeliveryPending> deliveryPendingList;

    public DeliveryProductStatusListAdapter(DeliveryStatusActivity context, List<DeliveryPending> deliveryPendingList)
    {
        this.context = context;
        this.deliveryPendingList = deliveryPendingList;
        Log.e("Log", "deliveryPendingList" + deliveryPendingList);
    }

    @NonNull
    @Override
    public DeliveryProductStatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_delivery_product_status, parent, false);
        return new DeliveryProductStatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeliveryProductStatusViewHolder holder, int position)
    {
        DeliveryPending deliveryPending = deliveryPendingList.get(position);

        holder.vendorNameTV.setText(deliveryPending.getVendorName());
        holder.dateTV.setText(deliveryPending.getSalesDate());
        holder.invoiceNumberTV.setText(deliveryPending.getInvoiceNumber());
        holder.amountTV.setText(deliveryPending.getGrandTotal().toString());

        holder.detailsTV.setTag(R.string.key_one,deliveryPending.getIdSalesHeader());
        holder.detailsTV.setOnClickListener(this);

    }


    @Override
    public int getItemCount()
    {
        return deliveryPendingList.size();
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.detailsTV:
            {
                int idSalesHeader = (int) view.getTag(R.string.key_one);
                Log.e("Log","idSalesHeader" + idSalesHeader);

                Intent intent = new Intent(context, DeliveryProductDetailsActivity.class);
                intent.putExtra("idSalesHeader", idSalesHeader);
                context.startActivity(intent);
            }
            break;
        }
    }
}

