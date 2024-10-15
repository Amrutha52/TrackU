package com.happy.tracku.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.happy.tracku.R;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Fns {
    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void neutralAlert(String title, String message, Context context)
    {

        AlertDialog.Builder adb = new AlertDialog.Builder(context);

        TextView titletxtview = new TextView(context);
        titletxtview.setText(title);
        titletxtview.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        titletxtview.setPadding(10, 10, 10, 10);
        titletxtview.setGravity(Gravity.CENTER);
        titletxtview.setTextColor(Color.WHITE);
        titletxtview.setTextSize(20);

        adb.setCustomTitle(titletxtview);

        TextView messagetxtview = new TextView(context);
        messagetxtview.setText(message);
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
                dialog.cancel();

            }
        });

        adb.setPositiveButton("Share", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                String shareBody = message;
                Log.e("LogFns", "fns message" + shareBody);
                intent.setType("text/plain");
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share");
                intent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                context.startActivity(Intent.createChooser(intent, "Share using"));

            }
        });
        AlertDialog ad = adb.create();
        ad.show();
    }

    public static String getErrorMsgFromException(Exception e) {

        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();

        return exceptionAsString;

    }

    public static String getErrorMessage(Context context, String errorMsg) {


        String dateTime = "Date : " + new SimpleDateFormat("dd-MM-yyy HH:mm:ss").format(Calendar.getInstance().getTime()) + "\n";
        String userId = "UserId : " + context.getSharedPreferences(Const.Shared_Pref_name, Context.MODE_PRIVATE).getString(Const.Shp_Id_Employee, "") + "\n";
        String errorMsgs = "Error Msg : " + errorMsg + "\n";

        return dateTime + userId + errorMsgs;

    }

    public static String convertFormatDate(String expectedDate, String currentFormat, String desiredFormat)
    {
        SimpleDateFormat currentSDF = new SimpleDateFormat(currentFormat, Locale.getDefault());
        Date currentDate = null;
        try {
            currentDate = currentSDF.parse(expectedDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        SimpleDateFormat desiredSDF = new SimpleDateFormat(desiredFormat, Locale.getDefault());
        return desiredSDF.format(currentDate);
    }

}
