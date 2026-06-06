package com.happy.tracku.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.happy.tracku.gson.deliverypendinglist.DeliveryPending;
import com.happy.tracku.gson.employeemasterdetails.EmployeeMasterDetail;
import com.happy.tracku.gson.login.ValidateLoginResponseEmployeeDatum;
import com.happy.tracku.gson.login.ValidateLoginResponseVehicle;
import com.happy.tracku.gson.login.Validateloginresponsejson;
import com.happy.tracku.gson.masterdata.ItemMaster;
import com.happy.tracku.gson.masterdata.MasterDataJson;
import com.happy.tracku.gson.masterdata.UnitMaster;
import com.happy.tracku.gson.masterdata.VendorMaster;
import com.happy.tracku.gson.purchaseorderitemlist.Data;
import com.happy.tracku.gson.purchaseorderitemlist.PurchaseOrderItem;
import com.happy.tracku.gson.purchaseorderitemlist.PurchaseOrderItemListJson;
import com.happy.tracku.gson.stockoutpurchaseorderitemlist.StockOutPurchaseOrderItem;
import com.happy.tracku.gson.stockoutpurchaseorderitemlist.StockOutPurchaseOrderItemListJson;
import com.happy.tracku.models.DailyTravelModel;
import com.happy.tracku.utils.Const;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper
{
    public static final int DATABASE_VERSION = 12;
    public static final String DATABASE_NAME = "TrackUDb";
    public static final String EMPLOYEES_DAILY_TRAVEL_ALL_LOCATION_TABLE = "EmployeesDailyTravelAllLocation";
    public static final String EMPLOYEE_MASTER = "EmployeeDetails";
    public static final String SAVE_PURCHASE_ORDER_TABLE = "SavePurchaseDetails";
    public static final String SAVE_STOCKOUT_PURCHASE_ORDER_TABLE = "SaveStockOutPurchaseDetails";
    public static final String VENDOR_MASTER = "VendorMaster";
    public static final String ITEM_MASTER =  "ItemMaster";
    public static final String UNIT_MASTER = "UnitMaster";
    public static final String DELIVERY_PENDING_DETAILS_TABLE = "DeliveryPending";
    public static final String VALIDATE_LOGIN_EMPLOYEE_DATA = "ValidateLoginEmployeeData";
    public static final String VALIDATE_LOGIN_VEHICLE_DATA = "ValidateLoginVehicleData";
    public static final String SAVE_STOCKOUT_PURCHASE_ORDER_TABLE_NEW = "SaveStockOutPurchaseDetailsNew";

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

        db.execSQL("CREATE TABLE IF NOT EXISTS "+SAVE_PURCHASE_ORDER_TABLE+" (idItem INTEGER,Item TEXT, idUnit INTEGER, idPurchaseOrder INTEGER,OrderQty INTEGER, RackNo INTEGER, Rate DOUBLE, FloorNo TEXT, AcceptedQty Double, CreatedBy Text, IsVerified INTEGER, employeeCode INTEGER, idVehicle INTEGER)");

        db.execSQL("CREATE TABLE IF NOT EXISTS "+SAVE_STOCKOUT_PURCHASE_ORDER_TABLE+" (idItem INTEGER,Item TEXT, idUnit INTEGER, idPurchaseOrder INTEGER,OrderQty INTEGER, RackNo INTEGER, Rate DOUBLE, FloorNo TEXT, StockOutQuantity Double, CreatedBy Text, IsVerified INTEGER, employeeCode INTEGER, idVehicle INTEGER)");

        db.execSQL("CREATE TABLE IF NOT EXISTS "+VENDOR_MASTER+" (idVendor INTEGER,vendorName TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS "+ITEM_MASTER+" (idItem INTEGER,itemName TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS "+UNIT_MASTER+" (idUnit INTEGER,unitName TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS "+DELIVERY_PENDING_DETAILS_TABLE+" (idSalesHeader INTEGER,vendorName TEXT, salesDate TEXT, grandTotal DOUBLE,itemName TEXT, quantity DOUBLE, idSalesDetails INTEGER, idPaymentType INTEGER)");

        db.execSQL("CREATE TABLE IF NOT EXISTS "+VALIDATE_LOGIN_EMPLOYEE_DATA+" (employeeCode INTEGER,employeeName TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS "+VALIDATE_LOGIN_VEHICLE_DATA+" (idVehicle INTEGER, vehicleNumber TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS "+SAVE_STOCKOUT_PURCHASE_ORDER_TABLE_NEW+" (idItem INTEGER,Item TEXT, idUnit INTEGER, idSalesDetails INTEGER,orderQuantity DOUBLE, rackNumber TEXT, Rate DOUBLE, FloorNo TEXT, StockOutQuantity Double, CreatedBy Text, IsVerified INTEGER, employeeCode INTEGER, idVehicle INTEGER)");

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
        if (oldVersion <= 4)
        {
            db.execSQL("CREATE TABLE IF NOT EXISTS "+SAVE_STOCKOUT_PURCHASE_ORDER_TABLE+" (idItem INTEGER,Item TEXT, idUnit INTEGER, idPurchaseOrder INTEGER,OrderQty INTEGER, RackNo INTEGER, Rate DOUBLE, FloorNo TEXT, StockOutQuantity Double, CreatedBy Text)");

        }
        if (oldVersion <= 5)
        {
            db.execSQL("CREATE TABLE IF NOT EXISTS "+VENDOR_MASTER+" (idVendor INTEGER,vendorName TEXT)");
            db.execSQL("CREATE TABLE IF NOT EXISTS "+ITEM_MASTER+" (idItem INTEGER,itemName TEXT)");

        }

        if (oldVersion <= 6)
        {
            db.execSQL("CREATE TABLE IF NOT EXISTS "+UNIT_MASTER+" (idUnit INTEGER,unitName TEXT)");
        }
        if (oldVersion <= 7)
        {
            db.execSQL("ALTER TABLE " + SAVE_PURCHASE_ORDER_TABLE + " ADD IsVerified INTEGER") ;
            db.execSQL("ALTER TABLE " + SAVE_STOCKOUT_PURCHASE_ORDER_TABLE + " ADD IsVerified INTEGER") ;
        }

        if (oldVersion <= 8)
        {
            db.execSQL("CREATE TABLE IF NOT EXISTS "+DELIVERY_PENDING_DETAILS_TABLE+" (idSalesHeader INTEGER,vendorName TEXT, salesDate TEXT, grandTotal DOUBLE,itemName TEXT, quantity DOUBLE, idSalesDetails INTEGER)");

        }
        if (oldVersion <= 9)
        {
            db.execSQL("CREATE TABLE IF NOT EXISTS "+VALIDATE_LOGIN_EMPLOYEE_DATA+" (employeeCode INTEGER,employeeName TEXT)");

            db.execSQL("CREATE TABLE IF NOT EXISTS "+VALIDATE_LOGIN_VEHICLE_DATA+" (idVehicle INTEGER, vehicleNumber TEXT)");

        }
        if (oldVersion <= 10)
        {
            db.execSQL("ALTER TABLE " + SAVE_PURCHASE_ORDER_TABLE + " ADD employeeCode INTEGER") ;
            db.execSQL("ALTER TABLE " + SAVE_STOCKOUT_PURCHASE_ORDER_TABLE + " ADD employeeCode INTEGER") ;
            db.execSQL("ALTER TABLE " + SAVE_PURCHASE_ORDER_TABLE + " ADD idVehicle INTEGER") ;
            db.execSQL("ALTER TABLE " + SAVE_STOCKOUT_PURCHASE_ORDER_TABLE + " ADD idVehicle INTEGER") ;

        }
        if (oldVersion <= 11)
        {
            db.execSQL("CREATE TABLE IF NOT EXISTS "+SAVE_STOCKOUT_PURCHASE_ORDER_TABLE_NEW+" (idItem INTEGER,Item TEXT, idUnit INTEGER, idSalesDetails INTEGER,orderQuantity DOUBLE, rackNumber TEXT, Rate DOUBLE, FloorNo TEXT, StockOutQuantity Double, CreatedBy Text, IsVerified INTEGER, employeeCode INTEGER, idVehicle INTEGER)");

        }
        if (oldVersion <= 12)
        {
            db.execSQL("ALTER TABLE " + DELIVERY_PENDING_DETAILS_TABLE + " ADD idPaymentType INTEGER");
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
            cv.put("idPurchaseOrder",purchaseOrderItem.getIdPurchaseOrderHeader());
            cv.put("Item",purchaseOrderItem.getItemName());
            cv.put("OrderQty",purchaseOrderItem.getOrderQuantity());
            cv.put("RackNo",purchaseOrderItem.getRackNumber());
            cv.put("Rate",purchaseOrderItem.getTotalAmount());
            cv.put("FloorNo",purchaseOrderItem.getFloor());
            cv.put("AcceptedQty",purchaseOrderItem.getAcceptedQuantity());
            cv.put("CreatedBy",shp.getString(Const.Shp_Employee_Code,""));


            Log.e("Log", "purchaseOrderItemList" + purchaseOrderItemList);
            db.insert(SAVE_PURCHASE_ORDER_TABLE, null, cv);

        }
        //db.close();

    }

    public void deletePurchaseOrderRequest()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + SAVE_PURCHASE_ORDER_TABLE);
    }

    public void updateAcceptedQuantity(int idItem, double acceptedQty, int employeeCode, int idVehicle)
    {
        Log.e("Log", "updateAcceptedQuantity");
        Log.e("Log", "acceptedQtyDB" + acceptedQty);
        Log.e("Log", "idItemDB" + idItem);
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("update " + SAVE_PURCHASE_ORDER_TABLE + " set AcceptedQty="+acceptedQty+", IsVerified = 1, employeeCode = "+employeeCode+", idVehicle = "+idVehicle+" where idItem='" + idItem + "'");

       // db.execSQL("update "+SAVE_PURCHASE_ORDER_TABLE+" set AcceptedQty="+acceptedQty+" where idItem="+idItem);

        // db.execSQL("update "+SAVE_PURCHASE_ORDER_TABLE+" set AcceptedQty=acceptedQty where idItem='" + idItem + "'");

       // db.execSQL("update "+SAVE_PURCHASE_ORDER_TABLE+" set IsVerified = 1 where idItem='" + idItem + "'");

    }

    public void updateAcceptedQuantityVerified(int idItem)
    {
        Log.e("Log", "updateAcceptedQuantityVerified");
        Log.e("Log", "idItem"+idItem);

        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("update " + SAVE_PURCHASE_ORDER_TABLE + " set IsVerified = 1 where idItem='" + idItem + "'");

    }

    public JSONObject getSendPurchaseRequest(String createdBy, int idStatus, int idPurchaseOrder, String fileName, String base64, Integer companyValue, int employeeCode, int idVehicle)
    {
        JSONObject finalJson = new JSONObject();
        JSONArray dataArray = new JSONArray();
        try {

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
                    singleDataObj.put("idPurchaseOrderDetails",cur.getInt(cur.getColumnIndex("idPurchaseOrder")));
                    singleDataObj.put("employeeCode",cur.getInt(cur.getColumnIndex("employeeCode")));
                    singleDataObj.put("idVehicle",cur.getInt(cur.getColumnIndex("idVehicle")));


                    dataArray.put(singleDataObj);


                    cur.moveToNext();

                }

            }
            cur.close();

            Log.e("Log", "companyValue" + companyValue);
            Log.e("Log", "dataarray"+dataArray);
            finalJson.put("createdBy", createdBy);
            finalJson.put("idStatus", idStatus);
            finalJson.put("fileName", fileName);
            finalJson.put("photoUpload", base64);
            finalJson.put("StockInTable",dataArray);
            finalJson.put("idCompany", companyValue);
            finalJson.put("employeeCode", employeeCode);
            finalJson.put("idVehicle", idVehicle);
            Log.e("Log", "finalJsonDB"+finalJson);

        } catch (JSONException e) {
            Log.e("Log", "exception" + e);
        }

        return finalJson;
    }

    public void insertStockOutRequest(StockOutPurchaseOrderItemListJson stockOutPurchaseOrderItemListJson)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        com.happy.tracku.gson.stockoutpurchaseorderitemlist.Data data = stockOutPurchaseOrderItemListJson.getData();
        List<StockOutPurchaseOrderItem> stockOutPurchaseOrderItemList = data.getStockOutPurchaseOrderItemList();

        for (StockOutPurchaseOrderItem stockOutPurchaseOrderItem:
                stockOutPurchaseOrderItemList) {

            ContentValues cv = new ContentValues();
            cv.put("idItem",stockOutPurchaseOrderItem.getIdItem());
            cv.put("idUnit",stockOutPurchaseOrderItem.getIdUnit());
            cv.put("idSalesDetails",stockOutPurchaseOrderItem.getIdSalesDetails());
            cv.put("Item",stockOutPurchaseOrderItem.getItemName());
            cv.put("orderQuantity",stockOutPurchaseOrderItem.getOrderQuantity());
            cv.put("rackNumber",stockOutPurchaseOrderItem.getRackNumber());
            cv.put("Rate",stockOutPurchaseOrderItem.getTotalAmount());
            cv.put("FloorNo",stockOutPurchaseOrderItem.getFloor());
            cv.put("StockOutQuantity",stockOutPurchaseOrderItem.getStockOutQuantity());
            cv.put("CreatedBy",shp.getString(Const.Shp_Employee_Code,""));


            Log.e("Log", "stockoutpurchaseOrderItemList" + stockOutPurchaseOrderItemList);
            db.insert(SAVE_STOCKOUT_PURCHASE_ORDER_TABLE_NEW, null, cv);

        }
        //db.close();

    }

    public void deleteStockOutRequest()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + SAVE_STOCKOUT_PURCHASE_ORDER_TABLE_NEW);
    }

    public void updateStockOutQuantity(Integer idItem, double stockOutQuantity, int employeeCode, int idVehicle)
    {
        Log.e("Log", "updateStockoutQuantity");
        Log.e("Log", "stockoutQtyDB" + stockOutQuantity);
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("update "+SAVE_STOCKOUT_PURCHASE_ORDER_TABLE_NEW+" set StockOutQuantity="+stockOutQuantity+", employeeCode = "+employeeCode+", idVehicle = "+idVehicle+" where idItem='" + idItem + "'");


    }

    public void updateStockOutQuantityVerified(int idItem)
    {
        Log.e("Log", "updateStockoutQuantity");
        Log.e("Log", "idItemstockoutQtyDB" + idItem);
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("update "+SAVE_STOCKOUT_PURCHASE_ORDER_TABLE_NEW+" set IsVerified = 1 where idItem='" + idItem + "'");


    }
    public JSONObject getSendStockoutRequest(String createdBy, int idStatus, int idSalesDetails, String fileName, String base64, Integer companyValue, int employeeCode, int idVehicle)
    {
        JSONObject finalJson = new JSONObject();
        JSONArray dataArray = new JSONArray();
        try {

            SQLiteDatabase db = this.getReadableDatabase();

            Cursor cur = db.rawQuery("select * from "+ SAVE_STOCKOUT_PURCHASE_ORDER_TABLE_NEW +" where idSalesDetails="+idSalesDetails+" and CreatedBy="+createdBy,null);

            if(cur.getCount() > 0)
            {

                cur.moveToFirst();

                for (int i = 0; i < cur.getCount(); i++) {

                    JSONObject singleDataObj = new JSONObject();
                    singleDataObj.put("idItem",cur.getInt(cur.getColumnIndex("idItem")));
                    singleDataObj.put("idUnit",cur.getInt(cur.getColumnIndex("idUnit")));
                    singleDataObj.put("Quantity",cur.getInt(cur.getColumnIndex("StockOutQuantity")));
                    singleDataObj.put("idPurchaseOrderDetails",cur.getInt(cur.getColumnIndex("idSalesDetails")));
                    singleDataObj.put("employeeCode",cur.getInt(cur.getColumnIndex("employeeCode")));
                    singleDataObj.put("idVehicle",cur.getInt(cur.getColumnIndex("idVehicle")));


                    dataArray.put(singleDataObj);


                    cur.moveToNext();

                }

            }
            cur.close();

            finalJson.put("createdBy", createdBy);
            finalJson.put("idStatus", idStatus);
            finalJson.put("idCompany", companyValue);
            finalJson.put("fileName", fileName);
            finalJson.put("photoUpload", base64);
            finalJson.put("employeeCode", employeeCode);
            finalJson.put("idVehicle", idVehicle);
            finalJson.put("StockOutTable",dataArray);
            Log.e("Log", "sendstockoutjson" + finalJson);
            Log.e("Log", "dataarray"+dataArray);
            Log.e("Log", "finalJsonDB"+finalJson);

        } catch (JSONException e) {
            Log.e("Log", "exception" + e);
        }

        return finalJson;
    }

    public void insertMasterData(MasterDataJson masterDataJson)
    {
        Log.e("LogDB", "masterDataJson" + masterDataJson);

        SQLiteDatabase db = this.getWritableDatabase();

        if (masterDataJson.getData().getVendorMaster() != null)
        {
            List<VendorMaster> vendorMasterList = masterDataJson.getData().getVendorMaster();

            for (VendorMaster vendorMaster : vendorMasterList)
            {
                ContentValues cv = new ContentValues();
                cv.put("idVendor", vendorMaster.getIdVendor());
                cv.put("vendorName", vendorMaster.getVendorName());

                db.insert( VENDOR_MASTER , null, cv);
                Log.e("LogDB", "vendorMasterCV" + cv);
            }
        }

        if (masterDataJson.getData().getItemMaster() != null)
        {
            List<ItemMaster> itemMasterList = masterDataJson.getData().getItemMaster();

            for (ItemMaster itemMaster : itemMasterList)
            {
                ContentValues cv = new ContentValues();
                cv.put("idItem", itemMaster.getIdItem());
                cv.put("itemName", itemMaster.getItemName());


                db.insert( ITEM_MASTER , null, cv);
                Log.e("LogDB", "itemMasterCV" + cv);

            }
        }

        if (masterDataJson.getData().getUnitMaster() != null)
        {
            List<UnitMaster> unitMasterList = masterDataJson.getData().getUnitMaster();

            for (UnitMaster unitMaster : unitMasterList)
            {
                ContentValues cv = new ContentValues();
                cv.put("idUnit", unitMaster.getIdUnit());
                cv.put("unitName", unitMaster.getUnitName());


                db.insert( UNIT_MASTER , null, cv);
                Log.e("LogDB", "unitMasterCV" + cv);

            }
        }
    }

    public void deleteVendorMaster()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + VENDOR_MASTER);
    }

    public void deleteItemMaster()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + ITEM_MASTER);
    }

    public void deleteUnitMaster()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + UNIT_MASTER);
    }

    public ArrayList<VendorMaster> getVendorMaster()
    {
        ArrayList<VendorMaster> vendorMasterArrayList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cur = db.rawQuery("select idVendor,vendorName from " + VENDOR_MASTER + " order by vendorName asc", null);
        cur.moveToFirst();

        for (int i = 0; i < cur.getCount(); i++) {

            VendorMaster vendorMaster = new VendorMaster();

            vendorMaster.setIdVendor(cur.getInt(cur.getColumnIndex("idVendor")));
            vendorMaster.setVendorName(cur.getString(cur.getColumnIndex("vendorName")));


            cur.moveToNext();

            vendorMasterArrayList.add(vendorMaster);
        }

        return vendorMasterArrayList;

    }

    public ArrayList<ItemMaster> getItemMaster()
    {
        ArrayList<ItemMaster> itemMasterArrayList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cur = db.rawQuery("select idItem,itemName from " + ITEM_MASTER + " order by itemName asc", null);
        cur.moveToFirst();

        for (int i = 0; i < cur.getCount(); i++) {

            ItemMaster itemMaster = new ItemMaster();

            itemMaster.setIdItem(cur.getInt(cur.getColumnIndex("idItem")));
            itemMaster.setItemName(cur.getString(cur.getColumnIndex("itemName")));


            cur.moveToNext();

            itemMasterArrayList.add(itemMaster);
        }

        return itemMasterArrayList;

    }

    public ArrayList<UnitMaster> getUnitMaster()
    {
        ArrayList<UnitMaster> unitMasterArrayList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cur = db.rawQuery("select idUnit,unitName from " + UNIT_MASTER + " order by unitName asc", null);
        cur.moveToFirst();

        for (int i = 0; i < cur.getCount(); i++) {

            UnitMaster unitMaster = new UnitMaster();

            unitMaster.setIdUnit(cur.getInt(cur.getColumnIndex("idUnit")));
            unitMaster.setUnitName(cur.getString(cur.getColumnIndex("unitName")));


            cur.moveToNext();

            unitMasterArrayList.add(unitMaster);
        }

        return unitMasterArrayList;

    }

    public int getPendingPurchaseOrder()
    {
        int count = 0;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cur = db.rawQuery("select COUNT(*) from "+ SAVE_PURCHASE_ORDER_TABLE + " where IsVerified IS NULL", null);

        if (cur.getCount() > 0)
        {
            cur.moveToFirst();

            count = cur.getInt(0);
            Log.e("LogDB","countDB" + count);
        }

        cur.close();
        return count;
    }

    public int getPendingStockOutPurchaseOrder()
    {
        int count = 0;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cur = db.rawQuery("select COUNT(*) from "+ SAVE_STOCKOUT_PURCHASE_ORDER_TABLE_NEW + " where IsVerified IS NULL", null);

        if (cur.getCount() > 0)
        {
            cur.moveToFirst();

            count = cur.getInt(0);
            Log.e("LogDB","countDBSO" + count);
        }

        cur.close();
        return count;
    }

    public void insertDeliveryPendingDetails(List<DeliveryPending> deliveryPendingList)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        for (DeliveryPending deliveryPending : deliveryPendingList)
        {
            ContentValues cv = new ContentValues();
            cv.put("idSalesHeader", deliveryPending.getIdSalesHeader());
            cv.put("vendorName", deliveryPending.getVendorName());
            cv.put("salesDate", deliveryPending.getSalesDate());
            cv.put("grandTotal", deliveryPending.getGrandTotal());
            cv.put("itemName", deliveryPending.getItemName());
            cv.put("quantity", deliveryPending.getQuantity());
            cv.put("idSalesDetails", deliveryPending.getIdSalesDeliveryDetails());

            db.insert(DELIVERY_PENDING_DETAILS_TABLE, null, cv);
            Log.e("Log", "insertDeliveryPendingDetails" + cv);
        }

        db.close();
    }

    public void deleteDeliveryPendingDetails()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + DELIVERY_PENDING_DETAILS_TABLE);
    }

    public JSONObject getDeliveryPendingDetails(int idSalesHeader, String createdBy)
    {
        JSONObject finalJson = new JSONObject();
        JSONArray dataArray = new JSONArray();

        try {

            SQLiteDatabase db = this.getReadableDatabase();

            Cursor cur = db.rawQuery("select idSalesDetails, quantity, idPaymentType from "+ DELIVERY_PENDING_DETAILS_TABLE +" where idSalesHeader="+idSalesHeader,null);

            if(cur.getCount() > 0)
            {

                cur.moveToFirst();

                for (int i = 0; i < cur.getCount(); i++) {

                    JSONObject singleDataObj = new JSONObject();
                    singleDataObj.put("idDetail",cur.getInt(cur.getColumnIndex("idSalesDetails")));
                    singleDataObj.put("deliveredQty",cur.getDouble(cur.getColumnIndex("quantity")));
                    singleDataObj.put("idPaymentMode",cur.getInt(cur.getColumnIndex("idPaymentType")));

                    dataArray.put(singleDataObj);


                    cur.moveToNext();

                }

            }
            cur.close();

            finalJson.put("createdBy", createdBy);
            finalJson.put("id", idSalesHeader);
            finalJson.put("action", 1);
            finalJson.put("deliveryDetails",dataArray);
            Log.e("Log", "finalJsonDBDel"+finalJson);
            Log.e("Log", "dataarrayDel"+dataArray);

        } catch (JSONException e) {
            Log.e("Log", "exception" + e);
        }

        return finalJson;
    }

    public JSONObject getDeliveryPendingReportDetails(int idSalesHeader, String createdBy)
    {
        JSONObject finalJson = new JSONObject();
        JSONArray dataArray = new JSONArray();

        try {

            SQLiteDatabase db = this.getReadableDatabase();

            Cursor cur = db.rawQuery("select idSalesDetails, quantity from "+ DELIVERY_PENDING_DETAILS_TABLE +" where idSalesHeader="+idSalesHeader,null);

            if(cur.getCount() > 0)
            {

                cur.moveToFirst();

                for (int i = 0; i < cur.getCount(); i++) {

                    JSONObject singleDataObj = new JSONObject();
                    singleDataObj.put("idDetail",cur.getInt(cur.getColumnIndex("idSalesDetails")));
                    singleDataObj.put("deliveredQty",cur.getDouble(cur.getColumnIndex("quantity")));

                    dataArray.put(singleDataObj);


                    cur.moveToNext();

                }

            }
            cur.close();

            finalJson.put("createdBy", createdBy);
            finalJson.put("id", idSalesHeader);
            finalJson.put("action", 1);
            finalJson.put("deliveryDetails",dataArray);
            Log.e("Log", "finalJsonDBDel"+finalJson);
            Log.e("Log", "dataarrayDel"+dataArray);

        } catch (JSONException e) {
            Log.e("Log", "exception" + e);
        }

        return finalJson;
    }

    public void insertValidateMasterData(Validateloginresponsejson validateloginresponsejson)
    {
        Log.e("LogDB", "validateloginresponsejson" + validateloginresponsejson);

        SQLiteDatabase db = this.getWritableDatabase();

        if (validateloginresponsejson.getData().getValidateLoginResponseEmployeeData() != null)
        {
            List<ValidateLoginResponseEmployeeDatum> validateLoginResponseEmployeeDatumList = validateloginresponsejson.getData().getValidateLoginResponseEmployeeData();

            for (ValidateLoginResponseEmployeeDatum validateLoginResponseEmployeeDatum : validateLoginResponseEmployeeDatumList)
            {
                ContentValues cv = new ContentValues();
                cv.put("employeeCode", validateLoginResponseEmployeeDatum.getEmployeeCode());
                cv.put("employeeName", validateLoginResponseEmployeeDatum.getEmployeeName());

                db.insert( VALIDATE_LOGIN_EMPLOYEE_DATA , null, cv);
                Log.e("LogDB", "employeeMasterCV" + cv);
            }
        }

        if (validateloginresponsejson.getData().getValidateLoginResponseVehicle() != null)
        {
            List<ValidateLoginResponseVehicle> validateLoginResponseVehicleList = validateloginresponsejson.getData().getValidateLoginResponseVehicle();

            for (ValidateLoginResponseVehicle validateLoginResponseVehicle : validateLoginResponseVehicleList)
            {
                ContentValues cv = new ContentValues();
                cv.put("idVehicle", validateLoginResponseVehicle.getIdVehicle());
                cv.put("vehicleNumber", validateLoginResponseVehicle.getVehicleNumber());


                db.insert( VALIDATE_LOGIN_VEHICLE_DATA , null, cv);
                Log.e("LogDB", "vehicleMasterCV" + cv);

            }
        }

    }
    public void deleteLoginEmployeeData()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + VALIDATE_LOGIN_EMPLOYEE_DATA);
    }

    public void deleteLoginVehicleData()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + VALIDATE_LOGIN_VEHICLE_DATA);
    }

    public ArrayList<ValidateLoginResponseEmployeeDatum> getLoginEmployeeMaster()
    {
        ArrayList<ValidateLoginResponseEmployeeDatum> validateLoginResponseEmployeeDatumArrayList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cur = db.rawQuery("select * from " + VALIDATE_LOGIN_EMPLOYEE_DATA + " order by employeeCode asc", null);
        cur.moveToFirst();

        for (int i = 0; i < cur.getCount(); i++)
        {
            ValidateLoginResponseEmployeeDatum employeeMasterDetail = new ValidateLoginResponseEmployeeDatum();

            employeeMasterDetail.setEmployeeCode(cur.getInt(cur.getColumnIndex("employeeCode")));
            employeeMasterDetail.setEmployeeName(cur.getString(cur.getColumnIndex("employeeName")));
            cur.moveToNext();

            validateLoginResponseEmployeeDatumArrayList.add(employeeMasterDetail);
        }

        return validateLoginResponseEmployeeDatumArrayList;
    }

    public ArrayList<ValidateLoginResponseVehicle> getLoginVehicleMaster()
    {
        ArrayList<ValidateLoginResponseVehicle> validateLoginResponseVehicleArrayList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cur = db.rawQuery("select * from " + VALIDATE_LOGIN_VEHICLE_DATA + " order by idVehicle asc", null);
        cur.moveToFirst();

        for (int i = 0; i < cur.getCount(); i++)
        {
            ValidateLoginResponseVehicle validateLoginResponseVehicle = new ValidateLoginResponseVehicle();

            validateLoginResponseVehicle.setIdVehicle(cur.getInt(cur.getColumnIndex("idVehicle")));
            validateLoginResponseVehicle.setVehicleNumber(cur.getString(cur.getColumnIndex("vehicleNumber")));
            cur.moveToNext();

            validateLoginResponseVehicleArrayList.add(validateLoginResponseVehicle);
        }

        return validateLoginResponseVehicleArrayList;
    }


    public void updateDeliveryPaymentType(int idSalesHeader, int idPaymentType)
    {
        Log.e("Log", "updateDeliveryPaymentType");
        Log.e("Log", "idSalesHeaderDB" + idSalesHeader);
        Log.e("Log", "idPaymentTypeDB" + idPaymentType);
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("update " + DELIVERY_PENDING_DETAILS_TABLE + " set idPaymentType="+idPaymentType+" where idSalesHeader='" + idSalesHeader + "'");

    }
}