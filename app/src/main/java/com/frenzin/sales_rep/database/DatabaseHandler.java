package com.frenzin.sales_rep.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.frenzin.sales_rep.common.EventItems;
import com.frenzin.sales_rep.common.RetailerItems;

import java.util.ArrayList;

public class DatabaseHandler extends SQLiteOpenHelper {

    // Retailer Table Fields
    public static final String KEY_RETAILER_ID = "id";
    public static final String KEY_SHOP_NAME = "shopname";
    public static final String KEY_OWNER_NAME = "shopownername";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_SUB_VILLAGE = "subvillage";
    public static final String KEY_VILLAGE = "village";
    public static final String KEY_SUB_DISTRICT = "subdistrict";
    public static final String KEY_DISTRICT = "district";
    public static final String KEY_LAT = "latitude";
    public static final String KEY_LONG = "longitude";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_DISTANCE = "distance";

    // Event Log Table Fields
    public static final String KEY_EVENT_ID = "id";
    public static final String KEY_S_ID = "sid";
    public static final String KEY_R_ID = "rid";
    public static final String KEY_EVENT = "event";
    public static final String KEY_EVENT_DATE_TIME = "date_time";

    // Database Name
    public static final String DATABASE_NAME = "salesrep";

    // Database Version
    public static final int DATABASE_VERSION = 1;

    // TABLES
    public static final String TABLE_RETAILER = "retailer_tbl";
    public static final String TABLE_EVENTLOG = "event_log_tbl";
    public static final String TABLE_AVAILABLE_RETAILER = "available_retailer_tbl";
    public static final String TABLE_INSERT_RETAILER = "insert_retailer_tbl";

    // Create Tables
    private static final String CREATE_RETAILER_TABLE = "create table "
            + TABLE_RETAILER + "( " + KEY_RETAILER_ID + " INTEGER PRIMARY KEY NOT NULL," + KEY_SHOP_NAME + " text NOT NULL,"
            + KEY_OWNER_NAME + " text," + KEY_ADDRESS + " text," + KEY_SUB_VILLAGE + " text," + KEY_VILLAGE + " text,"
            + KEY_SUB_DISTRICT + " text," + KEY_DISTRICT + " text," + KEY_LAT + " text NOT NULL," + KEY_LONG + " text NOT NULL,"
            + KEY_PHONE + " text," + KEY_EMAIL + " text," + KEY_DISTANCE + " INTEGER DEFAULT 100" + ")";

    private static final String CREATE_EVENT_LOG_TABLE = "create table "
            + TABLE_EVENTLOG + "( " + KEY_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_S_ID + " text," + KEY_R_ID + " text,"
            + KEY_EVENT + " text," + KEY_EVENT_DATE_TIME + " DATETIME" + ")";

    private static final String CREATE_AVAILABLE_RETAILER_TABLE = "create table "
            + TABLE_AVAILABLE_RETAILER + "( " + KEY_RETAILER_ID + " INTEGER PRIMARY KEY NOT NULL," + KEY_SHOP_NAME + " text NOT NULL,"
            + KEY_OWNER_NAME + " text," + KEY_ADDRESS + " text," + KEY_SUB_VILLAGE + " text," + KEY_VILLAGE + " text,"
            + KEY_SUB_DISTRICT + " text," + KEY_DISTRICT + " text," + KEY_LAT + " text NOT NULL," + KEY_LONG + " text NOT NULL,"
            + KEY_PHONE + " text," + KEY_EMAIL + " text," + KEY_DISTANCE + " INTEGER DEFAULT 100" + ")";

    private static final String CREATE_INSERT_RETAILER_TABLE = "create table "
            + TABLE_INSERT_RETAILER + "( " + KEY_SHOP_NAME + " text NOT NULL,"
            + KEY_OWNER_NAME + " text," + KEY_ADDRESS + " text," + KEY_SUB_VILLAGE + " text," + KEY_VILLAGE + " text,"
            + KEY_SUB_DISTRICT + " text," + KEY_DISTRICT + " text," + KEY_LAT + " text NOT NULL," + KEY_LONG + " text NOT NULL,"
            + KEY_PHONE + " text," + KEY_EMAIL + " text," + KEY_DISTANCE + " INTEGER DEFAULT 100" + ")";

    // Constructor
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_RETAILER_TABLE);
        db.execSQL(CREATE_EVENT_LOG_TABLE);
        db.execSQL(CREATE_AVAILABLE_RETAILER_TABLE);
        db.execSQL(CREATE_INSERT_RETAILER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RETAILER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTLOG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_AVAILABLE_RETAILER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INSERT_RETAILER);
        onCreate(db);
    }

    public void addRetailerDetails(String r_id, String shopname, String ownername,
                                   String address, String sub_village, String village,
                                   String sub_dist, String dist, String lat, String lng, String phone,
                                   String email, String distance) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cVal = new ContentValues();
        cVal.put(KEY_RETAILER_ID, r_id);
        cVal.put(KEY_SHOP_NAME, shopname);
        cVal.put(KEY_OWNER_NAME, ownername);
        cVal.put(KEY_ADDRESS, address);
        cVal.put(KEY_SUB_VILLAGE, sub_village);
        cVal.put(KEY_VILLAGE, village);
        cVal.put(KEY_SUB_DISTRICT, sub_dist);
        cVal.put(KEY_DISTRICT, dist);
        cVal.put(KEY_LAT, lat);
        cVal.put(KEY_LONG, lng);
        cVal.put(KEY_PHONE, phone);
        cVal.put(KEY_EMAIL, email);
        cVal.put(KEY_DISTANCE, distance);
        db.insert(TABLE_RETAILER, null, cVal);
        Log.e("inserteddetails:", r_id + ", " + address);
        db.close();
    }

    public void addEvent(String s_id, String r_id, String event, String datetime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cVal = new ContentValues();
        // cVal.put(KEY_EVENT_ID, id);
        cVal.put(KEY_S_ID, s_id);
        cVal.put(KEY_R_ID, r_id);
        cVal.put(KEY_EVENT, event);
        cVal.put(KEY_EVENT_DATE_TIME, datetime);
        db.insert(TABLE_EVENTLOG, null, cVal);
        Log.e("StoredEvents: ", s_id + "," + r_id + "," + event + "," + datetime);
        db.close();
    }

    public void addAvailableRetailers(String r_id, String shopname, String ownername,
                                      String address, String sub_village, String village,
                                      String sub_dist, String dist, String lat, String lng, String phone,
                                      String email, String distance) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cVal = new ContentValues();
        cVal.put(KEY_RETAILER_ID, r_id);
        cVal.put(KEY_SHOP_NAME, shopname);
        cVal.put(KEY_OWNER_NAME, ownername);
        cVal.put(KEY_ADDRESS, address);
        cVal.put(KEY_SUB_VILLAGE, sub_village);
        cVal.put(KEY_VILLAGE, village);
        cVal.put(KEY_SUB_DISTRICT, sub_dist);
        cVal.put(KEY_DISTRICT, dist);
        cVal.put(KEY_LAT, lat);
        cVal.put(KEY_LONG, lng);
        cVal.put(KEY_PHONE, phone);
        cVal.put(KEY_EMAIL, email);
        cVal.put(KEY_DISTANCE, distance);
        db.insert(TABLE_AVAILABLE_RETAILER, null, cVal);
        db.close();
    }

    public void insertRetailer(String shopname, String ownername, String address, String sub_village, String village,
                               String sub_dist, String dist, String lat, String lng, String phone, String email) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cVal = new ContentValues();
        //cVal.put(KEY_RETAILER_ID, r_id);
        cVal.put(KEY_SHOP_NAME, shopname);
        cVal.put(KEY_OWNER_NAME, ownername);
        cVal.put(KEY_ADDRESS, address);
        cVal.put(KEY_SUB_VILLAGE, sub_village);
        cVal.put(KEY_VILLAGE, village);
        cVal.put(KEY_SUB_DISTRICT, sub_dist);
        cVal.put(KEY_DISTRICT, dist);
        cVal.put(KEY_LAT, lat);
        cVal.put(KEY_LONG, lng);
        cVal.put(KEY_PHONE, phone);
        cVal.put(KEY_EMAIL, email);
      //  cVal.put(KEY_DISTANCE, distance);
        db.insert(TABLE_INSERT_RETAILER, null, cVal);
        Log.e("NewRetailer::", shopname + ", " + lat + ", " + lng);
        db.close();
    }

    public void deleteSingleCustomer(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_RETAILER + " WHERE " + KEY_RETAILER_ID + "= '" + id + "'");
        Log.e("Deleted", "Successfully");
        db.close();
    }


    public boolean isExists(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String Query = "Select * from " + TABLE_RETAILER + " where " + KEY_RETAILER_ID + " = " + id;
        Cursor cursor = db.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public boolean isDataExists(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String Query = "Select * from " + TABLE_AVAILABLE_RETAILER + " where " + KEY_RETAILER_ID + " = " + id;
        Cursor cursor = db.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_RETAILER);
        db.execSQL("delete from " + TABLE_EVENTLOG);
        db.execSQL("delete from " + TABLE_AVAILABLE_RETAILER);
        db.execSQL("delete from " + TABLE_INSERT_RETAILER);
        db.close();
    }

    public void deleteEvents() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_EVENTLOG);
        db.close();
    }

    public void deleteInsertedRetailers() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_INSERT_RETAILER);
        db.close();
    }

    public void deleteRetailers() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_RETAILER);
        db.close();
    }

    public ArrayList<RetailerItems> getAvailableRetailers() {
        ArrayList<RetailerItems> retailerItemsList = new ArrayList<RetailerItems>();
        String selectQuery = "SELECT  * FROM " + TABLE_AVAILABLE_RETAILER;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                RetailerItems item = new RetailerItems();
                item.setId(cursor.getString(0));
                item.setShop_name(cursor.getString(1));
                item.setShop_owner_name(cursor.getString(2));
                item.setAddress(cursor.getString(3));
                item.setSub_village(cursor.getString(4));
                item.setVillage(cursor.getString(5));
                item.setSub_dist(cursor.getString(6));
                item.setDist(cursor.getString(7));
                item.setLat(cursor.getString(8));
                item.setLng(cursor.getString(9));
                item.setR_phone(cursor.getString(10));
                item.setR_email(cursor.getString(11));
                item.setDistance(cursor.getString(12));
                retailerItemsList.add(item);
            } while (cursor.moveToNext());
        }

        return retailerItemsList;
    }

    public ArrayList<RetailerItems> getAllRetailers() {
        ArrayList<RetailerItems> retailerItemsList = new ArrayList<RetailerItems>();
        String selectQuery = "SELECT  * FROM " + TABLE_RETAILER;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                RetailerItems item = new RetailerItems();
                item.setId(cursor.getString(0));
                item.setShop_name(cursor.getString(1));
                item.setShop_owner_name(cursor.getString(2));
                item.setAddress(cursor.getString(3));
                item.setSub_village(cursor.getString(4));
                item.setVillage(cursor.getString(5));
                item.setSub_dist(cursor.getString(6));
                item.setDist(cursor.getString(7));
                item.setLat(cursor.getString(8));
                item.setLng(cursor.getString(9));
                item.setR_phone(cursor.getString(10));
                item.setR_email(cursor.getString(11));
                item.setDistance(cursor.getString(12));
                retailerItemsList.add(item);
            } while (cursor.moveToNext());
        }

        return retailerItemsList;
    }

    public ArrayList<RetailerItems> getNewRetailers() {
        ArrayList<RetailerItems> retailerItemsList = new ArrayList<RetailerItems>();
        String selectQuery = "SELECT  * FROM " + TABLE_INSERT_RETAILER;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                RetailerItems item = new RetailerItems();
                //item.setId(cursor.getString(0));
                item.setShop_name(cursor.getString(0));
                item.setShop_owner_name(cursor.getString(1));
                item.setAddress(cursor.getString(2));
                item.setSub_village(cursor.getString(3));
                item.setVillage(cursor.getString(4));
                item.setSub_dist(cursor.getString(5));
                item.setDist(cursor.getString(6));
                item.setLat(cursor.getString(7));
                item.setLng(cursor.getString(8));
                item.setR_phone(cursor.getString(9));
                item.setR_email(cursor.getString(10));
                item.setDistance(cursor.getString(11));
                retailerItemsList.add(item);
            } while (cursor.moveToNext());
        }

        return retailerItemsList;
    }

    public ArrayList<EventItems> getAllEvents() {
        ArrayList<EventItems> eventItemsArrayList = new ArrayList<EventItems>();
        String selectQuery = "SELECT  * FROM " + TABLE_EVENTLOG;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                EventItems item = new EventItems();
                item.setEvent_id(cursor.getString(0));
                item.setS_id(cursor.getString(1));
                item.setR_id(cursor.getString(2));
                item.setEvent(cursor.getString(3));
                item.setDatetime(cursor.getString(4));
                eventItemsArrayList.add(item);
            } while (cursor.moveToNext());
        }

        return eventItemsArrayList;
    }

}
