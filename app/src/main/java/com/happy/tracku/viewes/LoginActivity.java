package com.happy.tracku.viewes;

import static com.happy.tracku.utils.Const.URL_LOGIN;
import static com.happy.tracku.utils.Const.URL_MASTER_DATA;
import static com.happy.tracku.utils.Const.USING_IP;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.happy.tracku.R;
import com.happy.tracku.databinding.ActivityLoginBinding;
import com.happy.tracku.db.DbHelper;
import com.happy.tracku.gson.login.Validateloginresponsejson;
import com.happy.tracku.gson.masterdata.MasterDataJson;
import com.happy.tracku.ssl.CustomTrust;
import com.happy.tracku.utils.Const;
import com.happy.tracku.utils.Fns;

import org.json.JSONObject;

import java.io.InterruptedIOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;

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
    DbHelper dbHelper;
    MasterDataJson masterDataJson;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        activityLoginBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = activityLoginBinding.getRoot();
        setContentView(view);

        View rootView = findViewById(android.R.id.content); // Get root view

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            WindowInsetsCompat insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets.toWindowInsets());
            int systemBarsInsetsTop = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int systemBarsInsetsBottom = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

            // Apply padding to your main content view
            v.setPadding(v.getPaddingLeft(), systemBarsInsetsTop, v.getPaddingRight(), systemBarsInsetsBottom);

            return WindowInsetsCompat.CONSUMED;
        });

        Log.e("Log", "versionNo" + Fns.getAppVersionName(this));
        activityLoginBinding.versionNo.setText("Ver"+Fns.getAppVersionName(this));

        shp = getSharedPreferences(Const.Shared_Pref_name, MODE_PRIVATE);
        dbHelper = new DbHelper(this);

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


        new PullMasterData(this).execute();

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
        Validateloginresponsejson loginStatusJson;
        DbHelper dbHelper;

        public LoginTask(LoginActivity mContext, String usernameString, String passwordString)
        {

            this.mContext = mContext;
            this.usernameString = usernameString;
            this.passwordString = passwordString;

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
        protected String doInBackground(String... strings) {


            try {

                url = USING_IP + URL_LOGIN;

                Log.e("Log", "loginURL" +url);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("loginId", usernameString);
                jsonObject.put("password", passwordString);
                jsonObject.put("versionCode", shp.getString(Const.Shp_Version_No, ""));
                jsonObject.put("androidId", shp.getString(Const.Shp_Android_Id, ""));
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

                 loginStatusJson = gsonTwo.fromJson(resultOne, Validateloginresponsejson.class);

                if (loginStatusJson.getData().getValidateLoginResponseStatus().isEmpty() || loginStatusJson.getData().getValidateLoginResponseStatus().size() == 0 || loginStatusJson.getData().getValidateLoginResponseStatus() == null || loginStatusJson.getData().getValidateLoginResponseEmployeeData().isEmpty() || loginStatusJson.getData().getValidateLoginResponseEmployeeData().size() == 0 || loginStatusJson.getData().getValidateLoginResponseEmployeeData() == null || loginStatusJson.getData().getValidateLoginResponseVehicle().isEmpty() || loginStatusJson.getData().getValidateLoginResponseVehicle().size() == 0 || loginStatusJson.getData().getValidateLoginResponseVehicle() == null)
                {
                    return "failure";
                }
                else if (loginStatusJson.getData().getValidateLoginResponseStatus().get(0).getStatus() != 1)
                {
                    return "failure";
                }

                double versionAtServer = Double.parseDouble(loginStatusJson.getData().getValidateLoginResponseStatus().get(0).getVersion());
                double currentVersion = Double.parseDouble(Fns.getAppVersionName(mContext));


                if(versionAtServer > currentVersion)
                {

                    SharedPreferences.Editor edt = shp.edit();
                    edt.putString(Const.Shp_NEW_APP_VERSION,loginStatusJson.getData().getValidateLoginResponseStatus().get(0).getVersion());
                    edt.apply();
                    return "update";

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
                dbHelper.deleteLoginVehicleData();
                dbHelper.deleteLoginEmployeeData();
                dbHelper.insertValidateMasterData(loginStatusJson);

                Log.e("Log", "username" + usernameString);
                SharedPreferences.Editor edt = shp.edit();
                edt.putInt(Const.Shp_Id_Employee,loginStatusJson.getData().getValidateLoginResponseStatus().get(0).getIdEmployee());
                edt.putString(Const.Shp_Employee_Code, usernameString);
                edt.putString(Const.Shp_Employee_Name, loginStatusJson.getData().getValidateLoginResponseStatus().get(0).getName());
               // edt.putString(Const.Shp_Token, loginStatusJson.getData().get);
                edt.putInt(Const.Shp_Is_Admin, loginStatusJson.getData().getValidateLoginResponseStatus().get(0).getIsAdmin());
                edt.putBoolean(Const.Shp_Is_LoggedIn, true);
                edt.putString(Const.Shp_UserName, usernameString);
                edt.putString(Const.Shp_PassWord, passwordString);
                edt.putInt(Const.Shp_IsLocationCheckRequired, loginStatusJson.getData().getValidateLoginResponseStatus().get(0).getIsLocationCheckRequired());
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
                    String message = loginStatusJson.getData().getValidateLoginResponseStatus().get(0).getStatusMessage();
                    Fns.neutralAlert("Alert",message,mContext);
                }


            }
            else if(s.equals("update"))
            {

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                builder.setMessage("New Version of App Released. You have to update to continue");

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {

                    }
                });

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //Goto page saved successfully
                        //TODO call webservice

                        dialogInterface.dismiss();
                        //mContext.downloadNewApk();

                        Fns.openInPlayStore(mContext);


                    }
                });

                AlertDialog alertDialog = builder.create();

                alertDialog.show();



            }

        }
    }

    private static class PullMasterData extends AsyncTask<String, String, String>
    {

        WeakReference<LoginActivity> context;
        ProgressDialog pd;
        OkHttpClient okHttpClient;
        String url, resultString;
        Request request;
        Response response;
        SharedPreferences shp;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        MasterDataJson masterDataJson;
        DbHelper dbHelper;

        public PullMasterData(LoginActivity context)
        {
            this.context = new WeakReference<>(context);

            CustomTrust customTrust = new CustomTrust(context);
            OkHttpClient client = customTrust.getClient();
            okHttpClient = client;

            shp = context.getSharedPreferences(Const.Shared_Pref_name, MODE_PRIVATE);
            dbHelper = new DbHelper(context);

        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pd = new ProgressDialog(context.get());
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setMessage("Loading");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... strings)
        {
            try
            {

                JSONObject masterDataDetailsObj = new JSONObject();
                masterDataDetailsObj.put("createdBy", shp.getString(Const.Shp_Employee_Code, ""));

                url = USING_IP + URL_MASTER_DATA;
                Log.e("Log", "masterurl" + url);

                RequestBody body = RequestBody.create(masterDataDetailsObj.toString(), JSON);
                Log.e("Log", "masterDataDetailsObj" + masterDataDetailsObj);

                request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                Log.e("Log", "request" + request);

                response = okHttpClient.newCall(request).execute();
                Log.e("Log", "response" + response);

                if (!response.isSuccessful())
                {
                    return "failure";
                }

                resultString = response.body().string();
                Log.e("Log", "MasterResultString" + resultString);

                Gson gson = new Gson();
                masterDataJson = gson.fromJson(resultString, MasterDataJson.class);
                Log.e("Log", "masterDataJson" + masterDataJson);

                if (masterDataJson.getData().getVendorMaster() == null || masterDataJson.getData().getVendorMaster().size() == 0 || masterDataJson.getData().getVendorMaster().isEmpty() || masterDataJson.getData().getItemMaster().isEmpty() || masterDataJson.getData().getItemMaster() == null || masterDataJson.getData().getItemMaster().size() == 0)
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
        protected void onPostExecute(String s)
        {
            super.onPostExecute(s);

            if (s.equals("success"))
            {
                context.get().setMasterData(masterDataJson);

            }
            else if (s.equals("failure"))
            {
                Toast.makeText(context.get(), "Pull Failed", Toast.LENGTH_SHORT).show();
            } else if (s.equals("nullException"))
            {
                Toast.makeText(context.get(), "Null Exception From Server", Toast.LENGTH_SHORT).show();
            }

            pd.dismiss();
        }
    }

    private void setMasterData(MasterDataJson masterDataJson)
    {
        this.masterDataJson = masterDataJson;

        dbHelper.deleteVendorMaster();
        dbHelper.deleteItemMaster();
        dbHelper.deleteUnitMaster();
        dbHelper.insertMasterData(masterDataJson);

    }
}


/*
  if (versionString > Double.parseDouble(Fns.getAppVersionName(context))) {

                    SharedPreferences.Editor edt = shp.edit();
                    edt.putString(Const.Shp_NEW_APP_PATHNAME, path);
                    edt.apply();

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);

                    builder.setMessage("New Version of App Released. You have to update to continue");

                    if (isCompulsory == 1)
                    {

                        builder.setCancelable(false);

                    }

                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            dialogInterface.dismiss();


                        }
                    });

                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            dialogInterface.dismiss();
                            //downloadNewApk();

                            Fns.openInPlayStore(context);


                        }
                    });

                    AlertDialog alertDialog = builder.create();

                    alertDialog.show();


                }

                MainMenu
 */