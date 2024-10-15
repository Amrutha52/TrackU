package com.happy.tracku.viewes;

import static com.happy.tracku.utils.Const.USING_IP;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.happy.tracku.R;
import com.happy.tracku.databinding.ActivityMainMenuBinding;
import com.happy.tracku.db.DbHelper;
import com.happy.tracku.gson.gpsstatusjson.GPSUpdateStatusJson;
import com.happy.tracku.gson.login.LoginStatusJson;
import com.happy.tracku.models.DailyTravelModel;
import com.happy.tracku.ssl.CustomTrust;
import com.happy.tracku.utils.Const;
import com.happy.tracku.utils.Fns;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainMenuActivity extends AppCompatActivity {

    private ActivityMainMenuBinding binding;
    LocationManager locationManager;
    CustomLocationCallback locationCallback;
    Double latitude, longitude;
    SharedPreferences shp;
    DbHelper dbHelper;

    ArrayList<DailyTravelModel> dailyTravelModelArrayList;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        binding = ActivityMainMenuBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnNavigationItemSelectedListener(navListener);


        shp = getSharedPreferences(Const.Shared_Pref_name, MODE_PRIVATE);

        dbHelper = new DbHelper(this);


        FusedLocationProviderClient client = LocationServices
                .getFusedLocationProviderClient(getApplicationContext());
        LocationRequest request = LocationRequest.create();
        request.setInterval(15 * 1000); //Every 15 seconds
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationCallback = new CustomLocationCallback();

        client.requestLocationUpdates(request, locationCallback, null);

        Location location = locationCallback==null?null:locationCallback.getLocation();



        latitude = location==null?0.0:location.getLatitude();
        Log.e("Log", "locationLatitude" + latitude);

        longitude = location==null?0.0:location.getLongitude();
        Log.e("Log", "locationlongitude" + longitude);

        Calendar cal = Calendar.getInstance();
        Date dateNow = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateTimeString = sdf.format(dateNow);

        String returnedAddress = "AddressOfCustomer";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null) {
                returnedAddress = addresses.get(0).getAddressLine(0);

            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("loctionaddress", "Address didn't find...");
        }

        DailyTravelModel dailyTravelModel = new DailyTravelModel();
        dailyTravelModel.setIdLocation(shp.getString(Const.Shp_Employee_Code, "") + String.valueOf(dateNow.getTime()));
        dailyTravelModel.setLatitude(latitude);
        dailyTravelModel.setLongitude(longitude);
        dailyTravelModel.setAddress(returnedAddress);
        dailyTravelModel.setDateTime(dateTimeString);
        dailyTravelModel.setIdEmployee(1);
        dailyTravelModel.setIsForUpload(0);
        dailyTravelModel.setIsSynced(0);

        dbHelper.insertContinousGPSLocationOfAnEmployee(dailyTravelModel);

        LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) || location == null) {
            buildAlertMessageNoGps();
            return;
        }

        if(!manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) || location == null || latitude == 0 || longitude == 0)
        {

            Toast.makeText(this, "Please Turn On GPS and wait some minutes before submitting", Toast.LENGTH_LONG).show();
            return;
        }

        new updateLocationOfEmployee(this, dailyTravelModel).execute();

    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        // By using switch we can easily get
        // the selected fragment
        // by using there id.
        Fragment selectedFragment = null;
        int itemId = item.getItemId();
        if (itemId == R.id.home) {
           // selectedFragment = new AlgorithmFragment();
        } else if (itemId == R.id.message) {
           // selectedFragment = new CourseFragment();
        } else if (itemId == R.id.logout) {
            logout();
        }
        // It will help to replace the
        // one fragment to other.
        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, selectedFragment).commit();
        }
        return true;
    };
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.log_out_button:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logout()
    {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage("Do you want to logout?");

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    SharedPreferences.Editor edt = shp.edit();
                    edt.clear();
                    edt.apply();

                    startActivity(new Intent(MainMenuActivity.this, LoginActivity.class));

                    finish();

                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {


                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.setTitle("Alert");
            alertDialog.show();


    }

    private void buildAlertMessageNoGps()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("GPS is Mandatory to proceed, do you want to enable it? Also Please wait 5 mins after enabling")
                .setCancelable(false)
                .setPositiveButton("Enable Now", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public void listeners(View view)
    {
        switch(view.getId())
        {
            case R.id.adminMapViewActivity:
            {

            }
            break;
        }
    }

    static class CustomLocationCallback extends LocationCallback
    {

        Location location;

        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);

            location = locationResult.getLastLocation();
            // Do something with the location (may be null!)
            Log.e("Log", "LocationLatitude" + location.getLatitude());
            Log.e("Log", "LocationChanged: " + location);

            //setMethodForLocation(location.getLatitude(), location.getLongitude());

        }



        public Location getLocation() {
            Log.e("Log", "InsideGetLocation");
            return location;

        }

    }

    private static class updateLocationOfEmployee extends AsyncTask<String, String, String>
    {
        OkHttpClient okHttpClient;
        String url;
        Request request;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        MainMenuActivity mContext;

        ProgressDialog pd;
        SharedPreferences shp;
        String failureMsg, resultString;
        GPSUpdateStatusJson gpsUpdateStatusJson;
        DailyTravelModel dailyTravelModel;
        DbHelper dbHelper;
        ArrayList<DailyTravelModel> dailyTravelModelArrayList;

        int status;
        public updateLocationOfEmployee(MainMenuActivity mContext, DailyTravelModel dailyTravelModel)
        {
            Log.e("Log", "InsideUpdateLocation");
            this.mContext = mContext;
            this.dailyTravelModel = dailyTravelModel;

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
        protected void onPreExecute() {
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
            try {

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String todayString = sdf.format(Calendar.getInstance().getTime());

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("createdBy", shp.getString(Const.Shp_Employee_Code, ""));
                jsonObject.put("dailyGPSData", dbHelper.getDailyTravelDataForCompensation());
                //dailyTravelModelArrayList = dbHelper.getDailyTravelDataForCompensationAsArray();


                url = USING_IP + Const.URL_Update_Daily_GPS_Data;

                Log.e("Log", "locationURL" +url);
                Log.e("Log", "locationJson" +jsonObject.toString());

                RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
                request = new Request.Builder()
                        //.header("X-Client-Type", "Android")
                        .url(url)
                        .addHeader("Authorization", "Bearer " + shp.getString(Const.Shp_Token, ""))
                        .post(body)
                        .build();

                Response response = okHttpClient.newCall(request).execute();

                if (!response.isSuccessful()) {

                    return "failure";

                }

                resultString = response.body().string();

                Log.e("Log", "" + resultString);

                Gson gson = new Gson();
                gpsUpdateStatusJson = gson.fromJson(resultString, GPSUpdateStatusJson.class);

                status = gpsUpdateStatusJson.getData().getLocationUpdateStatus().get(0).getStatus();

                if (status != 1) {
                    return "failure";

                }


            } catch (Exception e) {

                Log.e("Log", "Exception", e);
                return "failure";
            }

            return "success";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            pd.dismiss();

            if (s.equals("success")) {

                try {

                    String statusMsg = gpsUpdateStatusJson.getData().getLocationUpdateStatus().get(0).getStatusMsg();

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    View dialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_success, null);

                    builder.setView(dialogView);

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                    TextView successMsg = dialogView.findViewById(R.id.success_msg);
                    successMsg.setText(statusMsg);

                    MaterialButton okButton = dialogView.findViewById(R.id.ok_button);
                    okButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            alertDialog.dismiss();

                        }
                    });

//                    if (Status == 1) {
//                        dbHelper.setAsSyncedTravelCompensationGPSData(dailyTravelModelArrayList);
//                        dbHelper.deleteTravelCompensationGPSData();
//                        context.get().updateRecyclerviewAndDistance();
//
//                    }


                } catch (Exception e) {

                    Fns.neutralAlert("Alert","Result String :  "+resultString+"\nException : "+Fns.getErrorMsgFromException(e), mContext);
                    Toast.makeText(mContext, "Exception Occurred", Toast.LENGTH_LONG).show();

                }

            } else if (s.equals("failure"))
            {
                String statusMsg = gpsUpdateStatusJson.getData().getLocationUpdateStatus().get(0).getStatusMsg();
                Toast.makeText(mContext, statusMsg, Toast.LENGTH_LONG).show();
            } else {

                Toast.makeText(mContext, "Failed to Push", Toast.LENGTH_LONG).show();

            }

        }
    }
}