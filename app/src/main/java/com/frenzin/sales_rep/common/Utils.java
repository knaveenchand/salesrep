package com.frenzin.sales_rep.common;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;


public class Utils {
    //public static String PREFIX = "http://52.205.112.111/sales_mgt/apis/";
    //public static String PREFIX = "http://salesapp.matenek.com/salesmatenek/salesmatenek_webservice/";
    public static String PREFIX = "http://salesapp.matenek.com/salesmatenek/salesmatenek_webservice_1.0/";

    public static String url_login = PREFIX + "login.php";
    public static String url_logout = PREFIX + "logout.php";
    public static String url_change_status = PREFIX + "change_status.php";
    public static String url_add_customer = PREFIX + "add_customer.php";
    public static String url_edit_customer = PREFIX + "edit_customer.php";
    public static String url_list_representative = PREFIX + "list_representative.php";
    public static String url_assign_rep = PREFIX + "assign_rep.php";
    public static String url_assign_list = PREFIX + "assign_list.php";

    public static int distance = 100;

    public static String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }

    public static String getListValueFromJson(JSONObject jsonObject) {
        String toString = "";
        Iterator<String> s = jsonObject.keys();
        try {
            while (s.hasNext()) {
                String k = s.next();
                Log.e("Test", "s :" + k);
                toString += k + "=" + jsonObject.getString(k) + "&";

                //  params.add(new BasicNameValuePair(k, person.getString(k)));
            }
        } catch (JSONException ex) {

        }
        return toString;
    }

}
