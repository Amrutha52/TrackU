package com.happy.tracku.viewes;

import static com.happy.tracku.utils.Const.URL_PURCHASE_ORDER_ITEM_LIST;
import static com.happy.tracku.utils.Const.URL_SEND_PURCHASE_REQUEST;

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
import com.happy.tracku.R;
import com.happy.tracku.adapters.PurchaseOrderItemListAdapter;
import com.happy.tracku.databinding.ActivityPurchaseOrderItemListBinding;
import com.happy.tracku.db.DbHelper;
import com.happy.tracku.gson.purchaseorderitemlist.PurchaseOrderItemListJson;
import com.happy.tracku.gson.sendpurchaserequest.SendPurchaseRequestStatusJson;
import com.happy.tracku.ssl.CustomTrust;
import com.happy.tracku.utils.Const;
import com.happy.tracku.utils.Fns;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PurchaseOrderItemListActivity extends AppCompatActivity
{
    private ActivityPurchaseOrderItemListBinding binding;
    Intent intent;
    Integer idPurchaseOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityPurchaseOrderItemListBinding.inflate(getLayoutInflater());
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

        new PullPurchaseOrderItemListDetails(this, idPurchaseOrder).execute();

    }

    public void listeners(View view)
    {
        switch (view.getId())
        {
            case R.id.complete_save_button:
            {
                new PushPurchaseOrderRequest(this, idPurchaseOrder).execute();

            }
            break;
        }
    }

    private static class PullPurchaseOrderItemListDetails extends AsyncTask<String, String, String>
    {
        ProgressDialog pd;
        WeakReference<PurchaseOrderItemListActivity> context;
        private OkHttpClient okHttpClient;
        private Request request;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String url;
        SharedPreferences shp;
        PurchaseOrderItemListJson purchaseOrderItemListJson;
        int idPurchaseOrder;

        DbHelper dbHelper;

        public PullPurchaseOrderItemListDetails(PurchaseOrderItemListActivity context, int idPurchaseOrder)
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
                url = Const.USING_IP + URL_PURCHASE_ORDER_ITEM_LIST;
                Log.e("Log", "purchaseOrderItemListURL" + url);

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
                purchaseOrderItemListJson = gson.fromJson(result, PurchaseOrderItemListJson.class);

               // dbHelper.deletePurchaseOrderRequest();
                dbHelper.insertPurchaseOrderRequest(purchaseOrderItemListJson);

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


                PurchaseOrderItemListAdapter purchaseOrderItemListAdapter = new PurchaseOrderItemListAdapter(context.get(), purchaseOrderItemListJson.getData().getPurchaseOrderItemList());
                purchaseOrderItemListRecyclerview.setAdapter(purchaseOrderItemListAdapter);



            }
            else
            {
                Fns.neutralAlert("Alert", "Failure", context.get());
            }
            pd.dismiss();
        }
    }

    private static class PushPurchaseOrderRequest extends AsyncTask<String, String, String>
    {
        ProgressDialog pd;
        WeakReference<PurchaseOrderItemListActivity> context;
        private OkHttpClient okHttpClient;
        private Request request;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String url;
        SharedPreferences shp;
        DbHelper dbHelper;
        SendPurchaseRequestStatusJson sendPurchaseRequestStatusJson;
        String message;

        int idPurchaseOrder;

        public PushPurchaseOrderRequest(PurchaseOrderItemListActivity context, Integer idPurchaseOrder)
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


                JSONObject pushDataObj = dbHelper.getSendPurchaseRequest(shp.getString(Const.Shp_Employee_Code,""), 1, idPurchaseOrder);

                url = Const.USING_IP + URL_SEND_PURCHASE_REQUEST;

                Log.e("Log","sendPurchaseRequest" +url);
                Log.e("Log","sendPurchaseObject"+pushDataObj.toString());


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

                sendPurchaseRequestStatusJson= gson.fromJson(resultString,SendPurchaseRequestStatusJson.class);
                Log.e("Log", "sendPurchaseRequestStatusJson" + sendPurchaseRequestStatusJson);

                int status = sendPurchaseRequestStatusJson.getData().getSendPurchaseRequestStatus().get(0).getStatus();
                Log.e("Log", "status" + status);
                message = sendPurchaseRequestStatusJson.getData().getSendPurchaseRequestStatus().get(0).getStatusMsg();
                Log.e("Log", "message" + message);

                if(status != 1)
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