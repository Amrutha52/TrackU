package com.happy.ecofied.viewes;

import static com.happy.ecofied.utils.Const.URL_MANUAL_PUNCH;
import static com.happy.ecofied.utils.Const.USING_IP;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.happy.ecofied.R;
import com.happy.ecofied.db.DbHelper;
import com.happy.ecofied.gson.photopunchingjson.Photopunchingjson;
import com.happy.ecofied.ssl.CustomTrust;
import com.happy.ecofied.utils.Const;
import com.happy.ecofied.utils.Fns;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PhotoPunchActivity extends AppCompatActivity
{

    // Define the pic id
    private static final int pic_id = 123;
    // Define the button and imageview type variable
    Button camera_open_id;
    ImageView click_image_id;
    Bitmap photo, resizedBitmapBig;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_punch);

        View rootView = findViewById(android.R.id.content); // Get root view

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            WindowInsetsCompat insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets.toWindowInsets());
            int systemBarsInsetsTop = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int systemBarsInsetsBottom = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

            // Apply padding to your main content view
            v.setPadding(v.getPaddingLeft(), systemBarsInsetsTop, v.getPaddingRight(), systemBarsInsetsBottom);

            return WindowInsetsCompat.CONSUMED;
        });
        // By ID we can get each component which id is assigned in XML file get Buttons and imageview.
        camera_open_id = findViewById(R.id.camera_button);
        click_image_id = findViewById(R.id.click_image);

        // Camera_open button is for open the camera and add the setOnClickListener in this button
//        camera_open_id.setOnClickListener(v -> {
//            // Create the camera_intent ACTION_IMAGE_CAPTURE it will open the camera for capture the image
//            Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            // Start the activity with camera_intent, and request pic id
//            startActivityForResult(camera_intent, pic_id);
//        });

    }

    // This method will help to retrieve the image
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Match the request 'pic id with requestCode
        if (requestCode == pic_id) {
            // BitMap is data structure of image file which store the image in memory
            photo = (Bitmap) data.getExtras().get("data");
            // Set the image in imageview for display
            click_image_id.setImageBitmap(photo);
        }
    }

    public void listeners(View view)
    {
        switch (view.getId())
        {
            case R.id.camera_button:
            {

                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Start the activity with camera_intent, and request pic id
                startActivityForResult(camera_intent, pic_id);
            }
            break;

            case R.id.submitButon:
            {
                /**
                 * Today's Date
                 */
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String currentDateAndTime = sdf.format(calendar.getTime());
                Log.e("Log", "currentDateAndTime" + currentDateAndTime);

                /**
                 * Bitmap to base64
                 */

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] bytearray = stream.toByteArray();

                InputStream myInputStream = new ByteArrayInputStream(bytearray);
                Bitmap bitmap = BitmapFactory.decodeStream(myInputStream);
                //Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 300, 200, true);
                //Drawable image = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(bytearray, 0, bytearray.length));


                //previewImageView.setImageDrawable(image);
                resizedBitmapBig = Bitmap.createScaledBitmap(bitmap, 480, 800, true);
                if(bytearray.length<=1024)
                {

                    resizedBitmapBig = bitmap;

                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                resizedBitmapBig.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream .toByteArray();

                String base64 = Base64.encodeToString(byteArray, Base64.DEFAULT);

                new PushPhotoPunchingDetails(this, currentDateAndTime, base64).execute();

            }
            break;
        }
    }

    private static class PushPhotoPunchingDetails extends AsyncTask<String, String, String>
    {
        OkHttpClient okHttpClient;
        String url;
        Request request;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        PhotoPunchActivity mContext;
        ProgressDialog pd;
        SharedPreferences shp;
        String failureMsg, resultString;
        Photopunchingjson photopunchingjson;
        DbHelper dbHelper;
        int status;

        String punchingDateTime, punchingImage;
        public PushPhotoPunchingDetails(PhotoPunchActivity mContext, String currentDateAndTime, String base64)
        {
            this.mContext = mContext;
            this.punchingDateTime = currentDateAndTime;
            this.punchingImage = base64;

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
        protected void onPreExecute()
        {
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

                JSONObject photoPunchObj = new JSONObject();
                photoPunchObj.put("createdBy", shp.getString(Const.Shp_Employee_Code, ""));
                photoPunchObj.put("employeeCode", shp.getString(Const.Shp_Employee_Code, ""));
                photoPunchObj.put("date", punchingDateTime);
                photoPunchObj.put("employeeImage", punchingImage);
                //photoPunchObj.put("versionCode", shp.getString(Const.Shp_Version_No, ""));

                url = USING_IP + URL_MANUAL_PUNCH;
                Log.e("Log", "photoPunchURL" + url);
                Log.e("Log", "photoPunchJsonObject" + photoPunchObj);

                RequestBody body = RequestBody.create(photoPunchObj.toString(), JSON);

                request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                Log.e("Log", "request" + request);

                Response response = okHttpClient.newCall(request).execute();
                Log.e("Log", "response" + response);

                if (!response.isSuccessful())
                {
                    return "failure";
                }

                resultString = response.body().string();
                Log.e("Log", "resultString" + resultString);

                Gson gson = new Gson();
                photopunchingjson = gson.fromJson(resultString, Photopunchingjson.class);

                if (photopunchingjson.getData().getPhotoPunchStatus().isEmpty() || photopunchingjson.getData().getPhotoPunchStatus().size() == 0 || photopunchingjson.getData().getPhotoPunchStatus() == null)
                {
                    return "nullException";
                }
                else
                {
                    status = photopunchingjson.getData().getPhotoPunchStatus().get(0).getStatus();
                    if (status != 1)
                    {
                        return "failure";
                    }
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
            pd.dismiss();

            if (s.equals("success"))
            {
              //  Fns.neutralAlert("Alert", photopunchingjson.getData().getPhotoPunchStatus().get(0).getStatusMsg(), mContext);

//                if (photopunchingjson.getData().getPhotoPunchStatus().get(0).getStatus() == 1)
//                {
//
//                    mContext.finish();
//                }

                AlertDialog.Builder adb = new AlertDialog.Builder(mContext);

                TextView titletxtview = new TextView(mContext);
                titletxtview.setText("Alert");
                titletxtview.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                titletxtview.setPadding(10, 10, 10, 10);
                titletxtview.setGravity(Gravity.CENTER);
                titletxtview.setTextColor(Color.WHITE);
                titletxtview.setTextSize(20);

                adb.setCustomTitle(titletxtview);

                TextView messagetxtview = new TextView(mContext);
                messagetxtview.setText(photopunchingjson.getData().getPhotoPunchStatus().get(0).getStatusMessage());
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
                        //dialog.cancel();
                        mContext.finish();

                    }
                });

                AlertDialog ad = adb.create();
                ad.show();

            }
            else if (s.equals("failure"))
            {
                Fns.neutralAlert("Alert", photopunchingjson.getData().getPhotoPunchStatus().get(0).getStatusMessage(), mContext);
                // Toast.makeText(context.get(), "Updation Failed", Toast.LENGTH_SHORT).show();
            }
            else if (s.equals("nullException"))
            {
                Toast.makeText(mContext, "Null Exception From Server", Toast.LENGTH_SHORT).show();
            }
        }

    }
}