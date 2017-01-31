package com.frenzin.sales_rep;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.frenzin.sales_rep.adapters.ListAdapter;
import com.frenzin.sales_rep.common.EventItems;
import com.frenzin.sales_rep.common.LocationService;
import com.frenzin.sales_rep.common.RetailerItems;
import com.frenzin.sales_rep.common.ServiceHandler;
import com.frenzin.sales_rep.common.SessionManager;
import com.frenzin.sales_rep.common.Utils;
import com.frenzin.sales_rep.database.DatabaseHandler;
import com.google.android.gms.maps.model.LatLng;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements ListAdapter.SelectedItem, View.OnClickListener,
        LocationListener, SharedPreferences.OnSharedPreferenceChangeListener {

    @Bind(R.id.tvUsername)
    TextView tvUsername;
    @Bind(R.id.btnClockIn)
    ImageView btnClockIn;
    @Bind(R.id.btnStart)
    ImageView btnStart;

    String username;
    SessionManager sessionManager;
    double lat, lng;
    Timer myTimer;
    SharedPreferences prefs;
    LocationService locationService;
    ArrayList<Map<String, String>> distanceList;
    String r_id, datetime, date;
    double newLat, newLng, distance;
    SimpleDateFormat format, dateFormat;
    Date curDate;
    DatabaseHandler databaseHandler;
    ArrayList<RetailerItems> retailerData, data;
    boolean isStart, isClockIn;
    public static int pos = -1;
    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setElevation(0);

        sessionManager = new SessionManager(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        locationService = new LocationService(MainActivity.this);
        distanceList = new ArrayList<>();
        databaseHandler = new DatabaseHandler(this);
        curDate = new Date();
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        username = getIntent().getStringExtra("username");
        tvUsername.setText("Welcome, " + username);

        btnStart.setOnClickListener(this);
        btnClockIn.setOnClickListener(this);
        btnClockIn.setImageResource(R.drawable.btn_clockin_disabled);
        btnClockIn.setEnabled(false);

        isStart = prefs.getBoolean("isStart", false);
        if (isStart) {
            btnClockIn.setImageResource(R.drawable.btn_clockin);
            btnClockIn.setEnabled(true);
            btnStart.setImageResource(R.drawable.btn_stop);
            boolean isClockIn = prefs.getBoolean("isClockIn", false);
            if (isClockIn) {
                btnClockIn.setImageResource(R.drawable.btn_clockout);
            }
        }

        // call on every 2 minutes
        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                lat = locationService.getLatitude();
                lng = locationService.getLongitude();
                Log.e("Current ", "Lat" + String.valueOf(lat));
                Log.e("Current ", "Long" + String.valueOf(lng));
            }

        }, 0, 120000);

        prefs.registerOnSharedPreferenceChangeListener(this);

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnStart:
                isStart = prefs.getBoolean("isStart", false);
                isClockIn = prefs.getBoolean("isClockIn", false);
                if (!isStart) {
                    prefs.edit().putBoolean("isStart", true).commit();
                    if (isClockIn) {
                        btnClockIn.setImageResource(R.drawable.btn_clockout);
                    } else {
                        btnClockIn.setImageResource(R.drawable.btn_clockin);
                    }
                    btnClockIn.setEnabled(true);
                    curDate = new Date();
                    datetime = format.format(curDate);
                    databaseHandler.addEvent(sessionManager.getId(), "", "0", datetime);
                    btnStart.setImageResource(R.drawable.btn_stop);
                    if (isConnectionAvailable()) {
                        new UploadData().execute(Utils.url_change_status);
                    }

                } else {
                    prefs.edit().putBoolean("isStart", false).commit();
                    btnClockIn.setImageResource(R.drawable.btn_clockin_disabled);
                    btnClockIn.setEnabled(false);
                    curDate = new Date();
                    datetime = format.format(curDate);
                    databaseHandler.addEvent(sessionManager.getId(), "", "3", datetime);
                    btnStart.setImageResource(R.drawable.btn_start);
                    if (isConnectionAvailable()) {
                        new UploadData().execute(Utils.url_change_status);
                    }
                }
                break;
            case R.id.btnClockIn:
                distanceList.clear();
                isClockIn = prefs.getBoolean("isClockIn", false);
                if (!isClockIn) {
                    retailerData = databaseHandler.getAllRetailers();
                    for (int i = 0; i < retailerData.size(); i++) {
                        lat = locationService.getLatitude();
                        lng = locationService.getLongitude();
                        LatLng current = new LatLng(lat, lng);
                        LatLng retailer = new LatLng(Double.parseDouble(retailerData.get(i).getLat()),
                                Double.parseDouble(retailerData.get(i).getLng()));
                        Log.e("RetailerLatLong:", String.valueOf(retailer));
                        double distance = getDistance(current, retailer);
                        int distanceValue;
                        try {
                            distanceValue = Integer.parseInt(String.valueOf(retailerData.get(i).getDistance()));
                        } catch (NumberFormatException e) {
                            distanceValue = 100;
                            Log.e("Exception", "Called");
                        }
                        if (distance < distanceValue) {
                            Map<String, String> map = new HashMap<>();
                            map.put("distance", String.valueOf(distance));
                            map.put("rid", retailerData.get(i).getId());
                            map.put("lat", retailerData.get(i).getLat());
                            map.put("lng", retailerData.get(i).getLng());
                            map.put("distance", retailerData.get(i).getDistance());
                            distanceList.add(map);
                            String rid1 = retailerData.get(i).getId();
                            String shop_name = retailerData.get(i).getShop_name();
                            String shop_owner_name = retailerData.get(i).getShop_owner_name();
                            String address = retailerData.get(i).getAddress();
                            String sub_village = retailerData.get(i).getSub_village();
                            String village = retailerData.get(i).getVillage();
                            String sub_dist = retailerData.get(i).getSub_dist();
                            String dist = retailerData.get(i).getDist();
                            String lat = retailerData.get(i).getLat();
                            String lng = retailerData.get(i).getLng();
                            String r_phone = retailerData.get(i).getR_phone();
                            String r_email = retailerData.get(i).getR_email();
                            String distance1 = retailerData.get(i).getDistance();
                            if (!databaseHandler.isDataExists(rid1)) {
                                databaseHandler.addAvailableRetailers(rid1, shop_name, shop_owner_name, address, sub_village, village,
                                        sub_dist, dist, lat, lng, r_phone, r_email, distance1);
                            }
                        }
                        Log.e("Distances::", String.valueOf(distance));
                    }
                    Log.e("SizeOfDistanceList", String.valueOf(distanceList.size()));
                    if (distanceList.size() == 0) {
                        Toast.makeText(getApplicationContext(), "No Retailers Available", Toast.LENGTH_SHORT).show();
                    } else if (distanceList.size() == 1) {
                        r_id = distanceList.get(0).get("rid");
                        newLat = Double.parseDouble(distanceList.get(0).get("lat"));
                        newLng = Double.parseDouble(distanceList.get(0).get("lng"));
                        int dist;
                        try {
                            dist = Integer.parseInt(distanceList.get(0).get("distance"));
                        } catch (NumberFormatException e) {
                            dist = 100;
                        }
                        distance = dist;
                        prefs.edit().putInt("r_id", Integer.parseInt(r_id)).commit();
                        prefs.edit().putString("newLat", String.valueOf(newLat)).commit();
                        prefs.edit().putString("newLng", String.valueOf(newLng)).commit();
                        prefs.edit().putString("distance", String.valueOf(distance)).commit();
                        curDate = new Date();
                        datetime = format.format(curDate);
                        databaseHandler.addEvent(sessionManager.getId(), r_id, "1", datetime);
                        Toast.makeText(getApplicationContext(), "Retailer is Available. Clocked In Successfully.", Toast.LENGTH_SHORT).show();
                        prefs.edit().putBoolean("isClockIn", true).commit();
                        btnClockIn.setImageResource(R.drawable.btn_clockout);
                        if (isConnectionAvailable()) {
                            new UploadData().execute(Utils.url_change_status);
                        }
                    } else if (distanceList.size() > 1) {
                        showList();
                    } else {
                        Toast.makeText(getApplicationContext(), "Unable to find Retailer", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    curDate = new Date();
                    datetime = format.format(curDate);
                    databaseHandler.addEvent(sessionManager.getId(), r_id, "2", datetime);
                    prefs.edit().putBoolean("isClockIn", false).commit();
                    Toast.makeText(getApplicationContext(), "Clocked Out Successfully.", Toast.LENGTH_SHORT).show();
                    btnClockIn.setImageResource(R.drawable.btn_clockin);
                    if (isConnectionAvailable()) {
                        new UploadData().execute(Utils.url_change_status);
                    }
                }
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case R.id.nav_retailers:
                startActivity(new Intent(this, RetailerListActivity.class));
                overridePendingTransition(R.anim.right_in, R.anim.right_out);
                break;

            case R.id.nav_sync:
                if (isConnectionAvailable()) {
                    ArrayList<EventItems> eventList = databaseHandler.getAllEvents();
                    ArrayList<RetailerItems> list = databaseHandler.getNewRetailers();
                    /*if (eventList.size() == 0 && list.size() == 0) {
                        Toast.makeText(getApplicationContext(), "No Data to Sync.", Toast.LENGTH_SHORT).show();
                    }*/
                    if (eventList.size() != 0) {
                        new UploadData().execute(Utils.url_change_status);
                    }
                    if (list.size() != 0) {
                        new AddRetailer(MainActivity.this).execute(Utils.url_add_customer);
                    } else {
                        new AssignList(this).execute();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Internet connection not available", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.nav_logout:
                if (isConnectionAvailable()) {
                    isStart = prefs.getBoolean("isStart", false);
                    if (isStart) {
                        // Stop event
                        prefs.edit().putBoolean("isStart", false).commit();
                        btnClockIn.setEnabled(false);
                        curDate = new Date();
                        datetime = format.format(curDate);
                        databaseHandler.addEvent(sessionManager.getId(), "", "3", datetime);
                        // Upload previously stored data
                        ArrayList<EventItems> eventList1 = databaseHandler.getAllEvents();
                        if (eventList1.size() != 0) {
                            new UploadData().execute(Utils.url_change_status);
                        }
                        ArrayList<RetailerItems> list1 = databaseHandler.getNewRetailers();
                        if (list1.size() != 0) {
                            new AddRetailer(MainActivity.this).execute(Utils.url_add_customer);
                        }
                        new Logout().execute();
                    } else {
                        // Upload previously stored data
                        prefs.edit().putBoolean("isStart", true).commit();
                        ArrayList<EventItems> eventList1 = databaseHandler.getAllEvents();
                        if (eventList1.size() != 0) {
                            new UploadData().execute(Utils.url_change_status);
                        }
                        ArrayList<RetailerItems> list1 = databaseHandler.getNewRetailers();
                        if (list1.size() != 0) {
                            new AddRetailer(MainActivity.this).execute(Utils.url_add_customer);
                        }
                        new Logout().execute();

                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Internet connection not available", Toast.LENGTH_SHORT).show();
                }
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    public class UploadData extends AsyncTask<String, Void, String> {
        String server_response;
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("Please wait....");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestMethod("POST");

                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());

                dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                curDate = new Date();
                date = dateFormat.format(curDate);
                ArrayList<EventItems> list = databaseHandler.getAllEvents();
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("sales_rep_id", sessionManager.getId());
                    obj.put("date", date);
                    JSONArray jsonArray = new JSONArray();
                    for (int j = 0; j < list.size(); j++) {
                        JSONObject object = new JSONObject();
                        object.put("event", list.get(j).getEvent());
                        object.put("retail_id", list.get(j).getR_id());
                        Date date = null;
                        try {
                            date = format.parse(list.get(j).getDatetime());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                        String time = sdf.format(date);
                        Log.e("Time", time);
                        object.put("time", time);
                        jsonArray.put(object);
                    }
                    obj.put("event_arr", jsonArray);

                    wr.writeBytes(obj.toString());
                    Log.e("JSONInput", obj.toString());
                    wr.flush();
                    wr.close();
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    server_response = Utils.readStream(urlConnection.getInputStream());
                    Log.v("CatalogClient", server_response);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return server_response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("UploadDataResponse", "" + server_response);

            if (server_response != null) {
                try {
                    JSONObject obj = new JSONObject(server_response);
                    if (obj.getString("status").trim().equalsIgnoreCase("true")) {
                        Toast.makeText(MainActivity.this, "Data Uploaded Successfully.", Toast.LENGTH_SHORT).show();
                        databaseHandler.deleteEvents();
                        dialog.dismiss();
                    } else {
                        dialog.dismiss();
                        Toast t = Toast.makeText(MainActivity.this, "Something went wrong. Please try again later.", Toast.LENGTH_LONG);
                        if (t != null)
                            t.show();
                    }
                } catch (JSONException e) {
                    dialog.dismiss();
                    e.printStackTrace();
                }
            } else {
                dialog.dismiss();
                Toast t = Toast.makeText(MainActivity.this, "Something went wrong. Please try again later.", Toast.LENGTH_LONG);
                if (t != null)
                    t.show();
            }
        }
    }

    public class Logout extends AsyncTask<String, Void, String> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("Please wait..");
            dialog.setCancelable(false);
            dialog.show();

        }

        @Override
        protected String doInBackground(String... strings) {
            List<BasicNameValuePair> nameValuePairs = new ArrayList<>(1);
            nameValuePairs.add(new BasicNameValuePair("sales_rep_id", sessionManager.getId()));
            return new ServiceHandler().makeServiceCall(Utils.url_logout, ServiceHandler.GET, nameValuePairs);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("LogoutResponse", "" + s);

            if (s != null) {
                try {
                    JSONObject obj = new JSONObject(s);
                    if (obj.getString("status").trim().equalsIgnoreCase("true")) {
                        // Clear all data
                        myTimer.cancel();
                        sessionManager.logoutUser();
                        databaseHandler.deleteAll();
                        prefs.edit().clear().commit();
                        dialog.dismiss();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        overridePendingTransition(R.anim.right_in, R.anim.right_out);
                        finish();
                    } else {
                        dialog.dismiss();
                        Toast t = Toast.makeText(MainActivity.this, "Something went wrong. Please try again later.", Toast.LENGTH_LONG);
                        if (t != null)
                            t.show();
                    }
                } catch (JSONException e) {
                    dialog.dismiss();
                    e.printStackTrace();
                }
            } else {
                dialog.dismiss();
                Toast t = Toast.makeText(MainActivity.this, "Something went wrong. Please try again later.", Toast.LENGTH_LONG);
                if (t != null)
                    t.show();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        isStart = prefs.getBoolean("isStart", false);
        if (isStart) {
            btnClockIn.setEnabled(true);
            btnStart.setImageResource(R.drawable.btn_stop);
            boolean isClockIn = prefs.getBoolean("isClockIn", false);
            if (isClockIn) {
                btnClockIn.setImageResource(R.drawable.btn_clockout);
            } else {
                btnClockIn.setImageResource(R.drawable.btn_clockin);
            }
        }
    }

    public void showList() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.alert_listview, null);
        alertDialogBuilder.setView(convertView);
        ListView lv = (ListView) convertView.findViewById(R.id.lv);
        data = databaseHandler.getAvailableRetailers();
        ListAdapter adapter = new ListAdapter(this, data);
        lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        adapter.setSelectedItem(this);
        lv.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        alertDialog = alertDialogBuilder.show();
    }


    @Override
    public void onItemSelected() {
        try {
            r_id = distanceList.get(pos).get("rid");
            newLat = Double.parseDouble(distanceList.get(pos).get("lat"));
            newLng = Double.parseDouble(distanceList.get(pos).get("lng"));
            int dist;
            try {
                dist = Integer.parseInt(distanceList.get(pos).get("distance"));
            } catch (NumberFormatException e) {
                dist = 100;
            }
            distance = dist;
            prefs.edit().putInt("r_id", Integer.parseInt(r_id)).commit();
            prefs.edit().putString("newLat", String.valueOf(newLat)).commit();
            prefs.edit().putString("newLng", String.valueOf(newLng)).commit();
            prefs.edit().putString("distance", String.valueOf(distance)).commit();
            alertDialog.dismiss();
            curDate = new Date();
            datetime = format.format(curDate);
            databaseHandler.addEvent(sessionManager.getId(), r_id, "1", datetime);
            Toast.makeText(getApplicationContext(), "Clocked In Successfully.", Toast.LENGTH_SHORT).show();
            prefs.edit().putBoolean("isClockIn", true).commit();
            btnClockIn.setImageResource(R.drawable.btn_clockout);
            if (isConnectionAvailable()) {
                new UploadData().execute(Utils.url_change_status);
            }
        } catch (IndexOutOfBoundsException e) {
            Toast.makeText(getApplicationContext(), "This retailer is not available. Select another retailer.", Toast.LENGTH_SHORT).show();
        }
    }


    public class AddRetailer extends AsyncTask<String, Void, String> {

        String server_response;
        ProgressDialog dialog;
        Context context;
        ArrayList<RetailerItems> list;
        ArrayList<String> idList;

        AddRetailer(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(context);
            dialog.setMessage("Please wait....");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestMethod("POST");

                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                databaseHandler = new DatabaseHandler(context);
                list = databaseHandler.getNewRetailers();
                try {
                    JSONObject object = new JSONObject();
                    JSONArray jsonArray = new JSONArray();
                    for (int i = 0; i < list.size(); i++) {
                        JSONObject obj = new JSONObject();
                        obj.put("shop_name", list.get(i).getShop_name());
                        obj.put("shop_owner", list.get(i).getShop_owner_name());
                        obj.put("address", list.get(i).getAddress());
                        obj.put("lat", list.get(i).getLat());
                        obj.put("lng", list.get(i).getLng());
                        obj.put("village", list.get(i).getVillage());
                        obj.put("sub_village", list.get(i).getSub_village());
                        obj.put("district", list.get(i).getDist());
                        obj.put("sub_district", list.get(i).getSub_dist());
                        obj.put("phone", list.get(i).getR_phone());
                        obj.put("email", list.get(i).getR_email());
                        obj.put("distance", list.get(i).getDistance());
                        jsonArray.put(obj);
                    }
                    object.put("cust_arr", jsonArray);
                    wr.writeBytes(object.toString());
                    Log.e("JsonInputAddRetailer", object.toString());
                    wr.flush();
                    wr.close();
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    server_response = Utils.readStream(urlConnection.getInputStream());
                    Log.v("CatalogClient", server_response);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return server_response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("AddRetailerResponse", "" + server_response);

            if (server_response != null) {
                try {
                    JSONObject obj = new JSONObject(server_response);
                    if (obj.getString("status").trim().equalsIgnoreCase("true")) {
                        //Toast.makeText(context, "Data Uploaded Successfully.", Toast.LENGTH_SHORT).show();
                        databaseHandler.deleteInsertedRetailers();
                        JSONArray jsonArray = obj.getJSONArray("customer_id");
                        idList = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            idList.add(object.getString("customer_id"));
                        }
                        for (int i = 0; i < list.size(); i++) {
                            String customer_id = idList.get(i);
                            String shop_name = list.get(i).getShop_name();
                            String shop_owner_name = list.get(i).getShop_owner_name();
                            String address = list.get(i).getAddress();
                            String sub_village = list.get(i).getSub_village();
                            String village = list.get(i).getVillage();
                            String sub_dist = list.get(i).getSub_dist();
                            String dist = list.get(i).getDist();
                            String lat = list.get(i).getLat();
                            String lng = list.get(i).getLng();
                            String r_phone = list.get(i).getR_phone();
                            String r_email = list.get(i).getR_email();
                            String distance = list.get(i).getDistance();
                            Log.e("Stored", "CustomerIds : " + customer_id);
                            if (!databaseHandler.isExists(customer_id)) {
                                databaseHandler.addRetailerDetails(customer_id, shop_name, shop_owner_name,
                                        address, sub_village, village, sub_dist, dist, lat, lng, r_phone, r_email, distance);
                            }
                            String rep_id = new SessionManager(context).getId();
                            new Assign(context).execute(customer_id, rep_id);
                        }
                       /* if (new SessionManager(MainActivity.this).getRole().equalsIgnoreCase("Sales Manager")) {
                            new ListRepresentative().execute();
                        } else {
                            String rep_id = new SessionManager(context).getId();
                            new Assign(context).execute(customer_id, rep_id);
                        }*/

                      /*  String rep_id = new SessionManager(context).getId();
                        new Assign(context).execute(customer_id, rep_id);*/

                        dialog.dismiss();
                    } else {
                        dialog.dismiss();
                        Toast t = Toast.makeText(context, "Unable to connect. Please try again later.", Toast.LENGTH_LONG);
                        if (t != null)
                            t.show();
                    }
                } catch (JSONException e) {
                    dialog.dismiss();
                    e.printStackTrace();
                }
            } else {
                dialog.dismiss();
                Toast t = Toast.makeText(context, "Unable to connect. Please try again later.", Toast.LENGTH_LONG);
                if (t != null)
                    t.show();
            }
        }
    }

    public class Assign extends AsyncTask<String, Void, String> {

        ProgressDialog dialog;
        Context context;

        Assign(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(context);
            dialog.setMessage("Please wait..");
            dialog.setCancelable(false);
            dialog.show();

        }

        @Override
        protected String doInBackground(String... strings) {
            List<BasicNameValuePair> nameValuePairs = new ArrayList<>(2);
            nameValuePairs.add(new BasicNameValuePair("customer_id", strings[0]));
            nameValuePairs.add(new BasicNameValuePair("rep_id", strings[1]));
            Log.e("InputAssign:", nameValuePairs.toString());
            return new ServiceHandler().makeServiceCall(Utils.url_assign_rep, ServiceHandler.GET, nameValuePairs);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("AssignResponse", "" + s);

            if (s != null) {
                try {
                    JSONObject obj = new JSONObject(s);
                    if (obj.getString("status").trim().equalsIgnoreCase("true")) {
                        Toast.makeText(context, "Representative Assigned Successfully.", Toast.LENGTH_SHORT).show();
                        new AssignList(context).execute();
                        dialog.dismiss();
                    } else {
                        dialog.dismiss();
                        Toast t = Toast.makeText(context, "Something went wrong. Please try again later.", Toast.LENGTH_LONG);
                        if (t != null)
                            t.show();
                    }
                } catch (JSONException e) {
                    dialog.dismiss();
                    e.printStackTrace();
                }
            } else {
                dialog.dismiss();
                Toast t = Toast.makeText(context, "Something went wrong. Please try again later.", Toast.LENGTH_LONG);
                if (t != null)
                    t.show();
            }
        }
    }

    public class AssignList extends AsyncTask<String, Void, String> {

        ProgressDialog dialog;
        Context context;

        AssignList(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(context);
            dialog.setMessage("Please wait..");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            List<BasicNameValuePair> nameValuePairs = new ArrayList<>(1);
            nameValuePairs.add(new BasicNameValuePair("id", new SessionManager(context).getId()));
            Log.e("AssignListInput:", nameValuePairs.toString());
            return new ServiceHandler().makeServiceCall(Utils.url_assign_list, ServiceHandler.GET, nameValuePairs);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("AssignListResponse", "" + s);

            if (s != null) {
                try {
                    JSONObject obj = new JSONObject(s);
                    if (obj.getString("status").trim().equalsIgnoreCase("true")) {
                        Toast.makeText(context, "Data Updated.", Toast.LENGTH_SHORT).show();
                        JSONArray jsonArray = obj.getJSONArray("retail_arr");
                        DatabaseHandler db = new DatabaseHandler(context);
                        db.deleteRetailers();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            String customer_id = object.getString("id");
                            String shop_name = object.getString("shop_name");
                            String shop_owner_name = object.getString("shop_owner_name");
                            String address = object.getString("address");
                            String sub_village = object.getString("sub_village");
                            String village = object.getString("village");
                            String sub_dist = object.getString("sub_district");
                            String dist = object.getString("district");
                            String lat = object.getString("latitude");
                            String lng = object.getString("longitude");
                            String r_phone = object.getString("phone");
                            String r_email = object.getString("email");
                            String distance = object.getString("distance");

                            if (!db.isExists(customer_id)) {
                                db.addRetailerDetails(customer_id, shop_name, shop_owner_name, address, sub_village, village,
                                        sub_dist, dist, lat, lng, r_phone, r_email, distance);
                            }
                        }
                        dialog.dismiss();
                    } else {
                        dialog.dismiss();
                        Toast t = Toast.makeText(context, "Something went wrong. Please try again later.", Toast.LENGTH_LONG);
                        if (t != null)
                            t.show();
                    }
                } catch (JSONException e) {
                    dialog.dismiss();
                    e.printStackTrace();
                }
            } else {
                dialog.dismiss();
                Toast t = Toast.makeText(context, "Something went wrong. Please try again later.", Toast.LENGTH_LONG);
                if (t != null)
                    t.show();
            }
        }
    }
}
