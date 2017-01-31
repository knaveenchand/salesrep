package com.frenzin.sales_rep;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.frenzin.sales_rep.common.LocationService;
import com.frenzin.sales_rep.common.ServiceHandler;
import com.frenzin.sales_rep.common.SessionManager;
import com.frenzin.sales_rep.common.Utils;
import com.frenzin.sales_rep.database.DatabaseHandler;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LoginActivity extends BaseActivity {

    @Bind(R.id.etUserName)
    EditText etUserName;
    @Bind(R.id.etPassword)
    EditText etPassword;
    @Bind(R.id.btnSignIn)
    ImageView btnSignIn;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        setPermissions();

        if (!new LocationService(this).canGetLocation()) {
            new LocationService(this).showSettingsAlert();
        }

        session = new SessionManager(this);
        if (session.isLoggedIn())
        {
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            i.putExtra("username", session.getName());
            startActivity(i);
            finish();
        }

        getSupportActionBar().hide();

        SQLiteDatabase db = new DatabaseHandler(getApplicationContext()).getWritableDatabase();

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etUserName.getText().toString().equals("") || etPassword.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Enter Username and Password", Toast.LENGTH_SHORT).show();
                } else if (!isConnectionAvailable()) {
                    Toast.makeText(getApplicationContext(), "Internet connection not available", Toast.LENGTH_SHORT).show();
                } else {
                    new AuthenticateLogin().execute();
                }
            }
        });
    }

    public class AuthenticateLogin extends AsyncTask<String, Void, String> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialog = new ProgressDialog(LoginActivity.this);
            dialog.setMessage("Please wait..");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            List<BasicNameValuePair> nameValuePairs = new ArrayList<>(3);
            nameValuePairs.add(new BasicNameValuePair("username", etUserName.getText().toString()));
            nameValuePairs.add(new BasicNameValuePair("password", etPassword.getText().toString()));
            nameValuePairs.add(new BasicNameValuePair("device_type", "1"));
            ServiceHandler sh = new ServiceHandler();
            return sh.makeServiceCall(Utils.url_login, ServiceHandler.GET, nameValuePairs);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("LoginResponse", "" + s);
            //dialog.dismiss();

            if (s != null) {
                try {
                    JSONObject obj = new JSONObject(s);
                    if (obj.getString("status").trim().equalsIgnoreCase("true")) {
                        Toast.makeText(getApplicationContext(), "Successfully logged in.", Toast.LENGTH_SHORT).show();
                        String id = obj.getString("id");
                        String f_name = obj.getString("firstname");
                        String l_name = obj.getString("lastname");
                        String phone = obj.getString("phone");
                        String username = obj.getString("username");
                        String email = obj.getString("email");
                        String role = obj.getString("role");

                        JSONArray jsonArray = obj.getJSONArray("retail_arr");
                        String r_id = null;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            r_id = object.getString("id");
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

                            DatabaseHandler db = new DatabaseHandler(LoginActivity.this);
                            if (!db.isExists(r_id)) {
                                db.addRetailerDetails(r_id, shop_name, shop_owner_name, address, sub_village, village,
                                        sub_dist, dist, lat, lng, r_phone, r_email, distance);
                            }
                        }
                        session.createLoginSession(id, username, role);
                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                        i.putExtra("username", etUserName.getText().toString());
                        startActivity(i);
                        overridePendingTransition(R.anim.right_in, R.anim.right_out);
                        finish();

                    } else {
                        dialog.dismiss();
                        Toast t = Toast.makeText(LoginActivity.this, obj.getString("msg"), Toast.LENGTH_LONG);
                        if (t != null)
                            t.show();
                    }
                } catch (JSONException e) {
                    dialog.dismiss();
                    e.printStackTrace();
                }
            } else {
                dialog.dismiss();
                Toast t = Toast.makeText(LoginActivity.this, "Something went wrong. Please try again later.", Toast.LENGTH_LONG);
                if (t != null)
                    t.show();
            }
        }
    }
}
