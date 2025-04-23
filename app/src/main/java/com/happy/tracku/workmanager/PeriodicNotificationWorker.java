package com.happy.tracku.workmanager;

import static android.content.Context.MODE_PRIVATE;

import static com.happy.tracku.utils.Const.URL_Update_Daily_GPS_Data;
import static com.happy.tracku.utils.Const.USING_IP;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.happy.tracku.db.DbHelper;
import com.happy.tracku.gson.gpsstatusjson.GPSUpdateStatusJson;
import com.happy.tracku.models.DailyTravelModel;
import com.happy.tracku.ssl.CustomTrust;
import com.happy.tracku.utils.Const;
import com.happy.tracku.utils.Fns;

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

public class PeriodicNotificationWorker extends Worker
{
    private static final int UPDATE_INTERVAL_IN_MILLI_SECONDS = 300000; // 5 minutes
    private static final int UPDATE_FASTEST_INTERVAL_IN_MILLI_SECONDS = 300000;
    private static final int PERMISSION_REQUEST_ID = 44;

    String locationAddress;

    private FusedLocationProviderClient mFusedLocationClient;

    public PeriodicNotificationWorker(@NonNull Context context, @NonNull WorkerParameters params)
    {
        super(context, params);
        Log.e("Log", "InsidePeriodicNotification");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    @NonNull
    @Override
    public Result doWork()
    {
        Context appContext  = getApplicationContext();
        getLocation(appContext);
        return Result.success();
    }

    private void getLocation(Context appContext)
    {
        LocationRequest mLocationRequestHighAccuracy = new LocationRequest();

        mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequestHighAccuracy.setInterval(UPDATE_INTERVAL_IN_MILLI_SECONDS);
        mLocationRequestHighAccuracy.setFastestInterval(UPDATE_FASTEST_INTERVAL_IN_MILLI_SECONDS);
        if (ActivityCompat.checkSelfPermission(appContext,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) getApplicationContext(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_ID);
            Log.e("Log", "Permission not granted");
            // Handle permission not granted
        }

        DbHelper dbHelper = new DbHelper(appContext);
        SharedPreferences shp = appContext.getSharedPreferences(Const.Shared_Pref_name, MODE_PRIVATE);

       // FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

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
                                    new UploadEmployeeTravelGPSDataForWorkManager(appContext).execute();
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


    private static class UploadEmployeeTravelGPSDataForWorkManager extends AsyncTask<String,String,String>
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
        public UploadEmployeeTravelGPSDataForWorkManager(Context context)
        {
            Log.e("Log", "UploadEmployeeTravelGPSDataForWorkManager");
            this.context = new WeakReference<>(context);

            CustomTrust customTrust = new CustomTrust(context);
            OkHttpClient client = customTrust.getClient();
            okHttpClient = client;
            /*okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(180, TimeUnit.SECONDS)
                    .build();*/
            dbHelper = new DbHelper(context);
            shp = context.getSharedPreferences(Const.Shared_Pref_name, MODE_PRIVATE);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /*pd = new ProgressDialog(context.get());
            pd.setTitle("Updating");
            pd.setMessage("Please wait few seconds...");
            pd.setCancelable(false);
            //pd.setIndeterminate(true);
            pd.setMax(2);
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.show();*/

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

                Log.e("Log","UploadEmployeeTravelGPSDataForWorkManagerURL" + url);
                Log.e("Log", "UploadEmployeeTravelGPSDataForWorkManagerJson"+jsonObject.toString());

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

                Log.e("Log", "UploadEmployeeTravelGPSDataForWorkManagerResultStriing" + resultString);

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
