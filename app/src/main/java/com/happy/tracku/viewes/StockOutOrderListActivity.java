package com.happy.tracku.viewes;

import static com.happy.tracku.utils.Const.URL_PURCHASE_ORDER_LIST;
import static com.happy.tracku.utils.Const.URL_STOCKOUT_PURCHASE_ORDER_LIST;

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
import com.happy.tracku.R;
import com.happy.tracku.adapters.PurchaseOrderListAdapter;
import com.happy.tracku.adapters.StockoutPurchaseOrderListAdapter;
import com.happy.tracku.databinding.ActivityPurchaseOrderListBinding;
import com.happy.tracku.databinding.ActivityStockOutOrderListBinding;
import com.happy.tracku.gson.purchaseorderlist.PurchaseOrder;
import com.happy.tracku.gson.purchaseorderlist.PurchaseOrderListJson;
import com.happy.tracku.gson.stockoutorderlist.StockOutPurchaseOrder;
import com.happy.tracku.gson.stockoutorderlist.StockOutPurchaseOrderListJson;
import com.happy.tracku.ssl.CustomTrust;
import com.happy.tracku.utils.Const;
import com.happy.tracku.utils.Fns;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class StockOutOrderListActivity extends AppCompatActivity
{

    private ActivityStockOutOrderListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityStockOutOrderListBinding.inflate(getLayoutInflater());
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

        getSupportActionBar().setTitle("StockOut Order List");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        new PullStockOutOrderListDetails(this).execute();
    }

    private static class PullStockOutOrderListDetails extends AsyncTask<String, String, String>
    {
        ProgressDialog pd;
        WeakReference<StockOutOrderListActivity> context;
        private OkHttpClient okHttpClient;
        private Request request;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String url;
        SharedPreferences shp;
        StockOutPurchaseOrderListJson stockOutPurchaseOrderListJson;
        List<StockOutPurchaseOrder> stockOutPurchaseOrderList;
        SearchView vendorNameSearchView;
        String employeeCode;
        public PullStockOutOrderListDetails(StockOutOrderListActivity context)
        {
            this.context = new WeakReference<>(context);


            shp = context.getSharedPreferences(Const.Shared_Pref_name,MODE_PRIVATE);

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
                url = Const.USING_IP + URL_STOCKOUT_PURCHASE_ORDER_LIST;
                Log.e("Log", "stockoutpurchaseOrderListURL" + url);

                JSONObject jsonObjectPurchaseOrderList = new JSONObject();

                jsonObjectPurchaseOrderList.put("createdBy", shp.getString(Const.Shp_Employee_Code, ""));


                Log.e("Log", "jsonObjectpurchaseOrderList" + jsonObjectPurchaseOrderList);

                RequestBody body = RequestBody.create(jsonObjectPurchaseOrderList.toString(), JSON);
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


                Log.e("Log", "stockoutpurchaseOrderResult" + result);
                Gson gson = new Gson();
                stockOutPurchaseOrderListJson = gson.fromJson(result, StockOutPurchaseOrderListJson.class);



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

                RecyclerView stockOutPurchaseOrderListRecyclerview = context.get().findViewById(R.id.stockoutpurchaseorderlistrecyclerview);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context.get());
                stockOutPurchaseOrderListRecyclerview.setLayoutManager(layoutManager);
                stockOutPurchaseOrderListRecyclerview.setItemAnimator(new DefaultItemAnimator());


                StockoutPurchaseOrderListAdapter purchaseOrderListAdapter = new StockoutPurchaseOrderListAdapter(context.get(), stockOutPurchaseOrderListJson.getData().getStockOutPurchaseOrderList());
                stockOutPurchaseOrderListRecyclerview.setAdapter(purchaseOrderListAdapter);

                vendorNameSearchView = context.get().findViewById(R.id.searchbox_vendor_name);
                SearchManager searchManager = (SearchManager) context.get().getSystemService(Context.SEARCH_SERVICE);
                vendorNameSearchView.setSearchableInfo(searchManager
                        .getSearchableInfo(context.get().getComponentName()));
                vendorNameSearchView.setMaxWidth(Integer.MAX_VALUE);

                vendorNameSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
                {
                    @Override
                    public boolean onQueryTextSubmit(String query) {

                        purchaseOrderListAdapter.getFilter().filter(query);

                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText)
                    {
                        purchaseOrderListAdapter.getFilter().filter(newText);
                        return false;
                    }

                });

            }
            else
            {
                Fns.neutralAlert("Alert", "Failure", context.get());
            }
            pd.dismiss();
        }
    }
}