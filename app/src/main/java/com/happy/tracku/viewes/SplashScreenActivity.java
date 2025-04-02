package com.happy.tracku.viewes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.happy.tracku.R;
import com.happy.tracku.utils.Const;

public class SplashScreenActivity extends AppCompatActivity
{

    SharedPreferences shp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

        shp = getSharedPreferences(Const.Shared_Pref_name,MODE_PRIVATE);

        boolean hasLoggedIn = shp.getBoolean(Const.Shp_Is_LoggedIn,false);
        Log.e("Log","hasLoggedIn" + hasLoggedIn);


        new Handler().postDelayed(new Runnable() {
            // Using handler with postDelayed called runnable run method

            @Override

            public void run() {

                if(hasLoggedIn)
                {

                    Intent i = new Intent(SplashScreenActivity.this, MainMenuActivity.class);

                    startActivity(i);
                    // close this activity
                    finish();


                }else
                {

                    Intent i = new Intent(SplashScreenActivity.this, LoginActivity.class);

                    startActivity(i);
                    // close this activity
                    finish();


                }



            }

        }, 5000); // wait for 5 seconds
    }
}