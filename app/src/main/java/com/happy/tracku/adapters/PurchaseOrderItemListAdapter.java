package com.happy.tracku.adapters;

import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.happy.tracku.R;
import com.happy.tracku.db.DbHelper;
import com.happy.tracku.gson.employeemasterdetails.EmployeeMasterDetail;
import com.happy.tracku.gson.purchaseorderitemlist.PurchaseOrderItem;
import com.happy.tracku.gson.purchaseorderlist.PurchaseOrder;
import com.happy.tracku.utils.Fns;
import com.happy.tracku.viewes.PurchaseOrderItemListActivity;
import com.happy.tracku.viewes.PurchaseOrderListActivity;
import com.happy.tracku.viewholders.PurchaseOrderItemListViewHolder;
import com.happy.tracku.viewholders.PurchaseOrderListViewHolder;

import java.util.ArrayList;
import java.util.List;




public class PurchaseOrderItemListAdapter extends RecyclerView.Adapter<PurchaseOrderItemListViewHolder> implements View.OnClickListener {
    Context context;
    List<PurchaseOrderItem> purchaseOrderItemList;
    List<EmployeeMasterDetail> employeeMasterDetailList;
    DbHelper dbHelper;
    String employeeCode;
    public PurchaseOrderItemListAdapter(PurchaseOrderItemListActivity context, List<PurchaseOrderItem> purchaseOrderItemList)
    {
        this.context = context;
        this.purchaseOrderItemList = purchaseOrderItemList;
        Log.e("Log", "purchaseOrderItemList" + purchaseOrderItemList);

        dbHelper = new DbHelper(context);

    }

    @NonNull
    @Override
    public PurchaseOrderItemListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_purchase_order_item, parent, false);
        return new PurchaseOrderItemListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PurchaseOrderItemListViewHolder holder, int position)
    {
        PurchaseOrderItem purchaseOrderItem = purchaseOrderItemList.get(position);

        employeeMasterDetailList = dbHelper.getEmployeeMaster();
        Log.e("Log", "employeeMasterDetailList" + employeeMasterDetailList);

        ArrayAdapter<EmployeeMasterDetail> adpterEmployeeMaster = new ArrayAdapter<EmployeeMasterDetail>(context, android.R.layout.simple_dropdown_item_1line, employeeMasterDetailList);
        holder.employeeCodeMATV.setAdapter(adpterEmployeeMaster);

        holder.employeeCodeMATV.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View arg0)
            {
                holder.employeeCodeMATV.showDropDown();
            }
        });

        holder.employeeCodeMATV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                employeeCode = employeeMasterDetailList.get(position).getEmployeeCode();
                Log.e("Log", "employeeCode" + employeeCode);

            }
        });

        holder.itemTV.setText(purchaseOrderItem.getItemName());
        holder.orderQtyTV.setText(purchaseOrderItem.getOrderQuantity().toString());
        holder.rackNoTV.setText(purchaseOrderItem.getRackNumber().toString());
        holder.rateTV.setText(purchaseOrderItem.getTotalAmount().toString());
        holder.floorNoTV.setText(purchaseOrderItem.getFloor().toString());

        holder.acceptedQtyET.setText(String.valueOf(purchaseOrderItem.getOrderQuantity()));

        holder.acceptedQtyOkButton.setTag(R.string.key_one,purchaseOrderItem);
        holder.acceptedQtyOkButton.setTag(R.string.key_two,holder.acceptedQtyET);
        holder.acceptedQtyOkButton.setTag(R.string.key_three, holder.verifiedQtyTV);
        holder.acceptedQtyOkButton.setTag(R.string.key_four, holder.verifiedQtyLL);
        holder.acceptedQtyOkButton.setOnClickListener(this);

    }

    @Override
    public int getItemCount()
    {
        return purchaseOrderItemList.size();
    }


    @Override
    public void onClick(View view)
    {
     switch (view.getId())
     {
         case R.id.acceptedQtyOkButton:
         {
             PurchaseOrderItem purchaseOrderItem = (PurchaseOrderItem) view.getTag(R.string.key_one);
             TextInputEditText acceptedQtyTextInput = (TextInputEditText)view.getTag(R.string.key_two);
             TextView verifiedQuantityTV = (TextView) view.getTag(R.string.key_three);
             LinearLayout verifiedQtyLL = (LinearLayout) view.getTag(R.string.key_four);

             double acceptedQty = Double.parseDouble(acceptedQtyTextInput.getText().toString());
             Log.e("Log","acceptedQtyAdapter" + acceptedQty);

             verifiedQtyLL.setVisibility(VISIBLE);
             verifiedQuantityTV.setText(String.valueOf(acceptedQty));

             purchaseOrderItem.setAcceptedQuantity(acceptedQty);


                 dbHelper.updateAcceptedQuantity(purchaseOrderItem.getIdItem(), acceptedQty);
                 dbHelper.updateAcceptedQuantityVerified(purchaseOrderItem.getIdItem());

                 Fns.neutralAlert("Alert", "The accepted quantity is marked as " + acceptedQty, context);


         }
         break;
     }
    }
}
