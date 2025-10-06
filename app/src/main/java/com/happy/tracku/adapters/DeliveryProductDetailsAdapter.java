package com.happy.tracku.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.happy.tracku.R;
import com.happy.tracku.gson.deliverypendinglist.DeliveryPending;
import com.happy.tracku.viewes.DeliveryProductDetailsActivity;
import com.happy.tracku.viewholders.DeliveryProductDetailsViewHolder;
import com.happy.tracku.viewholders.DeliveryProductStatusViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DeliveryProductDetailsAdapter extends RecyclerView.Adapter<DeliveryProductDetailsViewHolder>
{
    Context context;
    List<DeliveryPending> deliveryPendingList;
    List<DeliveryPending> deliveryPendingFilterList;
    public DeliveryProductDetailsAdapter(DeliveryProductDetailsActivity context, List<DeliveryPending> deliveryPendingList)
    {
        this.context = context;
        this.deliveryPendingList = deliveryPendingList;
        this.deliveryPendingFilterList = deliveryPendingList;
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

        DeliveryPending deliveryPending = deliveryPendingFilterList.get(position);

        holder.vendorNameTV.setText(deliveryPending.getVendorName());
        holder.dateTV.setText(deliveryPending.getSalesDate());
        holder.invoiceNumberTV.setText(deliveryPending.getInvoiceNumber());
        holder.amountTV.setText(deliveryPending.getGrandTotal().toString());
        holder.itemNameTV.setText(deliveryPending.getItemName());
        holder.quantityET.setText(deliveryPending.getQuantity().toString());
    }



    @Override
    public int getItemCount() {
        return deliveryPendingFilterList.size();
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
                    deliveryPendingFilterList = deliveryPendingList;
                }else
                {

                    List<DeliveryPending> filteredList = new ArrayList<>();
                    Log.e("Log", "deliveryPendingList" + deliveryPendingList);


                    for(DeliveryPending row: deliveryPendingList)
                    {

                        if(row.getVendorName().toLowerCase().contains(charString.toLowerCase())||
                                row.getInvoiceNumber().contains(charString.toLowerCase()))

                        {

                            filteredList.add(row);
                            Log.e("Log", "filteredList" + filteredList);
                        }

                    }

                    deliveryPendingFilterList = filteredList;

                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = deliveryPendingFilterList;
                Log.e("Log", "filterResults" + filterResults.toString());
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

                deliveryPendingFilterList = (ArrayList<DeliveryPending>)filterResults.values;
                notifyDataSetChanged();

            }
        };

    }
}
