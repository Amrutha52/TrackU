package com.happy.tracku.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.happy.tracku.gson.employeemasterdetails.EmployeeMasterDetail;
import com.happy.tracku.gson.purchaseorderitemlist.Data;
import com.happy.tracku.gson.purchaseorderitemlist.PurchaseOrderItem;
import com.happy.tracku.gson.purchaseorderitemlist.PurchaseOrderItemListJson;
import com.happy.tracku.models.DailyTravelModel;
import com.happy.tracku.utils.Const;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper
{
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "TrackUDb";
    public static final String EMPLOYEES_DAILY_TRAVEL_ALL_LOCATION_TABLE = "EmployeesDailyTravelAllLocation";
    public static final String EMPLOYEE_MASTER = "EmployeeDetails";
    public static final String SAVE_PURCHASE_ORDER_TABLE = "SavePurchaseDetails";

    private SharedPreferences shp;
    private Context context;

    public DbHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        shp = context.getSharedPreferences(Const.Shared_Pref_name, Context.MODE_PRIVATE);
        this.context = context;
    }
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE " + EMPLOYEES_DAILY_TRAVEL_ALL_LOCATION_TABLE + "(idLocation TEXT, Latitude DOUBLE, Longitude DOUBLE, Address TEXT, DateTime TEXT,idEmployee INTEGER,IsForUpload INTEGER,IsSynced INTEGER)");

        db.execSQL("CREATE TABLE IF NOT EXISTS "+EMPLOYEE_MASTER+" (idEmployee INTEGER,employeeCode TEXT, employeeName TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS "+SAVE_PURCHASE_ORDER_TABLE+" (idItem INTEGER,Item TEXT, idUnit INTEGER, idPurchaseOrder INTEGER,OrderQty INTEGER, RackNo INTEGER, Rate DOUBLE, FloorNo TEXT, AcceptedQty Double, CreatedBy Text)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS EmployeesDailyTravelAllLocation");

        if (oldVersion <= 1)
        {
            db.execSQL("CREATE TABLE IF NOT EXISTS "+EMPLOYEE_MASTER+" (idEmployee INTEGER,employeeCode TEXT, employeeName TEXT)");

        }
        if (oldVersion <= 3)
        {
            db.execSQL("CREATE TABLE IF NOT EXISTS "+SAVE_PURCHASE_ORDER_TABLE+" (idItem INTEGER,Item TEXT, idUnit INTEGER, idPurchaseOrder INTEGER,OrderQty INTEGER, RackNo INTEGER, Rate DOUBLE, FloorNo TEXT, AcceptedQty Double, CreatedBy Text)");

        }
        onCreate(db);
    }

    public int insertContinousGPSLocationOfAnEmployee(DailyTravelModel dailyTravelModel)
    {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("idLocation", dailyTravelModel.getIdLocation());
        cv.put("Latitude", dailyTravelModel.getLatitude());
        cv.put("Longitude", dailyTravelModel.getLongitude());
        cv.put("Address", dailyTravelModel.getAddress());
        cv.put("DateTime", dailyTravelModel.getDateTime());
        cv.put("idEmployee", dailyTravelModel.getIdEmployee());
        cv.put("IsForUpload", dailyTravelModel.getIsForUpload());
        cv.put("IsSynced", dailyTravelModel.getIsSynced());

        db.insert(EMPLOYEES_DAILY_TRAVEL_ALL_LOCATION_TABLE, null, cv);

        return 1;
    }

    public JSONArray getDailyTravelDataForCompensation() throws JSONException {

        JSONArray jsonArray = new JSONArray();

        ArrayList<DailyTravelModel> dailyTravelModelArrayList = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cur = db.rawQuery("select * from " + EMPLOYEES_DAILY_TRAVEL_ALL_LOCATION_TABLE + " where IsSynced=0", null);
        if (cur.getCount() > 0) {
            cur.moveToFirst();
            for (int i = 0; i < cur.getCount(); i++) {

                DailyTravelModel dailyTravelModel = new DailyTravelModel();
                dailyTravelModel.setIdLocation(cur.getString(cur.getColumnIndex("idLocation")));
                dailyTravelModel.setLatitude(cur.getDouble(cur.getColumnIndex("Latitude")));
                dailyTravelModel.setLongitude(cur.getDouble(cur.getColumnIndex("Longitude")));
                dailyTravelModel.setAddress(cur.getString(cur.getColumnIndex("Address")));
                dailyTravelModel.setDateTime(cur.getString(cur.getColumnIndex("DateTime")));
                dailyTravelModel.setIdEmployee(cur.getInt(cur.getColumnIndex("idEmployee")));
                dailyTravelModel.setIsForUpload(cur.getInt(cur.getColumnIndex("IsForUpload")));
                dailyTravelModel.setIsSynced(cur.getInt(cur.getColumnIndex("IsSynced")));
                dailyTravelModelArrayList.add(dailyTravelModel);

                cur.moveToNext();
            }


        }
        cur.close();

        for (DailyTravelModel dailyTravelModel :
                dailyTravelModelArrayList) {

            JSONObject jsonObject = new JSONObject();

            jsonObject.put("idLocation", dailyTravelModel.getIdLocation());
            jsonObject.put("Longitude", dailyTravelModel.getLongitude());
            jsonObject.put("Latitude", dailyTravelModel.getLatitude());
            jsonObject.put("Address", dailyTravelModel.getAddress());
            jsonObject.put("DateTime", dailyTravelModel.getDateTime());
            jsonObject.put("idEmployee", dailyTravelModel.getIdEmployee());


            jsonArray.put(jsonObject);

        }

        return jsonArray;

    }

    public ArrayList<DailyTravelModel> getDailyTravelDataForCompensationAsArray()
    {

        JSONArray jsonArray = new JSONArray();

        ArrayList<DailyTravelModel> dailyTravelModelArrayList = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cur = db.rawQuery("select * from " + EMPLOYEES_DAILY_TRAVEL_ALL_LOCATION_TABLE + " where IsSynced=0", null);
        if (cur.getCount() > 0) {
            cur.moveToFirst();
            for (int i = 0; i < cur.getCount(); i++) {

                DailyTravelModel dailyTravelModel = new DailyTravelModel();
                dailyTravelModel.setIdLocation(cur.getString(cur.getColumnIndex("idLocation")));
                dailyTravelModel.setLatitude(cur.getDouble(cur.getColumnIndex("Latitude")));
                dailyTravelModel.setLongitude(cur.getDouble(cur.getColumnIndex("Longitude")));
                dailyTravelModel.setAddress(cur.getString(cur.getColumnIndex("Address")));
                dailyTravelModel.setDateTime(cur.getString(cur.getColumnIndex("DateTime")));
                dailyTravelModel.setIdEmployee(cur.getInt(cur.getColumnIndex("idEmployee")));
                dailyTravelModel.setIsForUpload(cur.getInt(cur.getColumnIndex("IsForUpload")));
                dailyTravelModel.setIsSynced(cur.getInt(cur.getColumnIndex("IsSynced")));

                dailyTravelModelArrayList.add(dailyTravelModel);

                cur.moveToNext();
            }


        }
        cur.close();

        return dailyTravelModelArrayList;

    }

    public void deleteTravelCompensationGPSData() {

        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DELETE FROM " + EMPLOYEES_DAILY_TRAVEL_ALL_LOCATION_TABLE + " where IsSynced=1");


    }

    public void setAsSyncedTravelCompensationGPSData(ArrayList<DailyTravelModel> dailyTravelModelArrayList) {

        SQLiteDatabase db = this.getWritableDatabase();

        for (DailyTravelModel dailyTravelModel : dailyTravelModelArrayList) {
            db.execSQL("update " + EMPLOYEES_DAILY_TRAVEL_ALL_LOCATION_TABLE + " set IsSynced=1 where idLocation='" + dailyTravelModel.getIdLocation() + "'");
        }

        db.close();

    }

    public void insertEmployeeMaster(List<EmployeeMasterDetail> employeeMasterList)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        for (EmployeeMasterDetail employeeMasterDetail : employeeMasterList)
        {
            ContentValues cv = new ContentValues();
            cv.put("idEmployee", employeeMasterDetail.getIdEmployee());
            cv.put("employeeCode", employeeMasterDetail.getEmployeeCode());
            cv.put("employeeName", employeeMasterDetail.getEmployeeName());

            db.insert(EMPLOYEE_MASTER, null, cv);
        }
        db.close();
    }



    public void deleteEmployeeMaster()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + EMPLOYEE_MASTER);
    }

    public ArrayList<EmployeeMasterDetail> getEmployeeMaster()
    {
        ArrayList<EmployeeMasterDetail> employeeMasterArrayList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cur = db.rawQuery("select * from " + EMPLOYEE_MASTER + " order by employeeCode asc", null);
        cur.moveToFirst();

        for (int i = 0; i < cur.getCount(); i++)
        {
            EmployeeMasterDetail employeeMasterDetail = new EmployeeMasterDetail();
            employeeMasterDetail.setIdEmployee(cur.getInt(cur.getColumnIndex("idEmployee")));
            employeeMasterDetail.setEmployeeCode(cur.getString(cur.getColumnIndex("employeeCode")));
            employeeMasterDetail.setEmployeeName(cur.getString(cur.getColumnIndex("employeeName")));
            cur.moveToNext();

            employeeMasterArrayList.add(employeeMasterDetail);
        }

        return employeeMasterArrayList;
    }


    public void insertPurchaseOrderRequest(PurchaseOrderItemListJson purchaseOrderItemListJson)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Data data = purchaseOrderItemListJson.getData();
        List<PurchaseOrderItem> purchaseOrderItemList = data.getPurchaseOrderItemList();

        for (PurchaseOrderItem purchaseOrderItem:
                purchaseOrderItemList) {

            ContentValues cv = new ContentValues();
            cv.put("idItem",purchaseOrderItem.getIdItem());
            cv.put("idUnit",purchaseOrderItem.getIdUnit());
            cv.put("idPurchaseOrderDetails",purchaseOrderItem.getIdPurchaseOrderHeader());
            cv.put("Item",purchaseOrderItem.getItemName());
            cv.put("OrderQty",purchaseOrderItem.getOrderQuantity());
            cv.put("RackNo",purchaseOrderItem.getRackNumber());
            cv.put("Rate",purchaseOrderItem.getTotalAmount());
            cv.put("FloorNo",purchaseOrderItem.getFloor());
            cv.put("AcceptedQty",purchaseOrderItem.getAcceptedQuantity());
            cv.put("CreatedBy",shp.getString(Const.Shp_Employee_Code,""));


            Log.e("Log", "purchaseOrderItemList" + purchaseOrderItemList);
            db.insert(EMPLOYEE_MASTER, null, cv);

        }
        db.close();

    }

    public void deletePurchaseOrderRequest()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + SAVE_PURCHASE_ORDER_TABLE);
    }

    public void updateAcceptedQuantity(Integer idItem, double acceptedQty)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("update "+SAVE_PURCHASE_ORDER_TABLE+" set AcceptedQty="+acceptedQty+" where idItem="+idItem);


    }

    public JSONObject getSendPurchaseRequest(String createdBy, int idStatus, int idPurchaseOrder)
    {
        JSONObject finalJson = new JSONObject();
        try {
            finalJson.put("createdBy", createdBy);
            finalJson.put("idStatus", idStatus);
            Log.e("Log", "sendpurchasejson" + finalJson);

            JSONArray dataArray = new JSONArray();

            SQLiteDatabase db = this.getReadableDatabase();

            Cursor cur = db.rawQuery("select * from "+ SAVE_PURCHASE_ORDER_TABLE +" where idPurchaseOrder="+idPurchaseOrder+" and CreatedBy="+createdBy,null);

            if(cur.getCount() > 0)
            {

                cur.moveToFirst();

                for (int i = 0; i < cur.getCount(); i++) {

                    JSONObject singleDataObj = new JSONObject();
                    singleDataObj.put("idItem",cur.getInt(cur.getColumnIndex("idItem")));
                    singleDataObj.put("idUnit",cur.getInt(cur.getColumnIndex("idUnit")));
                    singleDataObj.put("Quantity",cur.getInt(cur.getColumnIndex("AcceptedQty")));
                    singleDataObj.put("idPurchaseOrderDetails",cur.getInt(cur.getColumnIndex("idPurchaseOrderDetails")));


                    dataArray.put(singleDataObj);
                    Log.e("Log", "dataarray"+dataArray);

                    cur.moveToNext();

                }

            }
            cur.close();

            finalJson.put("StockInTable",dataArray);
            Log.e("Log", "finalJson"+finalJson);

        } catch (JSONException e) {
            Log.e("Log", "exception" + e);
        }

        return finalJson;
    }


}