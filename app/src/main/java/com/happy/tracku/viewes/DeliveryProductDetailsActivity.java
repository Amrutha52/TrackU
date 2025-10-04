package com.happy.tracku.viewes;

import static com.happy.tracku.utils.Const.URL_GET_DELIVERY_PENDING;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.happy.tracku.R;
import com.happy.tracku.adapters.DeliveryProductDetailsAdapter;
import com.happy.tracku.adapters.DeliveryProductStatusListAdapter;
import com.happy.tracku.databinding.ActivityDeliveryProductDetailsBinding;
import com.happy.tracku.databinding.ActivityDeliveryStatusBinding;
import com.happy.tracku.db.DbHelper;
import com.happy.tracku.gson.deliverypendinglist.DeliveryPendingListJson;
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

public class DeliveryProductDetailsActivity extends AppCompatActivity
{
    private ActivityDeliveryProductDetailsBinding binding;
    Intent intent;
    int idSalesHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDeliveryProductDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Delivery Product Details");

        View rootView = findViewById(android.R.id.content); // Get root view

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            WindowInsetsCompat insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets.toWindowInsets());
            int systemBarsInsetsTop = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int systemBarsInsetsBottom = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

            // Apply padding to your main content view
            v.setPadding(v.getPaddingLeft(), systemBarsInsetsTop, v.getPaddingRight(), systemBarsInsetsBottom);

            return WindowInsetsCompat.CONSUMED;
        });

        intent = getIntent();
        idSalesHeader = intent.getIntExtra("idSalesHeader", 0);

        new GetDeliveryPendingListFromIdSalesOrder(this, idSalesHeader).execute();

    }

    public void listeners(View view)
    {
    }

    private static class GetDeliveryPendingListFromIdSalesOrder extends AsyncTask<String, String, String>
    {
        ProgressDialog pd;
        WeakReference<DeliveryProductDetailsActivity> context;
        private OkHttpClient okHttpClient;
        private Request request;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String url;
        SharedPreferences shp;
        DeliveryPendingListJson deliveryPendingListJson;
        int idSalesHeader;
        DbHelper dbHelper;
        public GetDeliveryPendingListFromIdSalesOrder(DeliveryProductDetailsActivity context, int idSalesHeader)
        {
            this.context = new WeakReference<>(context);
            this.idSalesHeader = idSalesHeader;

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
                url = Const.USING_IP + URL_GET_DELIVERY_PENDING;
                Log.e("Log", "deliveryPendingListURL" + url);

                JSONObject jsonObjectDeliveryPending = new JSONObject();

                jsonObjectDeliveryPending.put("createdBy", shp.getString(Const.Shp_Employee_Code, ""));
                jsonObjectDeliveryPending.put("action", 4);
                jsonObjectDeliveryPending.put("id", idSalesHeader);

                Log.e("Log", "jsonObjectProdcutDetailsActivity" + jsonObjectDeliveryPending);

                RequestBody body = RequestBody.create(jsonObjectDeliveryPending.toString(), JSON);
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


                Log.e("Log", "deliveryPendingResultProdcutDetails" + result);
                Gson gson = new Gson();
                deliveryPendingListJson = gson.fromJson(result, DeliveryPendingListJson.class);

            }
            catch (Exception e)
            {

                Log.e("Log","Exception",e);
                return "failure";
            }
            return "success";
        }

        @Override
        protected void onPostExecute(String s)
        {
            super.onPostExecute(s);


            if (s.equals("success"))
            {

                RecyclerView deliveryProductDetailsRecyclerview = context.get().findViewById(R.id.deliveryProductDetailsRecyclerview);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context.get());
                deliveryProductDetailsRecyclerview.setLayoutManager(layoutManager);
                deliveryProductDetailsRecyclerview.setItemAnimator(new DefaultItemAnimator());

                Log.e("Log", "deliveryProductDetails" + deliveryPendingListJson.getData().getDeliveryPending().toString());

                DeliveryProductDetailsAdapter deliveryProductDetailsAdapter = new DeliveryProductDetailsAdapter(context.get(), deliveryPendingListJson.getData().getDeliveryPending());
                deliveryProductDetailsRecyclerview.setAdapter(deliveryProductDetailsAdapter);



            }
            else
            {
                Fns.neutralAlert("Alert", "Failure", context.get());
            }
            pd.dismiss();
        }
    }
}