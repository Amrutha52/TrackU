package com.happy.ecofied.viewes;

import static com.happy.ecofied.utils.Const.URL_MANUAL_PUNCH;
import static com.happy.ecofied.utils.Const.USING_IP;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.happy.ecofied.R;
import com.happy.ecofied.db.DbHelper;
import com.happy.ecofied.gson.photopunchingjson.Photopunchingjson;
import com.happy.ecofied.ssl.CustomTrust;
import com.happy.ecofied.utils.Const;
import com.happy.ecofied.utils.Fns;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PhotoPunchActivity extends AppCompatActivity
{

    // Define the pic id
    private static final int pic_id = 123;
    // Define the button and imageview type variable
    Button camera_open_id;
    ImageView click_image_id;
    Bitmap photo, resizedBitmapBig;
    Double latitude=0.0, longitude=0.0;
    String locationAddress;
    FusedLocationProviderClient mFusedLocationClient;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_punch);

        View rootView = findViewById(android.R.id.content); // Get root view

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            WindowInsetsCompat insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets.toWindowInsets());
            int systemBarsInsetsTop = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int systemBarsInsetsBottom = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

            // Apply padding to your main content view
            v.setPadding(v.getPaddingLeft(), systemBarsInsetsTop, v.getPaddingRight(), systemBarsInsetsBottom);

            return WindowInsetsCompat.CONSUMED;
        });
        // By ID we can get each component which id is assigned in XML file get Buttons and imageview.
        camera_open_id = findViewById(R.id.camera_button);
        click_image_id = findViewById(R.id.click_image);

        // Camera_open button is for open the camera and add the setOnClickListener in this button
//        camera_open_id.setOnClickListener(v -> {
//            // Create the camera_intent ACTION_IMAGE_CAPTURE it will open the camera for capture the image
//            Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            // Start the activity with camera_intent, and request pic id
//            startActivityForResult(camera_intent, pic_id);
//        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // method to get the location
        getLastLocation();

    }

    private void getLastLocation() {
        // check if permissions are given
        if (checkPermissions()) {

            // check if location is enabled
            if (isLocationEnabled()) {

                // getting last
                // location from
                // FusedLocationClient
                // object
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
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            Log.e("Log", "latitudeInsideFusedlocation" + location.getLatitude());
                            Log.e("Log", "longitudeInsideFusedlocation" + location.getLongitude());
                            latitude = location==null?0.0:location.getLatitude();
                            Log.e("Log", "location Latitude" + latitude);

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

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        // If we want background location
        // on Android 10.0 and higher,
        // use:
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // method to request for permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, 44);
    }

    private void requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            Log.e("Log", "latitude" + mLastLocation.getLatitude());
            Log.e("Log", "latitude" + mLastLocation.getLatitude());
            // latitudeTextView.setText("Latitude: " + mLastLocation.getLatitude() + "");
            //longitTextView.setText("Longitude: " + mLastLocation.getLongitude() + "");
        }
    };

    // method to check
    // if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    // This method will help to retrieve the image
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Match the request 'pic id with requestCode
        if (requestCode == pic_id) {
            // BitMap is data structure of image file which store the image in memory
            photo = (Bitmap) data.getExtras().get("data");
            // Set the image in imageview for display
            click_image_id.setImageBitmap(photo);
        }
    }

    public void listeners(View view)
    {
        switch (view.getId())
        {
            case R.id.camera_button:
            {

                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Start the activity with camera_intent, and request pic id
                startActivityForResult(camera_intent, pic_id);
            }
            break;

            case R.id.submitButon:
            {
                /**
                 * Today's Date
                 */
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String currentDateAndTime = sdf.format(calendar.getTime());
                Log.e("Log", "currentDateAndTime" + currentDateAndTime);

                /**
                 * Bitmap to base64
                 */

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] bytearray = stream.toByteArray();

                InputStream myInputStream = new ByteArrayInputStream(bytearray);
                Bitmap bitmap = BitmapFactory.decodeStream(myInputStream);
                //Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 300, 200, true);
                //Drawable image = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(bytearray, 0, bytearray.length));


                //previewImageView.setImageDrawable(image);
                resizedBitmapBig = Bitmap.createScaledBitmap(bitmap, 480, 800, true);
                if(bytearray.length<=1024)
                {

                    resizedBitmapBig = bitmap;

                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                resizedBitmapBig.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream .toByteArray();

                String base64 = Base64.encodeToString(byteArray, Base64.DEFAULT);

                new PushPhotoPunchingDetails(this, currentDateAndTime, base64, longitude, latitude, locationAddress).execute();

            }
            break;
        }
    }

    private static class PushPhotoPunchingDetails extends AsyncTask<String, String, String>
    {
        OkHttpClient okHttpClient;
        String url;
        Request request;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        PhotoPunchActivity mContext;
        ProgressDialog pd;
        SharedPreferences shp;
        String failureMsg, resultString;
        Photopunchingjson photopunchingjson;
        DbHelper dbHelper;
        int status;
        double longitude, latitude;
        String locationAddress;

        String punchingDateTime, punchingImage;
        public PushPhotoPunchingDetails(PhotoPunchActivity mContext, String currentDateAndTime, String base64, double longitude, double latitude, String locationAddress)
        {
            this.mContext = mContext;
            this.punchingDateTime = currentDateAndTime;
            this.punchingImage = base64;
            this.longitude = longitude;
            this.latitude = latitude;
            this.locationAddress = locationAddress;

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

                JSONObject photoPunchObj = new JSONObject();
                photoPunchObj.put("createdBy", shp.getString(Const.Shp_Employee_Code, ""));
                photoPunchObj.put("employeeCode", shp.getString(Const.Shp_Employee_Code, ""));
                photoPunchObj.put("date", punchingDateTime);
                photoPunchObj.put("employeeImage", punchingImage);
                photoPunchObj.put("Longitude", longitude);
                photoPunchObj.put("Latitude", latitude);
                photoPunchObj.put("Address", locationAddress);

                //photoPunchObj.put("versionCode", shp.getString(Const.Shp_Version_No, ""));

                url = USING_IP + URL_MANUAL_PUNCH;
                Log.e("Log", "photoPunchURL" + url);
                Log.e("Log", "photoPunchJsonObject" + photoPunchObj);

                RequestBody body = RequestBody.create(photoPunchObj.toString(), JSON);

                request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                Log.e("Log", "request" + request);

                Response response = okHttpClient.newCall(request).execute();
                Log.e("Log", "response" + response);

                if (!response.isSuccessful())
                {
                    return "failure";
                }

                resultString = response.body().string();
                Log.e("Log", "resultString" + resultString);

                Gson gson = new Gson();
                photopunchingjson = gson.fromJson(resultString, Photopunchingjson.class);

                if (photopunchingjson.getData().getPhotoPunchStatus().isEmpty() || photopunchingjson.getData().getPhotoPunchStatus().size() == 0 || photopunchingjson.getData().getPhotoPunchStatus() == null)
                {
                    return "nullException";
                }
                else
                {
                    status = photopunchingjson.getData().getPhotoPunchStatus().get(0).getStatus();
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
            pd.dismiss();

            if (s.equals("success"))
            {
              //  Fns.neutralAlert("Alert", photopunchingjson.getData().getPhotoPunchStatus().get(0).getStatusMsg(), mContext);

//                if (photopunchingjson.getData().getPhotoPunchStatus().get(0).getStatus() == 1)
//                {
//
//                    mContext.finish();
//                }

                AlertDialog.Builder adb = new AlertDialog.Builder(mContext);

                TextView titletxtview = new TextView(mContext);
                titletxtview.setText("Alert");
                titletxtview.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                titletxtview.setPadding(10, 10, 10, 10);
                titletxtview.setGravity(Gravity.CENTER);
                titletxtview.setTextColor(Color.WHITE);
                titletxtview.setTextSize(20);

                adb.setCustomTitle(titletxtview);

                TextView messagetxtview = new TextView(mContext);
                messagetxtview.setText(photopunchingjson.getData().getPhotoPunchStatus().get(0).getStatusMessage());
                messagetxtview.setBackgroundColor(Color.WHITE);
                messagetxtview.setPadding(10, 24, 10, 10);
                messagetxtview.setGravity(Gravity.CENTER);
                messagetxtview.setTextColor(Color.BLACK);
                messagetxtview.setTextSize(18);
                messagetxtview.setVerticalScrollBarEnabled(true);
                messagetxtview.setMaxHeight(750);
                messagetxtview.setMovementMethod(new ScrollingMovementMethod());

                adb.setView(messagetxtview);

                adb.setNegativeButton("OK", new DialogInterface.OnClickListener()
                {

                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //dialog.cancel();
                        mContext.finish();

                    }
                });

                AlertDialog ad = adb.create();
                ad.show();

            }
            else if (s.equals("failure"))
            {
                Fns.neutralAlert("Alert", photopunchingjson.getData().getPhotoPunchStatus().get(0).getStatusMessage(), mContext);
                // Toast.makeText(context.get(), "Updation Failed", Toast.LENGTH_SHORT).show();
            }
            else if (s.equals("nullException"))
            {
                Toast.makeText(mContext, "Null Exception From Server", Toast.LENGTH_SHORT).show();
            }
        }

    }
}