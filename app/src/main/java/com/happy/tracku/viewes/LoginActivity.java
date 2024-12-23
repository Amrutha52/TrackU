package com.happy.tracku.viewes;

import static com.happy.tracku.utils.Const.URL_LOGIN;
import static com.happy.tracku.utils.Const.USING_IP;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.happy.tracku.R;
import com.happy.tracku.databinding.ActivityLoginBinding;
import com.happy.tracku.gson.login.LoginStatusJson;
import com.happy.tracku.ssl.CustomTrust;
import com.happy.tracku.utils.Const;
import com.happy.tracku.utils.Fns;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity
{
    private ActivityLoginBinding activityLoginBinding;
    String usernameString, versionNameString;

    SharedPreferences shp;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        activityLoginBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = activityLoginBinding.getRoot();
        setContentView(view);

        Log.e("Log", "versionNo" + Fns.getAppVersionName(this));
        activityLoginBinding.versionNo.setText("Ver"+Fns.getAppVersionName(this));

        shp = getSharedPreferences(Const.Shared_Pref_name, MODE_PRIVATE);

        String androidIdString = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        activityLoginBinding.androidId.setText("Android ID : "+androidIdString);
        Log.e("Log", "androidIdString" + androidIdString);

        /**
         * Android Id Saved into Shared Preferences
         */

        SharedPreferences.Editor edt = shp.edit();
        edt.putString(Const.Shp_Android_Id, androidIdString);
        edt.apply();

//        if (shp.getBoolean(Const.Shp_Is_LoggedIn, true))
//        {
//            startActivity(new Intent(this, MainMenuActivity.class));
//        }

    }

    @SuppressLint("NonConstantResourceId")
    public void listeners(View view) {
        switch (view.getId())
        {
            case R.id.loginButton:
            {
                usernameString = activityLoginBinding.usernameET.getText().toString();
                String passwordString = activityLoginBinding.passwordET.getText().toString();

                if (usernameString.isEmpty() || passwordString.isEmpty()) {

                    Toast.makeText(this, "Please enter username or password", Toast.LENGTH_LONG).show();


                }
                else
                {

                    if(Fns.isInternetAvailable(this))
                    {

                        //Uploading From Secret File
                        //new NetworkChangeReceiver.UploadPickedUpEntriesInSecretFileTask(this).execute();

                        new LoginTask(this, usernameString, passwordString).execute();


                    }else
                    {
                        Fns.neutralAlert("Alert","No Internet Connected",this);
                    }

                }
//                else {
//
//
//                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
//                            != PackageManager.PERMISSION_GRANTED||ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
//                            != PackageManager.PERMISSION_GRANTED||ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                            != PackageManager.PERMISSION_GRANTED) {
//                        // Permission is not granted
//
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//
//                            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_PHONE_STATE,Manifest.permission.ACCESS_COARSE_LOCATION ,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.POST_NOTIFICATIONS},121);
//
//                        }else
//                        {
//                            ActivityCompat.requestPermissions(this,
//                                    new String[] { Manifest.permission.READ_PHONE_STATE,Manifest.permission.ACCESS_COARSE_LOCATION ,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
//                                    121);
//                        }
//
//                        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//
//                            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.POST_NOTIFICATIONS},121);
//
//                        }*/
//
//                        //permissions();
//
//
//                    }
//
//
//                    else
//                    {
//
//                        if(Fns.isInternetAvailable(this))
//                        {
//
//                            //Uploading From Secret File
//                            //new NetworkChangeReceiver.UploadPickedUpEntriesInSecretFileTask(this).execute();
//
//                            new LoginTask(this, usernameString, passwordString).execute();
//
//
//                        }else
//                        {
//                            Fns.neutralAlert("Alert","No Internet Connected",this);
//                        }
//
//                    }
//
//                }
            }
            break;
        }
    }

    private static class LoginTask extends AsyncTask<String, String, String> {

        OkHttpClient okHttpClient;
        String url;
        Request request;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        LoginActivity mContext;

        ProgressDialog pd;
        SharedPreferences shp;
        TelephonyManager telephonyManager;
        String usernameString, passwordString;
        String failureMsg;
        boolean exceptionOccured = false,timeOutExceptionOccured = false;
        String inputAndOutputJson = "";
        LoginStatusJson loginStatusJson;

        public LoginTask(LoginActivity mContext, String username, String password) {

            this.mContext = mContext;

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

            usernameString = username;
            passwordString = password;

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
        protected String doInBackground(String... strings) {


            try {

                url = USING_IP + URL_LOGIN;

                Log.e("Log", "loginURL" +url);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("loginId", usernameString);
                jsonObject.put("password", passwordString);
                jsonObject.put("versionCode", shp.getString(Const.Shp_Version_No, ""));
                jsonObject.put("createdBy", "");


               // JSONObject loginJsonObj = new JSONObject();
               // loginJsonObj.put("logindetails", jsonObject);

               // Log.e("Log", loginJsonObj.toString());

                inputAndOutputJson = jsonObject.toString();
                Log.e("Log", " inputAndOutputJson" + inputAndOutputJson);

                RequestBody bodyOne = RequestBody.create(jsonObject.toString(), JSON);
                request = new Request.Builder()
                        //.header("X-Client-Type", "Android")
                        .url(url)
                        .post(bodyOne)
                        .build();


                Response responseOne = okHttpClient.newCall(request).execute();

                if (!responseOne.isSuccessful()) {

                    Log.e("Log", "failure");
                    failureMsg = "Response unsuccessfull";
                    return "failure";
                }

                pd.setProgress(25);
                String resultOne = responseOne.body().string();

                Log.e("Log", resultOne);

                inputAndOutputJson = inputAndOutputJson + "----------" + resultOne;


                Gson gsonTwo = new Gson();

                //JSONObject resultJsonObj = new JSONObject(result);

                //JSONArray resultArray = resultJsonObj.getJSONArray("Table");

                if (resultOne.equals("{}")) {
                    failureMsg = "empty String result";
                    return "failure";
                }

                if (resultOne.equals("{\"Status\":[{\"Status\":0,\"StatusMsg\":\"Invalid login\"}]}"))
                {
                    failureMsg = "Invalid Login Status";
                    return "failure";

                }

                 loginStatusJson = gsonTwo.fromJson(resultOne, LoginStatusJson.class);

                if (loginStatusJson.getData().getLoginResponseStatus().isEmpty() || loginStatusJson.getData().getLoginResponseStatus().size() == 0)
                {
                    return "failure";
                }
                else if (loginStatusJson.getData().getLoginResponseStatus().get(0).getStatus() != 1)
                {
                    return "failure";
                }




            }
            catch(SocketTimeoutException e)
            {

                failureMsg = Fns.getErrorMsgFromException(e);
                Log.e("Log", "FailureMessage" + failureMsg);
                //throw new RuntimeException(e);
                timeOutExceptionOccured = true;
                //exceptionOccured = true;
                return "failure";


            }catch (InterruptedIOException e)
            {

                failureMsg = Fns.getErrorMsgFromException(e);
                Log.e("Log", "failureMsgInterruptedIOException" + failureMsg);
                //throw new RuntimeException(e);
                timeOutExceptionOccured = true;
                //exceptionOccured = true;
                return "failure";

            }
            catch (Exception e)
            {

                failureMsg = Fns.getErrorMsgFromException(e);
                Log.e("Log", "failureMessageException" + failureMsg);
                //throw new RuntimeException(e);
                exceptionOccured = true;
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

                SharedPreferences.Editor edt = shp.edit();
                edt.putInt(Const.Shp_Id_Employee,loginStatusJson.getData().getLoginResponseStatus().get(0).getIdEmployee());
                edt.putString(Const.Shp_Employee_Code, loginStatusJson.getData().getLoginResponseStatus().get(0).getEmployeeCode());
                edt.putString(Const.Shp_Employee_Name, loginStatusJson.getData().getLoginResponseStatus().get(0).getName());
                edt.putString(Const.Shp_Token, loginStatusJson.getData().getToken());
                edt.putInt(Const.Shp_Is_Admin, loginStatusJson.getData().getLoginResponseStatus().get(0).getIsAdmin());
                edt.putBoolean(Const.Shp_Is_LoggedIn, true);
                edt.apply();

                mContext.startActivity(new Intent(mContext, MainMenuActivity.class));
                mContext.finish();


            }
            else if (s.equals("failure"))
            {

                //Fns.neutralAlert("Failure",failureMsg,mContext);
                //Toast.makeText(mContext, "Login Failed.Please check credentials "+failureMsg, Toast.LENGTH_LONG).show();

                if(timeOutExceptionOccured)
                {
                    Fns.neutralAlert("Alert","Timeout ",mContext);

                }
                else if(exceptionOccured)
                {
                    String errorMsg = Fns.getErrorMessage(mContext,failureMsg);
                    Fns.neutralAlert("Alert","Login Failed "+errorMsg,mContext);
                }
                else
                {
                    String message = loginStatusJson.getData().getLoginResponseStatus().get(0).getStatusMessage();
                    Fns.neutralAlert("Alert",message,mContext);
                }


            }



        }
    }
}