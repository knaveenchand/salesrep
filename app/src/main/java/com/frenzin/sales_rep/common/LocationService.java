package com.frenzin.sales_rep.common;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.frenzin.sales_rep.database.DatabaseHandler;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LocationService extends Service implements LocationListener {

    private final Context mContext;

    boolean isGPSEnabled = false;

    boolean isNetworkEnabled = false;

    boolean canGetLocation = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    ArrayList<RetailerItems> retailerData;
    ArrayList<String> distanceList;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;

    public LocationService(Context context) {
        this.mContext = context;
        getLocation();
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     */
    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(LocationService.this);
        }
    }

    /**
     * Function to get latitude
     */
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     * Function to get longitude
     */
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    /**
     * Function to check GPS/wifi enabled
     *
     * @return boolean
     */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        distanceList = new ArrayList<String>();
        String r_id = null;
        double newLat = 0, newLng = 0;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean isStart = prefs.getBoolean("isStart", false);
        boolean isClockIn = prefs.getBoolean("isClockIn", false);
        if (isStart) {
           /* if (!isClockIn) {
                retailerData = new DatabaseHandler(mContext).getAllRetailers();
                for (int i = 0; i < retailerData.size(); i++) {
                    LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                    LatLng retailer = new LatLng(Double.parseDouble(retailerData.get(i).getLat()),
                            Double.parseDouble(retailerData.get(i).getLng()));
                    Log.e("Auto", "RetailerLatLong:" + String.valueOf(retailer));
                    double distance = getDistance(current, retailer);
                    if (distance < Utils.distance) {
                        distanceList.clear();
                        while (distanceList.size() != 1) {
                            distanceList.add(String.valueOf(distance));
                            r_id = retailerData.get(i).getId();
                            newLat = Double.parseDouble(retailerData.get(i).getLat());
                            newLng = Double.parseDouble(retailerData.get(i).getLng());
                        }
                        prefs.edit().putInt("r_id", Integer.parseInt(r_id)).commit();
                        prefs.edit().putString("newLat", String.valueOf(newLat)).commit();
                        prefs.edit().putString("newLng", String.valueOf(newLng)).commit();
                    }
                    Log.e("Auto", "Distances::" + String.valueOf(distance));

                    if (distanceList.size() == 1) {
                        prefs.edit().putBoolean("isClockIn", true).commit();
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        Date curDate = new Date();
                        String datetime = format.format(curDate);
                        new DatabaseHandler(mContext).addEvent(new SessionManager(mContext).getId(), r_id, "1", datetime);
                    }
                }
            }*/
            if (isClockIn) {
                LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                LatLng retailerLtLng = new LatLng(Double.parseDouble(prefs.getString("newLat", "0")),
                        Double.parseDouble(prefs.getString("newLng", "0")));
                double distance = getDistance(current, retailerLtLng);
                Log.e("DistanceAuto", String.valueOf(distance));

                double d = Double.parseDouble(prefs.getString("distance", "0"));

                if (distance > d) {
                    prefs.edit().putBoolean("isClockIn", false).commit();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date curDate = new Date();
                    String datetime = format.format(curDate);
                    int r_id1 = prefs.getInt("r_id", 0);
                    Toast.makeText(mContext, "Auto Checked Out.", Toast.LENGTH_SHORT).show();
                    new DatabaseHandler(mContext).addEvent(new SessionManager(mContext).getId(), String.valueOf(r_id1), "2", datetime);
                }

                /*retailerData = new DatabaseHandler(this).getAllRetailers();
                for (int i = 0; i < retailerData.size(); i++) {
                    int d = Integer.parseInt(retailerData.get(i).getDistance());
                    Log.e("Distance", "FromDatabase" + String.valueOf(d));
                    if (distance > d) {
                        prefs.edit().putBoolean("isClockIn", false).commit();
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        Date curDate = new Date();
                        String datetime = format.format(curDate);
                        int r_id1 = prefs.getInt("r_id", 0);
                        new DatabaseHandler(mContext).addEvent(new SessionManager(mContext).getId(), String.valueOf(r_id1), "2", datetime);
                        break;
                    }
                }*/
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public double getDistance(LatLng LatLng1, LatLng LatLng2) {
        double distance = 0;
        Location locationA = new Location("A");
        locationA.setLatitude(LatLng1.latitude);
        locationA.setLongitude(LatLng1.longitude);
        Location locationB = new Location("B");
        locationB.setLatitude(LatLng2.latitude);
        locationB.setLongitude(LatLng2.longitude);
        distance = locationA.distanceTo(locationB);
        return distance;
    }

}