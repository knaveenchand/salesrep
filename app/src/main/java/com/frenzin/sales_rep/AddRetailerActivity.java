package com.frenzin.sales_rep;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.frenzin.sales_rep.adapters.RepresentativeListAdapter;
import com.frenzin.sales_rep.common.LocationService;
import com.frenzin.sales_rep.common.RepreItems;
import com.frenzin.sales_rep.common.RetailerItems;
import com.frenzin.sales_rep.common.ServiceHandler;
import com.frenzin.sales_rep.common.SessionManager;
import com.frenzin.sales_rep.common.Utils;
import com.frenzin.sales_rep.database.DatabaseHandler;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AddRetailerActivity extends BaseActivity implements RepresentativeListAdapter.SelectedItem {

    @Bind(R.id.mapView)
    MapView mapView;
    @Bind(R.id.etShopName)
    EditText etShopName;
    @Bind(R.id.etOwnerName)
    EditText etOwnerName;
    @Bind(R.id.etAddress)
    EditText etAddress;
    @Bind(R.id.etLat)
    EditText etLat;
    @Bind(R.id.etLng)
    EditText etLng;
    @Bind(R.id.etVillage)
    EditText etVillage;
    @Bind(R.id.etSubVillage)
    EditText etSubVillage;
    @Bind(R.id.etDistrict)
    EditText etDistrict;
    @Bind(R.id.etSubDistrict)
    EditText etSubDistrict;
    @Bind(R.id.etPhone)
    EditText etPhone;
    @Bind(R.id.etEmail)
    EditText etEmail;
    @Bind(R.id.etDistance)
    EditText etDistance;
    @Bind(R.id.btnSubmit)
    Button btnSubmit;
    @Bind(R.id.rlMap)
    RelativeLayout rlMap;

    GoogleMap map;
    double latitude, longitude;
    DatabaseHandler databaseHandler;
    ArrayList<RepreItems> repreItemsArrayList;
    AlertDialog alertDialog;
    public static int pos = -1;
    String rep_id, rep_name, customer_id;
    boolean toEdit = false;
    String c_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_retailer);
        ButterKnife.bind(this);

        mapView.onCreate(savedInstanceState);
        setActionbar();
        setMap();

        if (getIntent().getExtras() != null) {
            toEdit = true;
            Log.e("EditCustomer", "EditCustomer");
            c_id = getIntent().getStringExtra("cid");
            Log.e("CID", c_id);
            String shopName = getIntent().getStringExtra("shop_name");
            String ownerName = getIntent().getStringExtra("owner_name");
            String address = getIntent().getStringExtra("address");
            String lat = getIntent().getStringExtra("lat");
            String lng = getIntent().getStringExtra("lng");
            String subDist = getIntent().getStringExtra("sub_dist");
            String subVillage = getIntent().getStringExtra("sub_village");
            String village = getIntent().getStringExtra("village");
            String phone = getIntent().getStringExtra("phone");
            String email = getIntent().getStringExtra("email");
            String distance = getIntent().getStringExtra("distance");
            etShopName.setText(shopName);
            etOwnerName.setText(ownerName);
            etAddress.setText(address);
            etLat.setText(lat);
            etLng.setText(lng);
            etSubDistrict.setText(subDist);
            etSubVillage.setText(subVillage);
            etVillage.setText(village);
            etPhone.setText(phone);
            etEmail.setText(email);
            etDistance.setText(distance);
        }

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etShopName.getText().toString().equals("")) {
                    etShopName.setError("Required");
                    etShopName.requestFocus();
                } else if (etAddress.getText().toString().equals("")) {
                    etAddress.setError("Required");
                    etAddress.requestFocus();
                } else if (etLat.getText().toString().equals("")) {
                    etLat.setError("Required");
                    etLat.requestFocus();
                } else if (etLng.getText().toString().equals("")) {
                    etLng.setError("Required");
                    etLng.requestFocus();
                } else {
                    databaseHandler = new DatabaseHandler(AddRetailerActivity.this);
                    if (!toEdit) {
                        databaseHandler.insertRetailer(etShopName.getText().toString(), etOwnerName.getText().toString(),
                                etAddress.getText().toString(), etSubVillage.getText().toString(), etVillage.getText().toString(),
                                etSubDistrict.getText().toString(), etDistrict.getText().toString(), etLat.getText().toString(),
                                etLng.getText().toString(), etPhone.getText().toString(), etEmail.getText().toString());
                    }
                    if (isConnectionAvailable()) {
                        if (!toEdit) {
                            new AddRetailer(AddRetailerActivity.this).execute(Utils.url_add_customer);
                        } else {
                            new EditCustomer(AddRetailerActivity.this).execute(Utils.url_edit_customer);
                        }
                    } else {
                        Toast.makeText(AddRetailerActivity.this, "Customer Added Successfully.", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                }
            }
        });

        if (new SessionManager(this).getRole().equalsIgnoreCase("Sales Manager")) {
            if (!toEdit) {
                etDistance.setText("100");
            }
            etDistance.setVisibility(View.VISIBLE);
        } else {
            etDistance.setVisibility(View.GONE);
        }
    }

    public class AddRetailer extends AsyncTask<String, Void, String> {

        String server_response;
        ProgressDialog dialog;
        Context context;

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
                urlConnection.setRequestProperty("Content-Type" ,"text/html; charset=utf-8");

                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                databaseHandler = new DatabaseHandler(context);
                ArrayList<RetailerItems> list = databaseHandler.getNewRetailers();
                try {
                    JSONObject object = new JSONObject();
                    JSONArray jsonArray = new JSONArray();
                    for (int i = 0; i < list.size(); i++) {
                        JSONObject obj = new JSONObject();
                        obj.put("shop_name", URLEncoder.encode(etShopName.getText().toString(), "utf-8"));
                        obj.put("shop_owner", URLEncoder.encode(etOwnerName.getText().toString(), "utf-8"));
                        obj.put("address", URLEncoder.encode(etAddress.getText().toString(), "utf-8"));
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
                        Toast.makeText(context, "Data Uploaded Successfully.", Toast.LENGTH_SHORT).show();
                        databaseHandler.deleteInsertedRetailers();
                        JSONArray jsonArray = obj.getJSONArray("customer_id");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            customer_id = object.getString("customer_id");
                            Log.e("CustomerId", customer_id);
                        }
                        if (!databaseHandler.isExists(customer_id)) {
                            databaseHandler.addRetailerDetails(customer_id, etShopName.getText().toString(), etOwnerName.getText().toString(),
                                    etAddress.getText().toString(), etSubVillage.getText().toString(), etVillage.getText().toString(),
                                    etSubDistrict.getText().toString(), etDistrict.getText().toString(), etLat.getText().toString(),
                                    etLng.getText().toString(), etPhone.getText().toString(), etEmail.getText().toString(),
                                    etDistance.getText().toString());
                        }

                        if (new SessionManager(AddRetailerActivity.this).getRole().equalsIgnoreCase("Sales Manager")) {
                            new ListRepresentative().execute();
                        } else {
                            rep_id = new SessionManager(context).getId();
                            new Assign(context).execute(customer_id, rep_id);
                        }
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

    public class EditCustomer extends AsyncTask<String, Void, String> {

        String server_response;
        ProgressDialog dialog;
        Context context;

        EditCustomer(Context context) {
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
                urlConnection.setRequestProperty("Content-Type" ,"text/html; charset=utf-8");
               // urlConnection.set
               // header('Content-Type: text/html; charset=utf-8');
                //urlConnection.setRequestProperty("Accept-Charset" , "UTF-8");
                //post.setHeader("Accept-Charset","utf-8");
                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("customer_id", c_id);
                   // URLEncoder.encode("بلد", "utf-8");
                    obj.put("shop_name", URLEncoder.encode(etShopName.getText().toString(), "utf-8"));
                    obj.put("shop_owner", URLEncoder.encode(etOwnerName.getText().toString(), "utf-8"));
                    obj.put("address", URLEncoder.encode(etAddress.getText().toString(), "utf-8"));
                    obj.put("lat", etLat.getText().toString());
                    obj.put("lng", etLng.getText().toString());
                    obj.put("village", etVillage.getText().toString());
                    obj.put("sub_village", etSubVillage.getText().toString());
                    obj.put("district", etDistrict.getText().toString());
                    obj.put("sub_district", etSubDistrict.getText().toString());
                    obj.put("phone", etPhone.getText().toString());
                    obj.put("email", etEmail.getText().toString());
                    obj.put("distance", etDistance.getText().toString());

                    wr.writeBytes( obj.toString());
                    Log.e("JsonInputEditRetailer", obj.toString());
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
            Log.e("EditRetailerResponse", "" + server_response);

            if (server_response != null) {
                try {
                    JSONObject obj = new JSONObject(server_response);
                    if (obj.getString("status").trim().equalsIgnoreCase("true")) {
                        Toast.makeText(context, "Data Updated Successfully.", Toast.LENGTH_SHORT).show();
                        databaseHandler.deleteSingleCustomer(c_id);
                        if (!databaseHandler.isExists(customer_id)) {
                            databaseHandler.addRetailerDetails(c_id, etShopName.getText().toString(), etOwnerName.getText().toString(),
                                    etAddress.getText().toString(), etSubVillage.getText().toString(), etVillage.getText().toString(),
                                    etSubDistrict.getText().toString(), etDistrict.getText().toString(), etLat.getText().toString(),
                                    etLng.getText().toString(), etPhone.getText().toString(), etEmail.getText().toString(),
                                    etDistance.getText().toString());
                        }
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        prefs.edit().putString("distance", String.valueOf(etDistance.getText().toString())).commit();
                        new ListRepresentative().execute();
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

    public class ListRepresentative extends AsyncTask<String, Void, String> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(AddRetailerActivity.this);
            dialog.setMessage("Please wait..");
            dialog.setCancelable(false);
            dialog.show();

        }

        @Override
        protected String doInBackground(String... strings) {
            List<BasicNameValuePair> nameValuePairs = new ArrayList<>(1);
            nameValuePairs.add(new BasicNameValuePair("customer_id", c_id));
            return new ServiceHandler().makeServiceCall(Utils.url_list_representative, ServiceHandler.GET, nameValuePairs);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("ListRepreResponse", "" + s);

            if (s != null) {
                try {
                    JSONObject obj = new JSONObject(s);
                    if (obj.getString("status").trim().equalsIgnoreCase("true")) {
                        JSONArray jsonArray = obj.getJSONArray("sales_rep_arr");
                        repreItemsArrayList = new ArrayList<RepreItems>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            RepreItems repreItems = new RepreItems();
                            repreItems.setId(object.getString("id"));
                            repreItems.setName(object.getString("name"));
                            repreItems.setFlag(object.getString("flag"));
                            repreItemsArrayList.add(repreItems);
                        }
                        dialog.dismiss();
                        showList();
                    } else {
                        dialog.dismiss();
                        Toast t = Toast.makeText(AddRetailerActivity.this, "Something went wrong. Please try again later.", Toast.LENGTH_LONG);
                        if (t != null)
                            t.show();
                    }
                } catch (JSONException e) {
                    dialog.dismiss();
                    e.printStackTrace();
                }
            } else {
                dialog.dismiss();
                Toast t = Toast.makeText(AddRetailerActivity.this, "Something went wrong. Please try again later.", Toast.LENGTH_LONG);
                if (t != null)
                    t.show();
            }
        }
    }

    public void showList() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.alert_listview, null);
        alertDialogBuilder.setView(convertView);
        TextView title = (TextView) convertView.findViewById(R.id.title);
        title.setText("Assign Representative");
        ListView lv = (ListView) convertView.findViewById(R.id.lv);
        RepresentativeListAdapter adapter = new RepresentativeListAdapter(this, repreItemsArrayList);
        lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        adapter.setSelectedItem(this);
        lv.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        alertDialog = alertDialogBuilder.show();
    }

    @Override
    public void onItemSelected() {
        rep_id = repreItemsArrayList.get(pos).getId();
        rep_name = repreItemsArrayList.get(pos).getName();
        if (!toEdit) {
            new Assign(this).execute(customer_id, rep_id);
        } else {
            new Assign(this).execute(c_id, rep_id);
        }
        alertDialog.dismiss();
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
                   /*     Intent i = new Intent(context, MainActivity.class);
                        i.putExtra("username", new SessionManager(context).getName());
                        context.startActivity(i);*/
                        onBackPressed();
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

    public void setActionbar() {
        getSupportActionBar().setTitle("Add Customer");
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void setMap() {
        LocationService service = new LocationService(this);
        latitude = service.getLatitude();
        longitude = service.getLongitude();
        MapsInitializer.initialize(getApplicationContext());
        map = mapView.getMap();
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        LatLng latLng = new LatLng(latitude, longitude);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        Marker currLocationMarker = map.addMarker(markerOptions);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(14).build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        etLat.setText(String.valueOf(latitude));
        etLng.setText(String.valueOf(longitude));
        etLat.setEnabled(false);
        etLng.setEnabled(false);

        if (isConnectionAvailable()) {
            List<Address> addresses = null;
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String address = nullCheck(addresses.get(0).getAddressLine(0), 0);
            String city = nullCheck(addresses.get(0).getLocality(), 1);
            String postalCode = nullCheck(addresses.get(0).getPostalCode(), 1);
            etAddress.setText(address + city + " " + postalCode);
        } else {
            rlMap.setVisibility(View.GONE);
        }
    }

    public String nullCheck(String str, int a) {
        if (str == null) {
            return "";
        } else {
            if (a == 0)
                return str + ", ";
            else
                return str;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
