package com.happy.tracku.viewes;

import static com.happy.tracku.utils.Const.USING_IP;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.happy.tracku.MainActivity;
import com.happy.tracku.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.happy.tracku.db.DbHelper;
import com.happy.tracku.gson.dailywisegpsdatajsondetails.DailyWiseGPSDataJson;
import com.happy.tracku.gson.gpsstatusjson.GPSUpdateStatusJson;
import com.happy.tracku.models.DailyTravelModel;
import com.happy.tracku.ssl.CustomTrust;
import com.happy.tracku.utils.Const;
import com.happy.tracku.utils.Fns;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ShowUserGPSActivity extends AppCompatActivity
{

    TextInputEditText travelDateTIET, employeeCodeTIET;
    String travelDateString, employeeCodeString;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_user_gpsactivity);

        travelDateTIET = findViewById(R.id.travelDate);
        employeeCodeTIET = findViewById(R.id.employeeCode);

        travelDateTIET.setText(new SimpleDateFormat("dd-MM-yyyy").format(Calendar.getInstance().getTime()));

        // on below line we are adding click listener
        // for our pick date button
        travelDateTIET.setOnClickListener(new View.OnClickListener() {
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
                        ShowUserGPSActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // on below line we are setting date to our edit text.
                                travelDateTIET.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

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
        switch(view.getId())
        {
            case R.id.show_map_button:
            {
                employeeCodeString = employeeCodeTIET.getText().toString();
                travelDateString = Fns.convertFormatDate(travelDateTIET.getText().toString(), "dd-MM-yyyy", "yyyy-MM-dd");
                Log.e("Log", "travelDateString" + travelDateString);

                Intent intent = new Intent(this, MapActivity.class);
                intent.putExtra("fromDate", travelDateString);
                intent.putExtra("employeeCode", employeeCodeString);
                startActivity(intent);

            }
            break;
        }
    }


}