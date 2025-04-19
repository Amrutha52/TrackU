package com.happy.tracku.viewes;

import static com.happy.tracku.utils.Const.URL_MASTER_DATA;
import static com.happy.tracku.utils.Const.USING_IP;

import android.app.Dialog;
import android.app.ProgressDialog;
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
import com.happy.tracku.R;
import com.happy.tracku.databinding.ActivitySalesRequestBinding;
import com.happy.tracku.databinding.ActivityStockOutOrderListBinding;
import com.happy.tracku.db.DbHelper;
import com.happy.tracku.gson.masterdata.ItemMaster;
import com.happy.tracku.gson.masterdata.MasterDataJson;
import com.happy.tracku.gson.masterdata.VendorMaster;
import com.happy.tracku.ssl.CustomTrust;
import com.happy.tracku.utils.Const;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SalesRequestActivity extends AppCompatActivity
{
    private ActivitySalesRequestBinding binding;
    MasterDataJson masterDataJson;
    DbHelper dbHelper;
    Dialog dialog;
    ArrayList<VendorMaster> vendorMasterArrayList;
    ArrayList<ItemMaster> itemMasterArrayList;
    int idVendor, idItemMaster;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySalesRequestBinding.inflate(getLayoutInflater());
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

        getSupportActionBar().setTitle("SalesOrderRequest");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dbHelper = new DbHelper(this);

        new PullMasterData(this).execute();
    }

    public void listeners(View view)
    {

    }

    private static class PullMasterData extends AsyncTask<String, String, String>
    {

        WeakReference<SalesRequestActivity> context;
        ProgressDialog pd;
        OkHttpClient okHttpClient;
        String url, resultString;
        Request request;
        Response response;
        SharedPreferences shp;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        MasterDataJson masterDataJson;
        DbHelper dbHelper;

        public PullMasterData(SalesRequestActivity context)
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
        dbHelper.insertMasterData(masterDataJson);

        /**
         *  Searchable Hub Spinner
         */

        binding.vendorMasterDropdown.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dialog=new Dialog(SalesRequestActivity.this);
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
                vendorMasterArrayList = dbHelper.getVendorMaster();
                Log.e("Log", "vendorMasterArrayList" + vendorMasterArrayList);
                ArrayAdapter<VendorMaster> adapterVendorMaster = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, vendorMasterArrayList);
                listView.setAdapter(adapterVendorMaster);


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

                        Toast.makeText(SalesRequestActivity.this, "Selected:"+ adapterVendorMaster.getItem(position).getVendorName(), Toast.LENGTH_SHORT).show();
                        //dismiss dialog after choose
                        dialog.dismiss();
                    }
                });
            }
        });

        /**
         * Searchable Branch Spinner
         */

        binding.productMasterDropdown.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dialog=new Dialog(SalesRequestActivity.this);
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
                Log.e("Log", "itemMasterArrayList" + itemMasterArrayList);
                ArrayAdapter<ItemMaster> adapterItemMaster = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, itemMasterArrayList);
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

                        Toast.makeText(SalesRequestActivity.this, "Selected:"+ adapterItemMaster.getItem(position).getItemName(), Toast.LENGTH_SHORT).show();
                        //dismiss dialog after choose
                        dialog.dismiss();
                    }
                });
            }
        });


    }
}