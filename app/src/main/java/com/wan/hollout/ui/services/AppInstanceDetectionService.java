package com.wan.hollout.ui.services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.wan.hollout.api.JsonApiClient;
import com.wan.hollout.components.ApplicationLoader;
import com.wan.hollout.interfaces.DoneCallback;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AppStateManager;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.HolloutPreferences;
import com.wan.hollout.utils.HolloutUtils;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Wan Clem
 */

@SuppressWarnings("ConstantConditions")
public class AppInstanceDetectionService extends JobIntentService {

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 60000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * Callback for Location events.
     */
    private LocationCallback mLocationCallback;

    /**
     * Represents a geographical location.
     */
    private Location mCurrentLocation;

    private GetLocationTask getLocationTask;

    public static String TAG = AppInstanceDetectionService.class.getSimpleName();

    private static ParseObject signedInUser;
    private AppStateManager appStateManager;

    private AppStateManager.Listener myListener = new AppStateManager.Listener() {

        public void onBecameForeground() {
            signedInUser.put(AppConstants.APP_USER_ONLINE_STATUS, AppConstants.ONLINE);
            signedInUser.put(AppConstants.APP_USER_LAST_SEEN, System.currentTimeMillis());
            updateSignedInUserProps(false);
        }

        public void onBecameBackground() {
            //This is also a good place to blow new message notifications for a foregrounded app
            signedInUser.put(AppConstants.APP_USER_ONLINE_STATUS, AppConstants.OFFLINE);
            signedInUser.put(AppConstants.APP_USER_LAST_SEEN, System.currentTimeMillis());
            signedInUser.put(AppConstants.USER_CURRENT_TIME_STAMP, System.currentTimeMillis());
            updateSignedInUserProps(false);
            HolloutPreferences.destroyActivityCount();
        }

    };

    @Override
    public void onCreate() {
        super.onCreate();
        signedInUser = AuthUtil.getCurrentUser();
        appStateManager = AppStateManager.init(ApplicationLoader.getInstance());
        if (mFusedLocationClient == null) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        }
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        HolloutLogger.d(TAG, "OnHandleWorkCalled");

        if (mFusedLocationClient == null) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        }

        if (signedInUser == null) {
            signedInUser = AuthUtil.getCurrentUser();
        }
        if (appStateManager == null) {
            appStateManager = AppStateManager.init(ApplicationLoader.getInstance());
        }
        if (myListener != null) {
            try {
                AppStateManager.get(this).addListener(myListener);
            } catch (IllegalStateException ignored) {

            }
        }
        // Kick off the process of building the LocationCallback, LocationRequest, and
        if (HolloutPreferences.canAccessLocation()) {
            createLocationRequest();
            createLocationCallback();
            getLastLocation();
        } else {
            EventBus.getDefault().post(AppConstants.PLEASE_REQUEST_LOCATION_ACCESSS);
            stopSelf();
        }

    }

    /**
     * Removes location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void removeLocationUpdates() {
        HolloutLogger.i(TAG, "Removing location updates");
        try {
            if (mFusedLocationClient != null) {
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            }
            stopSelf();
        } catch (SecurityException unlikely) {
            HolloutLogger.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }

    private void getLastLocation() {
        try {
            if (mFusedLocationClient != null) {
                mFusedLocationClient.getLastLocation()
                        .addOnCompleteListener(new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                if (task.isSuccessful() && task.getResult() != null) {
                                    mCurrentLocation = task.getResult();
                                    cancelRunningLocationTaskBeforeRun(mCurrentLocation);
                                } else {
                                    HolloutLogger.w(TAG, "Failed to get location.");

                                }
                                startLocationUpdate();
                            }
                        });
            }
        } catch (SecurityException unlikely) {
            HolloutLogger.e(TAG, "Lost location permission." + unlikely);
        }
    }

    private void startLocationUpdate() {
        try {
            if (mFusedLocationClient != null) {
                mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                        mLocationCallback, Looper.myLooper());
            }
        } catch (SecurityException unlikely) {
            HolloutLogger.d(TAG, "Lost location permission. Could not request updates. " + unlikely);
        }
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mCurrentLocation = locationResult.getLastLocation();
                HolloutLogger.d(TAG, "A new location was received");
                if (mCurrentLocation != null) {
                    cancelRunningLocationTaskBeforeRun(mCurrentLocation);
                }
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (appStateManager != null && myListener != null) {
                AppStateManager.get(this).removeListener(myListener);
            }
        } catch (IllegalStateException | NullPointerException ignored) {

        }
        removeLocationUpdates();
    }

    private static class GetLocationTask extends AsyncTask<Location, Void, Void> {

        @SuppressLint("StaticFieldLeak")
        private Context context;
        // Create a list to contain the result address
        private List<Address> addresses;

        GetLocationTask(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(final Location... params) {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            // Get the current location from the input parameter list
            final Location loc = params[0];
            if (signedInUser != null) {
                ParseGeoPoint userGeoPoint = new ParseGeoPoint();
                userGeoPoint.setLatitude(loc.getLatitude());
                userGeoPoint.setLongitude(loc.getLongitude());
                HolloutLogger.d(TAG, "New Geo Points =  " + loc.getLatitude() + ", " + loc.getLongitude());
                signedInUser.put(AppConstants.APP_USER_GEO_POINT, userGeoPoint);
            }
            try {
                addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            } catch (IOException | IllegalArgumentException ignored) {

            }
            if (addresses != null && addresses.size() > 0 && signedInUser != null) {
                // Get the first address
                Address address = addresses.get(0);
                // COUNTRY
                final String countryName = address.getCountryName();
                //COUNTRY
                if (StringUtils.isNotEmpty(countryName)) {
                    HolloutLogger.d(TAG, "Country Name = " + countryName);
                    signedInUser.put(AppConstants.APP_USER_COUNTRY, HolloutUtils.stripDollar(countryName));
                }
                //STREET
                final String streetAddress = address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "";
                if (StringUtils.isNotEmpty(streetAddress)) {
                    HolloutLogger.d(TAG, "Street Name = " + streetAddress);
                    signedInUser.put(AppConstants.APP_USER_STREET, HolloutUtils.stripDollar(streetAddress));
                }
                //LOCALITY
                String locality = address.getLocality();
                if (StringUtils.isNotEmpty(locality)) {
                    HolloutLogger.d(TAG, "Locality Name = " + locality);
                    signedInUser.put(AppConstants.APP_USER_LOCALITY, HolloutUtils.stripDollar(locality));
                }
                //Admin
                String adminAddress = address.getAdminArea();
                if (StringUtils.isNotEmpty(adminAddress)) {
                    HolloutLogger.d(TAG, "Admin Name = " + adminAddress);
                    signedInUser.put(AppConstants.APP_USER_ADMIN_AREA, HolloutUtils.stripDollar(adminAddress));
                }
            }
            updateSignedInUserProps(true);
            return null;
        }

    }

    private void cancelRunningLocationTaskBeforeRun(Location location) {
        if (location != null) {
            if (getLocationTask != null && !getLocationTask.isCancelled()) {
                getLocationTask.cancel(true);
                getLocationTask = null;
            }
            getLocationTask = new GetLocationTask(this);
            getLocationTask.execute(location);
        }
    }

    private static void updateSignedInUserProps(final boolean sendPushNotification) {
        if (signedInUser != null) {
            AuthUtil.updateCurrentLocalUser(signedInUser, new DoneCallback<Boolean>() {
                @Override
                public void done(Boolean result, Exception e) {
                    if (e == null) {
                        if (sendPushNotification) {
                            sendAmNearbyPushNotification();
                        }
                    }
                }
            });
        }
    }

    private static void sendAmNearbyPushNotification() {
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        if (signedInUser != null) {
            String signedInUserId = signedInUser.getString(AppConstants.REAL_OBJECT_ID);
            List<String> savedUserChats = signedInUser.getList(AppConstants.APP_USER_CHATS);
            List<String> aboutUser = signedInUser.getList(AppConstants.ABOUT_USER);
            ArrayList<String> newUserChats = new ArrayList<>();
            final ParseQuery<ParseObject> peopleQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
            ParseGeoPoint signedInUserGeoPoint = signedInUser.getParseGeoPoint(AppConstants.APP_USER_GEO_POINT);
            if (signedInUserGeoPoint != null && aboutUser != null) {
                if (savedUserChats != null) {
                    if (!savedUserChats.contains(signedInUserId.toLowerCase())) {
                        savedUserChats.add(signedInUserId.toLowerCase());
                    }
                    peopleQuery.whereNotContainedIn(AppConstants.REAL_OBJECT_ID, savedUserChats);
                } else {
                    if (!newUserChats.contains(signedInUserId)) {
                        newUserChats.add(signedInUserId);
                    }
                    peopleQuery.whereNotContainedIn(AppConstants.REAL_OBJECT_ID, newUserChats);
                }
                peopleQuery.whereEqualTo(AppConstants.OBJECT_TYPE, AppConstants.OBJECT_TYPE_INDIVIDUAL);
                peopleQuery.whereContainedIn(AppConstants.ABOUT_USER, aboutUser);
                peopleQuery.whereWithinKilometers(AppConstants.APP_USER_GEO_POINT, signedInUserGeoPoint, 10.0);
                peopleQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> parseUsers, ParseException e) {
                        if (e == null && parseUsers != null && !parseUsers.isEmpty()) {
                            //Send notification to them one after the other
                            for (ParseObject user : parseUsers) {
                                String userFirebaseToken = user.getString(AppConstants.USER_FIREBASE_TOKEN);
                                if (StringUtils.isNotEmpty(userFirebaseToken)) {
                                    JsonApiClient.sendFirebasePushNotification(userFirebaseToken, AppConstants.NOTIFICATION_TYPE_AM_NEARBY);
                                }
                            }
                        }
                        peopleQuery.cancel();
                    }
                });
            }
        }

    }

}
