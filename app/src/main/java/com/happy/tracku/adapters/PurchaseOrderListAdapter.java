package com.happy.tracku.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.happy.tracku.R;
import com.happy.tracku.gson.purchaseorderlist.PurchaseOrder;
import com.happy.tracku.viewes.PurchaseOrderItemListActivity;
import com.happy.tracku.viewes.PurchaseOrderListActivity;
import com.happy.tracku.viewholders.PurchaseOrderListViewHolder;

import java.util.ArrayList;
import java.util.List;



public class PurchaseOrderListAdapter extends RecyclerView.Adapter<PurchaseOrderListViewHolder> implements Filterable, View.OnClickListener {
    Context context;
    List<PurchaseOrder> purchaseOrderList;
    List<PurchaseOrder> purchaseOrderFilterList;
    int companyValue;

    public PurchaseOrderListAdapter(PurchaseOrderListActivity context, List<PurchaseOrder> purchaseOrderList, int companyValue)
    {
        this.context = context;
        this.purchaseOrderList = purchaseOrderList;
        this.purchaseOrderFilterList = new ArrayList<>(purchaseOrderList);//purchaseOrderList;
        this.companyValue = companyValue;
        Log.e("Log", "purchaseOrderList" + purchaseOrderList);


    }

    @NonNull
    @Override
    public PurchaseOrderListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_purchase_order_list, parent, false);
        return new PurchaseOrderListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PurchaseOrderListViewHolder holder, int position)
    {

        if (position == 0)
        {
            holder.purchaseOrderNoTV.setText("Order\nNo");
            holder.purchaseOrderNoTV.setTypeface(Typeface.DEFAULT_BOLD);
          //  holder.purchaseOrderNoTV.setBackgroundColor(ContextCompat.getColor(holder.purchaseOrderNoTV.getContext(), R.color.black));

            holder.purchaseOrderDateTV.setText("Order\nDate");
            holder.purchaseOrderDateTV.setTypeface(Typeface.DEFAULT_BOLD);
         //   holder.purchaseOrderDateTV.setBackgroundColor(ContextCompat.getColor(holder.purchaseOrderDateTV.getContext(), R.color.black));

            holder.amountTV.setText("Amount");
            holder.amountTV.setTypeface(Typeface.DEFAULT_BOLD);
         //   holder.amountTV.setBackgroundColor(ContextCompat.getColor(holder.amountTV.getContext(), R.color.black));

            holder.vendorNameTV.setText("Vendor\nName");
            holder.vendorNameTV.setTypeface(Typeface.DEFAULT_BOLD);
          //  holder.vendorNameTV.setBackgroundColor(ContextCompat.getColor(holder.vendorNameTV.getContext(), R.color.black));

            holder.viewTV.setText("View");
            holder.viewTV.setTypeface(Typeface.DEFAULT_BOLD);
           // holder.viewTV.setBackgroundColor(ContextCompat.getColor(holder.viewTV.getContext(), R.color.yellow));


        }
        else
        {
            PurchaseOrder purchaseOrder = purchaseOrderFilterList.get(position-1);
            Log.e("Log", "purchaseOrder" + purchaseOrder);

            holder.purchaseOrderNoTV.setText(purchaseOrder.getPurchaseOrderNumber().toString());
            holder.purchaseOrderDateTV.setText(purchaseOrder.getPurchaseOrderDate());
            holder.amountTV.setText(purchaseOrder.getTotalAmount().toString());
            holder.vendorNameTV.setText(purchaseOrder.getVendorName());

            holder.viewTV.setOnClickListener(this);
            holder.viewTV.setTag(R.string.key_one, purchaseOrder.getIdPurchaseOrderHeader());
            holder.viewTV.setTag(R.string.key_two, companyValue);

        }



    }

    @Override
    public int getItemCount()
    {
        return purchaseOrderFilterList.size()+1;
    }

    public Filter getFilter() {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String charString = charSequence.toString();
                List<PurchaseOrder> filteredList = new ArrayList<>();
                Log.e("Log", "filterlist" + purchaseOrderList);

                if (charString.isEmpty())
                {
                    Log.e("Log", "InsidecharString.isEmpty()");
                    //  purchaseOrderFilterList = purchaseOrderList;
                    filteredList.addAll(purchaseOrderList);
                }
                else
                {




                    for(PurchaseOrder row: purchaseOrderList)
                    {

                        // Corrected case-insensitive search logic
                        String vendorName = row.getVendorName().toLowerCase();
                        String invoiceNo = row.getPurchaseOrderNumber().toLowerCase();
                        String searchString = charString.toString().toLowerCase();

                        if(vendorName.contains(searchString) || invoiceNo.contains(charString))
                        {

                            filteredList.add(row);
                            Log.e("Log", "filteredList" + filteredList);
                        }

                    }

                  //  purchaseOrderFilterList = filteredList;

                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;//purchaseOrderFilterList;
                Log.e("Log", "filterResults" + filterResults.toString());
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults)
            {

                purchaseOrderFilterList = (ArrayList<PurchaseOrder>)filterResults.values;
                notifyDataSetChanged();

            }
        };

    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.order_view_link:
            {
                Integer idPurchaseOrder = (Integer) view.getTag(R.string.key_one);
                Log.e("Log", "idPurchaseOrderAdapter" + idPurchaseOrder);

                Integer companyValue = (Integer) view.getTag(R.string.key_two);
                Log.e("Log", "companyValue" + companyValue);

                Intent intent = new Intent(context, PurchaseOrderItemListActivity.class);
                intent.putExtra("idPurchaseOrder", idPurchaseOrder);
                intent.putExtra("company", companyValue);
                context.startActivity(intent);

            }
            break;
        }
    }
}
