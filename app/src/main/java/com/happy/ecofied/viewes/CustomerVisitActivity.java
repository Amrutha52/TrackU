package com.happy.tracku.viewes;

import static com.happy.tracku.utils.Const.URL_CUSTOMER_VISIT_INSERT;
import static com.happy.tracku.utils.Const.USING_IP;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.happy.tracku.R;
import com.happy.tracku.db.DbHelper;
import com.happy.tracku.gson.insertcustomervisitdetails.CustomerVisitDetailsJson;
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

public class CustomerVisitActivity extends AppCompatActivity
{
    TextInputEditText companyNameET, companyEmailET, contactPersonNameET, contactPersonNumberET, contactPersonEmailET;
    CustomerVisitDetailsJson customerVisitDetailsJson;
    String companyNameString, companyEmailString, contactPersonNameString, contactPersonNumberString, contactPersonEmailString;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_visit);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Customer Visit");

        View rootView = findViewById(android.R.id.content); // Get root view

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            WindowInsetsCompat insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets.toWindowInsets());
            int systemBarsInsetsTop = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int systemBarsInsetsBottom = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

            // Apply padding to your main content view
            v.setPadding(v.getPaddingLeft(), systemBarsInsetsTop, v.getPaddingRight(), systemBarsInsetsBottom);

            return WindowInsetsCompat.CONSUMED;
        });

        companyNameET = findViewById(R.id.companyNameET);
        companyEmailET = findViewById(R.id.companyEmailET);
        contactPersonNameET = findViewById(R.id.contactPersonNameET);
        contactPersonEmailET = findViewById(R.id.contactPersonEmailET);
        contactPersonNumberET = findViewById(R.id.contactNumberET);

        companyNameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                companyNameString = s.toString();
                Log.e("Log", "companyNameAfterTextChanged" + companyNameString);
                if (companyNameString.length() >= 2) { // Set a minimum length to avoid unnecessary calls
                    new fetchCompanyDetails(CustomerVisitActivity.this,companyNameString).execute();
                }
            }
        });
    }

    public void listeners(View view)
    {
        switch (view.getId())
        {
            case R.id.customer_visit_details_save_button:
            {
                companyNameString = companyNameET.getText().toString();
                companyEmailString = companyEmailET.getText().toString();
                contactPersonNameString = contactPersonNameET.getText().toString();
                contactPersonNumberString = contactPersonNumberET.getText().toString();
                contactPersonEmailString = contactPersonEmailET.getText().toString();

                new SaveCustomerVisitDetails(this, companyNameString, companyEmailString, contactPersonNameString, contactPersonNumberString, contactPersonEmailString).execute();
            }
            break;
        }
    }

    private class fetchCompanyDetails extends AsyncTask<String, String, String>
    {
        OkHttpClient okHttpClient;
        String url;
        Request request;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        WeakReference<CustomerVisitActivity> mContext;
        ProgressDialog pd;
        SharedPreferences shp;
        String failureMsg, resultString;
        CustomerVisitDetailsJson customerVisitDetailsJson;
        DbHelper dbHelper;
        String companyName;

        TextWatcher textWatcher;

        public fetchCompanyDetails(CustomerVisitActivity mContext, String companyName)
        {
            this.mContext = new WeakReference<>(mContext);;
            this.companyName = companyName;

            CustomTrust customTrust = new CustomTrust(mContext);
            OkHttpClient client = customTrust.getClient();
            okHttpClient = client;
                    /*= new OkHttpClient.Builder()
                    .connectTimeout(180, TimeUnit.SECONDS)
                    .callTimeout(180, TimeUnit.SECONDS)
                    .readTimeout(180, TimeUnit.SECONDS)
                    .build();*/
            pd = new ProgressDialog(mContext);
            shp = mContext.getSharedPreferences(Const.Shared_Pref_name, MODE_PRIVATE);
            dbHelper = new DbHelper(mContext);

           // pd.setTitle("Please wait");
          //  pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
           // pd.setMessage("wait...");
           // pd.setCancelable(false);

        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            try
            {
              //  pd.show();

            }
            catch (Exception e)
            {
                Log.e("Log", "Exception", e);
            }

        }

        @Override
        protected String doInBackground(String... strings)
        {
            try
            {

                JSONObject pullCustomerVisitDetailsObj = new JSONObject();
                pullCustomerVisitDetailsObj.put("action", 10);
                pullCustomerVisitDetailsObj.put("createdBy", shp.getString(Const.Shp_Employee_Code, ""));
                pullCustomerVisitDetailsObj.put("companyName", companyName);
                pullCustomerVisitDetailsObj.put("companyEmail", "");
                pullCustomerVisitDetailsObj.put("contactPerson", "");
                pullCustomerVisitDetailsObj.put("contactNumber", "");
                pullCustomerVisitDetailsObj.put("contactPersonEmail", "");

                url = USING_IP + URL_CUSTOMER_VISIT_INSERT;
                Log.e("Log", "customerVisitURL" + url);
                Log.e("Log", "pullCustomerVisitDetailsObj" + pullCustomerVisitDetailsObj);

                RequestBody body = RequestBody.create(pullCustomerVisitDetailsObj.toString(), JSON);

                request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                Log.e("Log", "request" + request);

                Response response = okHttpClient.newCall(request).execute();
                Log.e("Log", "response" + response);

                if (!response.isSuccessful())
                {
                    return "failure";
                }

                resultString = response.body().string();
                Log.e("Log", "resultString" + resultString);

                Gson gson = new Gson();
                customerVisitDetailsJson = gson.fromJson(resultString, CustomerVisitDetailsJson.class);

                if (customerVisitDetailsJson.getData().getCustomerVisitDetailsStatus().isEmpty() || customerVisitDetailsJson.getData().getCustomerVisitDetailsStatus().size() == 0 || customerVisitDetailsJson.getData().getCustomerVisitDetailsStatus() == null)
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
            pd.dismiss();

            if (s.equals("success"))
            {
                autofillFields(customerVisitDetailsJson);
            }
            else if (s.equals("failure"))
            {
               // mContext.get().clearDetails();
               // Fns.neutralAlert("Alert", addEmployeeJson.getData().getInsertEmployeeDetailsStatus().get(0).getStatusMsg(), mContext);
                 Toast.makeText(mContext.get(), "Failed", Toast.LENGTH_SHORT).show();
            }
            else if (s.equals("nullException"))
            {
                Toast.makeText(mContext.get(), "Null Exception From Server", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void autofillFields(CustomerVisitDetailsJson customerVisitDetailsJson)
    {
        this.customerVisitDetailsJson = customerVisitDetailsJson;

        companyEmailET.setText(customerVisitDetailsJson.getData().getCustomerVisitDetailsStatus().get(0).getCompanyEmail());
        contactPersonNameET.setText(customerVisitDetailsJson.getData().getCustomerVisitDetailsStatus().get(0).getContactPerson());
        contactPersonNumberET.setText(customerVisitDetailsJson.getData().getCustomerVisitDetailsStatus().get(0).getContactPerson());
        contactPersonEmailET.setText(customerVisitDetailsJson.getData().getCustomerVisitDetailsStatus().get(0).getContactPersonEmail());
    }

    private static class SaveCustomerVisitDetails extends AsyncTask<String, String, String>
    {
        OkHttpClient okHttpClient;
        String url;
        Request request;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        WeakReference<CustomerVisitActivity> mContext;
        ProgressDialog pd;
        SharedPreferences shp;
        String failureMsg, resultString;
        CustomerVisitDetailsJson customerVisitDetailsJson;
        DbHelper dbHelper;
        String companyNameString, companyEmailString, contactPersonNameString, contactPersonNumberString,contactPersonEmailString;
        int status;
        String statusMessage;
        public SaveCustomerVisitDetails(CustomerVisitActivity mContext, String companyNameString, String companyEmailString, String contactPersonNameString, String contactPersonNumberString, String contactPersonEmailString)
        {
            this.mContext = new WeakReference<>(mContext);
            this.companyNameString = companyNameString;
            this.companyEmailString = companyEmailString;
            this.contactPersonNameString = contactPersonNameString;
            this.contactPersonEmailString = contactPersonEmailString;
            this.contactPersonNumberString = contactPersonNumberString;

            CustomTrust customTrust = new CustomTrust(mContext);
            OkHttpClient client = customTrust.getClient();
            okHttpClient = client;

            pd = new ProgressDialog(mContext);
            shp = mContext.getSharedPreferences(Const.Shared_Pref_name, MODE_PRIVATE);
            dbHelper = new DbHelper(mContext);

            pd.setTitle("Please wait");
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setMessage("wait...");
            pd.setCancelable(false);
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            try
            {
                pd.show();

            }
            catch (Exception e)
            {
                Log.e("Log", "Exception", e);
            }

        }

        @Override
        protected String doInBackground(String... strings)
        {
            try
            {

                JSONObject pullCustomerVisitDetailsObj = new JSONObject();
                pullCustomerVisitDetailsObj.put("action", 11);
                pullCustomerVisitDetailsObj.put("createdBy", shp.getString(Const.Shp_Employee_Code, ""));
                pullCustomerVisitDetailsObj.put("companyName", companyNameString);
                pullCustomerVisitDetailsObj.put("companyEmail", companyEmailString);
                pullCustomerVisitDetailsObj.put("contactPerson", contactPersonNameString);
                pullCustomerVisitDetailsObj.put("contactNumber", contactPersonNumberString);
                pullCustomerVisitDetailsObj.put("contactPersonEmail", contactPersonEmailString);

                url = USING_IP + URL_CUSTOMER_VISIT_INSERT;
                Log.e("Log", "customerVisitURL" + url);
                Log.e("Log", "pullCustomerVisitDetailsObj" + pullCustomerVisitDetailsObj);

                RequestBody body = RequestBody.create(pullCustomerVisitDetailsObj.toString(), JSON);

                request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                Log.e("Log", "request" + request);

                Response response = okHttpClient.newCall(request).execute();
                Log.e("Log", "response" + response);

                if (!response.isSuccessful())
                {
                    return "failure";
                }

                resultString = response.body().string();
                Log.e("Log", "resultString" + resultString);

                Gson gson = new Gson();
                customerVisitDetailsJson = gson.fromJson(resultString, CustomerVisitDetailsJson.class);

                if (customerVisitDetailsJson.getData().getCustomerVisitDetailsStatus().isEmpty() || customerVisitDetailsJson.getData().getCustomerVisitDetailsStatus().size() == 0 || customerVisitDetailsJson.getData().getCustomerVisitDetailsStatus() == null)
                {
                    return "nullException";
                }
                else
                {
                    status = customerVisitDetailsJson.getData().getCustomerVisitDetailsStatus().get(0).getStatus();
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
            pd.dismiss();

            if (s.equals("success"))
            {
               statusMessage = customerVisitDetailsJson.getData().getCustomerVisitDetailsStatus().get(0).getStatusMsg();

              //  Fns.neutralAlert("Alert", customerVisitDetailsJson.getData().getCustomerVisitDetailsStatus().get(0).getStatusMsg(), mContext.get());

                AlertDialog.Builder adb = new AlertDialog.Builder(mContext.get());

                TextView titletxtview = new TextView(mContext.get());
                titletxtview.setText("Alert");
                titletxtview.setBackgroundColor(ContextCompat.getColor(mContext.get(), R.color.colorPrimary));
                titletxtview.setPadding(10, 10, 10, 10);
                titletxtview.setGravity(Gravity.CENTER);
                titletxtview.setTextColor(Color.WHITE);
                titletxtview.setTextSize(20);

                adb.setCustomTitle(titletxtview);

                TextView messagetxtview = new TextView(mContext.get());
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
                        if (customerVisitDetailsJson.getData().getCustomerVisitDetailsStatus().get(0).getStatus() == 1)
                        {
                            mContext.get().clearDetails();
                            mContext.get().finish();
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
                        mContext.get().startActivity(Intent.createChooser(intent, "Share using"));

                    }
                });
                AlertDialog ad = adb.create();
                ad.show();


            }
            else if (s.equals("failure"))
            {
                Fns.neutralAlert("Alert", customerVisitDetailsJson.getData().getCustomerVisitDetailsStatus().get(0).getStatusMsg(), mContext.get());
                mContext.get().clearDetails();
                // Toast.makeText(textWatcher, "Updation Failed", Toast.LENGTH_SHORT).show();
            }
            else if (s.equals("nullException"))
            {
                Toast.makeText(mContext.get(), "Null Exception From Server", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void clearDetails()
    {
        companyNameET.setText("");
        companyEmailET.setText("");
        contactPersonNameET.setText("");
        contactPersonNumberET.setText("");
        contactPersonEmailET.setText("");
    }

}