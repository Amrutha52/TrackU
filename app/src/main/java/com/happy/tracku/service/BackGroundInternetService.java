package com.happy.tracku.service;

import static com.happy.tracku.utils.Const.URL_Update_Daily_GPS_Data;
import static com.happy.tracku.utils.Const.USING_IP;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.happy.tracku.MainActivity;
import com.happy.tracku.db.DbHelper;
import com.happy.tracku.gson.gpsstatusjson.GPSUpdateStatusJson;
import com.happy.tracku.models.DailyTravelModel;
import com.happy.tracku.receiverss.NetworkChangeReceiver;
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
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BackGroundInternetService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {


    private static NetworkChangeReceiver networkChangeReceiver;
    private static final String TAG = BackGroundInternetService.class.getSimpleName();
    //private MyLocationListener mMyLocationListener;

    private final int LOCATION_INTERVAL = 3 * 60 * 1000;
    private final int LOCATION_DISTANCE = 1;
    Context context;
    String locationAddress;

    // the notification id for the foreground notification
    public static final int GPS_NOTIFICATION = 1;
    // the interval in seconds that gps updates are requested
    private static final int UPDATE_INTERVAL_IN_MILLI_SECONDS = 600000;
    // is this service currently running in the foreground?
    private boolean isForeground = false;
    // the google api client
    private GoogleApiClient googleApiClient;

    // the wakelock used to keep the app alive while the screen is off
    private PowerManager.WakeLock wakeLock;
    LocationCallback locCallback;


    public BackGroundInternetService() {
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e("Log", "onStartCommand");

        super.onStartCommand(intent, flags, startId);

        context = getApplicationContext();

        if (!isForeground) {

            Log.v(TAG, "Starting the " + this.getClass().getSimpleName());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            {
                startForeground(BackGroundInternetService.GPS_NOTIFICATION,
                        notifyUserThatLocationServiceStarted(), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);

            }
            else
            {
                startForeground(BackGroundInternetService.GPS_NOTIFICATION,
                        notifyUserThatLocationServiceStarted());
            }



            isForeground = true;

            // connect to google api client
            googleApiClient.connect();

            // acquire wakelock
            //wakeLock.acquire();
        }

        return START_REDELIVER_INTENT;
    }

    private Notification notifyUserThatLocationServiceStarted() {

        // pop up a notification that the location service is running
        Intent notificationIntent = new Intent(this, MainMenuActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);

        final Notification.Builder builder = new Notification.Builder(this)
                //.setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("TrackU")
                .setContentText("GPS Service Running")
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis());

        final Notification notification;
        if (Build.VERSION.SDK_INT < 16) {
            notification = builder.getNotification();
        }else if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {

            NotificationChannel channel = new NotificationChannel(
                    "channel_01",
                    "My Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            Notification.Builder builder2 = new Notification.Builder(getApplicationContext(), "channel_01");
            notification = builder2.build();

        } else {
            notification = builder.build();
        }

        return notification;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");

        //return binder;
        return null;

    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.e("Log", "onCreate Service");

        context = getApplicationContext();
        //Internet Check and Upload
        networkChangeReceiver = new NetworkChangeReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkChangeReceiver, filter);

        // create google api client
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // get a wakelock from the power manager
        final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);





    }


    @Override
    public void onDestroy() {

        Log.v(TAG, "Stopping the " + this.getClass().getSimpleName());

        stopForeground(true);
        isForeground = false;

        // disconnect from google api client
        googleApiClient.disconnect();

        // release wakelock if it is held
        if (null != wakeLock && wakeLock.isHeld()) {
            wakeLock.release();
        }

        unregisterReceiver(networkChangeReceiver);

        super.onDestroy();

    }

    private LocationRequest getLocationRequest() {

        LocationRequest locationRequest = LocationRequest.create();

        // we always want the highest accuracy
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // we want to make sure that we get an updated location at the specified interval
        locationRequest.setInterval(TimeUnit.SECONDS.toMillis(0));

        // this sets the fastest interval that the app can receive updates from other apps accessing
        // the location service. for example, if Google Maps is running in the background
        // we can update our location from what it sees every five seconds
        locationRequest.setFastestInterval(TimeUnit.SECONDS.toMillis(0));
        locationRequest.setMaxWaitTime(TimeUnit.SECONDS.toMillis(UPDATE_INTERVAL_IN_MILLI_SECONDS));

        return locationRequest;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        try {


            FusedLocationProviderClient client = LocationServices
                    .getFusedLocationProviderClient(getApplicationContext());
            //Define quality of service:
            LocationRequest request = LocationRequest.create();
            //request.setInterval(30*60*1000); //Every 30 mins
            request.setInterval(60*1000*5); //Every 5 min
            request.setSmallestDisplacement(100); //Every 100 meters
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            if (locCallback != null) {
                client.removeLocationUpdates(locCallback);
            }
            locCallback = createNewLocationCallback();
            client.requestLocationUpdates(request, locCallback, null);

            // request location updates from the fused location provider


        } catch (SecurityException securityException) {
            Log.e(TAG, "Exception while requesting location updates", securityException);
        }

    }

    private LocationCallback createNewLocationCallback()
    {
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                Location location = result.getLastLocation();
                // Do something with the location (may be null!)

                Log.e("Log", "Location Changed: " + location);
                //TODO Push to database and send to server

                Calendar cal = Calendar.getInstance();
                Date dateNow = cal.getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String dateTimeString = sdf.format(dateNow);
                SharedPreferences shp = context.getSharedPreferences(Const.Shared_Pref_name,Context.MODE_PRIVATE);
                SimpleDateFormat sdf2 = new SimpleDateFormat("ddMMyyHHmmss");
                String idLocation = shp.getString(Const.Shp_Employee_Code, "") + "" + sdf2.format(dateNow);

                Double latitude = location.getLatitude();
                Double longitude = location.getLongitude();
                DbHelper dbHelper = new DbHelper(context);

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

                SimpleDateFormat sdf3 = new SimpleDateFormat("HHmmss");
                String timeNowString = sdf3.format(dateNow);
                int timeNowValue = Integer.parseInt(timeNowString);

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
                       // new UploadTravelCompensationGPSData(context).execute();
                    }


                }catch (Exception e)
                {
                    Log.e("Log","Exception",e);

                }


            }
        };
        return locationCallback;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



    private static class UploadTravelCompensationGPSData extends AsyncTask<String, String, String> {

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

        public UploadTravelCompensationGPSData(Context context)
        {
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

                Log.e("Log","uploadUserDataService" + url);
                Log.e("Log", jsonObject.toString());

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

                Log.e("Log", "" + resultString);

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

