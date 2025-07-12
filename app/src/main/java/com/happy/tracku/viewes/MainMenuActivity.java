package com.happy.tracku.viewes;

import static com.happy.tracku.utils.Const.URL_LOGIN;
import static com.happy.tracku.utils.Const.URL_LOGOUT_TRACK;
import static com.happy.tracku.utils.Const.USING_IP;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.happy.tracku.R;
import com.happy.tracku.databinding.ActivityMainMenuBinding;
import com.happy.tracku.db.DbHelper;
import com.happy.tracku.gson.gpsstatusjson.GPSUpdateStatusJson;
import com.happy.tracku.gson.login.LoginStatusJson;
import com.happy.tracku.gson.logouttrackjson.LogoutTrackJson;
import com.happy.tracku.models.DailyTravelModel;
//import com.happy.tracku.service.ForeGroundService;
import com.happy.tracku.ssl.CustomTrust;
import com.happy.tracku.utils.Const;
import com.happy.tracku.utils.Fns;
//import com.happy.tracku.workmanager.PeriodicNotificationWorker;

import org.json.JSONObject;

import java.io.InterruptedIOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainMenuActivity extends AppCompatActivity {

    private ActivityMainMenuBinding binding;
    LocationManager locationManager;
    //CustomLocationCallback locationCallback;
    Double latitude, longitude;
    String locationAddress;
    SharedPreferences shp;
    DbHelper dbHelper;
    TextView employeeName, employeeCode;
    ArrayList<DailyTravelModel> dailyTravelModelArrayList;
    LinearLayout mainLayout, travelLogLayout, punchHistoryLayout;
    private FusedLocationProviderClient mFusedLocationClient;
    DailyTravelModel dailyTravelModel;


    private double wayLatitude = 0.0, wayLongitude = 0.0;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private android.widget.Button btnLocation;
    private TextView txtLocation;
    private android.widget.Button btnContinueLocation;
    private TextView txtContinueLocation;
    private StringBuilder stringBuilder;

    private boolean isContinue = false;
    private boolean isGPS = false;
    private static final int PERMISSION_REQUEST_ID = 1000;
    String userNameString, passwordString;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainMenuBinding.inflate(getLayoutInflater());
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

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnNavigationItemSelectedListener(navListener);


        shp = getSharedPreferences(Const.Shared_Pref_name, MODE_PRIVATE);

        employeeName = findViewById(R.id.employee_name);
        employeeCode = findViewById(R.id.employee_code);
        mainLayout = findViewById(R.id.mainLayout);
        travelLogLayout = findViewById(R.id.travellogLL);
        punchHistoryLayout = findViewById(R.id.punchinghistoryLL);

        if (shp.getInt(Const.Shp_Is_Admin, 0) == 1) {
            mainLayout.setVisibility(View.VISIBLE);
            travelLogLayout.setVisibility(View.VISIBLE);
            punchHistoryLayout.setVisibility(View.VISIBLE);
        } else {
            mainLayout.setVisibility(View.VISIBLE);
            travelLogLayout.setVisibility(View.GONE);
            punchHistoryLayout.setVisibility(View.GONE);
        }

        employeeName.setText(shp.getString(Const.Shp_Employee_Name, ""));
        employeeCode.setText(shp.getString(Const.Shp_Employee_Code, ""));

        dbHelper = new DbHelper(this);

      /*  mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(600000); // 10 minutes
        locationRequest.setFastestInterval(600000); // 10 minutes

        getLastLocation();

       */



        /**
         * ForeGround Service
         */
    /*    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_ID);
        } else {
            Log.e("Log", "Service calling");
            Intent serviceIntent = new Intent(this, ForeGroundService.class);
            startService(serviceIntent);
        }

     */





        userNameString = shp.getString(Const.Shp_UserName, "");
        passwordString = shp.getString(Const.Shp_PassWord, "");

        new LoginTaskForVersionCheck(this, userNameString, passwordString).execute();

        /**
         * WorkManager Implementation
         */

      /*  PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(PeriodicNotificationWorker.class,15, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(this).enqueue(request);

       */





    }

    @Override
    protected void onPause() {
        super.onPause();
        // Do not logout here if the user is simply switching apps
        // Instead, consider saving any temporary states or pausing background tasks
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Avoid logging out the user when the app stops
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reset any session timeouts if necessary
    }
  /*  private void getLocation() {
        if (ActivityCompat.checkSelfPermission(MainMenuActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainMenuActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainMenuActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    Const.LOCATION_REQUEST);

        } else {
            if (isContinue) {
                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            } else {
                mFusedLocationClient.getLastLocation().addOnSuccessListener(MainMenuActivity.this, location -> {
                    if (location != null) {
                        wayLatitude = location.getLatitude();
                        Log.e("Log", "wayLatitude" + wayLatitude);
                        wayLongitude = location.getLongitude();
                        Log.e("Log", "wayLongitude" + wayLongitude);
                        txtLocation.setText(String.format(Locale.US, "%s - %s", wayLatitude, wayLongitude));
                    } else {
                        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    }
                });
            }
        }
    }

   */


  /*  @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_ID: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (isContinue) {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                                    PERMISSION_REQUEST_ID);
                            return;
                        }
                        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    } else {
                        mFusedLocationClient.getLastLocation().addOnSuccessListener(MainMenuActivity.this, location -> {
                            if (location != null) {
                                wayLatitude = location.getLatitude();
                                wayLongitude = location.getLongitude();
                                Log.e("Log", "wayLatitudeOnRequest" + wayLatitude);
                                Log.e("Log", "wayLongitudeOnRequest" + wayLongitude);

                            } else {
                                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                            }
                        });
                    }
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Const.GPS_REQUEST) {
                isGPS = true; // flag maintain before get location
            }
        }
    }




    private void getLastLocation()
    {
        Log.e("Log", "getLastLocation");
        // check if permissions are given
        if (checkPermissions()) {

            // check if location is enabled
            if (isLocationEnabled()) {

                // getting last
                // location from
                // FusedLocationClient
                // object
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSION_REQUEST_ID);
                    Log.e("Log", "permissionDeniedMainMenu");
                    return;
                }
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null || location.getLongitude() == 0 || location.getLatitude() == 0) {
                            requestNewLocationData();
                        } else {
                            Log.e("Log", "latitudeInsideFusedlocation" + location.getLatitude());
                            Log.e("Log", "longitudeInsideFusedlocation" + location.getLongitude());
                            latitude = location==null?0.0:location.getLatitude();
                            Log.e("Log", "locationLatitude" + latitude);

                            longitude = location==null?0.0:location.getLongitude();
                            Log.e("Log", "location longitude" + longitude);

                            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                            try
                            {
                                // throw new RuntimeException("Exception For Testing");

                                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                                Log.e("Log", latitude + "" + longitude);

                                if (addresses != null && addresses.size() != 0)
                                {
                                    locationAddress = addresses.get(0).getAddressLine(0);
                                    Log.e("address", locationAddress);
                                }

                            }
                            catch (Exception e)
                            {
                                locationAddress = "Not Able To Get Address";
                                Log.e("ExceptionAddress", locationAddress);
                                Log.e("Log", "Exception", e);
                            }

                            Calendar cal = Calendar.getInstance();
                            Date dateNow = cal.getTime();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String dateTimeString = sdf.format(dateNow);



                            dailyTravelModel = new DailyTravelModel();
                            dailyTravelModel.setIdLocation(shp.getString(Const.Shp_Employee_Code, "") + String.valueOf(dateNow.getTime()));
                            dailyTravelModel.setLatitude(latitude);
                            dailyTravelModel.setLongitude(longitude);
                            dailyTravelModel.setAddress(locationAddress);
                            dailyTravelModel.setDateTime(dateTimeString);
                            dailyTravelModel.setIdEmployee(1);
                            dailyTravelModel.setIsForUpload(0);
                            dailyTravelModel.setIsSynced(0);

                            dbHelper.insertContinousGPSLocationOfAnEmployee(dailyTravelModel);
                            new updateLocationOfEmployee(MainMenuActivity.this, dailyTravelModel).execute();
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            // if permissions aren't available,
            // request for permissions
            requestPermissions();
        }



    }

    // method to check for permissions
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ID);
    }

    private void requestNewLocationData() {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(600000);
        mLocationRequest.setFastestInterval(600000);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Log.e("Log", "LocationCallBackMainMenu");
            Location mLastLocation = locationResult.getLastLocation();
            Log.e("Log", "latitude" + mLastLocation.getLatitude());
            Log.e("Log", "latitude" + mLastLocation.getLatitude());

            latitude = mLastLocation == null ? 0.0 : mLastLocation.getLatitude();
            Log.e("Log", "Last location Latitude" + latitude);

            longitude = mLastLocation == null ? 0.0 : mLastLocation.getLongitude();
            Log.e("Log", "Last location longitude" + longitude);

            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            try {
                // throw new RuntimeException("Exception For Testing");

                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                Log.e("Log", latitude + "" + longitude);

                if (addresses != null && addresses.size() != 0) {
                    locationAddress = addresses.get(0).getAddressLine(0);
                    Log.e("address", locationAddress);
                }

            } catch (Exception e) {
                locationAddress = "Not Able To Get Address";
                Log.e("ExceptionAddress", locationAddress);
                Log.e("Log", "Exception", e);
            }

            Calendar cal = Calendar.getInstance();
            Date dateNow = cal.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateTimeString = sdf.format(dateNow);



            dailyTravelModel = new DailyTravelModel();
            dailyTravelModel.setIdLocation(shp.getString(Const.Shp_Employee_Code, "") + String.valueOf(dateNow.getTime()));
            dailyTravelModel.setLatitude(latitude);
            dailyTravelModel.setLongitude(longitude);
            dailyTravelModel.setAddress(locationAddress);
            dailyTravelModel.setDateTime(dateTimeString);
            dailyTravelModel.setIdEmployee(1);
            dailyTravelModel.setIsForUpload(0);
            dailyTravelModel.setIsSynced(0);

            dbHelper.insertContinousGPSLocationOfAnEmployee(dailyTravelModel);

            new updateLocationOfEmployee(MainMenuActivity.this, dailyTravelModel).execute();

        }
    };

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

   */




    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = item ->
    {
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
                    edt.putBoolean(Const.Shp_Is_LoggedIn, false);
                    edt.apply();

                    new LogoutTrack(MainMenuActivity.this).execute();

                    startActivity(new Intent(MainMenuActivity.this, LoginActivity.class));

                    //finish();

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



    public void listeners(View view)
    {
        switch(view.getId())
        {
            case R.id.adminMapViewActivity:
            {
                startActivity(new Intent(this, ShowUserGPSActivity.class));

            }
            break;

            case R.id.addEmployeeLL:
            {
                startActivity(new Intent(this, AddEmployeeActivity.class));

            }
            break;

            case R.id.photopunchingLL:
            {
                startActivity(new Intent(this, PhotoPunchActivity.class));
            }
            break;

            case R.id.punchinghistoryLL:
            {
                startActivity(new Intent(this, PhotoPunchHistoryActivity.class));
            }
            break;

            case R.id.travellogLL:
            {
                startActivity(new Intent(this, TravelLogActivity.class));
            }
            break;

            case R.id.purchaseOrderListLL:
            {
                startActivity(new Intent(this, PurchaseOrderListActivity.class));
            }
            break;

            case R.id.stockOutListLL:
            {
                startActivity(new Intent(this, StockOutOrderListActivity.class));
            }
            break;

            case R.id.salesRequestLL:
            {
                startActivity(new Intent(this, SalesRequestActivity.class));
            }
            break;
        }
    }

   /* @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        Log.e("Log", "onRequestPermissionsResult");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Intent serviceIntent = new Intent(this, ForeGroundService.class);
        startService(serviceIntent);

    }

    */



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
        String statusMsg;

        int status;
        public updateLocationOfEmployee(MainMenuActivity mContext, DailyTravelModel dailyTravelModel)
        {
            Log.e("Log", "InsideUpdateLocationMainMenu");
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
                //jsonObject.put("versionCode", shp.getString(Const.Shp_Version_No, ""));
                dailyTravelModelArrayList = dbHelper.getDailyTravelDataForCompensationAsArray();


                url = USING_IP + Const.URL_Update_Daily_GPS_Data;

                Log.e("Log", "locationURLMainMenuActivity" +url);
                Log.e("Log", "locationJsonMainMenuActivity" +jsonObject.toString());

                RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
                request = new Request.Builder()
                        //.header("X-Client-Type", "Android")
                        .url(url)
                        //.addHeader("Authorization", "Bearer " + shp.getString(Const.Shp_Token, ""))
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

                if (gpsUpdateStatusJson.getData().getLocationUpdateStatus().isEmpty() || gpsUpdateStatusJson.getData().getLocationUpdateStatus().size() == 0)
                {
                    return "nullException";
                }

                if (status != 1)
                {
                    return "failure";

                }

                status = gpsUpdateStatusJson.getData().getLocationUpdateStatus().get(0).getStatus();
                Log.e("Log", "gpsStatusMainMenu" + status);
                statusMsg = gpsUpdateStatusJson.getData().getLocationUpdateStatus().get(0).getStatusMsg();


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

                    //
//                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//                    View dialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_success, null);
//
//                    builder.setView(dialogView);
//
//                    AlertDialog alertDialog = builder.create();
//                    alertDialog.show();
//
//                    TextView successMsg = dialogView.findViewById(R.id.success_msg);
//                    successMsg.setText(statusMsg);
//
//                    MaterialButton okButton = dialogView.findViewById(R.id.ok_button);
//                    okButton.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//
//                            alertDialog.dismiss();
//
//                        }
//                    });

                    if (status == 1)
                    {
                        dbHelper.setAsSyncedTravelCompensationGPSData(dailyTravelModelArrayList);
                        dbHelper.deleteTravelCompensationGPSData();

                        Toast.makeText(mContext, statusMsg, Toast.LENGTH_LONG).show();

                    }


                }
                catch (Exception e)
                {

                    Fns.neutralAlert("Alert","Result String :  "+resultString+"\nException : "+Fns.getErrorMsgFromException(e), mContext);
                    Toast.makeText(mContext, "Exception Occurred", Toast.LENGTH_LONG).show();

                }

            }
            else if (s.equals("failure"))
            {

                Toast.makeText(mContext, "failure", Toast.LENGTH_LONG).show();
            }
            else if (s.equals("nullException"))
            {
                Toast.makeText(mContext, "Null Exception From Server", Toast.LENGTH_LONG).show();
            }
            else
            {

                Toast.makeText(mContext, "Failed to Push", Toast.LENGTH_LONG).show();

            }

        }
    }

    private static class LoginTaskForVersionCheck extends AsyncTask<String, String, String>
    {

        OkHttpClient okHttpClient;
        String url;
        Request request;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        MainMenuActivity mContext;

        ProgressDialog pd;
        SharedPreferences shp;
        TelephonyManager telephonyManager;
        String usernameString, passwordString;
        String failureMsg;
        boolean exceptionOccured = false,timeOutExceptionOccured = false;
        String inputAndOutputJson = "";
        LoginStatusJson loginStatusJson;
        public LoginTaskForVersionCheck(MainMenuActivity mContext, String userNameString, String passwordString)
        {
            this.mContext = mContext;
            this.usernameString = userNameString;
            this.passwordString = passwordString;

            Log.e("Log", "usernameStringMainMenu" + usernameString);
            Log.e("Log", "passwordStringMainMenu" + passwordString);

            CustomTrust customTrust = new CustomTrust(mContext);
            OkHttpClient client = customTrust.getClient();
            okHttpClient = client;

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
            try {

                url = USING_IP + URL_LOGIN;

                Log.e("Log", "loginURL" +url);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("loginId", usernameString);
                jsonObject.put("password", passwordString);
                jsonObject.put("versionCode", shp.getString(Const.Shp_Version_No, ""));
                jsonObject.put("androidId", shp.getString(Const.Shp_Android_Id, ""));
                jsonObject.put("createdBy", "");


                inputAndOutputJson = jsonObject.toString();
                Log.e("Log", " inputAndOutputJson" + inputAndOutputJson);

                RequestBody bodyOne = RequestBody.create(jsonObject.toString(), JSON);
                request = new Request.Builder()
                        //.header("X-Client-Type", "Android")
                        .url(url)
                        .post(bodyOne)
                        .build();


                Response responseOne = okHttpClient.newCall(request).execute();

                if (!responseOne.isSuccessful()) {

                    Log.e("Log", "failure");
                    failureMsg = "Response unsuccessfull";
                    return "failure";
                }

                pd.setProgress(25);
                String resultOne = responseOne.body().string();

                Log.e("Log", resultOne);

                inputAndOutputJson = inputAndOutputJson + "----------" + resultOne;


                Gson gsonTwo = new Gson();


             /*   if (resultOne.equals("{}")) {
                    failureMsg = "empty String result";
                    return "failure";
                }

                if (resultOne.equals("{\"Status\":[{\"Status\":0,\"StatusMsg\":\"Invalid login\"}]}"))
                {
                    failureMsg = "Invalid Login Status";
                    return "failure";

                }

              */

                loginStatusJson = gsonTwo.fromJson(resultOne, LoginStatusJson.class);

                if (loginStatusJson.getData().getLoginResponseStatus().isEmpty() || loginStatusJson.getData().getLoginResponseStatus().size() == 0)
                {
                    return "failure";
                }
                else if (loginStatusJson.getData().getLoginResponseStatus().get(0).getStatus() != 1)
                {
                    return "failure";
                }

                double versionAtServer = Double.parseDouble(loginStatusJson.getData().getLoginResponseStatus().get(0).getVersion());
                double currentVersion = Double.parseDouble(Fns.getAppVersionName(mContext));


                if(versionAtServer > currentVersion)
                {

                    SharedPreferences.Editor edt = shp.edit();
                    edt.putString(Const.Shp_NEW_APP_VERSION,loginStatusJson.getData().getLoginResponseStatus().get(0).getVersion());
                    edt.apply();
                    return "update";

                }




            }
            catch(SocketTimeoutException e)
            {

                failureMsg = Fns.getErrorMsgFromException(e);
                Log.e("Log", "FailureMessage" + failureMsg);
                //throw new RuntimeException(e);
                timeOutExceptionOccured = true;
                //exceptionOccured = true;
                return "failure";


            }catch (InterruptedIOException e)
            {

                failureMsg = Fns.getErrorMsgFromException(e);
                Log.e("Log", "failureMsgInterruptedIOException" + failureMsg);
                //throw new RuntimeException(e);
                timeOutExceptionOccured = true;
                //exceptionOccured = true;
                return "failure";

            }
            catch (Exception e)
            {

                failureMsg = Fns.getErrorMsgFromException(e);
                Log.e("Log", "failureMessageException" + failureMsg);
                //throw new RuntimeException(e);
                exceptionOccured = true;
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
                Toast.makeText(mContext, "Success ", Toast.LENGTH_LONG).show();

            }
            else if (s.equals("failure"))
            {

                //Fns.neutralAlert("Failure",failureMsg,mContext);
                Toast.makeText(mContext, "Failed "+failureMsg, Toast.LENGTH_LONG).show();

              /*  if(timeOutExceptionOccured)
                {
                    Fns.neutralAlert("Alert","Timeout ",mContext);

                }
                else if(exceptionOccured)
                {
                    String errorMsg = Fns.getErrorMessage(mContext,failureMsg);
                    Fns.neutralAlert("Alert","Failed "+errorMsg,mContext);
                }
                else
                {
                    String message = loginStatusJson.getData().getLoginResponseStatus().get(0).getStatusMessage();
                    Fns.neutralAlert("Alert",message,mContext);
                }

               */


            }
            else if(s.equals("update"))
            {

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                builder.setMessage("New Version of App Released. You have to update to continue");

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {

                    }
                });

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //Goto page saved successfully
                        //TODO call webservice

                        dialogInterface.dismiss();
                        //mContext.downloadNewApk();

                        Fns.openInPlayStore(mContext);


                    }
                });

                AlertDialog alertDialog = builder.create();

                alertDialog.show();

            }

        }
    }


    private static class LogoutTrack extends AsyncTask<String, String, String>
    {
        WeakReference<MainMenuActivity> context;
        ProgressDialog pd;
        OkHttpClient okHttpClient;
        String url, resultString;
        Request request;
        Response response;
        SharedPreferences shp;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        LogoutTrackJson logoutTrackJson;
        int status;
        String statusMessage;

        public LogoutTrack(MainMenuActivity context)
        {
            this.context = new WeakReference<>(context);

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

                JSONObject logoutTrackObj = new JSONObject();
                logoutTrackObj.put("imeiNumber", shp.getString(Const.Shp_Android_Id, ""));
                logoutTrackObj.put("createdBy", shp.getString(Const.Shp_Employee_Code, ""));

                url = USING_IP + URL_LOGOUT_TRACK;
                Log.e("Log", "logoutTrackURL" + url);

                RequestBody body = RequestBody.create(logoutTrackObj.toString(), JSON);
                Log.e("Log", "logoutTrackObj" + logoutTrackObj);

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
                logoutTrackJson = gson.fromJson(resultString, LogoutTrackJson.class);
                Log.e("Log", "logoutTrackJson" + logoutTrackJson);

                if (logoutTrackJson.getData().getLogoutTrackStatus() == null || logoutTrackJson.getData().getLogoutTrackStatus().size() == 0 || logoutTrackJson.getData().getLogoutTrackStatus().isEmpty())
                {
                    return "nullException";
                }
                else
                {
                    status = logoutTrackJson.getData().getLogoutTrackStatus().get(0).getStatus();

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
                statusMessage = logoutTrackJson.getData().getLogoutTrackStatus().get(0).getStatusMessage();
                Fns.neutralAlert("Alert", statusMessage, context.get());
            }
            else if (s.equals("failure"))
            {
                statusMessage = logoutTrackJson.getData().getLogoutTrackStatus().get(0).getStatusMessage();
                Fns.neutralAlert("Alert", statusMessage, context.get());
            } else if (s.equals("nullException"))
            {
                Toast.makeText(context.get(), "Null Exception From Server", Toast.LENGTH_SHORT).show();
            }

            pd.dismiss();
        }

    }
}