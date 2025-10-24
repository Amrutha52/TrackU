package com.happy.tracku.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.happy.tracku.R;
import com.happy.tracku.gson.deliverypendinglist.DeliveryPending;
import com.happy.tracku.gson.photopunchhistoryjson.GetPunchHistoryDetail;
import com.happy.tracku.gson.updatedeliverystatus.DeliveryStatusUpdate;
import com.happy.tracku.viewholders.DeliveryReportViewHolder;
import com.happy.tracku.viewholders.PhotoPunchHistoryViewHolder;

import java.util.List;


public class DeliveryReportAdapters extends RecyclerView.Adapter<DeliveryReportViewHolder>
{

    List<DeliveryStatusUpdate> getDeliveryReportList;
    Context context;

    public DeliveryReportAdapters(Context context, List<DeliveryStatusUpdate> getDeliveryReportList)
    {
        this.context = context;
        this.getDeliveryReportList = getDeliveryReportList;
    }

    @NonNull
    @Override
    public DeliveryReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_delivery_report, parent, false);
        return new DeliveryReportViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull DeliveryReportViewHolder holder, int position)
    {
        DeliveryStatusUpdate deliveryStatusUpdate = getDeliveryReportList.get(position);

        holder.vendorNameTV.setText(deliveryStatusUpdate.getVendorName());
        holder.salesDateTV.setText(deliveryStatusUpdate.getSalesDate());
        holder.invoiceNumberTV.setText(deliveryStatusUpdate.getInvoiceNumber());
        if (deliveryStatusUpdate.getItemsCount() != null)
        {
            holder.itemsCountTV.setText(deliveryStatusUpdate.getItemsCount());
        }
        if (deliveryStatusUpdate.getDeliveredItemsCount() != null)
        {
            holder.deliveredItemsCountTV.setText(deliveryStatusUpdate.getDeliveredItemsCount());
        }

        holder.deliveryStatusTV.setText(deliveryStatusUpdate.getDeliveryStatus());
        holder.allocatedToTV.setText(deliveryStatusUpdate.getAllocatedTo());
    }

    @Override
    public int getItemCount()
    {
        return getDeliveryReportList.size()+1;
    }
}


