package com.happy.tracku.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.happy.tracku.R;
import com.happy.tracku.gson.deliverypendinglist.DeliveryPending;
import com.happy.tracku.gson.photopunchhistoryjson.GetPunchHistoryDetail;
import com.happy.tracku.gson.updatedeliverystatus.DeliveryStatusUpdate;
import com.happy.tracku.viewholders.DeliveryReportViewHolder;
import com.happy.tracku.viewholders.PhotoPunchHistoryViewHolder;

import java.util.ArrayList;
import java.util.List;


public class DeliveryReportAdapters extends RecyclerView.Adapter<DeliveryReportViewHolder>
{

    List<DeliveryStatusUpdate> getDeliveryReportList;
    List<DeliveryStatusUpdate> getDeliveryReportFilterList;
    Context context;

    public DeliveryReportAdapters(Context context, List<DeliveryStatusUpdate> getDeliveryReportList)
    {
        this.context = context;
        this.getDeliveryReportList = getDeliveryReportList;
        this.getDeliveryReportFilterList = getDeliveryReportList;
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
        DeliveryStatusUpdate deliveryStatusUpdate = getDeliveryReportFilterList.get(position);

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
        return getDeliveryReportFilterList.size()+1;
    }

    public Filter getFilter()
    {

        return new Filter()
        {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence)
            {

                String charString = charSequence.toString();

                if (charString.isEmpty()) {
                    Log.e("Log", "InsidecharString.isEmpty()");
                    getDeliveryReportFilterList = getDeliveryReportList;
                }else
                {

                    List<DeliveryStatusUpdate> filteredList = new ArrayList<>();
                    Log.e("Log", "getDeliveryReportList" + getDeliveryReportList);


                    for(DeliveryStatusUpdate row: getDeliveryReportList)
                    {

                        if(row.getVendorName().toLowerCase().contains(charString.toLowerCase())||
                                row.getVendorName().toUpperCase().contains(charString.toUpperCase()))

                        {

                            filteredList.add(row);
                            Log.e("Log", "filteredList" + filteredList);
                        }

                    }

                    getDeliveryReportFilterList = filteredList;

                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = getDeliveryReportFilterList;
                Log.e("Log", "filterResults" + filterResults.toString());
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

                getDeliveryReportFilterList = (ArrayList<DeliveryStatusUpdate>)filterResults.values;
                notifyDataSetChanged();

            }
        };

    }
}


