package com.wan.hollout.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.LinearLayout;

import com.wan.hollout.R;


/**
 * * @author Wan Clem
 */
public class HolloutPermissions {
    private Activity activity;
    private LinearLayout snackBarLayout;

    public HolloutPermissions(Activity activity, LinearLayout linearLayout) {
        this.activity = activity;
        this.snackBarLayout = linearLayout;
    }

    public void checkRuntimePermissionForStorage() {
        if (PermissionsUtils.checkSelfForStoragePermission(activity)) {
            requestStoragePermissions();
        }
    }

    public void checkRuntimePermissionForLocation() {
        if (PermissionsUtils.checkSelfPermissionForLocation(activity)) {
            requestLocationPermissions();
        } else {
//            ((MainActivity) activity).processLocation();
        }
    }

    public void requestStoragePermissions() {
        if (PermissionsUtils.shouldShowRequestForStoragePermission(activity)) {
            showSnackBar(R.string.storage_permission, PermissionsUtils.PERMISSIONS_STORAGE, PermissionsUtils.REQUEST_STORAGE);
        } else {
            PermissionsUtils.requestPermissions(activity, PermissionsUtils.PERMISSIONS_STORAGE, PermissionsUtils.REQUEST_STORAGE);
        }
    }

    public void requestLocationPermissions() {
        if (PermissionsUtils.shouldShowRequestForLocationPermission(activity)) {
            showSnackBar(R.string.location_permission, PermissionsUtils.PERMISSIONS_LOCATION, PermissionsUtils.REQUEST_LOCATION);
        } else {
            PermissionsUtils.requestPermissions(activity, PermissionsUtils.PERMISSIONS_LOCATION, PermissionsUtils.REQUEST_LOCATION);
        }
    }

    public void requestAudio() {
        if (PermissionsUtils.shouldShowRequestForLocationPermission(activity)) {
            showSnackBar(R.string.record_audio, PermissionsUtils.PERMISSIONS_LOCATION, PermissionsUtils.REQUEST_AUDIO_RECORD);
        } else {
            PermissionsUtils.requestPermissions(activity, PermissionsUtils.PERMISSIONS_RECORD_AUDIO, PermissionsUtils.REQUEST_AUDIO_RECORD);
        }
    }

    public void requestCallPermission() {
        if (PermissionsUtils.shouldShowRequestForCallPermission(activity)) {
            showSnackBar(R.string.phone_call_permission, PermissionsUtils.PERMISSION_CALL, PermissionsUtils.REQUEST_CALL_PHONE);
        } else {
            PermissionsUtils.requestPermissions(activity, PermissionsUtils.PERMISSION_CALL, PermissionsUtils.REQUEST_CALL_PHONE);
        }
    }

    public void requestCameraPermission() {
        if (PermissionsUtils.shouldShowRequestForCameraPermission(activity)) {
            showSnackBar(R.string.phone_camera_permission, PermissionsUtils.PERMISSION_CAMERA, PermissionsUtils.REQUEST_CAMERA);
        } else {
            PermissionsUtils.requestPermissions(activity, PermissionsUtils.PERMISSION_CAMERA, PermissionsUtils.REQUEST_CAMERA);
        }
    }

    public void requestContactPermission() {
        if (PermissionsUtils.shouldShowRequestForContactPermission(activity)) {
            showSnackBar(R.string.contact_permission, PermissionsUtils.PERMISSION_CONTACT, PermissionsUtils.REQUEST_CONTACT);
        } else {
            PermissionsUtils.requestPermissions(activity, PermissionsUtils.PERMISSION_CONTACT, PermissionsUtils.REQUEST_CONTACT);
        }
    }

    private Snackbar snackbar;

    private void showSnackBar(int resId, final String[] permissions, final int requestCode) {
        snackbar = Snackbar.make(snackBarLayout, resId,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(android.R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PermissionsUtils.requestPermissions(activity, permissions, requestCode);
                    }
                });
        snackbar.show();
    }

    public Snackbar getSnackBar() {
        return snackbar;
    }

    public boolean permissionsForSendingAndReceivingSMSNotGranted() {
        return ((ActivityCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) && ((ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED)) && ((ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED)) && ((ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)));
    }

    public void requestSendSmsAndReceivePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.SEND_SMS) || ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_SMS) || ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECEIVE_SMS)) {
            showSnackBar(R.string.sms_permission, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_PHONE_STATE}, RequestCodes.SEND_AND_RECEIVE_SMS_PERMISSION_CODE);
        } else {
            PermissionsUtils.requestPermissions(activity, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_PHONE_STATE}, RequestCodes.SEND_AND_RECEIVE_SMS_PERMISSION_CODE);
        }
    }

    public boolean verifyPermissions(int[] grantResults) {
        return (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
    }

}
