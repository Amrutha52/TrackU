package com.happy.tracku.adapters;

import static android.view.View.VISIBLE;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.happy.tracku.R;
import com.happy.tracku.db.DbHelper;
import com.happy.tracku.gson.login.ValidateLoginResponseEmployeeDatum;
import com.happy.tracku.gson.login.ValidateLoginResponseVehicle;
import com.happy.tracku.gson.purchaseorderitemlist.PurchaseOrderItem;
import com.happy.tracku.gson.stockoutpurchaseorderitemlist.StockOutPurchaseOrderItem;
import com.happy.tracku.models.stockoutitem.StockoutItemModel;
import com.happy.tracku.utils.Fns;
import com.happy.tracku.viewes.PurchaseOrderItemListActivity;
import com.happy.tracku.viewes.StockOutPurchaseOrderItemListActivity;
import com.happy.tracku.viewholders.PurchaseOrderItemListViewHolder;
import com.happy.tracku.viewholders.StockoutOrderItemListViewHolder;

import java.util.List;


public class StockoutPurchaseOrderItemListAdapter extends RecyclerView.Adapter<StockoutOrderItemListViewHolder> implements View.OnClickListener
{
    Context context;
    List<StockOutPurchaseOrderItem> stockOutPurchaseOrderItemList;
    DbHelper dbHelper;
    int employeeCode=0, idVehicle=0;
    private List<StockoutItemModel> itemList;
    public StockoutPurchaseOrderItemListAdapter(StockOutPurchaseOrderItemListActivity context, List<StockOutPurchaseOrderItem> stockOutPurchaseOrderItemList)
    {
        this.context = context;
        this.stockOutPurchaseOrderItemList = stockOutPurchaseOrderItemList;
        Log.e("Log", "stockOutPurchaseOrderItemList" + stockOutPurchaseOrderItemList);

        dbHelper = new DbHelper(context);

    }

    @NonNull
    @Override
    public StockoutOrderItemListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_stockout_order, parent, false);
        return new StockoutOrderItemListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StockoutOrderItemListViewHolder holder, int position)
    {
        StockOutPurchaseOrderItem purchaseOrderItem = stockOutPurchaseOrderItemList.get(position);



        holder.itemTV.setText(purchaseOrderItem.getItemName());
        holder.orderQtyTV.setText(purchaseOrderItem.getOrderQuantity().toString());

        holder.rateTV.setText(purchaseOrderItem.getTotalAmount().toString());


       // holder.acceptedQtyET.setText(String.valueOf(purchaseOrderItem.getOrderQuantity()));

        // 2. Clear old TextWatcher to prevent recycling bugs
        if (holder.qtyTextWatcher != null) {
            holder.orderQtyET.removeTextChangedListener(holder.qtyTextWatcher);
        }

        // 3. Set the initial text (Prefix with order quantity if empty)
        double currentAccepted = purchaseOrderItem.getStockOutQuantity();

        // If it's a new entry (0.0), you might want to show the order quantity as prefix
        holder.orderQtyET.setText(String.valueOf(currentAccepted > 0 ? currentAccepted : purchaseOrderItem.getOrderQuantity()));

        // 4. Color Logic Function
        Runnable applyColorLogic = () -> {
            try {
                String input = holder.orderQtyET.getText().toString();
                double accepted = input.isEmpty() ? 0.0 : Double.parseDouble(input);
                double order = purchaseOrderItem.getOrderQuantity();

                if (accepted == 0) {
                    holder.itemView.setBackgroundColor(Color.parseColor("#f0120b")); // RED
                } else if (accepted == order) {
                    holder.itemView.setBackgroundColor(Color.parseColor("#C8E6C9")); // GREEN
                } else if (accepted < order) {
                    holder.itemView.setBackgroundColor(Color.parseColor("#FFF9C4")); // YELLOW
                } else {
                    holder.itemView.setBackgroundColor(Color.WHITE); // Default
                }
            } catch (Exception e) {
                holder.itemView.setBackgroundColor(Color.WHITE);
            }
        };

        // Apply color immediately on bind
        applyColorLogic.run();

        // 5. Update color in real-time as user types
        holder.qtyTextWatcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyColorLogic.run();
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        };
        holder.orderQtyET.addTextChangedListener(holder.qtyTextWatcher);

        // 6. Handle Sub-row Visibility (Verified Quantity)
        // If already verified in DB/Model, show it
        if (purchaseOrderItem.getStockOutQuantity() > 0) {
            holder.verifiedQtyLL.setVisibility(View.VISIBLE);
            holder.verifiedQtyTV.setText(String.valueOf(purchaseOrderItem.getStockOutQuantity()));
        } else {
            holder.verifiedQtyLL.setVisibility(View.GONE);
        }

        holder.orderQtyOkButton.setTag(R.string.key_one,purchaseOrderItem);
        holder.orderQtyOkButton.setTag(R.string.key_two,holder.orderQtyET);
        holder.orderQtyOkButton.setTag(R.string.key_three, holder.verifiedQtyTV);
        holder.orderQtyOkButton.setTag(R.string.key_four, holder.verifiedQtyLL);
        holder.orderQtyOkButton.setTag(R.string.key_five, employeeCode);
        holder.orderQtyOkButton.setTag(R.string.key_six, idVehicle);
        holder.orderQtyOkButton.setOnClickListener(this);

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
                int employeeCode = (Integer) view.getTag(R.string.key_five);
                int idVehicle = (Integer) view.getTag(R.string.key_six);

                double acceptedQty = Double.parseDouble(acceptedQtyTextInput.getText().toString());
                Log.e("Log","acceptedQtyAdapter" + acceptedQty);

                verifiedQtyLL.setVisibility(VISIBLE);
                verifiedQuantityTV.setText(String.valueOf(acceptedQty));

                purchaseOrderItem.setStockOutQuantity(acceptedQty);


                    dbHelper.updateStockOutQuantity(purchaseOrderItem.getIdItem(), acceptedQty, employeeCode, idVehicle);
                    dbHelper.updateStockOutQuantityVerified(purchaseOrderItem.getIdItem());

                    Fns.neutralAlert("Alert", "The accepted quantity is marked as " + acceptedQty, context);




            }
            break;
        }
    }
}
