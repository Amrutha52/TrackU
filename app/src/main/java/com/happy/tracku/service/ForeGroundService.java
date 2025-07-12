/*package com.happy.tracku.service;

import static com.happy.tracku.utils.Const.URL_Update_Daily_GPS_Data;
import static com.happy.tracku.utils.Const.USING_IP;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import android.Manifest;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.happy.tracku.R;
import com.happy.tracku.db.DbHelper;
import com.happy.tracku.gson.gpsstatusjson.GPSUpdateStatusJson;
import com.happy.tracku.models.DailyTravelModel;
import com.happy.tracku.ssl.CustomTrust;
import com.happy.tracku.utils.Const;
import com.happy.tracku.utils.Fns;
import com.happy.tracku.viewes.MainMenuActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ForeGroundService extends Service
{
    private static final int UPDATE_INTERVAL_IN_MILLI_SECONDS = 600000; // 10 minutes
    private static final int UPDATE_FASTEST_INTERVAL_IN_MILLI_SECONDS = 600000;

    private static final int PERMISSION_REQUEST_ID = 44;
    Context context;
    String locationAddress;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        context = getApplicationContext();

        createNotificationChannel();
        getLocation();
        startForeground(1, getNotification());
        return START_STICKY;
    }

    private Notification getNotification() {
        Intent notificationIntent = new Intent(this, MainMenuActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, PERMISSION_REQUEST_ID,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new
                NotificationCompat.Builder(this, "CHANNEL01")
                .setContentTitle("TrackU Location Service")
                .setContentText("Getting location updates")
                .setSmallIcon(R.drawable.trackulogo)
                .setContentIntent(pendingIntent)
                .setOngoing(true);
        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.S) {

            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE);
        }
        return builder.build();
    }

    private void getLocation()
    {
        LocationRequest mLocationRequestHighAccuracy = new LocationRequest();

        mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequestHighAccuracy.setInterval(UPDATE_INTERVAL_IN_MILLI_SECONDS);
        mLocationRequestHighAccuracy.setFastestInterval(UPDATE_FASTEST_INTERVAL_IN_MILLI_SECONDS);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) getApplicationContext(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_ID);
            Log.e("Log", "Permission not granted");
            // Handle permission not granted
        }

        DbHelper dbHelper = new DbHelper(context);
        SharedPreferences shp = context.getSharedPreferences(Const.Shared_Pref_name,Context.MODE_PRIVATE);

        FusedLocationProviderClient mFusedLocationClient = LocationServices
                .getFusedLocationProviderClient(getApplicationContext());

        mFusedLocationClient.requestLocationUpdates(mLocationRequestHighAccuracy,
                new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult)
                    {
                        Location location = locationResult.getLastLocation();
                        if (location != null)
                        {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            Log.e("Log", "latitudeForeGround" + latitude);
                            Log.e("Log", "longitudeForeGround" + longitude);
                            // Process latitude and longitude as needed

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

                            DailyTravelModel dailyTravelModel = new DailyTravelModel();
                            dailyTravelModel.setIdLocation(String.valueOf(dateNow.getTime()));
                            dailyTravelModel.setLatitude(latitude);
                            dailyTravelModel.setLongitude(longitude);
                            dailyTravelModel.setAddress(locationAddress);
                            dailyTravelModel.setDateTime(dateTimeString);
                            dailyTravelModel.setIdEmployee(shp.getInt(Const.Shp_Id_Employee, 0));
                            dailyTravelModel.setIsForUpload(0);
                            dailyTravelModel.setIsSynced(0);

                            dbHelper.insertContinousGPSLocationOfAnEmployee(dailyTravelModel);

                            try{

                                if(dbHelper.getDailyTravelDataForCompensationAsArray().size() > 1)
                                {
                                    new UploadEmployeeTravelGPSData(context).execute();
                                }


                            }catch (Exception e)
                            {
                                Log.e("Log","Exception",e);

                            }
                        }
                    }
                },
                Looper.myLooper());
    }


    private void createNotificationChannel()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "CHANNEL01",
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager =
                    getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private static class UploadEmployeeTravelGPSData extends AsyncTask<String, String, String>
    {
        ProgressDialog pd;
        WeakReference<Context> context;
        private OkHttpClient okHttpClient;
        private Request request;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String url;
        DbHelper dbHelper;
        SharedPreferences shp;
        String resultString;
        ArrayList<DailyTravelModel> dailyTravelModelArrayList;
        GPSUpdateStatusJson gpsUpdateStatusJson;
        int status;
        public UploadEmployeeTravelGPSData(Context context)
        {
            Log.e("Log", "UploadEmployeeTravelGPSDataForeGround");
            this.context = new WeakReference<>(context);

            CustomTrust customTrust = new CustomTrust(context);
            OkHttpClient client = customTrust.getClient();
            okHttpClient = client;

            dbHelper = new DbHelper(context);
            shp = context.getSharedPreferences(Const.Shared_Pref_name, MODE_PRIVATE);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();



        }

        @Override
        protected String doInBackground(String... strings)
        {

            try {

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String todayString = sdf.format(Calendar.getInstance().getTime());



                JSONObject jsonObject = new JSONObject();
                jsonObject.put("createdBy", shp.getString(Const.Shp_Employee_Code, ""));
                JSONArray dailyTravelDataJsonArray = dbHelper.getDailyTravelDataForCompensation();
                dailyTravelModelArrayList = dbHelper.getDailyTravelDataForCompensationAsArray();
                jsonObject.put("dailyGPSData", dailyTravelDataJsonArray);


                url = USING_IP + URL_Update_Daily_GPS_Data;

                Log.e("Log","uploadUserDataServiceURL" + url);
                Log.e("Log", "uploadUserDataServiceJson"+jsonObject.toString());

                RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
                request = new Request.Builder()
                        //.header("X-Client-Type", "Android")
                        .url(url)
                        .post(body)
                        .build();

                Response response = okHttpClient.newCall(request).execute();

                if (!response.isSuccessful()) {

                    return "failure";

                }

                resultString = response.body().string();

                Log.e("Log", "uploadUserDataServiceResultStriing" + resultString);

                Gson gson = new Gson();

                gpsUpdateStatusJson = gson.fromJson(resultString,GPSUpdateStatusJson.class);

                if (gpsUpdateStatusJson.getData().getLocationUpdateStatus().isEmpty() || gpsUpdateStatusJson.getData().getLocationUpdateStatus().size() == 0)
                {
                    return "nullException";
                }

                status = gpsUpdateStatusJson.getData().getLocationUpdateStatus().get(0).getStatus();
                if (status != 1)
                {
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

            //pd.dismiss();

            if (s.equals("success")) {

                try {

                    if (status == 1)
                    {
                        dbHelper.setAsSyncedTravelCompensationGPSData(dailyTravelModelArrayList);

                    }


                } catch (Exception e) {

                    Log.e("Log","Exception",e);
                    //Fns.neutralAlert("Alert","Result String :  "+resultString+"\nException : "+Fns.getErrorMsgFromException(e), context.get());
                    Toast.makeText(context.get(), "Exception Occurred GPS Compensation upload", Toast.LENGTH_LONG).show();

                }

            } else if (s.equals("nullException"))
            {
                Fns.neutralAlert("Alert", gpsUpdateStatusJson.getData().getLocationUpdateStatus().get(0).getStatusMsg(), context.get());
            } else
            {

                Toast.makeText(context.get(), "Failed to Push", Toast.LENGTH_LONG).show();

            }

        }

    }

}

 */

