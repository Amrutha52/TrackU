package com.happy.tracku.adapters;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.happy.tracku.R;
import com.happy.tracku.gson.deliverypendinglist.DeliveryPending;
import com.happy.tracku.gson.deliverypendinglist.DeliveryPendingListJson;
import com.happy.tracku.gson.purchaseorderitemlist.PurchaseOrderItem;
import com.happy.tracku.models.PaymentType;
import com.happy.tracku.viewes.DeliveryProductDetailsActivity;
import com.happy.tracku.viewes.DeliveryStatusActivity;
import com.happy.tracku.viewholders.DeliveryProductStatusViewHolder;
import com.happy.tracku.viewholders.PurchaseOrderItemListViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class DeliveryProductStatusListAdapter extends RecyclerView.Adapter<DeliveryProductStatusViewHolder> implements View.OnClickListener
{
    Context context;
    List<DeliveryPending> deliveryPendingList;
    List<DeliveryPending> deliveryPendingFilterList;

    public DeliveryProductStatusListAdapter(DeliveryStatusActivity context, List<DeliveryPending> deliveryPendingList)
    {
        this.context = context;
        this.deliveryPendingList = deliveryPendingList;
        this.deliveryPendingFilterList = new ArrayList<>(deliveryPendingList);//deliveryPendingList;
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
        DeliveryPending deliveryPending = deliveryPendingFilterList.get(position);

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
        return deliveryPendingFilterList.size();
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

    public Filter getFilter()
    {

        return new Filter()
        {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence)
            {

                String charString = charSequence.toString();
                List<DeliveryPending> filteredList = new ArrayList<>();
                Log.e("Log", "deliveryPendingList" + deliveryPendingList);

                if (charString.isEmpty())
                {
                    Log.e("Log", "InsidecharString.isEmpty()");
                    //deliveryPendingFilterList = deliveryPendingList;
                    filteredList.addAll(deliveryPendingList);
                }else
                {




                    for(DeliveryPending row: deliveryPendingList)
                    {

                        // Corrected case-insensitive search logic
                        String vendorName = row.getVendorName().toLowerCase();
                        String searchString = charString.toString().toLowerCase();

                        if(vendorName.contains(searchString))

                        {

                            filteredList.add(row);
                            Log.e("Log", "filteredList" + filteredList);
                        }

                    }

                   // deliveryPendingFilterList = filteredList;

                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;//deliveryPendingFilterList;
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
