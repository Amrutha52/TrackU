package com.happy.tracku.viewes;

import static com.happy.tracku.utils.Const.URL_CREATE_EMPLOYEE;
import static com.happy.tracku.utils.Const.USING_IP;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.happy.tracku.R;
import com.happy.tracku.db.DbHelper;
import com.happy.tracku.gson.addemployeejsondetails.AddEmployeeJson;
import com.happy.tracku.gson.dailywisegpsdatajsondetails.DailyWiseGPSDataJson;
import com.happy.tracku.gson.dailywisegpsdatajsondetails.DailyWiseGPSDataResponsestatus;
import com.happy.tracku.ssl.CustomTrust;
import com.happy.tracku.utils.Const;
import com.happy.tracku.utils.Fns;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddEmployeeActivity extends AppCompatActivity
{
    TextInputEditText employeeNameET, employeeAddressET, pinCodeET, mobileNumberET;
    TextInputEditText employeeEmailET, loginET, passwordET, employeeCodeET;
    MaterialAutoCompleteTextView isAdminDropDown;
    int isAdmin;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_employee);

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setTitle("Add Employee");

        employeeNameET = findViewById(R.id.employeeNameET);
        employeeAddressET = findViewById(R.id.employeeAddressET);
        pinCodeET = findViewById(R.id.pincodeET);
        mobileNumberET = findViewById(R.id.mobileNumberET);
        employeeEmailET = findViewById(R.id.employeeEmailET);
        loginET = findViewById(R.id.loginET);
        passwordET = findViewById(R.id.passwordET);
        employeeCodeET = findViewById(R.id.employeeCodeET);
        isAdminDropDown = findViewById(R.id.isAdmin_dropdown);

        String[] isAdminArray = this.getResources().getStringArray(R.array.isAdmin);
        ArrayAdapter<String> isAdminArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, isAdminArray);
        isAdminDropDown.setAdapter(isAdminArrayAdapter);

        isAdminDropDown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                isAdmin = (int) isAdminArrayAdapter.getItemId(position);
                Log.e("Log", "isAdmin: " + isAdmin);

            }
        });

    }

    public void listeners(View view)
    {
        switch(view.getId())
        {
            case R.id.employee_details_save_button:
            {
                new AddEmployee(this, employeeNameET.getText().toString(), employeeAddressET.getText().toString(), pinCodeET.getText().toString(), mobileNumberET.getText().toString(), employeeEmailET.getText().toString(), loginET.getText().toString(), passwordET.getText().toString(), employeeCodeET.getText().toString(), isAdmin).execute();

            }
            break;
        }
    }

    private static class AddEmployee extends AsyncTask<String, String, String>
    {
        OkHttpClient okHttpClient;
        String url;
        Request request;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        AddEmployeeActivity mContext;

        ProgressDialog pd;
        SharedPreferences shp;
        String failureMsg, resultString;
        AddEmployeeJson addEmployeeJson;
        DbHelper dbHelper;
        int isAdmin, status;
        String employeeNameString, employeeAddressString, pincodeString, mobileNumberString, emailString, loginString, passwordString, employeeCodeString;
        public AddEmployee(AddEmployeeActivity mContext, String employeeNameString, String employeeAddressString, String pincodeString, String mobileNumberString, String emailString, String loginString, String passwordString, String employeeCodeString, int isAdmin)
        {
            Log.e("Log", "InsideAddEmployee");
            this.mContext = mContext;
            this.employeeNameString = employeeNameString;
            this.employeeAddressString = employeeAddressString;
            this.pincodeString = pincodeString;
            this.mobileNumberString = mobileNumberString;
            this.emailString = emailString;
            this.loginString = loginString;
            this.passwordString = passwordString;
            this.employeeCodeString = employeeCodeString;
            this.isAdmin = isAdmin;

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

            try {

                pd.show();


            } catch (Exception e) {

                Log.e("Log", "Exception", e);

            }

        }
        @Override
        protected String doInBackground(String... strings)
        {
            try
            {

                JSONObject addEmployeeObj = new JSONObject();
                addEmployeeObj.put("createdBy", shp.getString(Const.Shp_Employee_Code, ""));
                addEmployeeObj.put("name", employeeNameString);
                addEmployeeObj.put("address", employeeAddressString);
                addEmployeeObj.put("pin", pincodeString);
                addEmployeeObj.put("contactNumber", mobileNumberString);
                addEmployeeObj.put("email", emailString);
                addEmployeeObj.put("loginId", loginString);
                addEmployeeObj.put("password", passwordString);
                addEmployeeObj.put("isAdmin", isAdmin);
                addEmployeeObj.put("employeeCode", employeeCodeString);


                url = USING_IP + URL_CREATE_EMPLOYEE;
                Log.e("Log", "addEmployeeURL" + url);
                Log.e("Log", "addEmployeeJsonObject" + addEmployeeObj);

                RequestBody body = RequestBody.create(addEmployeeObj.toString(), JSON);

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
                addEmployeeJson = gson.fromJson(resultString, AddEmployeeJson.class);

                if (addEmployeeJson.getData().getInsertEmployeeDetailsStatus().isEmpty() || addEmployeeJson.getData().getInsertEmployeeDetailsStatus().size() == 0)
                {
                    return "nullException";
                }
                else
                {
                    status = addEmployeeJson.getData().getInsertEmployeeDetailsStatus().get(0).getStatus();
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
                Fns.neutralAlert("Alert", addEmployeeJson.getData().getInsertEmployeeDetailsStatus().get(0).getStatusMsg(), mContext);

                if (addEmployeeJson.getData().getInsertEmployeeDetailsStatus().get(0).getStatus() == 1)
                {
                    mContext.clearDetails();
                    mContext.finish();
                }
            }
            else if (s.equals("failure"))
            {
                Fns.neutralAlert("Alert", addEmployeeJson.getData().getInsertEmployeeDetailsStatus().get(0).getStatusMsg(), mContext);
                // Toast.makeText(context.get(), "Updation Failed", Toast.LENGTH_SHORT).show();
            } else if (s.equals(""))
            {

            }
        }
    }

    private void clearDetails()
    {
        employeeNameET.setText("");
        employeeAddressET.setText("");
        pinCodeET.setText("");
        mobileNumberET.setText("");
        employeeEmailET.setText("");
        employeeCodeET.setText("");
        loginET.setText("");
        passwordET.setText("");
        isAdminDropDown.setSelection(0);
    }
}