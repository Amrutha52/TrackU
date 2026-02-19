package com.happy.tracku.viewes;

import static com.happy.tracku.utils.Const.URL_STOCKOUT_PURCHASE_ORDER_ITEM_LIST;
import static com.happy.tracku.utils.Const.URL_STOCKOUT_SEND_PURCHASE_REQUEST;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.gson.Gson;
import com.happy.tracku.R;
import com.happy.tracku.adapters.StockoutPurchaseOrderItemListAdapter;
import com.happy.tracku.databinding.ActivityStockOutPurchaseOrderItemListBinding;
import com.happy.tracku.db.DbHelper;
import com.happy.tracku.gson.login.ValidateLoginResponseEmployeeDatum;
import com.happy.tracku.gson.login.ValidateLoginResponseVehicle;
import com.happy.tracku.gson.sendstockoutrequest.StockOutSendPurchaseRequestJson;
import com.happy.tracku.gson.stockoutpurchaseorderitemlist.StockOutPurchaseOrderItemListJson;
import com.happy.tracku.ssl.CustomTrust;
import com.happy.tracku.utils.Const;
import com.happy.tracku.utils.Fns;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StockOutPurchaseOrderItemListActivity extends AppCompatActivity
{
    private ActivityStockOutPurchaseOrderItemListBinding binding;
    Intent intent;
    int idPurchaseOrder, companyValue;
    DbHelper dbHelper;

    /**
     *
     *  Camera Section
     *
     */
    // Define the pic id
    //private static final int pic_id = 123;
    // Define the button and imageview type variable
    MaterialButton camera_open_id, gallery_open_id;
    ImageView click_image_id;
    Bitmap photo, resizedBitmapBig;
    // private static final int REQUEST_IMAGE_PICK = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;
    String fileName, base64;
    Uri photoURI;
    File photoFile;
    MaterialAutoCompleteTextView employeeCodeMATV, vehicleModelMATV;
    List<ValidateLoginResponseEmployeeDatum> employeeMasterDetailList;
    List<ValidateLoginResponseVehicle> validateLoginResponseVehicleList;
    int employeeCode, idVehicle;
    int pulledIdSalesDetails;
    StockOutPurchaseOrderItemListJson stockOutPurchaseOrderItemListJson;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityStockOutPurchaseOrderItemListBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        EdgeToEdge.enable(this);

        View rootView = findViewById(android.R.id.content); // Get root view

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            WindowInsetsCompat insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets.toWindowInsets());
            int systemBarsInsetsTop = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int systemBarsInsetsBottom = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

            // Apply padding to your main content view
            v.setPadding(v.getPaddingLeft(), systemBarsInsetsTop, v.getPaddingRight(), systemBarsInsetsBottom);

            return WindowInsetsCompat.CONSUMED;
        });

        getSupportActionBar().setTitle("Stockout Order Item List");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dbHelper = new DbHelper(this);

        camera_open_id = findViewById(R.id.camera_button);
        click_image_id = findViewById(R.id.click_image);
        gallery_open_id = findViewById(R.id.attach_image);

        intent = getIntent();

        idPurchaseOrder = intent.getIntExtra("idPurchaseOrder",0);
        Log.e("Log", "idPurchaseOrderOrderItem" + idPurchaseOrder);

        companyValue = intent.getIntExtra("company",0);
        Log.e("Log", "companyValue" + companyValue);

        employeeCodeMATV = findViewById(R.id.employeecodeMATV);
        vehicleModelMATV = findViewById(R.id.vehicleModelMATV);

        employeeMasterDetailList = dbHelper.getLoginEmployeeMaster();
        Log.e("Log", "employeeMasterDetailList" + employeeMasterDetailList);

        ArrayAdapter<ValidateLoginResponseEmployeeDatum> adpterEmployeeMaster = new ArrayAdapter<ValidateLoginResponseEmployeeDatum>(this, android.R.layout.simple_dropdown_item_1line, employeeMasterDetailList);
        employeeCodeMATV.setAdapter(adpterEmployeeMaster);

        employeeCodeMATV.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View arg0)
            {
                employeeCodeMATV.showDropDown();
            }
        });

        employeeCodeMATV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                employeeCode = employeeMasterDetailList.get(position).getEmployeeCode();
                Log.e("Log", "employeeCodeSP" + employeeCode);

            }
        });

        validateLoginResponseVehicleList = dbHelper.getLoginVehicleMaster();
        Log.e("Log", "validateLoginResponseVehicleList" + validateLoginResponseVehicleList);

        ArrayAdapter<ValidateLoginResponseVehicle> adapterVehicle = new ArrayAdapter<ValidateLoginResponseVehicle>(this, android.R.layout.simple_dropdown_item_1line, validateLoginResponseVehicleList);
        vehicleModelMATV.setAdapter(adapterVehicle);

        vehicleModelMATV.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View arg0)
            {
                vehicleModelMATV.showDropDown();
            }
        });

        vehicleModelMATV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                idVehicle = validateLoginResponseVehicleList.get(position).getIdVehicle();
                Log.e("Log", "idVehiclePurchaseOrderItemList" + idVehicle);

            }
        });

        new PullStockoutPurchaseOrderItemListDetails(this, idPurchaseOrder,companyValue).execute();

    }

    // This method will help to retrieve the image
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("Log", "requestCode" + requestCode);
        //Log.e("Log", "pic_id" + pic_id);
        // Match the request 'pic id with requestCode
        if (requestCode == 123 && resultCode == RESULT_OK) // Camera
        {
           // if(photo != null)
          ///  {

                // BitMap is data structure of image file which store the image in memory
//                photo = (Bitmap) data.getExtras().get("data");
//                // Set the image in imageview for display
//                click_image_id.setImageBitmap(photo);
          //  }
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                // inSampleSize = 2 means the image is decoded at 1/2 width and 1/2 height
                // (saving 4x the memory). Use 1 for maximum possible quality.
                options.inSampleSize = 1;

                photo = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);
                click_image_id.setImageBitmap(photo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Check if the result is from our gallery pick request and was successful
        else if (requestCode == 124)  // && resultCode == RESULT_OK  // Gallery
        {
            // Check if the Intent 'data' is not null and contains a URI for the selected image
            if (data != null && data.getData() != null)
            {
                Uri selectedImageUri = data.getData(); // Get the URI of the selected image
                try {
                    // Get the Bitmap from the selected URI using ContentResolver
                    photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);

                    // Set the retrieved Bitmap to your ImageView
                    click_image_id.setImageBitmap(photo);

                    // Display the URI path in the TextView
                    //imagePathTextView.setText("Image URI: " + selectedImageUri.toString());
                    Toast.makeText(this, "Image selected from gallery!", Toast.LENGTH_SHORT).show();

                    // At this point, 'bitmap' contains your selected image.
                    // You can now convert it to a byte array, upload it to a server, etc.
                    // Example: Convert to byte array (requires more code for actual upload)
                    // ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    // bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); // Compress to JPEG with 100% quality
                    // byte[] imageData = baos.toByteArray();
                    // Log.d("ImageUpload", "Image data size: " + imageData.length + " bytes");
                    // Now 'imageData' can be uploaded to a server.

                } catch (IOException e) {
                    // Handle errors during bitmap loading
                    e.printStackTrace();
                    Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
            else
            {
                // User cancelled the selection or no data was returned
                Toast.makeText(this, "No image selected.", Toast.LENGTH_SHORT).show();
            }
        }
        else if (resultCode == RESULT_CANCELED)
        {
            // User explicitly canceled the gallery operation
            Toast.makeText(this, "Gallery selection cancelled.", Toast.LENGTH_SHORT).show();
        }
    }


    public void listeners(View view)
    {
        switch (view.getId())
        {
            case R.id.complete_save_button:
            {

                int count = dbHelper.getPendingStockOutPurchaseOrder();
                Log.e("Log", "count" + count);
                if(count > 0)
                {
                    Fns.neutralAlert("Alert","Please complete the verification process."+ count + " items remain pending verification.", this);
                    return;
                }
                else
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

//                    if (photo != null)
//                    {
//                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                        photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
//                        byte[] bytearray = stream.toByteArray();
//
//                        InputStream myInputStream = new ByteArrayInputStream(bytearray);
//                        Bitmap bitmap = BitmapFactory.decodeStream(myInputStream);
//                        //Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 300, 200, true);
//                        //Drawable image = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(bytearray, 0, bytearray.length));
//
//
//                        //previewImageView.setImageDrawable(image);
//                        resizedBitmapBig = Bitmap.createScaledBitmap(bitmap, 480, 800, true);
//                        if(bytearray.length<=1024)
//                        {
//
//                            resizedBitmapBig = bitmap;
//
//                        }
//
//                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//                        resizedBitmapBig.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
//                        byte[] byteArray = byteArrayOutputStream .toByteArray();
//
//                        fileName = idPurchaseOrder+"_"+currentDateAndTime+".jpg";
//
//                        base64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
//
//                    }

                    if (photo != null) {
                        // 1. Calculate a reasonable scale (e.g., Max 1920px for the longest side)
                        // This provides "Full HD" quality which is usually perfect for servers.
                        int maxDimension = 1920;
                        int width = photo.getWidth();
                        int height = photo.getHeight();
                        float ratio = Math.min((float) maxDimension / width, (float) maxDimension / height);

                        int finalWidth = Math.round(ratio * width);
                        int finalHeight = Math.round(ratio * height);

                        // Only scale down if the photo is actually larger than the max dimension
                        Bitmap finalBitmap;
                        if (ratio < 1.0) {
                            finalBitmap = Bitmap.createScaledBitmap(photo, finalWidth, finalHeight, true);
                        } else {
                            finalBitmap = photo;
                        }

                        // 2. Compress directly to JPEG (Use 80-90 quality for best balance)
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                        byte[] byteArray = byteArrayOutputStream.toByteArray();

                        // 3. Prepare for upload
                        fileName = idPurchaseOrder + "_" + System.currentTimeMillis() + ".jpg";
                        base64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
                    }
                    new PushStockOutRequest(this, pulledIdSalesDetails, fileName, base64, companyValue, employeeCode, idVehicle).execute();
                }


            }
            break;
            case R.id.camera_button:
            {

                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Start the activity with camera_intent, and request pic id
               // startActivityForResult(camera_intent, 123);
                if (camera_intent.resolveActivity(getPackageManager()) != null) {
                    try {
                        // Create an empty file
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                        File storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
                        photoFile = File.createTempFile("JPEG_" + timeStamp + "_", ".jpg", storageDir);

                        // Get the URI using FileProvider
                        photoURI = androidx.core.content.FileProvider.getUriForFile(this,
                                getApplicationContext().getPackageName() + ".fileprovider", photoFile);

                        // Tell the camera where to save the full image
                        camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(camera_intent, 123);
                    } catch (IOException ex) {
                        Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case R.id.attach_image:
            {
                // Check for Read External Storage permission before opening gallery
                // checkGalleryPermission();
                openGallery();
            }
            break;
        }
    }


    /**
     * Callback for the result from requesting permissions.
     * This method is invoked when the user responds to the permission request dialog.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // Always call super

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted by the user
                Toast.makeText(this, "Storage permission granted.", Toast.LENGTH_SHORT).show();
                openGallery(); // Re-attempt to open gallery now that permission is granted
            } else {
                // Permission denied by the user
                Toast.makeText(this, "Storage permission denied. Cannot pick image from gallery.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Launches an Intent to open the device's image gallery.
     */
    private void openGallery()
    {
        // Create an Intent with ACTION_PICK action to select an item from data.
        // MediaStore.Images.Media.EXTERNAL_CONTENT_URI points to the external storage's image collection.
       /* Intent intent = new Intent();
        //intent.setType("image/*|application/pdf");
        intent.setType("image/*");
        //intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra("return-data", true);
        startActivityForResult(Intent.createChooser(intent, "Complete action using"), 120);

        */

        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // Ensure that there's an activity on the device to handle this intent
        if (galleryIntent.resolveActivity(getPackageManager()) != null) {
            // Start the activity and expect a result back using REQUEST_IMAGE_PICK code
            startActivityForResult(galleryIntent, 124);
        } else {
            // If no gallery app is found
            Toast.makeText(this, "No gallery app found on this device.", Toast.LENGTH_SHORT).show();
        }


    }
    private static class PullStockoutPurchaseOrderItemListDetails extends AsyncTask<String, String, String>
    {
        ProgressDialog pd;
        WeakReference<StockOutPurchaseOrderItemListActivity> context;
        private OkHttpClient okHttpClient;
        private Request request;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String url;
        SharedPreferences shp;
        StockOutPurchaseOrderItemListJson stockOutPurchaseOrderItemListJson;
        int idPurchaseOrder, companyValue, pulledIdPurchaseOrder;

        DbHelper dbHelper;

        public PullStockoutPurchaseOrderItemListDetails(StockOutPurchaseOrderItemListActivity context, int idPurchaseOrder, int companyValue)
        {
            this.context = new WeakReference<>(context);
            this.idPurchaseOrder = idPurchaseOrder;
            this.companyValue = companyValue;
            //this.pulledIdPurchaseOrder = pulledIdPurchaseOrder;

            shp = context.getSharedPreferences(Const.Shared_Pref_name,MODE_PRIVATE);

            dbHelper = new DbHelper(context);

            CustomTrust customTrust = new CustomTrust(context);
            OkHttpClient client = customTrust.getClient();
            okHttpClient = client;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            pd = new ProgressDialog(context.get());
            pd.setTitle("Uploading Data");
            pd.setMessage("Please wait few seconds...");
            pd.setCancelable(false);
            //pd.setIndeterminate(true);
            pd.setMax(5);
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.show();

        }

        @Override
        protected String doInBackground(String... strings)
        {
            try
            {
                url = Const.USING_IP + URL_STOCKOUT_PURCHASE_ORDER_ITEM_LIST;
                Log.e("Log", "stockoutpurchaseOrderItemListURL" + url);

                JSONObject jsonObjectPurchaseOrderItemList = new JSONObject();

                jsonObjectPurchaseOrderItemList.put("createdBy", shp.getString(Const.Shp_Employee_Code, ""));
                jsonObjectPurchaseOrderItemList.put("idPurchaseOrder", idPurchaseOrder);

                Log.e("Log", "jsonObjectpurchaseOrderItemList" + jsonObjectPurchaseOrderItemList);

                RequestBody body = RequestBody.create(jsonObjectPurchaseOrderItemList.toString(), JSON);
                request = new Request.Builder()
                        //.header("X-Client-Type", "Android")
                        .url(url)
                        .post(body)
                        .build();

                Response response = okHttpClient.newCall(request).execute();

                if (!response.isSuccessful())
                {
                    return "failure";

                }

                String result = response.body().string();


                Log.e("Log", "purchaseOrderResult" + result);
                Gson gson = new Gson();
                stockOutPurchaseOrderItemListJson = gson.fromJson(result, StockOutPurchaseOrderItemListJson.class);

                if (stockOutPurchaseOrderItemListJson.getData().getStockOutPurchaseOrderItemList() == null || stockOutPurchaseOrderItemListJson.getData().getStockOutPurchaseOrderItemList().isEmpty() || stockOutPurchaseOrderItemListJson.getData().getStockOutPurchaseOrderItemList().size() == 0)
                {
                    return "nullException";
                }
                else
                {
                    dbHelper.deleteStockOutRequest();
                    dbHelper.insertStockOutRequest(stockOutPurchaseOrderItemListJson);

                }


            }
            catch (Exception e)
            {

                Log.e("Log","Exception",e);
                return "failure";
            }
            return "success";
        }

        protected void onPostExecute(String s)
        {
            super.onPostExecute(s);


            if (s.equals("success"))
            {

                context.get().getPulledDetails(stockOutPurchaseOrderItemListJson);
                Log.e("Log","pulledPurchaseOrder" + pulledIdPurchaseOrder);
                RecyclerView purchaseOrderItemListRecyclerview = context.get().findViewById(R.id.purchaseorderitemlistrecyclerview);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context.get());
                purchaseOrderItemListRecyclerview.setLayoutManager(layoutManager);
                purchaseOrderItemListRecyclerview.setItemAnimator(new DefaultItemAnimator());


                StockoutPurchaseOrderItemListAdapter purchaseOrderItemListAdapter = new StockoutPurchaseOrderItemListAdapter(context.get(), stockOutPurchaseOrderItemListJson.getData().getStockOutPurchaseOrderItemList());
                purchaseOrderItemListRecyclerview.setAdapter(purchaseOrderItemListAdapter);



            }
            else if (s.equals("nullException"))
            {
                Fns.neutralAlert("Alert", "Null Exception from Server Side", context.get());
            }
            else
            {
                Fns.neutralAlert("Alert", "Failure", context.get());
            }
            pd.dismiss();
        }

    }

    private void getPulledDetails(StockOutPurchaseOrderItemListJson stockOutPurchaseOrderItemListJson)
    {
        this.stockOutPurchaseOrderItemListJson = stockOutPurchaseOrderItemListJson;

        pulledIdSalesDetails = stockOutPurchaseOrderItemListJson.getData().getStockOutPurchaseOrderItemList().get(0).getIdSalesDetails();

    }

    private static class PushStockOutRequest extends AsyncTask<String, String, String>
    {
        ProgressDialog pd;
        WeakReference<StockOutPurchaseOrderItemListActivity> context;
        private OkHttpClient okHttpClient;
        private Request request;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String url;
        SharedPreferences shp;
        DbHelper dbHelper;
        StockOutSendPurchaseRequestJson stockOutSendPurchaseRequestJson;
        String message, fileName, base64;
        int pulledIdSalesDetails;
        int status;
        Integer companyValue;

        int employeeCode, idVehicle;

        public PushStockOutRequest(StockOutPurchaseOrderItemListActivity context, int pulledIdSalesDetails, String fileName, String base64, Integer companyValue, int employeeCode, int idVehicle)
        {
            this.context = new WeakReference<>(context);
            this.pulledIdSalesDetails = pulledIdSalesDetails;
            this.fileName = fileName;
            this.base64 = base64;
            this.companyValue = companyValue;
            this.employeeCode = employeeCode;
            this.idVehicle = idVehicle;

            Log.e("Log", "pulledIdSalesDetailsPush" + pulledIdSalesDetails);
            shp = context.getSharedPreferences(Const.Shared_Pref_name,MODE_PRIVATE);

            dbHelper = new DbHelper(context);

            CustomTrust customTrust = new CustomTrust(context);
            OkHttpClient client = customTrust.getClient();
            okHttpClient = client;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            pd = new ProgressDialog(context.get());
            pd.setTitle("Uploading Data");
            pd.setMessage("Please wait few seconds...");
            pd.setCancelable(false);
            //pd.setIndeterminate(true);
            pd.setMax(5);
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.show();

        }

        @Override
        protected String doInBackground(String... strings)
        {
            try {


                JSONObject pushDataObj = dbHelper.getSendStockoutRequest(shp.getString(Const.Shp_Employee_Code,""), 1, pulledIdSalesDetails, fileName, base64, companyValue, employeeCode, idVehicle);
               // pushDataObj.put("fileName", fileName);
               // pushDataObj.put("customerPhoto", base64);

                url = Const.USING_IP + URL_STOCKOUT_SEND_PURCHASE_REQUEST;

                Log.e("Log","sendStockoutRequestRequest" +url);
                Log.e("Log","sendStockoutObject"+pushDataObj.toString());


                RequestBody body = RequestBody.create(pushDataObj.toString(), JSON);
                Log.e("Log", "customer list body" + body);
                request = new Request.Builder()
                        //.header("X-Client-Type", "Android")
                        .url(url)
                        .post(body)
                        .build();
                Log.e("Log", "request" + request);

                Response response = okHttpClient.newCall(request).execute();
                Log.e("Log", "customer list" + response);

                if (!response.isSuccessful()) {

                    return "failure";

                }

                String resultString = response.body().string();

                Log.e("Log", "" + resultString);

                Gson gson = new Gson();

                stockOutSendPurchaseRequestJson = gson.fromJson(resultString,StockOutSendPurchaseRequestJson.class);
                Log.e("Log", "stockOutSendPurchaseRequestJson" + stockOutSendPurchaseRequestJson);

                if (stockOutSendPurchaseRequestJson.getData().getStockOutSendPurchaseRequestStatus().isEmpty() || stockOutSendPurchaseRequestJson.getData().getStockOutSendPurchaseRequestStatus().size() == 0 || stockOutSendPurchaseRequestJson.getData().getStockOutSendPurchaseRequestStatus() == null)
                {
                    return "nullException";
                }
                else
                {
                    status = stockOutSendPurchaseRequestJson.getData().getStockOutSendPurchaseRequestStatus().get(0).getStatus();
                    Log.e("Log", "status" + status);
                    message = stockOutSendPurchaseRequestJson.getData().getStockOutSendPurchaseRequestStatus().get(0).getStatusMsg();
                    Log.e("Log", "message" + message);

                    if(status != 3)
                    {
                        return "failure";
                    }
                }



            }catch (Exception e)
            {

                Log.e("Log","Exception",e);
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


              /*  AlertDialog.Builder builder = new AlertDialog.Builder(context.get());
                View dialogView = LayoutInflater.from(context.get()).inflate(R.layout.dialog_success,null);

                builder.setView(dialogView);

                AlertDialog alertDialog = builder.create();
                alertDialog.getWindow().setBackgroundDrawableResource(R.color.green_text);
                alertDialog.show();

                TextView successMsg = dialogView.findViewById(R.id.success_msg);
                successMsg.setTextColor(Color.BLACK);
                successMsg.setText(message);

                MaterialButton okButton = dialogView.findViewById(R.id.ok_button);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        alertDialog.dismiss();


                        context.get().startActivity(new Intent(context.get(), MainMenuActivity.class));
                        context.get().finish();
                    }
                });

               */
                android.app.AlertDialog.Builder adb = new android.app.AlertDialog.Builder(context.get());

                TextView titletxtview = new TextView(context.get());
                titletxtview.setText("Alert");
                titletxtview.setBackgroundColor(ContextCompat.getColor(context.get(), R.color.colorPrimary));
                titletxtview.setPadding(10, 10, 10, 10);
                titletxtview.setGravity(Gravity.CENTER);
                titletxtview.setTextColor(Color.WHITE);
                titletxtview.setTextSize(20);

                adb.setCustomTitle(titletxtview);

                TextView messagetxtview = new TextView(context.get());
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
                        // dialog.cancel();

                        dialog.dismiss();


                        context.get().startActivity(new Intent(context.get(), MainMenuActivity.class));
                        context.get().finish();

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
                        context.get().startActivity(Intent.createChooser(intent, "Share using"));

                    }
                });
                android.app.AlertDialog ad = adb.create();
                ad.show();


            }
            else if (s.equals("failure"))
            {

              /*  AlertDialog.Builder builder = new AlertDialog.Builder(context.get());
                View dialogView = LayoutInflater.from(context.get()).inflate(R.layout.dialog_success,null);

                builder.setView(dialogView);

                AlertDialog alertDialog = builder.create();
                alertDialog.getWindow().setBackgroundDrawableResource(R.color.red_button);
                alertDialog.show();

                TextView successMsg = dialogView.findViewById(R.id.success_msg);
                successMsg.setTextColor(Color.BLACK);
                successMsg.setText(message);

                MaterialButton okButton = dialogView.findViewById(R.id.ok_button);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        alertDialog.dismiss();


                        context.get().startActivity(new Intent(context.get(), MainMenuActivity.class));
                        context.get().finish();
                    }
                });

               */
                android.app.AlertDialog.Builder adb = new android.app.AlertDialog.Builder(context.get());

                TextView titletxtview = new TextView(context.get());
                titletxtview.setText("Alert");
                titletxtview.setBackgroundColor(ContextCompat.getColor(context.get(), R.color.colorPrimary));
                titletxtview.setPadding(10, 10, 10, 10);
                titletxtview.setGravity(Gravity.CENTER);
                titletxtview.setTextColor(Color.WHITE);
                titletxtview.setTextSize(20);

                adb.setCustomTitle(titletxtview);

                TextView messagetxtview = new TextView(context.get());
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
                        // dialog.cancel();

                        dialog.dismiss();


                        context.get().startActivity(new Intent(context.get(), MainMenuActivity.class));
                        context.get().finish();

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
                        context.get().startActivity(Intent.createChooser(intent, "Share using"));

                    }
                });
                android.app.AlertDialog ad = adb.create();
                ad.show();


            } else if (s.equals("nullException"))
            {
                Fns.neutralAlert("Alert", "Null Exception from server", context.get());
            } else
            {
                Log.e("Log", "failed to fetch");
            }

        }
    }
}