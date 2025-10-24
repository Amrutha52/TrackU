package com.happy.tracku.viewes;

import static com.happy.tracku.utils.Const.URL_UPDATE_DELIVERY_STATUS;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.happy.tracku.R;
import com.happy.tracku.adapters.DeliveryReportAdapters;
import com.happy.tracku.adapters.PhotoPunchHistoryAdapters;
import com.happy.tracku.databinding.ActivityDeliveryReportBinding;
import com.happy.tracku.databinding.ActivityDeliveryStatusBinding;
import com.happy.tracku.db.DbHelper;
import com.happy.tracku.gson.photopunchhistoryjson.GetPunchHistoryDetail;
import com.happy.tracku.gson.updatedeliverystatus.DeliveryStatusUpdate;
import com.happy.tracku.gson.updatedeliverystatus.UpdateDeliveryStatusJson;
import com.happy.tracku.ssl.CustomTrust;
import com.happy.tracku.utils.Const;
import com.happy.tracku.utils.Fns;

import org.json.JSONArray;
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

public class DeliveryReportActivity extends AppCompatActivity
{
    private ActivityDeliveryReportBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDeliveryReportBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Delivery Report");

        View rootView = findViewById(android.R.id.content); // Get root view

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            WindowInsetsCompat insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets.toWindowInsets());
            int systemBarsInsetsTop = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int systemBarsInsetsBottom = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

            // Apply padding to your main content view
            v.setPadding(v.getPaddingLeft(), systemBarsInsetsTop, v.getPaddingRight(), systemBarsInsetsBottom);

            return WindowInsetsCompat.CONSUMED;
        });

        binding.toDateET.setText(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));

        binding.fromDateET.setOnClickListener(new View.OnClickListener() {
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
                        DeliveryReportActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // on below line we are setting date to our edit text.
                                binding.fromDateET.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
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

        binding.toDateET.setOnClickListener(new View.OnClickListener() {
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
                        DeliveryReportActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // on below line we are setting date to our edit text.
                                binding.toDateET.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
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

    }

    public void listeners(View view)
    {
        switch (view.getId())
        {
            case R.id.pull_delivery_report_data:
            {
                Log.e("Log", "fromDate" + binding.fromDateET.getText().toString());
                if (binding.fromDateET.getText().toString().isEmpty() || binding.toDateET.getText().toString().isEmpty())
                {
                    Fns.neutralAlert("Alert", "Please Select From Date", this);
                    return;
                }
                else
                {
                    new GetDeliveryReport(this, binding.fromDateET.getText().toString(), binding.toDateET.getText().toString()).execute();

                }


            }
            break;
        }
    }

    private static class GetDeliveryReport extends AsyncTask<String, String, String>
    {
        ProgressDialog pd;
        WeakReference<DeliveryReportActivity> context;
        private OkHttpClient okHttpClient;
        private Request request;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String url;
        SharedPreferences shp;
        DbHelper dbHelper;
        UpdateDeliveryStatusJson updateDeliveryStatusJson;
        RecyclerView deliveryReportRecyclerView;
        String fromDateString, toDateString;

        public GetDeliveryReport(DeliveryReportActivity context, String fromDateString, String toDateString)
        {
            this.context = new WeakReference<>(context);
            this.fromDateString = fromDateString;
            this.toDateString = toDateString;

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

                url = Const.USING_IP + URL_UPDATE_DELIVERY_STATUS;

                JSONArray deliveryJsonArray = new JSONArray();

                JSONObject singleDataObj = new JSONObject();
                singleDataObj.put("idDetail",0);
                singleDataObj.put("deliveredQty",0);

                deliveryJsonArray.put(singleDataObj);


                Log.e("Log","deliveryReportURL" +url);
                Log.e("Log","deliverysingleDataObj"+singleDataObj.toString());

                JSONObject jsonObjectDeliveryPending = new JSONObject();
                jsonObjectDeliveryPending.put("createdBy", shp.getString(Const.Shp_Employee_Code, ""));
                jsonObjectDeliveryPending.put("action", 5);
                jsonObjectDeliveryPending.put("id", 0);
                jsonObjectDeliveryPending.put("fromDate", fromDateString);
                jsonObjectDeliveryPending.put("toDate", toDateString);
                jsonObjectDeliveryPending.put("deliveryDetails",deliveryJsonArray);

                Log.e("Log","jsonObjectDeliveryPending"+jsonObjectDeliveryPending.toString());

                RequestBody body = RequestBody.create(jsonObjectDeliveryPending.toString(), JSON);
                request = new Request.Builder()
                        //.header("X-Client-Type", "Android")
                        .url(url)
                        .post(body)
                        .build();


                Response response = okHttpClient.newCall(request).execute();
                Log.e("Log", "deliveryReportURLResponse" + response);

                if (!response.isSuccessful()) {

                    return "failure";

                }

                String resultString = response.body().string();

                Log.e("Log", "deliveryReportURLResultString" + resultString);

                Gson gson = new Gson();

                updateDeliveryStatusJson= gson.fromJson(resultString,UpdateDeliveryStatusJson.class);
                Log.e("Log", "updateDeliveryStatusJson" + updateDeliveryStatusJson);

                if (updateDeliveryStatusJson.getData().getDeliveryStatusUpdate().isEmpty() || updateDeliveryStatusJson.getData().getDeliveryStatusUpdate().size() == 0 || updateDeliveryStatusJson.getData().getDeliveryStatusUpdate() == null)
                {
                    return "nullException";
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
                deliveryReportRecyclerView =  context.get().findViewById(R.id.delivery_report_recycler_view);
                List<DeliveryStatusUpdate> getDeliveryReportList = updateDeliveryStatusJson.getData().getDeliveryStatusUpdate();
                Log.e("Log", "getDeliveryReportList size"+getDeliveryReportList.size());

                DeliveryReportAdapters deliveryReportAdapters = new DeliveryReportAdapters(context.get(), getDeliveryReportList);
                Log.e("Log", "deliveryReportAdapters" + deliveryReportAdapters);

                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context.get());

                deliveryReportRecyclerView.setLayoutManager(layoutManager);
                deliveryReportRecyclerView.setItemAnimator(new DefaultItemAnimator());
                deliveryReportRecyclerView.setAdapter(deliveryReportAdapters);

            } else if (s.equals("nullException"))
            {
                Toast.makeText(context.get(), "Null Exception from server", Toast.LENGTH_SHORT).show();
            }
            else if (s.equals("failure"))
            {
                Toast.makeText(context.get(), "Failure", Toast.LENGTH_SHORT).show();
            } else
            {
                Toast.makeText(context.get(), "Fetching Failed", Toast.LENGTH_SHORT).show();
            }

        }
    }
}