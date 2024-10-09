package com.happy.tracku.viewes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.happy.tracku.R;
import com.happy.tracku.databinding.ActivityMainMenuBinding;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainMenuActivity extends AppCompatActivity {

    private ActivityMainMenuBinding binding;
    LocationManager locationManager;
    CustomLocationCallback locationCallback;
    Double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainMenuBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        FusedLocationProviderClient client = LocationServices
                .getFusedLocationProviderClient(getApplicationContext());
        LocationRequest request = LocationRequest.create();
        request.setInterval(15 * 1000); //Every 15 seconds
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationCallback = new CustomLocationCallback();
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
        client.requestLocationUpdates(request, locationCallback, null);

        Location location = locationCallback==null?null:locationCallback.getLocation();

        latitude = location==null?0.0:location.getLatitude();
        Log.e("Log", "location Latitude" + latitude);

        longitude = location==null?0.0:location.getLongitude();
        Log.e("Log", "location longitude" + longitude);

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

        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
           Log.e("Log", "RuntimeException" + e);
        }
        Log.e("Log", latitude + "" + longitude);
        //   List<Address> addresses = geocoder.getFromLocation(10.530, 76.214, 1);
        String returnedAddress = "Not Able To Get Address";
        if (addresses != null && addresses.size() != 0) {
            returnedAddress = addresses.get(0).getAddressLine(0);
            Log.e("address", returnedAddress);

        }
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

            Log.e("Log", "LocationChanged: " + location);

        }

        public Location getLocation() {

            return location;

        }

    }
}