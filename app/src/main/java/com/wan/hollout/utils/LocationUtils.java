package com.wan.hollout.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 *@author Wan Clem
 */
public class LocationUtils {

    private static final String TAG = "LocationUtils";

    public static String getAddress(Context context, Location loc) {
        try {
            if (context != null) {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                List<Address> addresses;
                try {
                    addresses = geocoder.getFromLocation(loc.getLatitude(),
                            loc.getLongitude(), 1);
                } catch (IOException e1) {
                    Log.e(TAG, "IO Exception in getFromLocation()");
                    e1.printStackTrace();
                    return null;
                } catch (IllegalArgumentException e2) {
                    // Error message to post in the log
                    String errorString = "Illegal arguments " +
                            Double.toString(loc.getLatitude()) +
                            " , " +
                            Double.toString(loc.getLongitude()) +
                            " passed to address service";
                    Log.e(TAG, errorString);
                    e2.printStackTrace();
                    return null;
                }
                // If the reverse geocode returned an address
                if (addresses != null && addresses.size() > 0) {
                    // Get the first address
                    Address address = addresses.get(0);
                    String addressText = String.format(
                            "%s, %s, %s",
                            // If there's a street address, add it
                            address.getMaxAddressLineIndex() > 0 ?
                                    address.getAddressLine(0) : "",
                            // Locality is usually a city
                            address.getLocality(),
                            // The country of the address
                            address.getCountryName());
                    // Return the text
                    return addressText;
                } else {
                    return null;
                }
            }

        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public static String loadStaticMap(String lat,String lon) {
        String location = getLocationFromMessage(lat,lon);
        return "http://maps.googleapis.com/maps/api/staticmap?center=" + location
                + "&zoom=17&size=400x400&maptype=roadmap&format=png&visual_refresh=true&markers=" + location;
    }

    public static String getLocationFromMessage(String lat,String lon) {
        return lat + "," + lon;
    }

}
