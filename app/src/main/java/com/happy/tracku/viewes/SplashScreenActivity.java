package com.happy.tracku.viewes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

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

        View rootView = findViewById(android.R.id.content); // Get root view

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            WindowInsetsCompat insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets.toWindowInsets());
            int systemBarsInsetsTop = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int systemBarsInsetsBottom = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

            // Apply padding to your main content view
            v.setPadding(v.getPaddingLeft(), systemBarsInsetsTop, v.getPaddingRight(), systemBarsInsetsBottom);

            return WindowInsetsCompat.CONSUMED;
        });

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