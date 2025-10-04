package com.happy.ecofied.viewes;

import static com.happy.ecofied.utils.Const.URL_UPDATE_STORE_PURCHASE;
import static com.happy.ecofied.utils.Const.USING_IP;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.happy.ecofied.R;
import com.happy.ecofied.databinding.ActivityStorePurchaseBinding;
import com.happy.ecofied.db.DbHelper;
import com.happy.ecofied.gson.masterdata.ItemMaster;
import com.happy.ecofied.gson.storepurchaserequest.StorePurchaseRequestResponse;
import com.happy.ecofied.ssl.CustomTrust;
import com.happy.ecofied.utils.Const;
import com.happy.ecofied.utils.Fns;


import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StorePurchaseActivity extends AppCompatActivity
{
    private ActivityStorePurchaseBinding binding;
    ArrayList<ItemMaster> itemMasterArrayList;
    Dialog dialog;
    DbHelper dbHelper;
    int idProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityStorePurchaseBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Store Purchase Request");

        View rootView = findViewById(android.R.id.content); // Get root view

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            WindowInsetsCompat insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets.toWindowInsets());
            int systemBarsInsetsTop = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int systemBarsInsetsBottom = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

            // Apply padding to your main content view
            v.setPadding(v.getPaddingLeft(), systemBarsInsetsTop, v.getPaddingRight(), systemBarsInsetsBottom);

            return WindowInsetsCompat.CONSUMED;
        });

        dbHelper = new DbHelper(this);

        /**
         *  Searchable Product Spinner
         */

        binding.productMasterDropdown.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dialog=new Dialog(StorePurchaseActivity.this);
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
                itemMasterArrayList = dbHelper.getItemMaster();
                Log.e("Log", "itemMasterArrayListStorePurchase" + itemMasterArrayList);
                ArrayAdapter<ItemMaster> adapterItemMaster = new ArrayAdapter<>(getApplicationContext(), R.layout.custom_textview, itemMasterArrayList);
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

                        idProduct = adapterItemMaster.getItem(position).getIdItem();
                        Log.e("Log", "idProduct : " + idProduct);

                        Toast.makeText(StorePurchaseActivity.this, "Selected:"+ adapterItemMaster.getItem(position).getItemName(), Toast.LENGTH_SHORT).show();
                        //dismiss dialog after choose
                        dialog.dismiss();
                    }
                });
            }
        });


    }

    public void listeners(View view)
    {
        switch(view.getId())
        {
            case R.id.store_purchase_save_button:
            {
                new UpdateStorePurchase(this, idProduct, binding.physicalQuantityET.getText().toString(), binding.orderQuantityET.getText().toString()).execute();

            }
            break;
        }
    }

    private static class UpdateStorePurchase extends AsyncTask<String, String, String>
    {
        OkHttpClient okHttpClient;
        String url;
        Request request;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        StorePurchaseActivity mContext;
        ProgressDialog pd;
        SharedPreferences shp;
        String failureMsg, resultString;
        StorePurchaseRequestResponse storePurchaseRequestResponse;
        DbHelper dbHelper;
        int idProduct, status;
        String physicalQuantityString, orderQuantityString;
        public UpdateStorePurchase(StorePurchaseActivity mContext, int idProduct, String physicalQuantityString, String orderQuantityString)
        {
            this.mContext = mContext;
            this.idProduct = idProduct;
            this.physicalQuantityString = physicalQuantityString;
            this.orderQuantityString = orderQuantityString;

            CustomTrust customTrust = new CustomTrust(mContext);
            OkHttpClient client = customTrust.getClient();
            okHttpClient = client;

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

                JSONObject storePurchaseObj = new JSONObject();
                storePurchaseObj.put("createdBy", shp.getString(Const.Shp_Employee_Code, ""));
                storePurchaseObj.put("action", 1);
                storePurchaseObj.put("physicalQty", physicalQuantityString);
                storePurchaseObj.put("orderQty", orderQuantityString);


                url = USING_IP + URL_UPDATE_STORE_PURCHASE;
                Log.e("Log", "storePurchaseURL" + url);
                Log.e("Log", "storePurchaseJsonObject" + storePurchaseObj);

                RequestBody body = RequestBody.create(storePurchaseObj.toString(), JSON);

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
                Log.e("Log", "resultStringStorePurchase" + resultString);

                Gson gson = new Gson();
                storePurchaseRequestResponse = gson.fromJson(resultString, StorePurchaseRequestResponse.class);

                if (storePurchaseRequestResponse.getData().getStorePurchaseRequest().isEmpty() || storePurchaseRequestResponse.getData().getStorePurchaseRequest().size() == 0)
                {
                    return "nullException";
                }
                else
                {
                    status = storePurchaseRequestResponse.getData().getStorePurchaseRequest().get(0).getStatus();
                    if (status != 3)
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
                Fns.neutralAlert("Alert", storePurchaseRequestResponse.getData().getStorePurchaseRequest().get(0).getStatusMsg(), mContext);

                if (storePurchaseRequestResponse.getData().getStorePurchaseRequest().get(0).getStatus() == 1)
                {
                    mContext.clearDetails();
                    mContext.startActivity(new Intent(mContext, MainMenuActivity.class));
                }
            }
            else if (s.equals("failure"))
            {
                Fns.neutralAlert("Alert", storePurchaseRequestResponse.getData().getStorePurchaseRequest().get(0).getStatusMsg(), mContext);
                // Toast.makeText(context.get(), "Updation Failed", Toast.LENGTH_SHORT).show();
            }
            else if (s.equals("nullException"))
            {
                Toast.makeText(mContext, "Null Exception From Server", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void clearDetails()
    {
        binding.productMasterDropdown.setSelection(0);
        binding.physicalQuantityET.setText("");
        binding.orderQuantityET.setText("");
    }
}