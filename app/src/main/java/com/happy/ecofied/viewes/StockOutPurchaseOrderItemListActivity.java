package com.happy.ecofied.viewes;

import static com.happy.ecofied.utils.Const.URL_STOCKOUT_PURCHASE_ORDER_ITEM_LIST;
import static com.happy.ecofied.utils.Const.URL_STOCKOUT_SEND_PURCHASE_REQUEST;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.happy.ecofied.R;
import com.happy.ecofied.adapters.StockoutPurchaseOrderItemListAdapter;
import com.happy.ecofied.databinding.ActivityStockOutPurchaseOrderItemListBinding;
import com.happy.ecofied.db.DbHelper;
import com.happy.ecofied.gson.sendstockoutrequest.StockOutSendPurchaseRequestJson;
import com.happy.ecofied.gson.stockoutpurchaseorderitemlist.StockOutPurchaseOrderItemListJson;
import com.happy.ecofied.ssl.CustomTrust;
import com.happy.ecofied.utils.Const;
import com.happy.ecofied.utils.Fns;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StockOutPurchaseOrderItemListActivity extends AppCompatActivity
{
    private ActivityStockOutPurchaseOrderItemListBinding binding;
    Intent intent;
    Integer idPurchaseOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityStockOutPurchaseOrderItemListBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        EdgeToEdge.enable(this);

        View rootView = findViewById(android.R.id.content); // Get root view

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            WindowInsetsCompat insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets.toWindowInsets());
            int systemBarsInsetsTop = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int systemBarsInsetsBottom = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

            // Apply padding to your main content view
            v.setPadding(v.getPaddingLeft(), systemBarsInsetsTop, v.getPaddingRight(), systemBarsInsetsBottom);

            return WindowInsetsCompat.CONSUMED;
        });

        getSupportActionBar().setTitle("Purchase Order Item List");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        intent = getIntent();

        idPurchaseOrder = intent.getIntExtra("idPurchaseOrder",0);
        Log.e("Log", "idPurchaseOrderOrderItem" + idPurchaseOrder);

        new PullStockoutPurchaseOrderItemListDetails(this, idPurchaseOrder).execute();

    }

    public void listeners(View view)
    {
        switch (view.getId())
        {
            case R.id.complete_save_button:
            {
                new PushStockOutRequest(this, idPurchaseOrder).execute();

            }
            break;
        }
    }

    private static class PullStockoutPurchaseOrderItemListDetails extends AsyncTask<String, String, String>
    {
        ProgressDialog pd;
        WeakReference<StockOutPurchaseOrderItemListActivity> context;
        private OkHttpClient okHttpClient;
        private Request request;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String url;
        SharedPreferences shp;
        StockOutPurchaseOrderItemListJson stockOutPurchaseOrderItemListJson;
        int idPurchaseOrder;

        DbHelper dbHelper;

        public PullStockoutPurchaseOrderItemListDetails(StockOutPurchaseOrderItemListActivity context, int idPurchaseOrder)
        {
            this.context = new WeakReference<>(context);
            this.idPurchaseOrder = idPurchaseOrder;

            shp = context.getSharedPreferences(Const.Shared_Pref_name,MODE_PRIVATE);

            dbHelper = new DbHelper(context);

            CustomTrust customTrust = new CustomTrust(context);
            OkHttpClient client = customTrust.getClient();
            okHttpClient = client;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            pd = new ProgressDialog(context.get());
            pd.setTitle("Uploading Data");
            pd.setMessage("Please wait few seconds...");
            pd.setCancelable(false);
            //pd.setIndeterminate(true);
            pd.setMax(5);
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.show();

        }

        @Override
        protected String doInBackground(String... strings)
        {
            try
            {
                url = Const.USING_IP + URL_STOCKOUT_PURCHASE_ORDER_ITEM_LIST;
                Log.e("Log", "stockoutpurchaseOrderItemListURL" + url);

                JSONObject jsonObjectPurchaseOrderItemList = new JSONObject();

                jsonObjectPurchaseOrderItemList.put("createdBy", shp.getString(Const.Shp_Employee_Code, ""));
                jsonObjectPurchaseOrderItemList.put("idPurchaseOrder", idPurchaseOrder);

                Log.e("Log", "jsonObjectpurchaseOrderItemList" + jsonObjectPurchaseOrderItemList);

                RequestBody body = RequestBody.create(jsonObjectPurchaseOrderItemList.toString(), JSON);
                request = new Request.Builder()
                        //.header("X-Client-Type", "Android")
                        .url(url)
                        .post(body)
                        .build();

                Response response = okHttpClient.newCall(request).execute();

                if (!response.isSuccessful())
                {
                    return "failure";

                }

                String result = response.body().string();


                Log.e("Log", "purchaseOrderResult" + result);
                Gson gson = new Gson();
                stockOutPurchaseOrderItemListJson = gson.fromJson(result, StockOutPurchaseOrderItemListJson.class);

                if (stockOutPurchaseOrderItemListJson.getData().getStockOutPurchaseOrderItemList() == null || stockOutPurchaseOrderItemListJson.getData().getStockOutPurchaseOrderItemList().isEmpty() || stockOutPurchaseOrderItemListJson.getData().getStockOutPurchaseOrderItemList().size() == 0)
                {
                    return "nullException";
                }
                else
                {
                    dbHelper.deleteStockOutRequest();
                    dbHelper.insertStockOutRequest(stockOutPurchaseOrderItemListJson);

                }


            }
            catch (Exception e)
            {

                Log.e("Log","Exception",e);
                return "failure";
            }
            return "success";
        }

        protected void onPostExecute(String s)
        {
            super.onPostExecute(s);


            if (s.equals("success"))
            {

                RecyclerView purchaseOrderItemListRecyclerview = context.get().findViewById(R.id.purchaseorderitemlistrecyclerview);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context.get());
                purchaseOrderItemListRecyclerview.setLayoutManager(layoutManager);
                purchaseOrderItemListRecyclerview.setItemAnimator(new DefaultItemAnimator());


                StockoutPurchaseOrderItemListAdapter purchaseOrderItemListAdapter = new StockoutPurchaseOrderItemListAdapter(context.get(), stockOutPurchaseOrderItemListJson.getData().getStockOutPurchaseOrderItemList());
                purchaseOrderItemListRecyclerview.setAdapter(purchaseOrderItemListAdapter);



            }
            else if (s.equals("nullException"))
            {
                Fns.neutralAlert("Alert", "Null Exception from Server Side", context.get());
            }
            else
            {
                Fns.neutralAlert("Alert", "Failure", context.get());
            }
            pd.dismiss();
        }
    }

    private static class PushStockOutRequest extends AsyncTask<String, String, String>
    {
        ProgressDialog pd;
        WeakReference<StockOutPurchaseOrderItemListActivity> context;
        private OkHttpClient okHttpClient;
        private Request request;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String url;
        SharedPreferences shp;
        DbHelper dbHelper;
        StockOutSendPurchaseRequestJson stockOutSendPurchaseRequestJson;
        String message;

        int idPurchaseOrder;

        public PushStockOutRequest(StockOutPurchaseOrderItemListActivity context, Integer idPurchaseOrder)
        {
            this.context = new WeakReference<>(context);
            this.idPurchaseOrder = idPurchaseOrder;


            shp = context.getSharedPreferences(Const.Shared_Pref_name,MODE_PRIVATE);

            dbHelper = new DbHelper(context);

            CustomTrust customTrust = new CustomTrust(context);
            OkHttpClient client = customTrust.getClient();
            okHttpClient = client;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            pd = new ProgressDialog(context.get());
            pd.setTitle("Uploading Data");
            pd.setMessage("Please wait few seconds...");
            pd.setCancelable(false);
            //pd.setIndeterminate(true);
            pd.setMax(5);
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.show();

        }

        @Override
        protected String doInBackground(String... strings)
        {
            try {


                JSONObject pushDataObj = dbHelper.getSendStockoutRequest(shp.getString(Const.Shp_Employee_Code,""), 1, idPurchaseOrder);

                url = Const.USING_IP + URL_STOCKOUT_SEND_PURCHASE_REQUEST;

                Log.e("Log","sendStockoutRequestRequest" +url);
                Log.e("Log","sendStockoutObject"+pushDataObj.toString());


                RequestBody body = RequestBody.create(pushDataObj.toString(), JSON);
                Log.e("Log", "customer list body" + body);
                request = new Request.Builder()
                        //.header("X-Client-Type", "Android")
                        .url(url)
                        .post(body)
                        .build();
                Log.e("Log", "request" + request);

                Response response = okHttpClient.newCall(request).execute();
                Log.e("Log", "customer list" + response);

                if (!response.isSuccessful()) {

                    return "failure";

                }

                String resultString = response.body().string();

                Log.e("Log", "" + resultString);

                Gson gson = new Gson();

                stockOutSendPurchaseRequestJson = gson.fromJson(resultString,StockOutSendPurchaseRequestJson.class);
                Log.e("Log", "stockOutSendPurchaseRequestJson" + stockOutSendPurchaseRequestJson);

                int status = stockOutSendPurchaseRequestJson.getData().getStockOutSendPurchaseRequestStatus().get(0).getStatus();
                Log.e("Log", "status" + status);
                message = stockOutSendPurchaseRequestJson.getData().getStockOutSendPurchaseRequestStatus().get(0).getStatusMsg();
                Log.e("Log", "message" + message);

                if(status != 3)
                {
                    return "failure";
                }


            }catch (Exception e)
            {

                Log.e("Log","Exception",e);
                return "failure";
            }

            return "success";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            pd.dismiss();

            if (s.equals("success"))
            {


                AlertDialog.Builder builder = new AlertDialog.Builder(context.get());
                View dialogView = LayoutInflater.from(context.get()).inflate(R.layout.dialog_success,null);

                builder.setView(dialogView);

                AlertDialog alertDialog = builder.create();
                alertDialog.getWindow().setBackgroundDrawableResource(R.color.green_text);
                alertDialog.show();

                TextView successMsg = dialogView.findViewById(R.id.success_msg);
                successMsg.setTextColor(Color.BLACK);
                successMsg.setText(message);

                MaterialButton okButton = dialogView.findViewById(R.id.ok_button);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        alertDialog.dismiss();


                        context.get().startActivity(new Intent(context.get(), MainMenuActivity.class));
                        context.get().finish();
                    }
                });

            }
            else if (s.equals("failure"))
            {

                AlertDialog.Builder builder = new AlertDialog.Builder(context.get());
                View dialogView = LayoutInflater.from(context.get()).inflate(R.layout.dialog_success,null);

                builder.setView(dialogView);

                AlertDialog alertDialog = builder.create();
                alertDialog.getWindow().setBackgroundDrawableResource(R.color.red_button);
                alertDialog.show();

                TextView successMsg = dialogView.findViewById(R.id.success_msg);
                successMsg.setTextColor(Color.BLACK);
                successMsg.setText(message);

                MaterialButton okButton = dialogView.findViewById(R.id.ok_button);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        alertDialog.dismiss();


                        context.get().startActivity(new Intent(context.get(), MainMenuActivity.class));
                        context.get().finish();
                    }
                });

            }
            else
            {
                Log.e("Log", "failed to fetch");
            }

        }
    }
}