package com.happy.ecofied.adapters;

import static android.view.View.VISIBLE;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.happy.ecofied.R;
import com.happy.ecofied.db.DbHelper;
import com.happy.ecofied.gson.stockoutpurchaseorderitemlist.StockOutPurchaseOrderItem;
import com.happy.ecofied.utils.Fns;
import com.happy.ecofied.viewes.StockOutPurchaseOrderItemListActivity;
import com.happy.ecofied.viewholders.PurchaseOrderItemListViewHolder;

import java.util.List;


public class StockoutPurchaseOrderItemListAdapter extends RecyclerView.Adapter<PurchaseOrderItemListViewHolder> implements View.OnClickListener
{
    Context context;
    List<StockOutPurchaseOrderItem> stockOutPurchaseOrderItemList;
    DbHelper dbHelper;
    public StockoutPurchaseOrderItemListAdapter(StockOutPurchaseOrderItemListActivity context, List<StockOutPurchaseOrderItem> stockOutPurchaseOrderItemList)
    {
        this.context = context;
        this.stockOutPurchaseOrderItemList = stockOutPurchaseOrderItemList;
        Log.e("Log", "stockOutPurchaseOrderItemList" + stockOutPurchaseOrderItemList);

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
        StockOutPurchaseOrderItem purchaseOrderItem = stockOutPurchaseOrderItemList.get(position);

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
        return stockOutPurchaseOrderItemList.size();
    }


    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.acceptedQtyOkButton:
            {
                StockOutPurchaseOrderItem purchaseOrderItem = (StockOutPurchaseOrderItem) view.getTag(R.string.key_one);
                TextInputEditText acceptedQtyTextInput = (TextInputEditText)view.getTag(R.string.key_two);
                TextView verifiedQuantityTV = (TextView) view.getTag(R.string.key_three);
                LinearLayout verifiedQtyLL = (LinearLayout) view.getTag(R.string.key_four);

                double acceptedQty = Double.parseDouble(acceptedQtyTextInput.getText().toString());
                Log.e("Log","acceptedQtyAdapter" + acceptedQty);

                verifiedQtyLL.setVisibility(VISIBLE);
                verifiedQuantityTV.setText(String.valueOf(acceptedQty));

                purchaseOrderItem.setStockOutQuantity(acceptedQty);

              /*  if (purchaseOrderItem.getOrderQuantity() != acceptedQty)
                {
                    Fns.neutralAlert("Alert", "The accepted quantity is different from your order quantity.", context);
                }
                else
                {

               */
                dbHelper.updateStockOutQuantity(purchaseOrderItem.getIdItem(), acceptedQty);

                Fns.neutralAlert("Alert", "The accepted quantity is marked as " + acceptedQty, context);
                // }



            }
            break;
        }
    }
}
