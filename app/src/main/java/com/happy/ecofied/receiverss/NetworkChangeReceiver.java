package com.happy.ecofied.receiverss;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.util.Log;

import androidx.annotation.NonNull;


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
