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
import com.happy.tracku.gson.stockoutorderlist.StockOutPurchaseOrder;
import com.happy.tracku.viewes.StockOutOrderListActivity;
import com.happy.tracku.viewes.StockOutPurchaseOrderItemListActivity;
import com.happy.tracku.viewholders.PurchaseOrderListViewHolder;

import java.util.ArrayList;
import java.util.List;

public class StockoutPurchaseOrderListAdapter extends RecyclerView.Adapter<PurchaseOrderListViewHolder> implements Filterable, View.OnClickListener {
    Context context;
    List<StockOutPurchaseOrder> stockOutPurchaseOrderList;
    List<StockOutPurchaseOrder> stockOutPurchaseOrderFilterList;
    int companyValue;

    public StockoutPurchaseOrderListAdapter(StockOutOrderListActivity context, List<StockOutPurchaseOrder> stockOutPurchaseOrderList, int companyValue)
    {
        this.context = context;
        this.stockOutPurchaseOrderList = stockOutPurchaseOrderList;
        this.stockOutPurchaseOrderFilterList = new ArrayList<>(stockOutPurchaseOrderList);//stockOutPurchaseOrderList;
        Log.e("Log", "stockOutPurchaseOrderList" + stockOutPurchaseOrderList);
        this.companyValue = companyValue;


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
            StockOutPurchaseOrder purchaseOrder = stockOutPurchaseOrderFilterList.get(position-1);
            Log.e("Log", "purchaseOrder" + purchaseOrder);

            holder.purchaseOrderNoTV.setText(purchaseOrder.getPurchaseOrderNumber().toString());
            holder.purchaseOrderDateTV.setText(purchaseOrder.getPurchaseOrderDate());
            holder.amountTV.setText(purchaseOrder.getTotalAmount().toString());
            holder.vendorNameTV.setText(purchaseOrder.getVendorName());

            holder.viewTV.setOnClickListener(this);
            holder.viewTV.setTag(R.string.key_one, purchaseOrder.getIdSalesHeader());
            holder.viewTV.setTag(R.string.key_two, companyValue);


        }



    }

    @Override
    public int getItemCount()
    {
        return stockOutPurchaseOrderFilterList.size()+1;
    }

    public Filter getFilter()
    {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence)
            {

                String charString = charSequence.toString();

                List<StockOutPurchaseOrder> filteredList = new ArrayList<>();
                Log.e("Log", "filterlist" + stockOutPurchaseOrderList);

                if (charString.isEmpty())
                {
                    Log.e("Log", "InsidecharString.isEmpty()");
                   // stockOutPurchaseOrderFilterList = stockOutPurchaseOrderList;
                    // Correct: Use the full, original list when the search is empty
                    filteredList.addAll(stockOutPurchaseOrderList);
                }
                else
                {
                    // Loop through the original list (stockOutPurchaseOrderList)


                    for(StockOutPurchaseOrder row: stockOutPurchaseOrderList)
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

                   // stockOutPurchaseOrderList = filteredList;

                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;//stockOutPurchaseOrderFilterList;
                Log.e("Log", "filterResults" + filterResults.toString());
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults)
            {

                stockOutPurchaseOrderFilterList = (ArrayList<StockOutPurchaseOrder>)filterResults.values;
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

                int company = (int) view.getTag(R.string.key_two);


                Intent intent = new Intent(context, StockOutPurchaseOrderItemListActivity.class);
                intent.putExtra("idPurchaseOrder", idPurchaseOrder);
                intent.putExtra("company", companyValue);
                context.startActivity(intent);
            }
            break;
        }
    }
}

