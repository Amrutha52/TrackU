package com.happy.tracku.viewes;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.happy.tracku.utils.Const.URL_GET_DELIVERY_PENDING;
import static com.happy.tracku.utils.Const.URL_UPDATE_DELIVERY_STATUS;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.happy.tracku.R;
import com.happy.tracku.adapters.DeliveryProductDetailsAdapter;
import com.happy.tracku.databinding.ActivityDeliveryProductDetailsBinding;
import com.happy.tracku.db.DbHelper;
import com.happy.tracku.gson.deliverypendinglist.DeliveryPendingListJson;
import com.happy.tracku.gson.updatedeliverystatus.UpdateDeliveryStatusJson;
import com.happy.tracku.models.PaymentType;
import com.happy.tracku.ssl.CustomTrust;
import com.happy.tracku.utils.Const;
import com.happy.tracku.utils.Fns;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DeliveryProductDetailsActivity extends AppCompatActivity
{
    private ActivityDeliveryProductDetailsBinding binding;
    Intent intent;
    int idSalesHeader, idDetailsSalesHeader = 0;
    LinearLayout paymentTypeLL;
    Spinner paymentModeSpinner;
    boolean isCashSale;
    int paymentId;
    DeliveryPendingListJson deliveryPendingListJson;

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
        isCashSale = intent.getBooleanExtra("isCashSale", false);

        paymentTypeLL = findViewById(R.id.paymentTypeLL);
        paymentModeSpinner = findViewById(R.id.paymentTypeSpinner);

        if (isCashSale == true)
        {
            paymentTypeLL.setVisibility(VISIBLE);
        }
        else
        {
            paymentTypeLL.setVisibility(INVISIBLE);
        }

        List<PaymentType> paymentTypes = new ArrayList<>();
        paymentTypes.add(new PaymentType("Select", 0));
        paymentTypes.add(new PaymentType("Cash", 1));
        paymentTypes.add(new PaymentType("Credit", 2));
        paymentTypes.add(new PaymentType("Cash Not Received", 3));

        ArrayAdapter<PaymentType> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                paymentTypes);

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        paymentModeSpinner.setAdapter(adapter);

        paymentModeSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view,
                                               int position,
                                               long id) {

                        PaymentType selectedPaymentType =
                                (PaymentType) parent.getItemAtPosition(position);


                        paymentId = selectedPaymentType.getValue();

                        // Do something with the selected value
                        Log.d("Spinner",
                                ", ID: " + paymentId);


                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

        new GetDeliveryPendingListFromIdSalesOrder(this, idSalesHeader, idDetailsSalesHeader).execute();

    }

    public void listeners(View view)
    {
        switch (view.getId())
        {
            case R.id.update_delivery_status:
            {
                Log.e("Log", "idDetailsSalesHeaderUpdate" + idDetailsSalesHeader);
                new UpdateDeliveryStatus(this, idDetailsSalesHeader,paymentId).execute();
            }
            break;
        }
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
        int idSalesHeader, idDetailsSalesHeader;
        DbHelper dbHelper;
        SearchView searchByInvoiceNumber;
        public GetDeliveryPendingListFromIdSalesOrder(DeliveryProductDetailsActivity context, int idSalesHeader, int idDetailsSalesHeader)
        {
            this.context = new WeakReference<>(context);
            this.idSalesHeader = idSalesHeader;
            this.idDetailsSalesHeader = idDetailsSalesHeader;

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

                context.get().setDetails(deliveryPendingListJson);

                RecyclerView deliveryProductDetailsRecyclerview = context.get().findViewById(R.id.deliveryProductDetailsRecyclerview);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context.get());
                deliveryProductDetailsRecyclerview.setLayoutManager(layoutManager);
                deliveryProductDetailsRecyclerview.setItemAnimator(new DefaultItemAnimator());

                Log.e("Log", "deliveryProductDetails" + deliveryPendingListJson.getData().getDeliveryPending().toString());

                dbHelper.deleteDeliveryPendingDetails();
                dbHelper.insertDeliveryPendingDetails(deliveryPendingListJson.getData().getDeliveryPending());

                DeliveryProductDetailsAdapter deliveryProductDetailsAdapter = new DeliveryProductDetailsAdapter(context.get(), deliveryPendingListJson.getData().getDeliveryPending());
                deliveryProductDetailsRecyclerview.setAdapter(deliveryProductDetailsAdapter);

                searchByInvoiceNumber = context.get().findViewById(R.id.searchbox_invoice_number);
                SearchManager searchManager = (SearchManager) context.get().getSystemService(Context.SEARCH_SERVICE);
                searchByInvoiceNumber.setSearchableInfo(searchManager
                        .getSearchableInfo(context.get().getComponentName()));
                searchByInvoiceNumber.setMaxWidth(Integer.MAX_VALUE);


                searchByInvoiceNumber.setOnQueryTextListener(new SearchView.OnQueryTextListener()
                {
                    @Override
                    public boolean onQueryTextSubmit(String query) {

                        deliveryProductDetailsAdapter.getFilter().filter(query);

                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {

                        deliveryProductDetailsAdapter.getFilter().filter(newText);

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

    private void setDetails(DeliveryPendingListJson deliveryPendingListJson)
    {
        this.deliveryPendingListJson = deliveryPendingListJson;

        idDetailsSalesHeader = deliveryPendingListJson.getData().getDeliveryPending().get(0).getIdSalesHeader();
    }

    private static class UpdateDeliveryStatus extends AsyncTask<String, String, String>
    {
        ProgressDialog pd;
        WeakReference<DeliveryProductDetailsActivity> context;
        private OkHttpClient okHttpClient;
        private Request request;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String url;
        SharedPreferences shp;
        DbHelper dbHelper;
        UpdateDeliveryStatusJson updateDeliveryStatusJson;
        String message, fileName, base64;
        int idSalesHeader, paymentId;
        public UpdateDeliveryStatus(DeliveryProductDetailsActivity context, int idSalesHeader, int paymentId)
        {
            this.context = new WeakReference<>(context);
            this.idSalesHeader = idSalesHeader;
            this.paymentId = paymentId;

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


                JSONObject pushDataObj = dbHelper.getDeliveryPendingDetails(idSalesHeader,shp.getString(Const.Shp_Employee_Code,""), paymentId);


                url = Const.USING_IP + URL_UPDATE_DELIVERY_STATUS;

                Log.e("Log","updateDeliveryStatusURL" +url);
                Log.e("Log","updateDeliveryStatusObject"+pushDataObj.toString());


                RequestBody body = RequestBody.create(pushDataObj.toString(), JSON);
                request = new Request.Builder()
                        //.header("X-Client-Type", "Android")
                        .url(url)
                        .post(body)
                        .build();


                Response response = okHttpClient.newCall(request).execute();
                Log.e("Log", "updateDeliveryStatusResponse" + response);

                if (!response.isSuccessful()) {

                    return "failure";

                }

                String resultString = response.body().string();

                Log.e("Log", "updateDeliveryStatusResultString" + resultString);

                Gson gson = new Gson();

                updateDeliveryStatusJson= gson.fromJson(resultString,UpdateDeliveryStatusJson.class);
                Log.e("Log", "updateDeliveryStatusJson" + updateDeliveryStatusJson);

                if (updateDeliveryStatusJson.getData().getDeliveryStatusUpdate().isEmpty() || updateDeliveryStatusJson.getData().getDeliveryStatusUpdate().size() == 0 || updateDeliveryStatusJson.getData().getDeliveryStatusUpdate() == null)
                {
                    return "nullException";
                }
                else
                {
                    int status = updateDeliveryStatusJson.getData().getDeliveryStatusUpdate().get(0).getStatus();
                    Log.e("Log", "status" + status);
                    message = updateDeliveryStatusJson.getData().getDeliveryStatusUpdate().get(0).getStatusMsg();
                    Log.e("Log", "message" + message);
                    if(status != 3)
                    {
                        return "failure";
                    }
                }


            }catch (Exception e)
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

            pd.dismiss();

            if (s.equals("success"))
            {

                android.app.AlertDialog.Builder adb = new android.app.AlertDialog.Builder(context.get());

                TextView titletxtview = new TextView(context.get());
                titletxtview.setText("Alert");
                titletxtview.setBackgroundColor(ContextCompat.getColor(context.get(), R.color.colorPrimary));
                titletxtview.setPadding(10, 10, 10, 10);
                titletxtview.setGravity(Gravity.CENTER);
                titletxtview.setTextColor(Color.WHITE);
                titletxtview.setTextSize(20);

                adb.setCustomTitle(titletxtview);

                TextView messagetxtview = new TextView(context.get());
                messagetxtview.setText(message);
                messagetxtview.setBackgroundColor(Color.WHITE);
                messagetxtview.setPadding(10, 24, 10, 10);
                messagetxtview.setGravity(Gravity.CENTER);
                messagetxtview.setTextColor(Color.BLACK);
                messagetxtview.setTextSize(18);
                messagetxtview.setVerticalScrollBarEnabled(true);
                messagetxtview.setMaxHeight(750);
                messagetxtview.setMovementMethod(new ScrollingMovementMethod());

                adb.setView(messagetxtview);

                adb.setNegativeButton("OK", new DialogInterface.OnClickListener()
                {

                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // dialog.cancel();

                        dialog.dismiss();


                        context.get().startActivity(new Intent(context.get(), MainMenuActivity.class));
                        context.get().finish();

                    }
                });

                adb.setPositiveButton("Share", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                        String shareBody = message;
                        Log.e("LogFns", "fns message" + shareBody);
                        intent.setType("text/plain");
                        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share");
                        intent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                        context.get().startActivity(Intent.createChooser(intent, "Share using"));

                    }
                });
                android.app.AlertDialog ad = adb.create();
                ad.show();


            }
            else if (s.equals("failure"))
            {

                android.app.AlertDialog.Builder adb = new android.app.AlertDialog.Builder(context.get());

                TextView titletxtview = new TextView(context.get());
                titletxtview.setText("Alert");
                titletxtview.setBackgroundColor(ContextCompat.getColor(context.get(), R.color.colorPrimary));
                titletxtview.setPadding(10, 10, 10, 10);
                titletxtview.setGravity(Gravity.CENTER);
                titletxtview.setTextColor(Color.WHITE);
                titletxtview.setTextSize(20);

                adb.setCustomTitle(titletxtview);

                TextView messagetxtview = new TextView(context.get());
                messagetxtview.setText(message);
                messagetxtview.setBackgroundColor(Color.WHITE);
                messagetxtview.setPadding(10, 24, 10, 10);
                messagetxtview.setGravity(Gravity.CENTER);
                messagetxtview.setTextColor(Color.BLACK);
                messagetxtview.setTextSize(18);
                messagetxtview.setVerticalScrollBarEnabled(true);
                messagetxtview.setMaxHeight(750);
                messagetxtview.setMovementMethod(new ScrollingMovementMethod());

                adb.setView(messagetxtview);

                adb.setNegativeButton("OK", new DialogInterface.OnClickListener()
                {

                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // dialog.cancel();

                        dialog.dismiss();

                        context.get().startActivity(new Intent(context.get(), MainMenuActivity.class));
                        context.get().finish();

                    }
                });

                adb.setPositiveButton("Share", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                        String shareBody = message;
                        Log.e("LogFns", "fns message" + shareBody);
                        intent.setType("text/plain");
                        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share");
                        intent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                        context.get().startActivity(Intent.createChooser(intent, "Share using"));

                    }
                });
                android.app.AlertDialog ad = adb.create();
                ad.show();

              /*  AlertDialog.Builder builder = new AlertDialog.Builder(context.get());
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

               */

            }
            else if (s.equals("nullException"))
            {
                Fns.neutralAlert("Alert", "Null Exception From Server", context.get());
            }
            else
            {
                Log.e("Log", "failed to fetch");
            }

        }
    }
}