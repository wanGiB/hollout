package com.wan.hollout.utils;

/**
 *@author Wan Clem
 */
public class LocationUtils {

    private static final String TAG = "LocationUtils";

    public static String loadStaticMap(String lat,String lon) {
        String location = getLocationFromMessage(lat,lon);
        return "http://maps.googleapis.com/maps/api/staticmap?center=" + location
                + "&zoom=17&size=400x400&maptype=roadmap&format=png&visual_refresh=true&markers=" + location;
    }

    public static String getLocationFromMessage(String lat, String lon) {
        return lat + "," + lon;
    }

}
