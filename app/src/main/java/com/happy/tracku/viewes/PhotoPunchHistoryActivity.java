package com.happy.tracku.viewes;

import static com.happy.tracku.utils.Const.URL_MANUAL_PUNCH;
import static com.happy.tracku.utils.Const.URL_PUNCH_HISTORY;
import static com.happy.tracku.utils.Const.USING_IP;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.happy.tracku.R;
import com.happy.tracku.db.DbHelper;
import com.happy.tracku.gson.login.LoginStatusJson;
import com.happy.tracku.gson.photopunchhistoryjson.Punchinghistoryjson;
import com.happy.tracku.gson.photopunchingjson.Photopunchingjson;
import com.happy.tracku.ssl.CustomTrust;
import com.happy.tracku.utils.Const;
import com.happy.tracku.utils.Fns;

import org.json.JSONObject;

import java.util.Calendar;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PhotoPunchHistoryActivity extends AppCompatActivity
{
    TextView monthTV, yearTV;
    //String fromDateString, toDateString;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_punch_history);

        monthTV = findViewById(R.id.monthTV);
        yearTV = findViewById(R.id.yearTV);

        monthTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // on below line we are getting
                // the instance of our calendar.
                final Calendar c = Calendar.getInstance();

                // on below line we are getting
                // our day, month and year.
               // int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
               // int day = c.get(Calendar.DAY_OF_MONTH);

                // on below line we are creating a variable for date picker dialog.
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        // on below line we are passing context.
                        PhotoPunchHistoryActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // on below line we are setting date to our edit text.
                               // fromDateTV.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                                monthTV.setText(monthOfYear);

                            }
                        },
                        // on below line we are passing year,
                        // month and day for selected date in our date picker.
                        0, month, 0);
                // at last we are calling show to
                // display our date picker dialog.
                datePickerDialog.show();
            }
        });

        yearTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // on below line we are getting
                // the instance of our calendar.
                final Calendar c = Calendar.getInstance();

                // on below line we are getting
                // our day, month and year.
                int year = c.get(Calendar.YEAR);
               // int month = c.get(Calendar.MONTH);
               // int day = c.get(Calendar.DAY_OF_MONTH);

                // on below line we are creating a variable for date picker dialog.
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        // on below line we are passing context.
                        PhotoPunchHistoryActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // on below line we are setting date to our edit text.
                                yearTV.setText(year);

                            }
                        },
                        // on below line we are passing year,
                        // month and day for selected date in our date picker.
                        year, 0, 0);
                // at last we are calling show to
                // display our date picker dialog.
                datePickerDialog.show();
            }
        });
    }

    public void listeners(View view)
    {
        switch (view.getId())
        {
            case R.id.pull_punch_data:
            {
                new GetPunchingHistory(this, yearTV.getText().toString(), monthTV.getText().toString()).execute();
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
        String year, month;

        RecyclerView punchingHistoryRecyclerview;
        public GetPunchingHistory(PhotoPunchHistoryActivity mContext, String year, String month)
        {
            this.mContext = mContext;
            this.year = year;
            this.month = month;

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
                photoPunchHistoryObj.put("employeeCode", shp.getString(Const.Shp_Employee_Code, ""));
                photoPunchHistoryObj.put("year", Integer.parseInt(year));
                photoPunchHistoryObj.put("month", Integer.parseInt(month));
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
                List<EmiChartDetail> emiChartDetailList = emiChartDetailsJson.getData().getEmiChartDetails();
                Log.e("Log", "emiChart DetailList size"+emiChartDetailList.size());

                EmiChartAdapter emiChartAdapter = new EmiChartAdapter(context, emiChartDetailList);
                Log.e("Log", "emiChart adapter" + emiChartAdapter);

                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);

                emiTrackRecyclerView.setLayoutManager(layoutManager);
                emiTrackRecyclerView.setItemAnimator(new DefaultItemAnimator());
                emiTrackRecyclerView.setAdapter(emiChartAdapter);

            }
            else
            {
                Toast.makeText(context, "Fetching Failed", Toast.LENGTH_SHORT).show();
            }

        }
    }
}