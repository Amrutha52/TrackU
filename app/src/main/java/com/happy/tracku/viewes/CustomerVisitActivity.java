package com.happy.tracku.viewes;

import static com.happy.tracku.utils.Const.URL_CUSTOMER_VISIT_INSERT;
import static com.happy.tracku.utils.Const.USING_IP;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.happy.tracku.R;
import com.happy.tracku.db.DbHelper;
import com.happy.tracku.gson.insertcustomervisitdetails.CustomerVisitDetailsJson;
import com.happy.tracku.ssl.CustomTrust;
import com.happy.tracku.utils.Const;

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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_visit);

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
                String companyName = s.toString();
                Log.e("Log", "companyNameAfterTextChanged" + companyName);
                if (companyName.length() >= 2) { // Set a minimum length to avoid unnecessary calls
                    new fetchCompanyDetails(CustomerVisitActivity.this,companyName).execute();
                }
            }
        });
    }

    public void listeners(View view)
    {
        switch (view.getId())
        {

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
               // Fns.neutralAlert("Alert", addEmployeeJson.getData().getInsertEmployeeDetailsStatus().get(0).getStatusMsg(), mContext);
                // Toast.makeText(textWatcher, "Updation Failed", Toast.LENGTH_SHORT).show();
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
}