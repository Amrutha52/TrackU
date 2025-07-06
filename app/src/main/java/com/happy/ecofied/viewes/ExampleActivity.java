/*package com.happy.tracku.viewes;

import static com.happy.tracku.utils.Const.URL_LOGIN;
import static com.happy.tracku.utils.Const.URL_LOGOUT_TRACK;
import static com.happy.tracku.utils.Const.USING_IP;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.happy.tracku.service.ForeGroundService;
import com.happy.tracku.ssl.CustomTrust;
import com.happy.tracku.utils.Const;
import com.happy.tracku.utils.Fns;
import com.happy.tracku.workmanager.PeriodicNotificationWorker;

import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
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
    private LocationManager locationManager;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback; // Declare as a class member
    private double latitude, longitude;
    private String locationAddress;
    private SharedPreferences sharedPreferences;
    private DbHelper dbHelper;
    private TextView employeeName, employeeCode;
    private LinearLayout mainLayout, travelLogLayout, punchHistoryLayout;
    private DailyTravelModel dailyTravelModel;
    private boolean isGPS = false;
    private static final int PERMISSION_REQUEST_ID = 1000;
    private String userNameString, passwordString;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainMenuBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        View rootView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            WindowInsetsCompat insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets.toWindowInsets());
            int systemBarsInsetsTop = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int systemBarsInsetsBottom = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            v.setPadding(v.getPaddingLeft(), systemBarsInsetsTop, v.getPaddingRight(), systemBarsInsetsBottom);
            return WindowInsetsCompat.CONSUMED;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        sharedPreferences = getSharedPreferences(Const.Shared_Pref_name, MODE_PRIVATE);

        employeeName = findViewById(R.id.employee_name);
        employeeCode = findViewById(R.id.employee_code);
        mainLayout = findViewById(R.id.mainLayout);
        travelLogLayout = findViewById(R.id.travellogLL);
        punchHistoryLayout = findViewById(R.id.punchinghistoryLL);

        if (sharedPreferences.getInt(Const.Shp_Is_Admin, 0) == 1) {
            mainLayout.setVisibility(View.VISIBLE);
            travelLogLayout.setVisibility(View.VISIBLE);
            punchHistoryLayout.setVisibility(View.VISIBLE);
        } else {
            mainLayout.setVisibility(View.VISIBLE);
            travelLogLayout.setVisibility(View.GONE);
            punchHistoryLayout.setVisibility(View.GONE);
        }

        employeeName.setText(sharedPreferences.getString(Const.Shp_Employee_Name, ""));
        employeeCode.setText(sharedPreferences.getString(Const.Shp_Employee_Code, ""));

        dbHelper = new DbHelper(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize LocationRequest
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(300000) // 5 minutes
                .setFastestInterval(300000); // 5 minutes

        // Initialize LocationCallback  ***Initialize HERE***
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    Log.e("LocationCallback", "LocationResult is null");
                    return;
                }
                // Handle the location update
                for (Location location : locationResult.getLocations()) {
                    Log.d("LocationUpdate", "Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude());
                    // Update UI or process location data here
                    //Toast.makeText(MainMenuActivity.this, "Location: " + location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                    updateLocationData(location); // Extract location handling to a separate method
                }
            }
        };

        // Check for location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ID);
        } else {
            // Permission granted, start getting location
            startLocationUpdates();
        }

        //startForeGroundService(); // Start foreground service

        userNameString = sharedPreferences.getString(Const.Shp_UserName, "");
        passwordString = sharedPreferences.getString(Const.Shp_PassWord, "");
        new LoginTaskForVersionCheck(this, userNameString, passwordString).execute();

        // WorkManager
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(PeriodicNotificationWorker.class, 15, TimeUnit.MINUTES).build();
        WorkManager.getInstance(this).enqueue(request);
    }



    private void startForeGroundService() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ID);
        } else {
            Log.e("Log", "Service calling");
            Intent serviceIntent = new Intent(this, ForeGroundService.class);
            startService(serviceIntent);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        //  Remove location updates when activity is paused
        stopLocationUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Remove location updates when activity is stopped
        stopLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        if (fusedLocationProviderClient != null && locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            Log.d("LocationUpdate", "Location updates stopped");
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, start location updates
                startLocationUpdates();
                //startForeGroundService();
            } else {
                // Permission denied
                Toast.makeText(this, "Location permission is required for this feature.", Toast.LENGTH_LONG).show();
                // Handle the case where the user denies permission
                // You might disable location-dependent features or finish the activity
            }
        }
    }

    private void startLocationUpdates() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
            Log.d("LocationUpdate", "Location updates started");
        } catch (SecurityException e) {
            Log.e("LocationError", "SecurityException: " + e.getMessage());
            Toast.makeText(this, "Location permission error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }



    private void getLastLocation() {
        Log.e("Log", "getLastLocation");
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ID);
                    Log.e("Log", "permissionDeniedMainMenu");
                    return;
                }
                fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null || location.getLongitude() == 0 || location.getLatitude() == 0) {
                            requestNewLocationData();
                        } else {
                            updateLocationData(location);
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Please turn on your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }

    private void updateLocationData(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() != 0) {
                locationAddress = addresses.get(0).getAddressLine(0);
                Log.e("address", locationAddress);
            }
        } catch (IOException e) {
            locationAddress = "Not Able To Get Address";
            Log.e("ExceptionAddress", locationAddress);
            Log.e("Log", "Exception", e);
        }

        Calendar cal = Calendar.getInstance();
        Date dateNow = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String dateTimeString = sdf.format(dateNow);

        dailyTravelModel = new DailyTravelModel();
        dailyTravelModel.setIdLocation(sharedPreferences.getString(Const.Shp_Employee_Code, "") + String.valueOf(dateNow.getTime()));
        dailyTravelModel.setLatitude(latitude);
        dailyTravelModel.setLongitude(longitude);
        dailyTravelModel.setAddress(locationAddress);
        dailyTravelModel.setDateTime(dateTimeString);
        dailyTravelModel.setIdEmployee(1);
        dailyTravelModel.setIsForUpload(0);
        dailyTravelModel.setIsSynced(0);

        dbHelper.insertContinousGPSLocationOfAnEmployee(dailyTravelModel);
        new UpdateLocationOfEmployeeTask(this, dailyTravelModel).execute();
    }

    private void requestNewLocationData() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(300000);
        mLocationRequest.setFastestInterval(300000);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());
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

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();
        if (itemId == R.id.home) {
            // selectedFragment = new AlgorithmFragment();
        } else if (itemId == R.id.message) {
            // selectedFragment = new CourseFragment();
        } else if (itemId == R.id.logout) {
            logout();
        }
        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, selectedFragment).commit();
        }
        return true;
    };

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.log_out_button) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to logout?");
        builder.setPositiveButton("Yes", (dialogInterface, i) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(Const.Shp_Is_LoggedIn, false);
            editor.apply();
            new LogoutTrackTask(MainMenuActivity.this).execute(); // Use the corrected AsyncTask
            startActivity(new Intent(MainMenuActivity.this, LoginActivity.class));
        });
        builder.setNegativeButton("No", (dialogInterface, i) -> {
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setTitle("Alert");
        alertDialog.show();
    }

    public void listeners(View view) {
        int id = view.getId();
        if (id == R.id.adminMapViewActivity) {
            startActivity(new Intent(this, ShowUserGPSActivity.class));
        } else if (id == R.id.addEmployeeLL) {
            startActivity(new Intent(this, AddEmployeeActivity.class));
        } else if (id == R.id.photopunchingLL) {
            startActivity(new Intent(this, PhotoPunchActivity.class));
        } else if (id == R.id.punchinghistoryLL) {
            startActivity(new Intent(this, PhotoPunchHistoryActivity.class));
        } else if (id == R.id.travellogLL) {
            startActivity(new Intent(this, TravelLogActivity.class));
        } else if (id == R.id.purchaseOrderListLL) {
            startActivity(new Intent(this, PurchaseOrderListActivity.class));
        } else if (id == R.id.stockOutListLL) {
            startActivity(new Intent(this, StockOutOrderListActivity.class));
        } else if (id == R.id.salesRequestLL) {
            startActivity(new Intent(this, SalesRequestActivity.class));
        }
    }



    private static class UpdateLocationOfEmployeeTask extends AsyncTask<String, String, String> { // Corrected class name
        private OkHttpClient okHttpClient;
        private String url;
        private Request request;
        private MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        private final MainMenuActivity mContext; // Use final for better practice
        private ProgressDialog pd;
        private SharedPreferences sharedPreferences;
        private String resultString;
        private GPSUpdateStatusJson gpsUpdateStatusJson;
        private final DailyTravelModel dailyTravelModel; // Use final here too
        private DbHelper dbHelper;
        private String statusMsg;
        private int status;

        public UpdateLocationOfEmployeeTask(MainMenuActivity context, DailyTravelModel model) {
            mContext = context;
            this.dailyTravelModel = model;
            CustomTrust customTrust = new CustomTrust(mContext);
            okHttpClient = customTrust.getClient();
            sharedPreferences = mContext.getSharedPreferences(Const.Shared_Pref_name, MODE_PRIVATE);
            dbHelper = new DbHelper(mContext);
            pd = new ProgressDialog(mContext);
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
                Log.e("Log", "Exception in onPreExecute", e);
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("createdBy", sharedPreferences.getString(Const.Shp_Employee_Code, ""));
                jsonObject.put("dailyGPSData", dbHelper.getDailyTravelDataForCompensation());
                url = USING_IP + Const.URL_Update_Daily_GPS_Data;

                Log.e("Log", "locationURLMainMenuActivity" + url);
                Log.e("Log", "locationJsonMainMenuActivity" + jsonObject.toString());

                RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
                request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

                Response response = okHttpClient.newCall(request).execute();
                if (!response.isSuccessful()) {
                    return "failure";
                }
                resultString = response.body().string();
                Log.e("Log", "Result: " + resultString);

                Gson gson = new Gson();
                gpsUpdateStatusJson = gson.fromJson(resultString, GPSUpdateStatusJson.class);

                if (gpsUpdateStatusJson == null || gpsUpdateStatusJson.getData() == null || gpsUpdateStatusJson.getData().getLocationUpdateStatus() == null ||gpsUpdateStatusJson.getData().getLocationUpdateStatus().isEmpty() )
                {
                    return "nullException";
                }

                status = gpsUpdateStatusJson.getData().getLocationUpdateStatus().get(0).getStatus();
                statusMsg = gpsUpdateStatusJson.getData().getLocationUpdateStatus().get(0).getStatusMsg();

            } catch (SocketTimeoutException e) {
                Log.e("Log", "SocketTimeoutException", e);
                return "timeout";
            }
            catch (IOException e) {
                Log.e("Log", "IOException", e);
                return "failure";
            }catch (Exception e) {
                Log.e("Log", "Exception in doInBackground", e);
                return "failure";
            }
            return "success";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (mContext.isFinishing()) return; // Prevent errors if activity is finishing
            pd.dismiss();

            if (s.equals("success")) {
                try {
                    if (status == 1) {
                        dbHelper.setAsSyncedTravelCompensationGPSData(dbHelper.getDailyTravelDataForCompensationAsArray()); // Use a method to get the array
                        dbHelper.deleteTravelCompensationGPSData();
                        Toast.makeText(mContext, statusMsg, Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        Toast.makeText(mContext, statusMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Fns.neutralAlert("Alert", "Result String :  " + resultString + "\nException : " + Fns.getErrorMsgFromException(e), mContext);
                    Toast.makeText(mContext, "Exception Occurred", Toast.LENGTH_LONG).show();
                }
            } else if (s.equals("failure")) {
                Toast.makeText(mContext, "Failed to update location", Toast.LENGTH_LONG).show();
            }
            else if(s.equals("timeout"))
            {
                Toast.makeText(mContext, "Timeout Occurred", Toast.LENGTH_LONG).show();
            }
            else if (s.equals("nullException"))
            {
                Toast.makeText(mContext, "Null Exception Occurred", Toast.LENGTH_LONG).show();
            }

        }
    }



    private static class LogoutTrackTask extends AsyncTask<Void, Void, String> {
        private final MainMenuActivity mContext;
        private OkHttpClient okHttpClient;
        private SharedPreferences sharedPreferences;
        private ProgressDialog pd;
        private String resultString;

        public LogoutTrackTask(MainMenuActivity context) {
            mContext = context;
            CustomTrust customTrust = new CustomTrust(mContext);
            okHttpClient = customTrust.getClient();
            sharedPreferences = mContext.getSharedPreferences(Const.Shared_Pref_name, MODE_PRIVATE);
            pd = new ProgressDialog(mContext);
            pd.setMessage("Logging out...");
            pd.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try{
                pd.show();
            }catch(Exception e)
            {
                Log.e("onPreExecute","Exception",e);
            }

        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("employeeCode", sharedPreferences.getString(Const.Shp_Employee_Code, ""));

                RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json; charset=utf-8"));
                Request request = new Request.Builder()
                        .url(USING_IP + URL_LOGOUT_TRACK)
                        .post(body)
                        .build();

                Response response = okHttpClient.newCall(request).execute();
                if (!response.isSuccessful()) {
                    return "failure";
                }
                resultString = response.body().string();
                Log.e("Logout", "Response: " + resultString);
                Gson gson = new Gson();
                LogoutTrackJson logoutTrackJson = gson.fromJson(resultString, LogoutTrackJson.class);
                if (logoutTrackJson != null && logoutTrackJson.getData().getLogoutTrackStatus().get(0).getStatus() == 1) {
                    return "success";
                } else {
                    return "failure";
                }
            }  catch (SocketTimeoutException e) {
                Log.e("Log", "SocketTimeoutException", e);
                return "timeout";
            }catch (IOException e) {
                Log.e("Logout", "IO Exception: " + e.getMessage());
                return "failure";
            } catch (Exception e) {
                Log.e("Logout", "Exception: " + e.getMessage());
                return "failure";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (mContext.isFinishing()) return;
            pd.dismiss();
            if (result.equals("success")) {
                Toast.makeText(mContext, "Logged out successfully", Toast.LENGTH_SHORT).show();
            } else if(result.equals("timeout"))
            {
                Toast.makeText(mContext, "Timeout", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(mContext, "Failed to logout", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private static class LoginTaskForVersionCheck extends AsyncTask<String, String, String> {
        private final MainMenuActivity mContext;
        private String userName;
        private String password;
        private OkHttpClient okHttpClient;
        private SharedPreferences sharedPreferences;
        private String resultString;

        public LoginTaskForVersionCheck(MainMenuActivity context, String user, String pass) {
            mContext = context;
            this.userName = user;
            this.password = pass;
            CustomTrust customTrust = new CustomTrust(mContext);
            okHttpClient = customTrust.getClient();
            sharedPreferences = mContext.getSharedPreferences(Const.Shared_Pref_name, MODE_PRIVATE);
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("loginId", userName);
                jsonObject.put("password", password);
                jsonObject.put("versionCode", sharedPreferences.getString(Const.Shp_Version_No, ""));
                jsonObject.put("androidId", sharedPreferences.getString(Const.Shp_Android_Id, ""));
                jsonObject.put("createdBy", "");

                RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json; charset=utf-8"));
                Request request = new Request.Builder()
                        .url(USING_IP + URL_LOGIN)
                        .post(body)
                        .build();
                Response response = okHttpClient.newCall(request).execute();
                if (!response.isSuccessful()) {
                    return "failure";
                }
                resultString = response.body().string();
                Log.e("LoginTask", "Response: " + resultString);
                Gson gson = new Gson();
                LoginStatusJson loginStatusJson = gson.fromJson(resultString, LoginStatusJson.class);
                if (loginStatusJson != null && loginStatusJson.getData().getLoginResponseStatus().get(0).getStatus() == 1) {
                    return "success";
                } else if (loginStatusJson != null && loginStatusJson.getData().getLoginResponseStatus().get(0).getStatus() == 3) {
                    return "update";
                } else {
                    return "failure";
                }
            }  catch (SocketTimeoutException e) {
                Log.e("Log", "SocketTimeoutException", e);
                return "timeout";
            }catch (IOException e) {
                Log.e("LoginTask", "IO Exception: " + e.getMessage());
                return "failure";
            } catch (Exception e) {
                Log.e("LoginTask", "Exception: " + e.getMessage());
                return "failure";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (mContext.isFinishing()) return;  //check
            if (result.equals("success")) {
                // No action needed here, version is OK.
            } else if (result.equals("update")) {
                //showUpdateDialog();
            } else if(result.equals("timeout"))
            {
                Toast.makeText(mContext, "Timeout", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(mContext, "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
            }
        }


    }

}
*/
