/*package com.happy.tracku.viewes;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.happy.tracku.R;

public class SecondActivity extends AppCompatActivity
{
    Double latitude, longitude;
    CustomLocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {

            // TODO override
            // public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 501);

            return;
        }
        else {
            Log.e("Log", "Location Initialization");
            // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 0, this);
        }

        FusedLocationProviderClient client = LocationServices
                .getFusedLocationProviderClient(getApplicationContext());
        LocationRequest request = LocationRequest.create();
        request.setInterval(15 * 1000); //Every 15 seconds
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationCallback = new CustomLocationCallback();
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
}

 */