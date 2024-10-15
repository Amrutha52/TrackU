package com.happy.tracku.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.happy.tracku.models.DailyTravelModel;
import com.happy.tracku.utils.Const;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DbHelper extends SQLiteOpenHelper
{
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "TrackUDb";
    public static final String EMPLOYEES_DAILY_TRAVEL_ALL_LOCATION_TABLE = "EmployeesDailyTravelAllLocation";
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

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS EmployeesDailyTravelAllLocation");
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
                dailyTravelModel.setLatitude(cur.getString(cur.getColumnIndex("Latitude")));
                dailyTravelModel.setLongitude(cur.getString(cur.getColumnIndex("Longitude")));
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
}