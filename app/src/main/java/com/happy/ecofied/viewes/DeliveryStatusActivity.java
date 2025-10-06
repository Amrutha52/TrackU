package com.happy.ecofied.viewes;

import static com.happy.ecofied.utils.Const.URL_GET_DELIVERY_PENDING;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.happy.ecofied.R;
import com.happy.ecofied.adapters.DeliveryProductStatusListAdapter;
import com.happy.ecofied.databinding.ActivityDeliveryStatusBinding;
import com.happy.ecofied.db.DbHelper;
import com.happy.ecofied.gson.deliverypendinglist.DeliveryPendingListJson;
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

public class DeliveryStatusActivity extends AppCompatActivity
{
    private ActivityDeliveryStatusBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDeliveryStatusBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Delivery Status");

        View rootView = findViewById(android.R.id.content); // Get root view

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            WindowInsetsCompat insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets.toWindowInsets());
            int systemBarsInsetsTop = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int systemBarsInsetsBottom = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

            // Apply padding to your main content view
            v.setPadding(v.getPaddingLeft(), systemBarsInsetsTop, v.getPaddingRight(), systemBarsInsetsBottom);

            return WindowInsetsCompat.CONSUMED;
        });

        new GetDeliveryPendingList(this).execute();

    }

    private static class GetDeliveryPendingList extends AsyncTask<String, String, String>
    {
        ProgressDialog pd;
        WeakReference<DeliveryStatusActivity> context;
        private OkHttpClient okHttpClient;
        private Request request;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String url;
        SharedPreferences shp;
        DeliveryPendingListJson deliveryPendingListJson;
        int idPurchaseOrder;
        DbHelper dbHelper;
        SearchView searchByInvoiceNumber;
        public GetDeliveryPendingList(DeliveryStatusActivity context)
        {
            this.context = new WeakReference<>(context);

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
                jsonObjectDeliveryPending.put("id", 0);

                Log.e("Log", "jsonObjectdeliveryPendingList" + jsonObjectDeliveryPending);

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


                Log.e("Log", "deliveryPendingResult" + result);
                Gson gson = new Gson();
                deliveryPendingListJson = gson.fromJson(result, DeliveryPendingListJson.class);

                if (deliveryPendingListJson.getData().getDeliveryPending().isEmpty() || deliveryPendingListJson.getData().getDeliveryPending().size() == 0 || deliveryPendingListJson.getData().getDeliveryPending() == null)
                {
                    return "nullException";
                }


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

                RecyclerView deliveryProductStatusListRecyclerview = context.get().findViewById(R.id.deliveryStatusListrecyclerview);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context.get());
                deliveryProductStatusListRecyclerview.setLayoutManager(layoutManager);
                deliveryProductStatusListRecyclerview.setItemAnimator(new DefaultItemAnimator());

                Log.e("Log", "deliveryProductList" + deliveryPendingListJson.getData().getDeliveryPending().toString());

                DeliveryProductStatusListAdapter deliveryProductStatusListAdapter = new DeliveryProductStatusListAdapter(context.get(), deliveryPendingListJson.getData().getDeliveryPending());
                deliveryProductStatusListRecyclerview.setAdapter(deliveryProductStatusListAdapter);

                searchByInvoiceNumber = context.get().findViewById(R.id.searchbox_invoice_number);
                SearchManager searchManager = (SearchManager) context.get().getSystemService(Context.SEARCH_SERVICE);
                searchByInvoiceNumber.setSearchableInfo(searchManager
                        .getSearchableInfo(context.get().getComponentName()));
                searchByInvoiceNumber.setMaxWidth(Integer.MAX_VALUE);

                searchByInvoiceNumber.setOnQueryTextListener(new SearchView.OnQueryTextListener()
                {
                    @Override
                    public boolean onQueryTextSubmit(String query) {

                        deliveryProductStatusListAdapter.getFilter().filter(query);

                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {

                        deliveryProductStatusListAdapter.getFilter().filter(newText);

                        return false;
                    }

                });


            }
            else if (s.equals("nullException"))
            {
                Fns.neutralAlert("Alert", "Null from server side", context.get());
            }
            else
            {
                Fns.neutralAlert("Alert", "Failure", context.get());
            }
            pd.dismiss();
        }
    }
}