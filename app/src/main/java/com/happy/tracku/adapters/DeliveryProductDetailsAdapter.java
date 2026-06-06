package com.happy.tracku.adapters;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.happy.tracku.R;
import com.happy.tracku.db.DbHelper;
import com.happy.tracku.gson.deliverypendinglist.DeliveryPending;
import com.happy.tracku.models.PaymentType;
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
    DbHelper dbHelper;
    public DeliveryProductDetailsAdapter(DeliveryProductDetailsActivity context, List<DeliveryPending> deliveryPendingList)
    {
        this.context = context;
        this.deliveryPendingList = deliveryPendingList;
        this.deliveryPendingFilterList = new ArrayList<>(deliveryPendingList);//deliveryPendingList;
        Log.e("Log", "deliveryPendingList" + deliveryPendingList);

        dbHelper = new DbHelper(context);
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

//        if (deliveryPending.getIsCashSale() == true)
//        {
//            holder.paymentTypeLL.setVisibility(VISIBLE);
//        }
//        else
//        {
//            holder.paymentTypeLL.setVisibility(INVISIBLE);
//        }


        List<PaymentType> paymentTypes = new ArrayList<>();
        paymentTypes.add(new PaymentType("Select", 0));
        paymentTypes.add(new PaymentType("Cash", 1));
        paymentTypes.add(new PaymentType("Credit", 2));

        ArrayAdapter<PaymentType> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_item,
                paymentTypes);

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        holder.paymentTypeSpinner.setAdapter(adapter);

        holder.paymentTypeSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view,
                                               int position,
                                               long id) {

                        PaymentType selectedPaymentType =
                                (PaymentType) parent.getItemAtPosition(position);


                        int paymentId = selectedPaymentType.getValue();

                        // Do something with the selected value
                        Log.d("Spinner",
                                ", ID: " + paymentId);


                        dbHelper.updateDeliveryPaymentType(deliveryPending.getIdSalesHeader(), paymentId);
                         deliveryPending.setPaymentTypeId(paymentId);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

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
                List<DeliveryPending> filteredList = new ArrayList<>();
                Log.e("Log", "deliveryPendingList" + deliveryPendingList);

                if (charString.isEmpty())
                {
                    Log.e("Log", "InsidecharString.isEmpty()");
                    filteredList.addAll(deliveryPendingList);
                   // deliveryPendingFilterList = deliveryPendingList;
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
