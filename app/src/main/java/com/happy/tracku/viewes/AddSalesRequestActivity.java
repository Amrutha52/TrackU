package com.happy.tracku.viewes;

import static com.happy.tracku.utils.Const.URL_GETPRODUCT_NAME_FROM_IDVENDOR;
import static com.happy.tracku.utils.Const.URL_MASTER_DATA;
import static com.happy.tracku.utils.Const.URL_SALES_DATA_FILLING;
import static com.happy.tracku.utils.Const.URL_SEND_SALES_REQUEST;
import static com.happy.tracku.utils.Const.USING_IP;
import static com.happy.tracku.utils.Fns.getVendorPositionFromId;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.happy.tracku.R;
import com.happy.tracku.databinding.ActivityAddSalesRequestBinding;
import com.happy.tracku.databinding.ActivitySalesRequestBinding;
import com.happy.tracku.db.DbHelper;
import com.happy.tracku.gson.masterdata.ItemMaster;
import com.happy.tracku.gson.masterdata.MasterDataJson;
import com.happy.tracku.gson.masterdata.UnitMaster;
import com.happy.tracku.gson.masterdata.VendorMaster;
import com.happy.tracku.gson.productdetailsfromidvendor.ProductDetailsResponse;
import com.happy.tracku.gson.productdetailsfromidvendor.ProductDetailsResponseJson;
import com.happy.tracku.gson.salesrequestdatafilling.SalesRequestDataFillingJson;
import com.happy.tracku.gson.sendsalesrequest.SendSalesRequestJson;
import com.happy.tracku.ssl.CustomTrust;
import com.happy.tracku.utils.Const;
import com.happy.tracku.utils.Fns;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import kotlin.Unit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddSalesRequestActivity extends AppCompatActivity
{
    private ActivityAddSalesRequestBinding binding;
    int idVendor, idItemMaster, idUnitMaster;
    DatePickerDialog pickUpDatePicker;
    MasterDataJson masterDataJson;
    DbHelper dbHelper;
    Dialog dialog;
    ArrayList<VendorMaster> vendorMasterArrayList;
    List<ProductDetailsResponse> itemMasterArrayList;
    ArrayList<UnitMaster> unitMasterArrayList;
    SalesRequestDataFillingJson salesRequestDataFillingJson;
    Intent intent;
    MaterialAutoCompleteTextView vendorMasterET;

    ArrayAdapter<VendorMaster> adapterVendorMaster;
    ArrayAdapter<ItemMaster> adapterItemMaster;
    ArrayAdapter<UnitMaster> adapterUnitMaster;
    String vendorName, itemName, unitName;
    String deliveryDateStringFromSales, deliveryLocationStringFromSales, vendorMailIDStringFromSales, mobileNumberStringFromSales;

    /**
     *
     *  Camera Section
     *
     */
    // Define the pic id
    private static final int pic_id = 123;
    // Define the button and imageview type variable
    MaterialButton camera_open_id, gallery_open_id;
    ImageView click_image_id;
    Bitmap photo, resizedBitmapBig;
    private static final int PERMISSION_REQUEST_CODE = 100;
    String possibleDeliveryDateString, deliveryLocationString, mailIdString, mobileNumberString, unitFromET;
    int quantityFromET = 0;
    String descriptionETString;
    String fileName="", base64="";
    ProductDetailsResponseJson productDetailsResponseJson;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAddSalesRequestBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
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

        getSupportActionBar().setTitle("AddSalesRequest");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        camera_open_id = findViewById(R.id.camera_button);
        click_image_id = findViewById(R.id.click_image);
        gallery_open_id = findViewById(R.id.attach_image);

        vendorMasterET = findViewById(R.id.vendor_master_dropdown);

        intent = getIntent();

        idVendor = intent.getIntExtra("idVendor",0);
        Log.e("Log", "idVendorAddSales" + idVendor);


        vendorName = intent.getStringExtra("vendorName");
        Log.e("Log", "vendorNameSales" + vendorName);

        deliveryDateStringFromSales = intent.getStringExtra("date");
        Log.e("Log", "deliveryDateStringFromSales" + deliveryDateStringFromSales);

        deliveryLocationStringFromSales = intent.getStringExtra("deliveryLocation");
        Log.e("Log", "deliveryLocationStringFromSales" + deliveryLocationStringFromSales);

        vendorMailIDStringFromSales = intent.getStringExtra("vendorMailID");
        Log.e("Log", "vendorMailIDStringFromSales" + vendorMailIDStringFromSales);

        mobileNumberStringFromSales = intent.getStringExtra("vendorMobileNumber");
        Log.e("Log", "mobileNumberStringFromSales" + mobileNumberStringFromSales);

        gallery_open_id = findViewById(R.id.attach_image);

        dbHelper = new DbHelper(this);

        vendorMasterArrayList = dbHelper.getVendorMaster();
        Log.e("Log", "vendorMasterArrayList" + vendorMasterArrayList);
        adapterVendorMaster = new ArrayAdapter<>(getApplicationContext(), R.layout.custom_textview, vendorMasterArrayList);
        binding.vendorMasterDropdown.setAdapter(adapterVendorMaster);
        binding.vendorMasterDropdown.setText(vendorName);

      /*  itemMasterArrayList = dbHelper.getItemMaster();
        Log.e("Log", "itemMasterArrayList" + itemMasterArrayList);
        adapterItemMaster = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, itemMasterArrayList);
        binding.productMasterDropdown.setAdapter(adapterItemMaster);
        // binding.productMasterDropdown.setText(itemName);

        unitMasterArrayList = dbHelper.getUnitMaster();
        adapterUnitMaster = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, unitMasterArrayList);
        binding.unitMasterDropdown.setAdapter(adapterUnitMaster);
      //  binding.unitMasterDropdown.setText(unitName);

       */



        binding.possibleDeliveryDateEditText.setText(deliveryDateStringFromSales);
        binding.deliveryLocation.setText(deliveryLocationStringFromSales);
        binding.vendorMailId.setText(vendorMailIDStringFromSales);
        binding.mobileNumber.setText(mobileNumberStringFromSales);

        binding.possibleDeliveryDateEditText.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);

                // date picker dialog

                pickUpDatePicker = new DatePickerDialog(AddSalesRequestActivity.this,
                        new DatePickerDialog.OnDateSetListener()
                        {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                month = month + 1;

                                String monthString = "" + month, dayString = "" + dayOfMonth;
                                if (month < 10) {
                                    monthString = "0" + monthString;
                                }
                                if (dayOfMonth < 10) {
                                    dayString = "0" + dayString;
                                }

                                binding.possibleDeliveryDateEditText.setText(year + "-" + monthString + "-" + dayString);
                                Log.e("pickUpDate", binding.possibleDeliveryDateEditText.getText().toString());

                            }
                        }, mYear, mMonth, mDay);

                //Date minDateObj = Calendar.getInstance().getTime();
                //pickUpDatePicker.getDatePicker().setMinDate(minDateObj.getTime());
                //pickUpDatePicker.getDatePicker().setMaxDate(minDateObj.getTime()+(365*24*60*60));
                pickUpDatePicker.show();
            }
        });

        /**
         *  Searchable Vendor Spinner
         */

        binding.vendorMasterDropdown.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dialog=new Dialog(AddSalesRequestActivity.this);
                //set  (our custom layout for dialog)
                dialog.setContentView(R.layout.layout_searchable_spinner);

                //set transparent background
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                //show dialog
                dialog.show();

                //initialize and assign variable
                EditText editText=dialog.findViewById(R.id.editText_of_searchableSpinner);
                ListView listView=dialog.findViewById(R.id.listView_of_searchableSpinner);
                //array adapter
             /*   vendorMasterArrayList = dbHelper.getVendorMaster();
                Log.e("Log", "vendorMasterArrayList" + vendorMasterArrayList);
                ArrayAdapter<VendorMaster> adapterVendorMaster = new ArrayAdapter<>(getApplicationContext(), R.layout.custom_textview, vendorMasterArrayList);
                listView.setAdapter(adapterVendorMaster);

              */


                //Textwatcher for change data after every text type by user

                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        //filter arraylist
                        adapterVendorMaster.getFilter().filter(charSequence);
                    }
                    @Override
                    public void afterTextChanged(Editable editable) {
                    }
                });

                // listview onitem click listener
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        // textViewSpinner.setText( adapterBranchMaster.getItem(i));
                        binding.vendorMasterDropdown.setText(adapterVendorMaster.getItem(position).getVendorName());

                        idVendor = adapterVendorMaster.getItem(position).getIdVendor();
                        Log.e("Log", "idVendor : " + idVendor);

                        vendorName = adapterVendorMaster.getItem(position).getVendorName();
                        Log.e("Log", "vendorName" + vendorName);

                        new PullProductDetailsFromIdVendor(AddSalesRequestActivity.this, idVendor).execute();

                        new PullSalesRequestDataFilling(AddSalesRequestActivity.this, idVendor).execute();

                        Toast.makeText(AddSalesRequestActivity.this, "Selected:"+ adapterVendorMaster.getItem(position).getVendorName(), Toast.LENGTH_SHORT).show();
                        //dismiss dialog after choose
                        dialog.dismiss();
                    }
                });
            }
        });


        /**
         * Unit Master
         */
        binding.unitMasterDropdown.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dialog=new Dialog(AddSalesRequestActivity.this);
                //set  (our custom layout for dialog)
                dialog.setContentView(R.layout.layout_searchable_spinner);

                //set transparent background
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                //show dialog
                dialog.show();

                //initialize and assign variable
                EditText editText=dialog.findViewById(R.id.editText_of_searchableSpinner);
                ListView listView=dialog.findViewById(R.id.listView_of_searchableSpinner);
                //array adapter
                unitMasterArrayList = dbHelper.getUnitMaster();
                Log.e("Log", "unitMasterArrayList" + unitMasterArrayList);
                ArrayAdapter<UnitMaster> adapterUnitMaster = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, unitMasterArrayList);
                listView.setAdapter(adapterUnitMaster);


                //Textwatcher for change data after every text type by user

                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        //filter arraylist
                        adapterUnitMaster.getFilter().filter(charSequence);
                    }
                    @Override
                    public void afterTextChanged(Editable editable) {
                    }
                });

                // listview onitem click listener
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        // textViewSpinner.setText( adapterBranchMaster.getItem(i));
                        binding.unitMasterDropdown.setText(adapterUnitMaster.getItem(position).getUnitName());

                        idUnitMaster = adapterUnitMaster.getItem(position).getIdUnit();
                        Log.e("Log", "idUnitMaster : " + idUnitMaster);

                        unitName = adapterUnitMaster.getItem(position).getUnitName();
                        Log.e("Log", "unitName" + unitName);


                        Toast.makeText(AddSalesRequestActivity.this, "Selected:"+ adapterUnitMaster.getItem(position).getUnitName(), Toast.LENGTH_SHORT).show();
                        //dismiss dialog after choose
                        dialog.dismiss();
                    }
                });
            }
        });

    }

    // This method will help to retrieve the image
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("Log", "requestCode" + requestCode);
        //Log.e("Log", "pic_id" + pic_id);
        // Match the request 'pic id with requestCode
        if (requestCode == 123) // Camera
        {
            // BitMap is data structure of image file which store the image in memory
            photo = (Bitmap) data.getExtras().get("data");
            // Set the image in imageview for display
            click_image_id.setImageBitmap(photo);
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
            case R.id.add_another_item_button:
            {

                possibleDeliveryDateString = binding.possibleDeliveryDateEditText.getText().toString();
                deliveryLocationString = binding.deliveryLocation.getText().toString();
                mailIdString = binding.vendorMailId.getText().toString();
                mobileNumberString = binding.mobileNumber.getText().toString();
                String quantityString = binding.quantityET.getText().toString().trim(); // Get text and trim whitespace

                if (quantityString.isEmpty()) {
                    // Handle the case where the input is empty
                    binding.quantityET.setError("This field cannot be empty."); // Show an error message
                    // Optionally, you might want to stop further processing here,
                    // or assign a default value like 0.
                    int parsedNumber = 0; // Default to 0 if empty
                    Toast.makeText(AddSalesRequestActivity.this, "Please enter a quantity.", Toast.LENGTH_SHORT).show();
                    return; // Stop the method execution here if input is mandatory
                }

                try {
                    quantityFromET = Integer.parseInt(quantityString);
                    // If parsing is successful, 'parsedNumber' now holds the integer value.
                    // You can safely use 'parsedNumber' here.
                    // Log.d("SalesRequest", "Successfully parsed number: " + parsedNumber);

                } catch (NumberFormatException e) {
                    // Handle cases where the input is not a valid integer (e.g., "abc", "1.5")
                    binding.quantityET.setError("Please enter a valid whole number.");
                    Toast.makeText(AddSalesRequestActivity.this, "Invalid number format.", Toast.LENGTH_SHORT).show();
                    // Log.e("SalesRequest", "NumberFormatException: " + e.getMessage());
                    return; // Stop the method execution if parsing failed
                }
               // quantityFromET = Integer.parseInt(binding.quantityET.getText().toString());
               // unitFromET = binding.unitET.getText().toString();
                descriptionETString = binding.descriptionET.getText().toString();

                if (idVendor == 0)
                {
                    vendorName = binding.vendorNameET.getText().toString();
                }
                if (idItemMaster == 0)
                {
                    itemName = binding.productNameET.getText().toString();
                }
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


                if (photo != null)
                {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] bytearray = stream.toByteArray();

                    InputStream myInputStream = new ByteArrayInputStream(bytearray);
                    Bitmap bitmap = BitmapFactory.decodeStream(myInputStream);
                    //Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 300, 200, true);
                    //Drawable image = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(bytearray, 0, bytearray.length));

                    resizedBitmapBig = Bitmap.createScaledBitmap(bitmap, 480, 800, true);
                    if(bytearray.length<=1024)
                    {

                        resizedBitmapBig = bitmap;

                    }

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    resizedBitmapBig.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream .toByteArray();

                    fileName = mobileNumberString+"_"+currentDateAndTime+".jpg";

                    base64 = Base64.encodeToString(byteArray, Base64.DEFAULT);

                }
                else
                {
                    Toast.makeText(this, "No image selected or captured.", Toast.LENGTH_SHORT).show();
                }
                new PushSalesRequest(this, idVendor, idItemMaster, quantityFromET, unitFromET, possibleDeliveryDateString, deliveryLocationString, mailIdString, mobileNumberString, descriptionETString, fileName, base64, vendorName, itemName, idUnitMaster).execute();

                //previewImageView.setImageDrawable(image);


            }
            break;

            case R.id.complete_button:
            {


                Intent intent = new Intent(this, SalesRequestActivity.class);
                this.startActivity(intent);

            }
            break;

            case R.id.camera_button:
            {

                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Start the activity with camera_intent, and request pic id
                startActivityForResult(camera_intent, pic_id);
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
     * Launches an Intent to open the device's image gallery.
     */
    private void openGallery() {
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

    private static class PullSalesRequestDataFilling extends AsyncTask<String, String, String>
    {
        WeakReference<AddSalesRequestActivity> context;
        ProgressDialog pd;
        OkHttpClient okHttpClient;
        String url, resultString;
        Request request;
        Response response;
        SharedPreferences shp;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        SalesRequestDataFillingJson salesRequestDataFillingJson;
        int idVendor;


        public PullSalesRequestDataFilling(AddSalesRequestActivity context, int idVendor)
        {
            this.context = new WeakReference<>(context);
            this.idVendor = idVendor;

            CustomTrust customTrust = new CustomTrust(context);
            OkHttpClient client = customTrust.getClient();
            okHttpClient = client;

            shp = context.getSharedPreferences(Const.Shared_Pref_name, MODE_PRIVATE);
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

                JSONObject dataFillingObj = new JSONObject();
                dataFillingObj.put("idVendor", idVendor);
                dataFillingObj.put("createdBy", shp.getString(Const.Shp_Employee_Code, ""));

                url = USING_IP + URL_SALES_DATA_FILLING;
                Log.e("Log", "salesDataFillingURL" + url);

                RequestBody body = RequestBody.create(dataFillingObj.toString(), JSON);
                Log.e("Log", "dataFillingObj" + dataFillingObj);

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
                Log.e("Log", "SalesDataFillingResultString" + resultString);

                Gson gson = new Gson();
                salesRequestDataFillingJson = gson.fromJson(resultString, SalesRequestDataFillingJson.class);
                Log.e("Log", "salesRequestDataFillingJson" + salesRequestDataFillingJson);

                if (salesRequestDataFillingJson.getData().getSalesRequestDataFillingDetails() == null || salesRequestDataFillingJson.getData().getSalesRequestDataFillingDetails().size() == 0 || salesRequestDataFillingJson.getData().getSalesRequestDataFillingDetails().isEmpty())
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
                context.get().clearFillingDetails();
                context.get().setFillingData(salesRequestDataFillingJson);
                Toast.makeText(context.get(), "Data Fetched Successfully", Toast.LENGTH_SHORT).show();
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

    private void setFillingData(SalesRequestDataFillingJson salesRequestDataFillingJson)
    {
        this.salesRequestDataFillingJson = salesRequestDataFillingJson;

        /**
         * Filling Email
         */
        if (salesRequestDataFillingJson.getData().getSalesRequestDataFillingDetails().get(0).geteMail().isEmpty())
        {
            Toast.makeText(this, "Please Enter Your Email", Toast.LENGTH_LONG).show();
        }
        else
        {
            binding.vendorMailId.setText(salesRequestDataFillingJson.getData().getSalesRequestDataFillingDetails().get(0).geteMail());
        }

        /**
         * Filling MobileNumber
         */
        if (salesRequestDataFillingJson.getData().getSalesRequestDataFillingDetails().get(0).getMobileNumber().isEmpty())
        {
            Toast.makeText(this, "Please Enter Your Phone Number", Toast.LENGTH_LONG).show();
        }
        else
        {
            binding.mobileNumber.setText(salesRequestDataFillingJson.getData().getSalesRequestDataFillingDetails().get(0).getMobileNumber());
        }

    }

    private void clearFillingDetails()
    {
       // binding.vendorMailId.setText("");
      //  binding.deliveryLocation.setText("");
      //  binding.mobileNumber.setText("");
         binding.productMasterDropdown.setText("");
        binding.quantityET.setText("");
        binding.unitMasterDropdown.setText("");
     //   binding.deliveryLocation.setText("");
     //   binding.possibleDeliveryDateEditText.setText("");
        click_image_id.setImageBitmap(null);
    }

    private static class PushSalesRequest extends AsyncTask<String, String, String>
    {
        WeakReference<AddSalesRequestActivity> context;
        ProgressDialog pd;
        OkHttpClient okHttpClient;
        String url, resultString;
        Request request;
        Response response;
        SharedPreferences shp;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        SendSalesRequestJson sendSalesRequestJson;
        int status;
        String statusMessage;
        int idVendor, idItemMaster, quantityFromET, idUnit;
        String descriptionETString;
        String fileName, base64;
        String vendorName, itemName;
        String possibleDeliveryDateString, deliveryLocationString, mailIdString, mobileNumberString, unitFromET;
        public PushSalesRequest(AddSalesRequestActivity context, int idVendor, int idItemMaster, int quantityFromET, String unitFromET, String possibleDeliveryDateString, String deliveryLocationString, String mailIdString, String mobileNumberString, String descriptionETString, String fileName, String base64, String vendorName, String itemName, int idUnit)
        {
            this.context = new WeakReference<>(context);
            this.idVendor = idVendor;
            this.idItemMaster = idItemMaster;
            this.quantityFromET = quantityFromET;
            this.unitFromET = unitFromET;
            this.deliveryLocationString = deliveryLocationString;
            this.possibleDeliveryDateString = possibleDeliveryDateString;
            this.mailIdString = mailIdString;
            this.mobileNumberString = mobileNumberString;
            this.descriptionETString = descriptionETString;
            this.base64 = base64;
            this.fileName = fileName;
            this.vendorName = vendorName;
            this.itemName = itemName;
            this.idUnit = idUnit;

            CustomTrust customTrust = new CustomTrust(context);
            OkHttpClient client = customTrust.getClient();
            okHttpClient = client;
            shp = context.getSharedPreferences(Const.Shared_Pref_name, MODE_PRIVATE);
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

                JSONObject sendSalesRequestObj = new JSONObject();
                sendSalesRequestObj.put("requestDate", possibleDeliveryDateString);
                sendSalesRequestObj.put("idItem", idItemMaster);
                sendSalesRequestObj.put("orderQty", quantityFromET);
                sendSalesRequestObj.put("createdBy", shp.getString(Const.Shp_Employee_Code, ""));
                sendSalesRequestObj.put("description", descriptionETString);
                sendSalesRequestObj.put("vendorName", vendorName);
                sendSalesRequestObj.put("itemName", itemName);
                sendSalesRequestObj.put("idUnit", idUnit);
                sendSalesRequestObj.put("fileName", fileName);
                sendSalesRequestObj.put("photoUpload", base64);


                url = USING_IP + URL_SEND_SALES_REQUEST;
                Log.e("Log", "sendSalesRequestURL" + url);

                RequestBody body = RequestBody.create(sendSalesRequestObj.toString(), JSON);
                Log.e("Log", "sendSalesRequestObj" + sendSalesRequestObj);

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
                Log.e("Log", "sendSalesRequestResultString" + resultString);

                Gson gson = new Gson();
                sendSalesRequestJson = gson.fromJson(resultString, SendSalesRequestJson.class);
                Log.e("Log", "sendSalesRequestJson" + sendSalesRequestJson);

                if (sendSalesRequestJson.getData().getSendSalesRequestStatus() == null || sendSalesRequestJson.getData().getSendSalesRequestStatus().size() == 0 || sendSalesRequestJson.getData().getSendSalesRequestStatus().isEmpty())
                {
                    return "nullException";
                }
                else
                {
                    status = sendSalesRequestJson.getData().getSendSalesRequestStatus().get(0).getStatus();

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

            if (s.equals("success"))
            {
                statusMessage = sendSalesRequestJson.getData().getSendSalesRequestStatus().get(0).getStatusMsg();
                // Fns.neutralAlert("Alert", statusMessage, context.get());

                AlertDialog.Builder adb = new AlertDialog.Builder(context.get());

                TextView titletxtview = new TextView(context.get());
                titletxtview.setText("Alert");
                titletxtview.setBackgroundColor(ContextCompat.getColor(context.get(), R.color.yellow));
                titletxtview.setPadding(10, 10, 10, 10);
                titletxtview.setGravity(Gravity.CENTER);
                titletxtview.setTextColor(Color.WHITE);
                titletxtview.setTextSize(20);

                adb.setCustomTitle(titletxtview);

                TextView messagetxtview = new TextView(context.get());
                messagetxtview.setText(statusMessage);
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
                        if (sendSalesRequestJson.getData().getSendSalesRequestStatus().get(0).getStatus() == 1)
                        {
                            context.get().clearFillingDetails();


                        }

                    }
                });

                adb.setPositiveButton("Share", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                        String shareBody = statusMessage;
                        Log.e("LogFns", "fns message" + shareBody);
                        intent.setType("text/plain");
                        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share");
                        intent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                        context.get().startActivity(Intent.createChooser(intent, "Share using"));

                    }
                });
                AlertDialog ad = adb.create();
                ad.show();

            }
            else if (s.equals("failure"))
            {

                statusMessage = sendSalesRequestJson.getData().getSendSalesRequestStatus().get(0).getStatusMsg();
                Fns.neutralAlert("Alert", statusMessage, context.get());

            }
            else if (s.equals("nullException"))
            {
                Toast.makeText(context.get(), "Null Exception From Server", Toast.LENGTH_SHORT).show();
            }

            pd.dismiss();
        }
    }

    private static class PullProductDetailsFromIdVendor extends AsyncTask<String, String, String>
    {
        WeakReference<AddSalesRequestActivity> context;
        ProgressDialog pd;
        OkHttpClient okHttpClient;
        String url, resultString;
        Request request;
        Response response;
        SharedPreferences shp;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        ProductDetailsResponseJson productDetailsResponseJson;
        int idVendor;
        public PullProductDetailsFromIdVendor(AddSalesRequestActivity context, int idVendor)
        {
            this.context = new WeakReference<>(context);
            this.idVendor = idVendor;

            CustomTrust customTrust = new CustomTrust(context);
            OkHttpClient client = customTrust.getClient();
            okHttpClient = client;

            shp = context.getSharedPreferences(Const.Shared_Pref_name, MODE_PRIVATE);
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

                JSONObject dataFillingObj = new JSONObject();
                dataFillingObj.put("idVendor", idVendor);
                dataFillingObj.put("createdBy", shp.getString(Const.Shp_Employee_Code, ""));

                url = USING_IP + URL_GETPRODUCT_NAME_FROM_IDVENDOR;
                Log.e("Log", "PullProductDetailsFromIdVendorURL" + url);

                RequestBody body = RequestBody.create(dataFillingObj.toString(), JSON);
                Log.e("Log", "PullProductDetailsFromIdVendorObj" + dataFillingObj);

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
                Log.e("Log", "PullProductDetailsFromIdVendorResultString" + resultString);

                Gson gson = new Gson();
                productDetailsResponseJson = gson.fromJson(resultString, ProductDetailsResponseJson.class);
                Log.e("Log", "productDetailsResponseJson" + productDetailsResponseJson);

                if (productDetailsResponseJson.getData().getProductDetailsResponse() == null || productDetailsResponseJson.getData().getProductDetailsResponse().size() == 0 || productDetailsResponseJson.getData().getProductDetailsResponse().isEmpty())
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
                context.get().clearFillingDetails();
                context.get().setFillingProductDropDown(productDetailsResponseJson);
                Toast.makeText(context.get(), "Data Fetched Successfully", Toast.LENGTH_SHORT).show();
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

    private void setFillingProductDropDown(ProductDetailsResponseJson productDetailsResponseJson)
    {
        this.productDetailsResponseJson = productDetailsResponseJson;

        /**
         * Searchable Product Spinner
         */

        binding.productMasterDropdown.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dialog=new Dialog(AddSalesRequestActivity.this);
                //set  (our custom layout for dialog)
                dialog.setContentView(R.layout.layout_searchable_spinner);

                //set transparent background
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                //show dialog
                dialog.show();

                //initialize and assign variable
                EditText editText=dialog.findViewById(R.id.editText_of_searchableSpinner);
                ListView listView=dialog.findViewById(R.id.listView_of_searchableSpinner);
                //array adapter
                itemMasterArrayList = productDetailsResponseJson.getData().getProductDetailsResponse();
                Log.e("Log", "PullProductDetailsFromIdVendoritemMasterArrayList" + itemMasterArrayList);
                ArrayAdapter<ProductDetailsResponse> adapterItemMaster = new ArrayAdapter<>(getApplicationContext(), R.layout.custom_textview, itemMasterArrayList);
                listView.setAdapter(adapterItemMaster);

                //Textwatcher for change data after every text type by user

                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        //filter arraylist
                        adapterItemMaster.getFilter().filter(charSequence);
                    }
                    @Override
                    public void afterTextChanged(Editable editable) {
                    }
                });

                // listview onitem click listener
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        // textViewSpinner.setText( adapterBranchMaster.getItem(i));
                        binding.productMasterDropdown.setText(adapterItemMaster.getItem(position).getItemName());
                        idItemMaster = adapterItemMaster.getItem(position).getIdItem();
                        Log.e("Log", "idItemMaster : " + idItemMaster);

                        itemName = adapterItemMaster.getItem(position).getItemName();
                        Log.e("Log", "itemName" + itemName);

                        Toast.makeText(AddSalesRequestActivity.this, "Selected:"+ adapterItemMaster.getItem(position).getItemName(), Toast.LENGTH_SHORT).show();
                        //dismiss dialog after choose
                        dialog.dismiss();
                    }
                });
            }
        });

    }
}