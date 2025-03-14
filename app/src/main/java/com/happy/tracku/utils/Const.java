package com.happy.tracku.utils;

public class Const
{
    public static final String Shared_Pref_name = "TrackUPref";
    public static final String Shp_Employee_Code = "EmployeeCode";
    public static final String Shp_Employee_Name = "EmployeeName";
    public static final String Shp_Id_Employee = "IdEmployee";
    public static final String Shp_Token= "Token";
    public static final String Shp_Is_Admin = "IsAdmin";
    public static final String Shp_Is_LoggedIn = "IsLoggedIn";
    public static final String Shp_Version_No = "VersionNo";
    public static final String Shp_Android_Id = "AndroidId";

    public static final int LOCATION_REQUEST = 1000;
    public static final int GPS_REQUEST = 1001;


    public static final String URL_PUBLIC_TEST = "https://test1.maxvalue.co.in:1222/api/TrackVeDetails/";
   // public static final String URL_PUBLIC_LIVE = "https://yardmobapp.maxvalue.co.in:92/api/TrackVeDetails/";
    public static final String URL_PUBLIC_LIVE = "https://tracku.ecopackuae.com/api/TrackVeDetails/";

     public static String USING_IP = URL_PUBLIC_TEST;
  // public static String USING_IP = URL_PUBLIC_LIVE;

    public static final String URL_LOGIN = "TrackVeValidateLogin";
    public static final String URL_Update_Daily_GPS_Data = "UpdateDailyGPSData";
    public static final String URL_GET_DAILYWISE_EMPLOYEE_TRAVEL_DATA = "GetDailyWiseGPSData";
    public static final String URL_CREATE_EMPLOYEE = "InsertEmployeeDetails";
    public static final String URL_MANUAL_PUNCH = "PhotoPunching";
    public static final String URL_PUNCH_HISTORY = "GetPunchHistory";
    public static final String URL_EMPLOYEE_MASTER = "TrackVeMaster";
    public static final String URL_TRAVEL_LOG = "TravelLog";
    public static final String URL_PURCHASE_ORDER_LIST = "PurchaseOrderList";
    public static final String URL_PURCHASE_ORDER_ITEM_LIST= "PurchaseOrderItemList";
    public static final String URL_VENDOR_LIST = "VendorList";
    public static final String URL_SEND_PURCHASE_REQUEST = "SendPurchaseRequest";
}
