package com.happy.tracku.viewes;

import static android.icu.util.MeasureUnit.DOT;
import static com.happy.tracku.utils.Const.USING_IP;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.gson.Gson;
import com.google.maps.android.SphericalUtil;
import com.happy.tracku.R;
import com.happy.tracku.db.DbHelper;
import com.happy.tracku.gson.dailywisegpsdatajsondetails.DailyWiseGPSDataJson;
import com.happy.tracku.gson.dailywisegpsdatajsondetails.DailyWiseGPSDataResponsestatus;
import com.happy.tracku.ssl.CustomTrust;
import com.happy.tracku.utils.Const;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener {
    private static final int PATTERN_GAP_LENGTH_PX = 10;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);
    ArrayList<LatLng> latlngPoints;
    Double latitude, longitude;
    private GoogleMap mMap;
    List<DailyWiseGPSDataJson> gpsDetailArrayList;
    Intent intent;
    String travelDateString, employeeCodeString;
    ArrayList<String> cityArrayList;
    String returnAddress;
    private static final int COLOR_YELLOW_ARGB = 0xffF9A825;
    private static final int POLYLINE_STROKE_WIDTH_PX = 10;
    LatLng copoints, firstLatLng;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        intent = getIntent();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        latlngPoints = new ArrayList<>();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap)
    {
        mMap = googleMap;

        travelDateString = intent.getStringExtra("fromDate");
        Log.e("Log", "travelDateString" + travelDateString);
        employeeCodeString = intent.getStringExtra("employeeCode");
        Log.e("Log", "employeeCodeString" + employeeCodeString);

        new getLocationOfEmployee(this, employeeCodeString, travelDateString).execute();

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture)
    {
        super.onPointerCaptureChanged(hasCapture);
    }

    private String getCompleteAddressString(double latitude, double longitude)
    {

        cityArrayList = new ArrayList<String>();

        latlngPoints.add(new LatLng(latitude, longitude));
        Log.e("Log", "latlngPoints" +latlngPoints.toString());


        for (int j =0; j<latlngPoints.size(); j++)
        {
            Log.e("Log","InsideForJ");
            Log.e("Log", "latlngPointsSize" + latlngPoints.size());
            firstLatLng = latlngPoints.get(j);
            Log.e("firstLatLng", String.valueOf(firstLatLng));


            for (int k =1; k<latlngPoints.size(); k++)
            {
                Log.e("Log", "InsideForK");
                copoints = latlngPoints.get(k);
                Log.e("coPoint", String.valueOf(copoints));

                //  getDistance(firstLatLng, copoints);

                String.valueOf(SphericalUtil.computeDistanceBetween(firstLatLng, copoints)); // Return distance between in Meters
                //  Log.e("distOfTwoPoints", String.valueOf(SphericalUtil.computeDistanceBetween(firstLatLng, copoints)));

                Polyline polyline2 = mMap.addPolyline(new PolylineOptions()
                        .clickable(true)
                        .add(firstLatLng,copoints));
                polyline2.setTag("A");

                stylePolyline(polyline2);
                mMap.setOnPolylineClickListener(this);
            }
        }


        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null) {
                returnAddress = addresses.get(0).getAddressLine(0);
                Log.e("locationAddress", returnAddress);
                cityArrayList.add(returnAddress);

                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(returnAddress)
                );

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 10));



            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("loctionaddress", "Unnamed Address!");
        }
        return returnAddress;
    }

    @Override
    public void onPolylineClick(@NonNull Polyline polyline)
    {
        Log.e("Log", "InsideonPolyLineClick");
        // Flip from solid stroke to dotted stroke pattern.
        if ((polyline.getPattern() == null) || (!polyline.getPattern().contains(DOT))) {
            polyline.setPattern(PATTERN_POLYLINE_DOTTED);
        } else {
            // The default pattern is a solid stroke.
            polyline.setPattern(null);
        }

        Toast.makeText(this, "Route type " + polyline.getTag().toString(),
                Toast.LENGTH_SHORT).show();
    }


    private class getLocationOfEmployee extends AsyncTask<String, String, String>  {
        OkHttpClient okHttpClient;
        String url, employeeCodeString, travelDateString;
        Request request;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        MapActivity mContext;

        ProgressDialog pd;
        SharedPreferences shp;
        String failureMsg, resultString;
        DailyWiseGPSDataJson dailyWiseGPSDataJson;
        DbHelper dbHelper;
        List<DailyWiseGPSDataResponsestatus> dailyWiseGPSDataResponsestatusList;
        ArrayList<LatLng> latlngPoints;
        Double latitude, longitude;
        LatLng copoints, firstLatLng;

        public getLocationOfEmployee(MapActivity mContext, String employeeCodeString, String travelDateString)
        {
            Log.e("Log", "InsideUpdateLocation");
            this.mContext = mContext;
            this.employeeCodeString = employeeCodeString;
            this.travelDateString = travelDateString;

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
            try
            {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("createdBy", employeeCodeString);
                jsonObject.put("fromDate", travelDateString);

                url = USING_IP + Const.URL_GET_DAILYWISE_EMPLOYEE_TRAVEL_DATA;

                Log.e("Log", "getEmployeeLocationURL" +url);
                Log.e("Log", "getEmployeeLocationJSON" +jsonObject.toString());

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
                dailyWiseGPSDataJson = gson.fromJson(resultString, DailyWiseGPSDataJson.class);

                if (dailyWiseGPSDataJson.getData().getDailyWiseGPSDataResponseStatus().isEmpty() || dailyWiseGPSDataJson.getData().getDailyWiseGPSDataResponseStatus().size() == 0 )
                {
                    return "nullException";
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
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            pd.dismiss();

            if (s.equals("success"))
            {
                dailyWiseGPSDataResponsestatusList = dailyWiseGPSDataJson.getData().getDailyWiseGPSDataResponseStatus();

                DailyWiseGPSDataResponsestatus dailyWiseGPSDataResponsestatus = new DailyWiseGPSDataResponsestatus();

                for (int i =0; i < dailyWiseGPSDataResponsestatusList.size(); i++){
                    dailyWiseGPSDataResponsestatus = dailyWiseGPSDataResponsestatusList.get(i);
                    latitude = Double.valueOf(dailyWiseGPSDataResponsestatus.getLatitude());
                    longitude = Double.valueOf(dailyWiseGPSDataResponsestatus.getLongitude());

                    getCompleteAddressString(latitude, longitude);

                }


            }
            else if (s.equals("failure"))
            {
                Toast.makeText(mContext, "Failure", Toast.LENGTH_LONG).show();
            }
            else if (s.equals("nullException"))
            {

                Toast.makeText(mContext, "Null Exception From Server", Toast.LENGTH_LONG).show();

            }

        }


    }

    private void stylePolyline(Polyline polyline)
    {
        Log.e("Log", "polyline" + polyline.getTag());
        Log.e("Log","stylePolyline");
        String type = "";
        // Get the data object stored with the polyline.
        if (polyline.getTag() != null) {
            type = polyline.getTag().toString();
        }

        switch (type) {

            // If no type is given, allow the API to use the default.
            case "B":
                // Use a custom bitmap as the cap at the start of the line.
                polyline.setStartCap(
                        new CustomCap(
                                BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow), 10));
                break;
            case "A":
                // Use a round cap at the start of the line.
                polyline.setStartCap(new RoundCap());
                break;
        }

        polyline.setEndCap(new RoundCap());
        polyline.setWidth(POLYLINE_STROKE_WIDTH_PX);
        polyline.setColor(COLOR_YELLOW_ARGB);
        polyline.setJointType(JointType.ROUND);
    }

}