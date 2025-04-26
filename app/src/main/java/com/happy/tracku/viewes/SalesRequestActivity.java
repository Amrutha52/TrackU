package com.happy.tracku.viewes;

import static com.happy.tracku.utils.Const.URL_MASTER_DATA;
import static com.happy.tracku.utils.Const.URL_SALES_DATA_FILLING;
import static com.happy.tracku.utils.Const.URL_SEND_SALES_REQUEST;
import static com.happy.tracku.utils.Const.USING_IP;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.happy.tracku.R;
import com.happy.tracku.databinding.ActivitySalesRequestBinding;
import com.happy.tracku.databinding.ActivityStockOutOrderListBinding;
import com.happy.tracku.db.DbHelper;
import com.happy.tracku.gson.masterdata.ItemMaster;
import com.happy.tracku.gson.masterdata.MasterDataJson;
import com.happy.tracku.gson.masterdata.VendorMaster;
import com.happy.tracku.gson.salesrequestdatafilling.SalesRequestDataFillingJson;
import com.happy.tracku.gson.sendsalesrequest.SendSalesRequestJson;
import com.happy.tracku.ssl.CustomTrust;
import com.happy.tracku.utils.Const;
import com.happy.tracku.utils.Fns;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SalesRequestActivity extends AppCompatActivity
{
    private ActivitySalesRequestBinding binding;
    MasterDataJson masterDataJson;
    SalesRequestDataFillingJson salesRequestDataFillingJson;
    DbHelper dbHelper;
    Dialog dialog;
    ArrayList<VendorMaster> vendorMasterArrayList;
    ArrayList<ItemMaster> itemMasterArrayList;
    int idVendor, idItemMaster;
    DatePickerDialog pickUpDatePicker;
    String possibleDeliveryDateString, deliveryLocationString, mailIdString, mobileNumberString, unitFromET;
    int quantityFromET;
    String descriptionETString;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySalesRequestBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        View rootView = findViewById(android.R.id.content); // Get root view

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            WindowInsetsCompat insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets.toWindowInsets());
            int systemBarsInsetsTop = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int systemBarsInsetsBottom = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

            // Apply padding to your main content view
            v.setPadding(v.getPaddingLeft(), systemBarsInsetsTop, v.getPaddingRight(), systemBarsInsetsBottom);

            return WindowInsetsCompat.CONSUMED;
        });

        getSupportActionBar().setTitle("SalesOrderRequest");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dbHelper = new DbHelper(this);

        /**
         * Possible Delivery Date
         */

        binding.possibleDeliveryDateEditText.setText(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));

        binding.possibleDeliveryDateEditText.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);

                // date picker dialog

                pickUpDatePicker = new DatePickerDialog(SalesRequestActivity.this,
                        new DatePickerDialog.OnDateSetListener()
                        {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                month = month + 1;

                                String monthString = "" + month, dayString = "" + dayOfMonth;
                                if (month < 10) {
                                    monthString = "0" + monthString;
                                }
                                if (dayOfMonth < 10) {
                                    dayString = "0" + dayString;
                                }

                                binding.possibleDeliveryDateEditText.setText(year + "-" + monthString + "-" + dayString);
                                Log.e("pickUpDate", binding.possibleDeliveryDateEditText.getText().toString());

                            }
                        }, mYear, mMonth, mDay);

                //Date minDateObj = Calendar.getInstance().getTime();
                //pickUpDatePicker.getDatePicker().setMinDate(minDateObj.getTime());
                //pickUpDatePicker.getDatePicker().setMaxDate(minDateObj.getTime()+(365*24*60*60));
                pickUpDatePicker.show();
            }
        });

        new PullMasterData(this).execute();
    }

    public void listeners(View view)
    {
        switch (view.getId())
        {
            case R.id.submit_button:
            {

                possibleDeliveryDateString = binding.possibleDeliveryDateEditText.getText().toString();
                deliveryLocationString = binding.deliveryLocation.getText().toString();
                mailIdString = binding.vendorMailId.getText().toString();
                mobileNumberString = binding.mobileNumber.getText().toString();
                quantityFromET = Integer.parseInt(binding.quantityET.getText().toString());
                unitFromET = binding.unitET.getText().toString();
                descriptionETString = binding.descriptionET.getText().toString();

                new PushSalesRequest(this, idVendor, idItemMaster, quantityFromET, unitFromET, possibleDeliveryDateString, deliveryLocationString, mailIdString, mobileNumberString, descriptionETString).execute();


            }
            break;
        }
    }

    private static class PullMasterData extends AsyncTask<String, String, String>
    {

        WeakReference<SalesRequestActivity> context;
        ProgressDialog pd;
        OkHttpClient okHttpClient;
        String url, resultString;
        Request request;
        Response response;
        SharedPreferences shp;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        MasterDataJson masterDataJson;
        DbHelper dbHelper;

        public PullMasterData(SalesRequestActivity context)
        {
            this.context = new WeakReference<>(context);

            CustomTrust customTrust = new CustomTrust(context);
            OkHttpClient client = customTrust.getClient();
            okHttpClient = client;

            shp = context.getSharedPreferences(Const.Shared_Pref_name, MODE_PRIVATE);
            dbHelper = new DbHelper(context);

        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pd = new ProgressDialog(context.get());
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setMessage("Loading");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... strings)
        {
            try
            {

                JSONObject masterDataDetailsObj = new JSONObject();
                masterDataDetailsObj.put("createdBy", shp.getString(Const.Shp_Employee_Code, ""));

                url = USING_IP + URL_MASTER_DATA;
                Log.e("Log", "masterurl" + url);

                RequestBody body = RequestBody.create(masterDataDetailsObj.toString(), JSON);
                Log.e("Log", "masterDataDetailsObj" + masterDataDetailsObj);

                request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                Log.e("Log", "request" + request);

                response = okHttpClient.newCall(request).execute();
                Log.e("Log", "response" + response);

                if (!response.isSuccessful())
                {
                    return "failure";
                }

                resultString = response.body().string();
                Log.e("Log", "MasterResultString" + resultString);

                Gson gson = new Gson();
                masterDataJson = gson.fromJson(resultString, MasterDataJson.class);
                Log.e("Log", "masterDataJson" + masterDataJson);

                if (masterDataJson.getData().getVendorMaster() == null || masterDataJson.getData().getVendorMaster().size() == 0 || masterDataJson.getData().getVendorMaster().isEmpty() || masterDataJson.getData().getItemMaster().isEmpty() || masterDataJson.getData().getItemMaster() == null || masterDataJson.getData().getItemMaster().size() == 0)
                {
                    return "nullException";
                }
            }
            catch (Exception e)
            {
                Log.e("Log", "Exception", e);
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
                context.get().setMasterData(masterDataJson);

            }
            else if (s.equals("failure"))
            {
                Toast.makeText(context.get(), "Pull Failed", Toast.LENGTH_SHORT).show();
            } else if (s.equals("nullException"))
            {
                Toast.makeText(context.get(), "Null Exception From Server", Toast.LENGTH_SHORT).show();
            }

            pd.dismiss();
        }
    }

    private void setMasterData(MasterDataJson masterDataJson)
    {
        this.masterDataJson = masterDataJson;

        dbHelper.deleteVendorMaster();
        dbHelper.deleteItemMaster();
        dbHelper.insertMasterData(masterDataJson);

        /**
         *  Searchable Vendor Spinner
         */

        binding.vendorMasterDropdown.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dialog=new Dialog(SalesRequestActivity.this);
                //set  (our custom layout for dialog)
                dialog.setContentView(R.layout.layout_searchable_spinner);

                //set transparent background
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                //show dialog
                dialog.show();

                //initialize and assign variable
                EditText editText=dialog.findViewById(R.id.editText_of_searchableSpinner);
                ListView listView=dialog.findViewById(R.id.listView_of_searchableSpinner);
                //array adapter
                vendorMasterArrayList = dbHelper.getVendorMaster();
                Log.e("Log", "vendorMasterArrayList" + vendorMasterArrayList);
                ArrayAdapter<VendorMaster> adapterVendorMaster = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, vendorMasterArrayList);
                listView.setAdapter(adapterVendorMaster);


                //Textwatcher for change data after every text type by user

                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        //filter arraylist
                        adapterVendorMaster.getFilter().filter(charSequence);
                    }
                    @Override
                    public void afterTextChanged(Editable editable) {
                    }
                });

                // listview onitem click listener
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        // textViewSpinner.setText( adapterBranchMaster.getItem(i));
                        binding.vendorMasterDropdown.setText(adapterVendorMaster.getItem(position).getVendorName());

                        idVendor = adapterVendorMaster.getItem(position).getIdVendor();
                        Log.e("Log", "idVendor : " + idVendor);

                        new PullSalesRequestDataFilling(SalesRequestActivity.this, idVendor).execute();

                        Toast.makeText(SalesRequestActivity.this, "Selected:"+ adapterVendorMaster.getItem(position).getVendorName(), Toast.LENGTH_SHORT).show();
                        //dismiss dialog after choose
                        dialog.dismiss();
                    }
                });
            }
        });

        /**
         * Searchable Product Spinner
         */

        binding.productMasterDropdown.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dialog=new Dialog(SalesRequestActivity.this);
                //set  (our custom layout for dialog)
                dialog.setContentView(R.layout.layout_searchable_spinner);

                //set transparent background
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                //show dialog
                dialog.show();

                //initialize and assign variable
                EditText editText=dialog.findViewById(R.id.editText_of_searchableSpinner);
                ListView listView=dialog.findViewById(R.id.listView_of_searchableSpinner);
                //array adapter
                itemMasterArrayList = dbHelper.getItemMaster();
                Log.e("Log", "itemMasterArrayList" + itemMasterArrayList);
                ArrayAdapter<ItemMaster> adapterItemMaster = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, itemMasterArrayList);
                listView.setAdapter(adapterItemMaster);

                //Textwatcher for change data after every text type by user

                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        //filter arraylist
                        adapterItemMaster.getFilter().filter(charSequence);
                    }
                    @Override
                    public void afterTextChanged(Editable editable) {
                    }
                });

                // listview onitem click listener
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        // textViewSpinner.setText( adapterBranchMaster.getItem(i));
                        binding.productMasterDropdown.setText(adapterItemMaster.getItem(position).getItemName());
                        idItemMaster = adapterItemMaster.getItem(position).getIdItem();
                        Log.e("Log", "idItemMaster : " + idItemMaster);

                        Toast.makeText(SalesRequestActivity.this, "Selected:"+ adapterItemMaster.getItem(position).getItemName(), Toast.LENGTH_SHORT).show();
                        //dismiss dialog after choose
                        dialog.dismiss();
                    }
                });
            }
        });


    }

    private static class PullSalesRequestDataFilling extends AsyncTask<String, String, String>
    {
        WeakReference<SalesRequestActivity> context;
        ProgressDialog pd;
        OkHttpClient okHttpClient;
        String url, resultString;
        Request request;
        Response response;
        SharedPreferences shp;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        SalesRequestDataFillingJson salesRequestDataFillingJson;
        int idVendor;


        public PullSalesRequestDataFilling(SalesRequestActivity context, int idVendor)
        {
            this.context = new WeakReference<>(context);
            this.idVendor = idVendor;

            CustomTrust customTrust = new CustomTrust(context);
            OkHttpClient client = customTrust.getClient();
            okHttpClient = client;

            shp = context.getSharedPreferences(Const.Shared_Pref_name, MODE_PRIVATE);
        }


        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pd = new ProgressDialog(context.get());
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setMessage("Loading");
            pd.setCancelable(false);
            pd.show();
        }
        @Override
        protected String doInBackground(String... strings)
        {
            try
            {

                JSONObject dataFillingObj = new JSONObject();
                dataFillingObj.put("idVendor", idVendor);
                dataFillingObj.put("createdBy", shp.getString(Const.Shp_Employee_Code, ""));

                url = USING_IP + URL_SALES_DATA_FILLING;
                Log.e("Log", "salesDataFillingURL" + url);

                RequestBody body = RequestBody.create(dataFillingObj.toString(), JSON);
                Log.e("Log", "dataFillingObj" + dataFillingObj);

                request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                Log.e("Log", "request" + request);

                response = okHttpClient.newCall(request).execute();
                Log.e("Log", "response" + response);

                if (!response.isSuccessful())
                {
                    return "failure";
                }

                resultString = response.body().string();
                Log.e("Log", "SalesDataFillingResultString" + resultString);

                Gson gson = new Gson();
                salesRequestDataFillingJson = gson.fromJson(resultString, SalesRequestDataFillingJson.class);
                Log.e("Log", "salesRequestDataFillingJson" + salesRequestDataFillingJson);

                if (salesRequestDataFillingJson.getData().getSalesRequestDataFillingDetails() == null || salesRequestDataFillingJson.getData().getSalesRequestDataFillingDetails().size() == 0 || salesRequestDataFillingJson.getData().getSalesRequestDataFillingDetails().isEmpty())
                {
                    return "nullException";
                }
            }
            catch (Exception e)
            {
                Log.e("Log", "Exception", e);
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
                context.get().clearFillingDetails();
                context.get().setFillingData(salesRequestDataFillingJson);
                Toast.makeText(context.get(), "Data Fetched Successfully", Toast.LENGTH_SHORT).show();
            }
            else if (s.equals("failure"))
            {
                Toast.makeText(context.get(), "Pull Failed", Toast.LENGTH_SHORT).show();
            } else if (s.equals("nullException"))
            {
                Toast.makeText(context.get(), "Null Exception From Server", Toast.LENGTH_SHORT).show();
            }

            pd.dismiss();
        }

    }

    private void clearFillingDetails()
    {
        binding.vendorMailId.setText("");
        binding.deliveryLocation.setText("");
        binding.mobileNumber.setText("");
        binding.productMasterDropdown.setText("");
        binding.quantityET.setText("");
        binding.unitET.setText("");
        binding.deliveryLocation.setText("");
        binding.possibleDeliveryDateEditText.setText("");
    }

    private void setFillingData(SalesRequestDataFillingJson salesRequestDataFillingJson)
    {
        this.salesRequestDataFillingJson = salesRequestDataFillingJson;

        /**
         * Filling Email
         */
        if (salesRequestDataFillingJson.getData().getSalesRequestDataFillingDetails().get(0).geteMail().isEmpty())
        {
            Toast.makeText(this, "Please Enter Your Email", Toast.LENGTH_LONG).show();
        }
        else
        {
            binding.vendorMailId.setText(salesRequestDataFillingJson.getData().getSalesRequestDataFillingDetails().get(0).geteMail());
        }

        /**
         * Filling MobileNumber
         */
        if (salesRequestDataFillingJson.getData().getSalesRequestDataFillingDetails().get(0).getMobileNumber().isEmpty())
        {
            Toast.makeText(this, "Please Enter Your Phone Number", Toast.LENGTH_LONG).show();
        }
        else
        {
            binding.vendorMailId.setText(salesRequestDataFillingJson.getData().getSalesRequestDataFillingDetails().get(0).getMobileNumber());
        }

    }

    private static class PushSalesRequest extends AsyncTask<String, String, String>
    {
        WeakReference<SalesRequestActivity> context;
        ProgressDialog pd;
        OkHttpClient okHttpClient;
        String url, resultString;
        Request request;
        Response response;
        SharedPreferences shp;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        SendSalesRequestJson sendSalesRequestJson;
        int status;
        String statusMessage;
        int idVendor, idItemMaster, quantityFromET;
        String descriptionETString;
        String possibleDeliveryDateString, deliveryLocationString, mailIdString, mobileNumberString, unitFromET;
        public PushSalesRequest(SalesRequestActivity context, int idVendor, int idItemMaster, int quantityFromET, String unitFromET, String possibleDeliveryDateString, String deliveryLocationString, String mailIdString, String mobileNumberString, String descriptionETString)
        {
            this.context = new WeakReference<>(context);
            this.idVendor = idVendor;
            this.idItemMaster = idItemMaster;
            this.quantityFromET = quantityFromET;
            this.unitFromET = unitFromET;
            this.deliveryLocationString = deliveryLocationString;
            this.possibleDeliveryDateString = possibleDeliveryDateString;
            this.mailIdString = mailIdString;
            this.mobileNumberString = mobileNumberString;
            this.descriptionETString = descriptionETString;

            CustomTrust customTrust = new CustomTrust(context);
            OkHttpClient client = customTrust.getClient();
            okHttpClient = client;
            shp = context.getSharedPreferences(Const.Shared_Pref_name, MODE_PRIVATE);
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pd = new ProgressDialog(context.get());
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setMessage("Loading");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... strings)
        {
            try
            {

                JSONObject sendSalesRequestObj = new JSONObject();
                sendSalesRequestObj.put("requestDate", possibleDeliveryDateString);
                sendSalesRequestObj.put("idItem", idItemMaster);
                sendSalesRequestObj.put("orderQty", quantityFromET);
                sendSalesRequestObj.put("createdBy", shp.getString(Const.Shp_Employee_Code, ""));
                sendSalesRequestObj.put("description", descriptionETString);

                url = USING_IP + URL_SEND_SALES_REQUEST;
                Log.e("Log", "sendSalesRequestURL" + url);

                RequestBody body = RequestBody.create(sendSalesRequestObj.toString(), JSON);
                Log.e("Log", "sendSalesRequestObj" + sendSalesRequestObj);

                request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                Log.e("Log", "request" + request);

                response = okHttpClient.newCall(request).execute();
                Log.e("Log", "response" + response);

                if (!response.isSuccessful())
                {
                    return "failure";
                }

                resultString = response.body().string();
                Log.e("Log", "sendSalesRequestResultString" + resultString);

                Gson gson = new Gson();
                sendSalesRequestJson = gson.fromJson(resultString, SendSalesRequestJson.class);
                Log.e("Log", "sendSalesRequestJson" + sendSalesRequestJson);

                if (sendSalesRequestJson.getData().getSendSalesRequestStatus() == null || sendSalesRequestJson.getData().getSendSalesRequestStatus().size() == 0 || sendSalesRequestJson.getData().getSendSalesRequestStatus().isEmpty())
                {
                    return "nullException";
                }
                else
                {
                    status = sendSalesRequestJson.getData().getSendSalesRequestStatus().get(0).getStatus();

                    if (status != 1)
                    {
                        return "failure";
                    }
                }
            }
            catch (Exception e)
            {
                Log.e("Log", "Exception", e);
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
                statusMessage = sendSalesRequestJson.getData().getSendSalesRequestStatus().get(0).getStatusMsg();
               // Fns.neutralAlert("Alert", statusMessage, context.get());

                AlertDialog.Builder adb = new AlertDialog.Builder(context.get());

                TextView titletxtview = new TextView(context.get());
                titletxtview.setText("Alert");
                titletxtview.setBackgroundColor(ContextCompat.getColor(context.get(), R.color.yellow));
                titletxtview.setPadding(10, 10, 10, 10);
                titletxtview.setGravity(Gravity.CENTER);
                titletxtview.setTextColor(Color.WHITE);
                titletxtview.setTextSize(20);

                adb.setCustomTitle(titletxtview);

                TextView messagetxtview = new TextView(context.get());
                messagetxtview.setText(statusMessage);
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
                        if (sendSalesRequestJson.getData().getSendSalesRequestStatus().get(0).getStatus() == 1)
                        {
                            context.get().clearFillingDetails();

                            Intent intent = new Intent(context.get(), MainMenuActivity.class);
                            context.get().startActivity(intent);

                        }

                    }
                });

                adb.setPositiveButton("Share", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                        String shareBody = statusMessage;
                        Log.e("LogFns", "fns message" + shareBody);
                        intent.setType("text/plain");
                        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share");
                        intent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                        context.get().startActivity(Intent.createChooser(intent, "Share using"));

                    }
                });
                AlertDialog ad = adb.create();
                ad.show();


            }
            else if (s.equals("failure"))
            {

                statusMessage = sendSalesRequestJson.getData().getSendSalesRequestStatus().get(0).getStatusMsg();
                Fns.neutralAlert("Alert", statusMessage, context.get());

            }
            else if (s.equals("nullException"))
            {
                Toast.makeText(context.get(), "Null Exception From Server", Toast.LENGTH_SHORT).show();
            }

            pd.dismiss();
        }
    }
}