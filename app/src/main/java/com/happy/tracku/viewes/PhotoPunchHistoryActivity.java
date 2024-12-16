package com.happy.tracku.viewes;

import static com.happy.tracku.utils.Const.URL_PUNCH_HISTORY;
import static com.happy.tracku.utils.Const.USING_IP;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.gson.Gson;
import com.happy.tracku.R;
import com.happy.tracku.adapters.PhotoPunchHistoryAdapters;
import com.happy.tracku.db.DbHelper;
import com.happy.tracku.gson.employeemasterdetails.EmployeeMasterDetail;
import com.happy.tracku.gson.photopunchhistoryjson.GetPunchHistoryDetail;
import com.happy.tracku.gson.photopunchhistoryjson.Punchinghistoryjson;
import com.happy.tracku.ssl.CustomTrust;
import com.happy.tracku.utils.Const;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PhotoPunchHistoryActivity extends AppCompatActivity
{

    EditText fromDateET, toDateET;
    DbHelper dbHelper;
    MaterialAutoCompleteTextView employeeCodeMATV;
    List<EmployeeMasterDetail> employeeMasterDetailList;
    String employeeCode;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_punch_history);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Punch History");

        fromDateET = findViewById(R.id.fromDateET);
        toDateET = findViewById(R.id.toDateET);
        employeeCodeMATV = findViewById(R.id.employeecodeMATV);

        dbHelper = new DbHelper(this);

        new PullEmployeeMasterDetails(this).execute();

        toDateET.setText(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));

        fromDateET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // on below line we are getting
                // the instance of our calendar.
                final Calendar c = Calendar.getInstance();

                // on below line we are getting
                // our day, month and year.
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                // on below line we are creating a variable for date picker dialog.
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        // on below line we are passing context.
                        PhotoPunchHistoryActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // on below line we are setting date to our edit text.
                                fromDateET.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                              //  monthET.setText(String.valueOf((monthOfYear + 1)));
                            }
                        },
                        // on below line we are passing year,
                        // month and day for selected date in our date picker.
                        year, month, day);
                // at last we are calling show to
                // display our date picker dialog.
                datePickerDialog.show();
            }
        });

        toDateET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // on below line we are getting
                // the instance of our calendar.
                final Calendar c = Calendar.getInstance();

                // on below line we are getting
                // our day, month and year.
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                // on below line we are creating a variable for date picker dialog.
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        // on below line we are passing context.
                        PhotoPunchHistoryActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // on below line we are setting date to our edit text.
                                toDateET.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                                //yearET.setText(String.valueOf(year));

                            }
                        },
                        // on below line we are passing year,
                        // month and day for selected date in our date picker.
                        year, month, day);
                // at last we are calling show to
                // display our date picker dialog.
                datePickerDialog.show();
            }
        });

        employeeMasterDetailList = dbHelper.getEmployeeMaster();
        Log.e("Log", "employeeMasterDetailList" + employeeMasterDetailList);

        ArrayAdapter<EmployeeMasterDetail> adpterEmployeeMaster = new ArrayAdapter<EmployeeMasterDetail>(this, android.R.layout.simple_dropdown_item_1line, employeeMasterDetailList);

        employeeCodeMATV.setAdapter(adpterEmployeeMaster);

        employeeCodeMATV.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View arg0)
            {
                employeeCodeMATV.showDropDown();
            }
        });

        employeeCodeMATV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                employeeCode = employeeMasterDetailList.get(position).getEmployeeCode();
                Log.e("Log", "employeeCode" + employeeCode);

            }
        });


    }

    public void listeners(View view)
    {
        switch (view.getId())
        {
            case R.id.pull_punch_data:
            {

                new GetPunchingHistory(this, fromDateET.getText().toString(), toDateET.getText().toString(), employeeCode).execute();
            }
            break;
        }
    }

    private static class GetPunchingHistory extends AsyncTask<String, String, String>
    {
        OkHttpClient okHttpClient;
        String url;
        Request request;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        PhotoPunchHistoryActivity mContext;
        ProgressDialog pd;
        SharedPreferences shp;
        String failureMsg, resultString;
        Punchinghistoryjson punchinghistoryjson;
        int status;
        String fromDate, toDate, employeeCode;
        RecyclerView punchingHistoryRecyclerview;

        public GetPunchingHistory(PhotoPunchHistoryActivity mContext, String fromDate, String toDate, String employeeCode)
        {
            this.mContext = mContext;
            this.fromDate = fromDate;
            this.toDate = toDate;
            this.employeeCode = employeeCode;

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
                JSONObject photoPunchHistoryObj = new JSONObject();
                photoPunchHistoryObj.put("createdBy", shp.getString(Const.Shp_Employee_Code, ""));
                photoPunchHistoryObj.put("employeeCode", employeeCode);
                photoPunchHistoryObj.put("fromDate", fromDate);
                photoPunchHistoryObj.put("toDate", toDate);
                photoPunchHistoryObj.put("isCompressed", 0);

                url = USING_IP + URL_PUNCH_HISTORY;
                Log.e("Log", "photoPunchHistoryURL" + url);
                Log.e("Log", "photoPunchHistoryJsonObject" + photoPunchHistoryObj);

                RequestBody body = RequestBody.create(photoPunchHistoryObj.toString(), JSON);
                request = new Request.Builder()
                        //.header("X-Client-Type", "Android")
                        .url(url)
                        .post(body)
                        .build();

                Response response = okHttpClient.newCall(request).execute();
                Log.e("Log", "response" + response);

                if (!response.isSuccessful()) {

                    return "failure";

                }

                resultString = response.body().string();
                Log.e("Log", "punch history result string" + resultString);

                Gson gson = new Gson();

                punchinghistoryjson = gson.fromJson(resultString, Punchinghistoryjson.class);
                Log.e("Log", "punchinghistoryjson" + punchinghistoryjson);

                if (punchinghistoryjson.getData().getGetPunchHistoryDetails().isEmpty() || punchinghistoryjson.getData().getGetPunchHistoryDetails().size() == 0 || punchinghistoryjson.getData().getGetPunchHistoryDetails() == null || punchinghistoryjson.getData().getGetPunchHistoryStatus().isEmpty() || punchinghistoryjson.getData().getGetPunchHistoryStatus().size() == 0 || punchinghistoryjson.getData().getGetPunchHistoryStatus() == null)
                {
                    return "nullException";
                }
                else if (punchinghistoryjson.getData().getGetPunchHistoryStatus().get(0).getStatus() != 1)
                {
                    return "failure";

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
                punchingHistoryRecyclerview =  mContext.findViewById(R.id.punch_history_recycler_view);
                List<GetPunchHistoryDetail> getPunchHistoryDetailList = punchinghistoryjson.getData().getGetPunchHistoryDetails();
                Log.e("Log", "getPunchHistoryDetailList size"+getPunchHistoryDetailList.size());

                PhotoPunchHistoryAdapters punchHistoryAdapters = new PhotoPunchHistoryAdapters(mContext, getPunchHistoryDetailList);
                Log.e("Log", "punchHistoryAdapters" + punchHistoryAdapters);

                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);

                punchingHistoryRecyclerview.setLayoutManager(layoutManager);
                punchingHistoryRecyclerview.setItemAnimator(new DefaultItemAnimator());
                punchingHistoryRecyclerview.setAdapter(punchHistoryAdapters);

            } else if (s.equals("nullException"))
            {
                Toast.makeText(mContext, "Null Exception from server", Toast.LENGTH_SHORT).show();
            }
            else if (s.equals("failure"))
            {
                Toast.makeText(mContext, "Failure", Toast.LENGTH_SHORT).show();
            } else
            {
                Toast.makeText(mContext, "Fetching Failed", Toast.LENGTH_SHORT).show();
            }

        }
    }
}