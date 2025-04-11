package com.happy.tracku.viewes;

import static android.icu.util.MeasureUnit.DOT;
import static com.google.android.gms.maps.model.JointType.ROUND;
import static com.happy.tracku.utils.Const.USING_IP;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import com.happy.tracku.R;
import com.happy.tracku.db.DbHelper;
import com.happy.tracku.gson.dailywisegpsdatajsondetails.DailyWiseGPSDataJson;
import com.happy.tracku.gson.dailywisegpsdatajsondetails.DailyWiseGPSDataResponsestatus;
import com.happy.tracku.models.events.BeginJourneyEvent;
import com.happy.tracku.models.events.CurrentJourneyEvent;
import com.happy.tracku.models.events.EndJourneyEvent;
import com.happy.tracku.models.events.JourneyEventBus;
import com.happy.tracku.models.events.Result;
import com.happy.tracku.ssl.CustomTrust;
import com.happy.tracku.utils.ApiClient;
import com.happy.tracku.utils.ApiInterface;
import com.happy.tracku.utils.Common;
import com.happy.tracku.utils.Const;
import com.happy.tracku.utils.IGoogleApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;
import retrofit2.Callback;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener {
    private static final int PATTERN_GAP_LENGTH_PX = 10;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);
    List<LatLng> latlngPoints;

    private GoogleMap mMap;
    List<DailyWiseGPSDataJson> gpsDetailArrayList;
    Intent intent;
    String travelDateString, employeeCodeString;
    ArrayList<String> cityArrayList;
    String returnAddress;
    private static final int COLOR_YELLOW_ARGB = 0xffF9A825;
    private static final int POLYLINE_STROKE_WIDTH_PX = 5;
    LatLng copoints, firstLatLng;
    Bitmap BitMapMarker;
    SupportMapFragment mapFragment;
    /**
     * PolyLine
     */
    private List<LatLng> polylineList;
    private Marker marker;
    private float v;
    private double latitude, longitude;
    private Handler handler;
    private LatLng startPosition, endPosition;
    private int index, next;
   // private PolylineOptions polylineOptions, blackPolyLineOptions;
  //  private Polyline blackPolyLine, greyPolyLine;
    private LatLng myLocation;
    IGoogleApi mService;
    private Marker carMarker;

    /**
     * Uber like
     */
    private PolylineOptions polylineOptions, blackPolylineOptions;
    private Polyline blackPolyline, greyPolyLine;

    private double lat = 0.0, lng = 0.0;
    private LinearLayout linearLayout;
    private Disposable disposable;
    private ApiInterface apiInterface;
    private LatLng sydney;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        intent = getIntent();

        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_car);
        Bitmap b = bitmapdraw.getBitmap();
        BitMapMarker = Bitmap.createScaledBitmap(b, 110, 60, false);


        /**
         * directionapi
         */
        polylineList = new ArrayList<>();
        mService = Common.getGoogleApi();

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        latlngPoints = new ArrayList<>();

        endPosition = new LatLng(lat, lng);
        startPosition = new LatLng(lat, lng);

    }

  /*  @Override
    protected void onResume()
    {
        super.onResume();
        //This is an event bus for receiving journey events this can be shifted anywhere
        //in code.
        //Do remember to dispose when not in use. For eg. its necessary to dispose it in
        //onStop as activity is not visible.
        disposable = JourneyEventBus.getInstance().getOnJourneyEvent()
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        if (o instanceof BeginJourneyEvent) {
                            Snackbar.make(linearLayout, "Journey has started",
                                    Snackbar.LENGTH_SHORT).show();
                        } else if (o instanceof EndJourneyEvent) {
                            Snackbar.make(linearLayout, "Journey has ended",
                                    Snackbar.LENGTH_SHORT).show();
                        } else if (o instanceof CurrentJourneyEvent) {

                             // This can be used to receive the current location update of the car

                            //Log.d(TAG,"Current "+((CurrentJourneyEvent) o).getCurrentLatLng());
                        }
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!disposable.isDisposed()) {
            disposable.dispose();
        }
    }

   */


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap)
    {
        mMap = googleMap;

        /**
         * Uber like execution
         */
        // Add a marker in Home and move the camera
       // sydney = new LatLng(28.671246, 77.317654);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);

 /*     LatLng thrissur = new LatLng(10.5276416 , 76.21443490000001);
      LatLng ernakulam = new LatLng(9.981636  , 76.299884);
      mMap.addMarker(new MarkerOptions().position(thrissur).title("My Location"));
      mMap.moveCamera(CameraUpdateFactory.newLatLng(thrissur));
      mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
              .target(googleMap.getCameraPosition().target)
              .zoom(17)
              .bearing(30)
              .tilt(45)
              .build())
      );

  */


    /*  String requestURL = null;
      try
      {
          requestURL = "https://maps.googleapis.com/maps/api/directions/json?"+"mode=driving&"
                  +"transit_routing_preference=less_driving&"
                  +"origin="+thrissur.latitude+","+thrissur.longitude+"&"+
                  "destination="+ernakulam.latitude+","+ernakulam.longitude+"&"+
                  "key="+getResources().getString(R.string.google_map_api_key);
          Log.e("URL", requestURL);
          mService.getDataFromGoogleApi(requestURL)
                  .enqueue(new Callback<String>() {
                      @Override
                      public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                          try {
                              JSONObject jsonObject = new JSONObject(response.body().toString());
                              JSONArray jsonArray = jsonObject.getJSONArray("routes");
                              for (int i = 0; i < jsonArray.length(); i++)
                              {
                                  JSONObject route = jsonArray.getJSONObject(i);
                                  JSONObject poly = route.getJSONObject("overview_polyline");
                                  String polyline = poly.getString("points");
                                  polylineList = decodePoly(polyline);
                              }

                              // Adjusting Bounds
                              LatLngBounds.Builder builder = new LatLngBounds.Builder();

                              for (LatLng latLng : polylineList) {
                                  builder.include(latLng);
                              }
                              LatLngBounds bounds = builder.build();
                              CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2);
                              mMap.animateCamera(mCameraUpdate);

                              polylineOptions = new PolylineOptions();
                              polylineOptions.color(Color.GRAY);
                              polylineOptions.width(5);
                              polylineOptions.startCap(new SquareCap());
                              polylineOptions.endCap(new SquareCap());
                              polylineOptions.jointType(ROUND);
                              polylineOptions.addAll(polylineList);
                              greyPolyLine = mMap.addPolyline(polylineOptions);

                              blackPolylineOptions = new PolylineOptions();
                              blackPolylineOptions.width(5);
                              blackPolylineOptions.color(Color.BLACK);
                              blackPolylineOptions.startCap(new SquareCap());
                              blackPolylineOptions.endCap(new SquareCap());
                              blackPolylineOptions.jointType(ROUND);
                              blackPolylineOptions.addAll(polylineList);
                              blackPolyline = mMap.addPolyline(blackPolylineOptions);

                              mMap.addMarker(new MarkerOptions()
                                      .position(polylineList.get(polylineList.size() - 1)));

                              ValueAnimator polylineAnimator = ValueAnimator.ofInt(0, 100);
                              polylineAnimator.setDuration(2000);
                              polylineAnimator.setInterpolator(new LinearInterpolator());
                              polylineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                  @Override
                                  public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                      List<LatLng> points = greyPolyLine.getPoints();
                                      int percentValue = (int) valueAnimator.getAnimatedValue();
                                      int size = points.size();
                                      int newPoints = (int) (size * (percentValue / 100.0f));
                                      List<LatLng> p = points.subList(0, newPoints);
                                      blackPolyline.setPoints(p);
                                  }
                              });
                              polylineAnimator.start();
                              Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_car);

                              int desiredWidth = 60;
                              int desiredHeight = 60;

                              Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, desiredWidth, desiredHeight, false);

                              BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(scaledBitmap);

                              marker = mMap.addMarker(new MarkerOptions().position(thrissur)
                                      .flat(true)
                                      .icon(icon));
                              handler = new Handler();
                              index = -1;
                              next = 1;
                              handler.postDelayed(new Runnable() {
                                  @Override
                                  public void run() {
                                      if (index < polylineList.size() - 1) {
                                          index++;
                                          next = index + 1;
                                      }
                                      if (index < polylineList.size() - 1) {
                                          startPosition = polylineList.get(index);
                                          endPosition = polylineList.get(next);
                                      }

                                      ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                                      valueAnimator.setDuration(3000);
                                      valueAnimator.setInterpolator(new LinearInterpolator());
                                      valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                          @Override
                                          public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                              v = valueAnimator.getAnimatedFraction();
                                              lng = v * endPosition.longitude + (1 - v)
                                                      * startPosition.longitude;
                                              lat = v * endPosition.latitude + (1 - v)
                                                      * startPosition.latitude;
                                              LatLng newPos = new LatLng(lat, lng);
                                         //     CurrentJourneyEvent currentJourneyEvent = new CurrentJourneyEvent();
                                           //   currentJourneyEvent.setCurrentLatLng(newPos);
                                             // JourneyEventBus.getInstance().setOnJourneyUpdate(currentJourneyEvent);
                                              marker.setPosition(newPos);
                                              marker.setAnchor(0.5f, 0.5f);
                                              marker.setRotation(getBearing(startPosition, newPos));
                                              mMap.moveCamera(CameraUpdateFactory.newCameraPosition
                                                      (new CameraPosition.Builder().target(newPos)
                                                              .zoom(15.5f).build()));
                                          }
                                      });
                                      valueAnimator.start();
                                      if (index != polylineList.size() - 1) {
                                          handler.postDelayed(this, 3000);
                                      }
                                  }
                              }, 3000);



                          }
                          catch (Exception e)
                          {
                              e.printStackTrace();
                          }
                      }

                      @Override
                      public void onFailure(Call<String> call, Throwable t) {
                          Toast.makeText(MapActivity.this,""+t.getMessage(),Toast.LENGTH_SHORT).show();
                      }
                  });
      }
      catch (Exception e)
      {
          throw new RuntimeException(e);
      }

     */

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

        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_car);

        int desiredWidth = 60;
        int desiredHeight = 60;

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, desiredWidth, desiredHeight, false);

        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(scaledBitmap);


        marker = mMap.addMarker(new MarkerOptions().position(latlngPoints.get(0))
                .flat(true)
                .icon(icon));

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

                showRoute(firstLatLng,copoints);
                //  getDistance(firstLatLng, copoints);

             //   String.valueOf(SphericalUtil.computeDistanceBetween(firstLatLng, copoints)); // Return distance between in Meters
                //  Log.e("distOfTwoPoints", String.valueOf(SphericalUtil.computeDistanceBetween(firstLatLng, copoints)));

             //   Polyline polyline2 = mMap.addPolyline(new PolylineOptions()
                //        .clickable(true)
               //         .add(firstLatLng,copoints));
              //  polyline2.setTag("A");

              //  stylePolyline(polyline2);
              //  mMap.setOnPolylineClickListener(this);

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

                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                        .target(mMap.getCameraPosition().target)
                        .zoom(17)
                        .bearing(30)
                        .tilt(45)
                        .build())
                );



            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("loctionaddress", "Unnamed Address!");
        }
        return returnAddress;
    }

    private void showRoute(LatLng firstLatLng, LatLng copoints)
    {
        String requestURL = null;
        try
        {
            requestURL = "https://maps.googleapis.com/maps/api/directions/json?"+"mode=driving&"
                    +"transit_routing_preference=less_driving&"
                    +"origin="+firstLatLng.latitude+","+firstLatLng.longitude+"&"+
                    "destination="+copoints.latitude+","+copoints.longitude+"&"+
                    "key="+getResources().getString(R.string.google_map_api_key);
            Log.e("URL", requestURL);
            mService.getDataFromGoogleApi(requestURL)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                JSONArray jsonArray = jsonObject.getJSONArray("routes");
                                for (int i = 0; i < jsonArray.length(); i++)
                                {
                                    JSONObject route = jsonArray.getJSONObject(i);
                                    JSONObject poly = route.getJSONObject("overview_polyline");
                                    String polyline = poly.getString("points");
                                    polylineList = decodePoly(polyline);
                                }

                                // Adjusting Bounds
                                LatLngBounds.Builder builder = new LatLngBounds.Builder();

                                for (LatLng latLng : polylineList) {
                                    builder.include(latLng);
                                }
                                LatLngBounds bounds = builder.build();
                                CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2);
                                mMap.animateCamera(mCameraUpdate);

                                polylineOptions = new PolylineOptions();
                                polylineOptions.color(Color.GRAY);
                                polylineOptions.width(5);
                                polylineOptions.startCap(new SquareCap());
                                polylineOptions.endCap(new SquareCap());
                                polylineOptions.jointType(ROUND);
                                polylineOptions.addAll(polylineList);
                                greyPolyLine = mMap.addPolyline(polylineOptions);

                                blackPolylineOptions = new PolylineOptions();
                                blackPolylineOptions.width(5);
                                blackPolylineOptions.color(Color.BLACK);
                                blackPolylineOptions.startCap(new SquareCap());
                                blackPolylineOptions.endCap(new SquareCap());
                                blackPolylineOptions.jointType(ROUND);
                                blackPolylineOptions.addAll(polylineList);
                                blackPolyline = mMap.addPolyline(blackPolylineOptions);

                                mMap.addMarker(new MarkerOptions()
                                        .position(polylineList.get(polylineList.size() - 1)));

                                ValueAnimator polylineAnimator = ValueAnimator.ofInt(0, 100);
                                polylineAnimator.setDuration(2000);
                                polylineAnimator.setInterpolator(new LinearInterpolator());
                                polylineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                        List<LatLng> points = greyPolyLine.getPoints();
                                        int percentValue = (int) valueAnimator.getAnimatedValue();
                                        int size = points.size();
                                        int newPoints = (int) (size * (percentValue / 100.0f));
                                        List<LatLng> p = points.subList(0, newPoints);
                                        blackPolyline.setPoints(p);
                                    }
                                });
                                polylineAnimator.start();


                                handler = new Handler();
                                index = -1;
                                next = 1;
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (index < polylineList.size() - 1) {
                                            index++;
                                            next = index + 1;
                                        }
                                        if (index < polylineList.size() - 1) {
                                            startPosition = polylineList.get(index);
                                            endPosition = polylineList.get(next);
                                        }

                                        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                                        valueAnimator.setDuration(3000);
                                        valueAnimator.setInterpolator(new LinearInterpolator());
                                        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                            @Override
                                            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                                                v = valueAnimator.getAnimatedFraction();
                                                lng = v * endPosition.longitude + (1 - v)
                                                        * startPosition.longitude;
                                                lat = v * endPosition.latitude + (1 - v)
                                                        * startPosition.latitude;
                                                LatLng newPos = new LatLng(lat, lng);
                                                //     CurrentJourneyEvent currentJourneyEvent = new CurrentJourneyEvent();
                                                //   currentJourneyEvent.setCurrentLatLng(newPos);
                                                // JourneyEventBus.getInstance().setOnJourneyUpdate(currentJourneyEvent);
                                                marker.setPosition(newPos);
                                                marker.setAnchor(0.5f, 0.5f);
                                                marker.setRotation(getBearing(startPosition, newPos));
                                                mMap.moveCamera(CameraUpdateFactory.newCameraPosition
                                                        (new CameraPosition.Builder().target(newPos)
                                                                .zoom(15.5f).build()));
                                            }
                                        });
                                        valueAnimator.start();
                                        if (index != polylineList.size() - 1) {
                                            handler.postDelayed(this, 3000);
                                        }
                                    }
                                }, 3000);



                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(MapActivity.this,""+t.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private void drawPolyLineAndAnimateCar() {
        //Adjusting bounds
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : latlngPoints) {
            builder.include(latLng);
        }
        LatLngBounds bounds = builder.build();
        CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2);
        mMap.animateCamera(mCameraUpdate);

        polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.GRAY);
        polylineOptions.width(5);
        polylineOptions.startCap(new SquareCap());
        polylineOptions.endCap(new SquareCap());
        polylineOptions.jointType(ROUND);
        polylineOptions.addAll(latlngPoints);
        greyPolyLine = mMap.addPolyline(polylineOptions);

        blackPolylineOptions = new PolylineOptions();
        blackPolylineOptions.width(5);
        blackPolylineOptions.color(Color.BLACK);
        blackPolylineOptions.startCap(new SquareCap());
        blackPolylineOptions.endCap(new SquareCap());
        blackPolylineOptions.jointType(ROUND);
        blackPolyline = mMap.addPolyline(blackPolylineOptions);

        mMap.addMarker(new MarkerOptions()
                .position(latlngPoints.get(latlngPoints.size() - 1)));

        ValueAnimator polylineAnimator = ValueAnimator.ofInt(0, 100);
        polylineAnimator.setDuration(2000);
        polylineAnimator.setInterpolator(new LinearInterpolator());
        polylineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                List<LatLng> points = greyPolyLine.getPoints();
                int percentValue = (int) valueAnimator.getAnimatedValue();
                int size = points.size();
                int newPoints = (int) (size * (percentValue / 100.0f));
                List<LatLng> p = points.subList(0, newPoints);
                blackPolyline.setPoints(p);
            }
        });
        polylineAnimator.start();
        marker = mMap.addMarker(new MarkerOptions().position(sydney)
                .flat(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car)));
        handler = new Handler();
        index = -1;
        next = 1;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (index < latlngPoints.size() - 1) {
                    index++;
                    next = index + 1;
                }
                if (index < latlngPoints.size() - 1) {
                    startPosition = latlngPoints.get(index);
                    endPosition = latlngPoints.get(next);
                }
                if (index == 0) {
                    BeginJourneyEvent beginJourneyEvent = new BeginJourneyEvent();
                    beginJourneyEvent.setBeginLatLng(startPosition);
                    JourneyEventBus.getInstance().setOnJourneyBegin(beginJourneyEvent);
                }
                if (index == latlngPoints.size() - 1) {
                    EndJourneyEvent endJourneyEvent = new EndJourneyEvent();
                    endJourneyEvent.setEndJourneyLatLng(new LatLng(latlngPoints.get(index).latitude,
                            latlngPoints.get(index).longitude));
                    JourneyEventBus.getInstance().setOnJourneyEnd(endJourneyEvent);
                }
                ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                valueAnimator.setDuration(3000);
                valueAnimator.setInterpolator(new LinearInterpolator());
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        v = valueAnimator.getAnimatedFraction();
                        lng = v * endPosition.longitude + (1 - v)
                                * startPosition.longitude;
                        lat = v * endPosition.latitude + (1 - v)
                                * startPosition.latitude;
                        LatLng newPos = new LatLng(lat, lng);
                        CurrentJourneyEvent currentJourneyEvent = new CurrentJourneyEvent();
                        currentJourneyEvent.setCurrentLatLng(newPos);
                        JourneyEventBus.getInstance().setOnJourneyUpdate(currentJourneyEvent);
                        marker.setPosition(newPos);
                        marker.setAnchor(0.5f, 0.5f);
                        marker.setRotation(getBearing(startPosition, newPos));
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition
                                (new CameraPosition.Builder().target(newPos)
                                        .zoom(15.5f).build()));
                    }
                });
                valueAnimator.start();
                if (index != latlngPoints.size() - 1) {
                    handler.postDelayed(this, 3000);
                }
            }
        }, 3000);
    }

    private List<LatLng> decodePoly(String polyline) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = polyline.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = polyline.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = polyline.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
  /*  private ArrayList<LatLng> decodePoly(String encoded) {
        ArrayList<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

   */

    private float getBearing(LatLng begin, LatLng end) {
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;
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


    private class getLocationOfEmployee extends AsyncTask<String, String, String>
    {
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
        List<LatLng> latlngPoints;
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

                if (resultString == null  || resultString.isEmpty())
                {
                    return "nullException";
                }

              /*  Gson gson = new Gson();
                dailyWiseGPSDataJson = gson.fromJson(resultString, DailyWiseGPSDataJson.class);

                if (dailyWiseGPSDataJson.getData().getDailyWiseGPSDataResponseStatus().isEmpty() || dailyWiseGPSDataJson.getData().getDailyWiseGPSDataResponseStatus().size() == 0 )
                {
                    return "nullException";
                }

               */

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
               // dailyWiseGPSDataResponsestatusList = dailyWiseGPSDataJson.getData().getDailyWiseGPSDataResponseStatus();

               // parseCoordinatesFromJson(dailyWiseGPSDataResponsestatusList);
              /*  DailyWiseGPSDataResponsestatus dailyWiseGPSDataResponsestatus = new DailyWiseGPSDataResponsestatus();

                for (int i =0; i < dailyWiseGPSDataResponsestatusList.size(); i++){
                    dailyWiseGPSDataResponsestatus = dailyWiseGPSDataResponsestatusList.get(i);
                    latitude = Double.valueOf(dailyWiseGPSDataResponsestatus.getLatitude());
                    longitude = Double.valueOf(dailyWiseGPSDataResponsestatus.getLongitude());

                    getCompleteAddressString(latitude, longitude);

                }

               */

                latlngPoints = parseCoordinatesFromJson(resultString);

                if(!latlngPoints.isEmpty()){
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlngPoints.get(0), 12));
                    drawRoutes(latlngPoints);
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

    private List<LatLng> parseCoordinatesFromJson(String resultString)
    {
        try {
            JSONObject json = new JSONObject(resultString);
            JSONObject data = json.getJSONObject("data");
            JSONArray responseStatus = data.getJSONArray("getDailyWiseGPSDataResponseStatus");

            for (int i = 0; i < responseStatus.length(); i++) {
                JSONObject location = responseStatus.getJSONObject(i);
                double latitude = location.getDouble("latitude");
                double longitude = location.getDouble("longitude");
                latlngPoints.add(new LatLng(latitude, longitude));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.e("Log", "latlngPoints" + latlngPoints);

        return latlngPoints;
    }

    private void drawRoutes(List<LatLng> latlngPoints)
    {
        if (latlngPoints == null || latlngPoints.size() < 2)
        {
            Log.e("RouteDrawer", "Invalid coordinates or not enough points.");
            return;
        }

        for (int i = 0; i < latlngPoints.size() - 1; i++)
        {
            LatLng origin = latlngPoints.get(i);
            LatLng destination = latlngPoints.get(i + 1);

            endPosition = new LatLng(lat, lng);
            startPosition = new LatLng(lat, lng);


            String requestURL = null;
            try
            {
                requestURL = "https://maps.googleapis.com/maps/api/directions/json?"+"mode=driving&"
                        +"transit_routing_preference=less_driving&"
                        +"origin="+origin.latitude+","+origin.longitude+"&"+
                        "destination="+destination.latitude+","+destination.longitude+"&"+
                        "key="+getResources().getString(R.string.google_map_api_key);
                Log.e("URL", requestURL);
                mService.getDataFromGoogleApi(requestURL)
                        .enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response.body().toString());
                                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                                    for (int i = 0; i < jsonArray.length(); i++)
                                    {
                                        JSONObject route = jsonArray.getJSONObject(i);
                                        JSONObject poly = route.getJSONObject("overview_polyline");
                                        String polyline = poly.getString("points");
                                        polylineList = decodePoly(polyline);
                                    }

                                    // Adjusting Bounds
                                    LatLngBounds.Builder builder = new LatLngBounds.Builder();

                                    for (LatLng latLng : polylineList) {
                                        builder.include(latLng);
                                    }
                                    LatLngBounds bounds = builder.build();
                                    CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2);
                                    mMap.animateCamera(mCameraUpdate);

                                    polylineOptions = new PolylineOptions();
                                    polylineOptions.color(Color.GRAY);
                                    polylineOptions.width(5);
                                    polylineOptions.startCap(new SquareCap());
                                    polylineOptions.endCap(new SquareCap());
                                    polylineOptions.jointType(ROUND);
                                    polylineOptions.addAll(polylineList);
                                    greyPolyLine = mMap.addPolyline(polylineOptions);

                                    blackPolylineOptions = new PolylineOptions();
                                    blackPolylineOptions.width(5);
                                    blackPolylineOptions.color(Color.BLACK);
                                    blackPolylineOptions.startCap(new SquareCap());
                                    blackPolylineOptions.endCap(new SquareCap());
                                    blackPolylineOptions.jointType(ROUND);
                                    blackPolylineOptions.addAll(polylineList);
                                    blackPolyline = mMap.addPolyline(blackPolylineOptions);

                                    mMap.addMarker(new MarkerOptions()
                                            .position(polylineList.get(polylineList.size() - 1)));

                                    ValueAnimator polylineAnimator = ValueAnimator.ofInt(0, 100);
                                    polylineAnimator.setDuration(2000);
                                    polylineAnimator.setInterpolator(new LinearInterpolator());
                                    polylineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                        @Override
                                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                            List<LatLng> points = greyPolyLine.getPoints();
                                            int percentValue = (int) valueAnimator.getAnimatedValue();
                                            int size = points.size();
                                            int newPoints = (int) (size * (percentValue / 100.0f));
                                            List<LatLng> p = points.subList(0, newPoints);
                                            blackPolyline.setPoints(p);
                                        }
                                    });
                                    polylineAnimator.start();
                                    Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_car);

                                    int desiredWidth = 60;
                                    int desiredHeight = 60;

                                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, desiredWidth, desiredHeight, false);

                                    BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(scaledBitmap);

                                    marker = mMap.addMarker(new MarkerOptions().position(origin)
                                            .flat(true)
                                            .icon(icon));
                                    handler = new Handler();
                                    index = -1;
                                    next = 1;
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (index < polylineList.size() - 1) {
                                                index++;
                                                next = index + 1;
                                            }
                                            if (index < polylineList.size() - 1) {
                                                startPosition = polylineList.get(index);
                                                endPosition = polylineList.get(next);
                                            }

                                            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                                            valueAnimator.setDuration(3000);
                                            valueAnimator.setInterpolator(new LinearInterpolator());
                                            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                                @Override
                                                public void onAnimationUpdate(ValueAnimator valueAnimator)
                                                {
                                                    v = valueAnimator.getAnimatedFraction();
                                                   // LatLng animatedPosition = (LatLng) valueAnimator.getAnimatedValue();
                                                  //  Log.e("Log", "animatedPosition" + animatedPosition);
                                                    if (v != 0.0)
                                                    {

                                                        lng = v * endPosition.longitude + (1 - v)
                                                                * startPosition.longitude;
                                                        lat = v * endPosition.latitude + (1 - v)
                                                                * startPosition.latitude;
                                                        LatLng newPos = new LatLng(lat, lng);
                                                        //     CurrentJourneyEvent currentJourneyEvent = new CurrentJourneyEvent();
                                                        //   currentJourneyEvent.setCurrentLatLng(newPos);
                                                        // JourneyEventBus.getInstance().setOnJourneyUpdate(currentJourneyEvent);
                                                        marker.setPosition(newPos);
                                                        marker.setAnchor(0.5f, 0.5f);
                                                        marker.setRotation(getBearing(startPosition, newPos));
                                                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition
                                                                (new CameraPosition.Builder().target(newPos)
                                                                        .zoom(15.5f).build()));
                                                    }
                                                    else
                                                    {
                                                        Log.e("MapActivityAnimationError", "Error: animatedPosition is null during animation update.");
                                                    }
                                                }
                                            });
                                            valueAnimator.start();
                                            if (index != polylineList.size() - 1) {
                                                handler.postDelayed(this, 3000);
                                            }
                                        }
                                    }, 3000);



                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(Call<String> call, Throwable t) {
                                Toast.makeText(MapActivity.this,""+t.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }


    private void drawPolyline(Result result) {
        List<LatLng> points = new ArrayList<>();
        List<com.happy.tracku.models.events.Route> routes = result.getRoutes();
        if (routes != null && !routes.isEmpty()) {
            List<com.happy.tracku.models.events.Leg> legs = routes.get(0).getLegs();
            if (legs != null && !legs.isEmpty()) {
                List<com.happy.tracku.models.events.Step> steps = legs.get(0).getSteps();
                for (com.happy.tracku.models.events.Step step : steps) {
                    List<LatLng> decodedPath = PolyUtil.decode(step.getPolyline().getPoints());
                    points.addAll(decodedPath);
                }
            }
        }
        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(points)
                .width(10)
                .color(0xFF0000FF);

        mMap.addPolyline(polylineOptions);
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
        polyline.setJointType(ROUND);
    }

}