package com.happy.ecofied.viewes;

import static com.happy.ecofied.utils.Const.URL_TRAVEL_LOG;
import static com.happy.ecofied.utils.Const.USING_IP;

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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.happy.ecofied.R;
import com.happy.ecofied.adapters.TravelLogDetailsAdapter;
import com.happy.ecofied.databinding.ActivityTravelLogBinding;
import com.happy.ecofied.db.DbHelper;
import com.happy.ecofied.gson.employeemasterdetails.EmployeeMasterDetail;
import com.happy.ecofied.gson.travellogdetails.Travellogjson;
import com.happy.ecofied.ssl.CustomTrust;
import com.happy.ecofied.utils.Const;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TravelLogActivity extends AppCompatActivity
{
    private ActivityTravelLogBinding binding;
    DatePickerDialog pickUpDatePicker;
    List<EmployeeMasterDetail> employeeMasterDetailList;
    DbHelper dbHelper;
    String employeeCode;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityTravelLogBinding.inflate(getLayoutInflater());
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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(" Travel Log ");

        dbHelper = new DbHelper(this);

        new PullEmployeeMasterDetails(this).execute();

        binding.date.setText(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));

        binding.date.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);

                // date picker dialog

                pickUpDatePicker = new DatePickerDialog(TravelLogActivity.this,
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

                                binding.date.setText(year + "-" + monthString + "-" + dayString);
                                Log.e("pickUpDate", binding.date.getText().toString());

                            }
                        }, mYear, mMonth, mDay);

                //Date minDateObj = Calendar.getInstance().getTime();
                //pickUpDatePicker.getDatePicker().setMinDate(minDateObj.getTime());
                //pickUpDatePicker.getDatePicker().setMaxDate(minDateObj.getTime()+(365*24*60*60));
                pickUpDatePicker.show();
            }
        });

        employeeMasterDetailList = dbHelper.getEmployeeMaster();
        Log.e("Log", "employeeMasterDetailList" + employeeMasterDetailList);

        ArrayAdapter<EmployeeMasterDetail> adpterEmployeeMaster = new ArrayAdapter<EmployeeMasterDetail>(this, android.R.layout.simple_dropdown_item_1line, employeeMasterDetailList);

        binding.employeecodeMATV.setAdapter(adpterEmployeeMaster);

        binding.employeecodeMATV.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View arg0)
            {
                binding.employeecodeMATV.showDropDown();
            }
        });

        binding.employeecodeMATV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
            case R.id.pull_travel_log_list:
            {
                new PullTravelLogDetails(this, binding.date.getText().toString(), employeeCode).execute();
            }
            break;
        }
    }

    private static class PullTravelLogDetails extends AsyncTask<String, String, String>
    {
        WeakReference<TravelLogActivity> context;
        ProgressDialog pd;
        OkHttpClient okHttpClient;
        String url, resultString;
        Request request;
        Response response;
        SharedPreferences shp;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        Travellogjson travellogjson;
        DbHelper dbHelper;
        String employeeCode, date;

        public PullTravelLogDetails(TravelLogActivity context, String dateString, String employeeCode)
        {
            this.context = new WeakReference<>(context);
            this.employeeCode = employeeCode;
            this.date = dateString;

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

                JSONObject travelLogObject = new JSONObject();
                travelLogObject.put("fromDate", date);
                travelLogObject.put("employeeCode", employeeCode);
                travelLogObject.put("createdBy", "");

                url = USING_IP + URL_TRAVEL_LOG;
                Log.e("Log", "travelLogURL" + url);

                RequestBody body = RequestBody.create(travelLogObject.toString(), JSON);
                Log.e("Log", "travelLogObject" + travelLogObject.toString());

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
                Log.e("Log", "commonApprovalResultString" + resultString);

                Gson gson = new Gson();
                travellogjson = gson.fromJson(resultString, Travellogjson.class);
                Log.e("Log", "travellogjson" + travellogjson);

                if (travellogjson.getData().getTravelLogDetails().isEmpty() || travellogjson.getData().getTravelLogDetails().size() == 0)
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

                    RecyclerView travelDetailsRecyclerView = context.get().findViewById(R.id.travel_log_list_recyclerview);
                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context.get());
                    travelDetailsRecyclerView.setLayoutManager(layoutManager);
                    travelDetailsRecyclerView.setItemAnimator(new DefaultItemAnimator());

                    Log.e("Log", "travelLogList" + travellogjson.getData().getTravelLogDetails());
                    TravelLogDetailsAdapter travelLogDetailsAdapter = new TravelLogDetailsAdapter(context.get(), travellogjson.getData().getTravelLogDetails());
                    travelDetailsRecyclerView.setAdapter(travelLogDetailsAdapter);


            }
            else if (s.equals("failure"))
            {
                Toast.makeText(context.get(), "Pull Failed", Toast.LENGTH_SHORT).show();
            }
            else if (s.equals("nullException"))
            {
                Toast.makeText(context.get(), "Data is not available from server", Toast.LENGTH_SHORT).show();
            }

            pd.dismiss();
        }
    }
}