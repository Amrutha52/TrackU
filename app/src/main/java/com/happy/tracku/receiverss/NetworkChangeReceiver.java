package com.happy.tracku.receiverss;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.happy.tracku.db.DbHelper;
import com.happy.tracku.ssl.CustomTrust;
import com.happy.tracku.utils.Const;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;



public class NetworkChangeReceiver extends BroadcastReceiver {

    static int count = 0;
    private static final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback(){
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            Log.e("Log","internet available");
        }
    };
    private static final NetworkRequest networkRequest = new NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build();

    @Override
    public void onReceive(Context context, Intent intent) {


        Log.e("Log","OnReceive parent Broadcast Receiver"+(count++));

        if(isOnline(context))
        {

            Log.e("Log","Wow Internet came");

            //TODO push to server

          //  new UploadPickedUpEntriesTask(context).execute();


        }


        /*ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.registerNetworkCallback(networkRequest,networkCallback);
        connectivityManager.unregisterNetworkCallback(networkCallback);*/
    }

    public boolean isOnline(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null

        //Log.e("Log","netinfo "+netInfo+" connected ");

        return (netInfo != null && netInfo.isAvailable());
    }


}
