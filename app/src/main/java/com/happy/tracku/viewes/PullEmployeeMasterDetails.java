package com.happy.tracku.viewes;

import static com.happy.tracku.utils.Const.URL_EMPLOYEE_MASTER;
import static com.happy.tracku.utils.Const.USING_IP;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.happy.tracku.db.DbHelper;
import com.happy.tracku.gson.employeemasterdetails.Employeemasterjson;
import com.happy.tracku.ssl.CustomTrust;
import com.happy.tracku.utils.Const;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class PullEmployeeMasterDetails extends AsyncTask<String,String,String>
{

    OkHttpClient okHttpClient;
    String url;
    Request request;
    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    Context context;
    ProgressDialog pd;
    DbHelper dbHelper;
    SharedPreferences shp;
    Employeemasterjson employeemasterjson;

    public PullEmployeeMasterDetails(Context context)
    {
        this.context = context;

        dbHelper = new DbHelper(context);
        CustomTrust customTrust = new CustomTrust(context);
        OkHttpClient client = customTrust.getClient();
        okHttpClient = client;

        pd = new ProgressDialog(context);

        shp = context.getSharedPreferences(Const.Shared_Pref_name, Context.MODE_PRIVATE);

        pd.setTitle("Please wait");
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMessage("wait...");
        pd.setCancelable(false);

    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        pd.show();

    }

    @Override
    protected String doInBackground(String... strings)
    {
        try
        {
            url = USING_IP + URL_EMPLOYEE_MASTER;
            Log.e("Log", "employeeMasterURL" + url);

            JSONObject jsonObjectEmployeeMasterFinal = new JSONObject();

            jsonObjectEmployeeMasterFinal.put("CreatedBy", shp.getString(Const.Shp_Employee_Code, ""));

            Log.e("Log", "employeeMasterJSON" + jsonObjectEmployeeMasterFinal);

            RequestBody body = RequestBody.create(jsonObjectEmployeeMasterFinal.toString(), JSON);
            request = new Request.Builder()
                    //.header("X-Client-Type", "Android")
                    .url(url)
                    .post(body)
                    .build();

            Response response = okHttpClient.newCall(request).execute();

            if (!response.isSuccessful()) {

                return "failure";

            }

            String result = response.body().string();

            Log.e("Log", result);
            Gson gson = new Gson();
            employeemasterjson = gson.fromJson(result, Employeemasterjson.class);

            if (employeemasterjson.getData().getEmployeeMasterDetails().isEmpty() || employeemasterjson.getData().getEmployeeMasterDetails().size() == 0 || employeemasterjson.getData().getWareHouseMasterDetails().size() == 0 || employeemasterjson.getData().getWareHouseMasterDetails().isEmpty())
            {
                return "nullPointerException";
            }

            dbHelper.deleteEmployeeMaster();
            dbHelper.deleteWareHouseMaster();

            dbHelper.insertEmployeeMaster(employeemasterjson.getData().getEmployeeMasterDetails());
            dbHelper.insertWareHouseMaster(employeemasterjson.getData().getWareHouseMasterDetails());

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

            Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();


        }
        else if (s.equals("nullPointerException"))
        {
            Toast.makeText(context, "Employee Master Null Pointer Exception", Toast.LENGTH_SHORT).show();
        }
        else
        {

            Toast.makeText(context, "Failed to Fetch", Toast.LENGTH_SHORT).show();

        }

    }
}
