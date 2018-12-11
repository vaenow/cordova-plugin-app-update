package com.vaenow.appupdate.android;

import android.app.Activity;
import android.Manifest;
import android.os.Build;
import android.net.Uri;
import android.provider.Settings;
import android.content.Intent;
import android.content.pm.PackageManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.BuildHelper;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by LuoWen on 2015/10/27.
 */
public class CheckAppUpdate extends CordovaPlugin {
    public static final String TAG = "CheckAppUpdate";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("checkAppUpdate")) {
            getUpdateManager().options(args, callbackContext);
            if (verifyInstallPermission() && verifyOtherPermissions())
                getUpdateManager().checkUpdate();
            return true;
        }

        callbackContext.error(Utils.makeJSON(Constants.NO_SUCH_METHOD, "No such method: " + action));
        return false;
    }

    //////////
    // Update Manager
    //////////

    // UpdateManager singleton
    private UpdateManager updateManager = null;

    // Generate or retrieve the UpdateManager singleton
    public UpdateManager getUpdateManager() {
        if (updateManager == null)
            updateManager = new UpdateManager(cordova.getActivity(), cordova);

        return updateManager;
    }

    //////////
    // Permissions
    //////////

    private static final int INSTALL_PERMISSION_REQUEST_CODE = 0;
    private static final int UNKNOWN_SOURCES_PERMISSION_REQUEST_CODE = 1;
    private static final int OTHER_PERMISSIONS_REQUEST_CODE = 2;

    // Other necessary permissions for this plugin.
    private static String[] OTHER_PERMISSIONS = {
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    // Prompt user for install permission if we don't already have it.
    public boolean verifyInstallPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!cordova.getActivity().getPackageManager().canRequestPackageInstalls()) {
                String applicationId = (String) BuildHelper.getBuildConfigValue(cordova.getActivity(), "APPLICATION_ID");
                Uri packageUri = Uri.parse("package:" + applicationId);
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                    .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .setData(packageUri);
                cordova.setActivityResultCallback(this);
                cordova.getActivity().startActivityForResult(intent, INSTALL_PERMISSION_REQUEST_CODE);
                return false;
            }
        }
        else {
            try {
                if (Settings.Secure.getInt(cordova.getActivity().getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS) != 1) {
                    Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                    cordova.setActivityResultCallback(this);
                    cordova.getActivity().startActivityForResult(intent, UNKNOWN_SOURCES_PERMISSION_REQUEST_CODE);
                    return false;
                }
            }
            catch (Settings.SettingNotFoundException e) {}
        }

        return true;
    }

    // Prompt user for all other permissions if we don't already have them all.
    public boolean verifyOtherPermissions() {
        boolean hasOtherPermissions = true;
        for (String permission:OTHER_PERMISSIONS)
            hasOtherPermissions = hasOtherPermissions && cordova.hasPermission(permission);

        if (!hasOtherPermissions) {
            cordova.requestPermissions(this, OTHER_PERMISSIONS_REQUEST_CODE, OTHER_PERMISSIONS);
            return false;
        }

        return true;
    }

    // React to user's response to our request for install permission.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INSTALL_PERMISSION_REQUEST_CODE) {
            if (!cordova.getActivity().getPackageManager().canRequestPackageInstalls()) {
                getUpdateManager().permissionDenied("Permission Denied: " + Manifest.permission.REQUEST_INSTALL_PACKAGES);
                return;
            }

            if (verifyOtherPermissions())
                getUpdateManager().checkUpdate();
        }
        else if (requestCode == UNKNOWN_SOURCES_PERMISSION_REQUEST_CODE) {
            try {
                if (Settings.Secure.getInt(cordova.getActivity().getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS) != 1) {
                    getUpdateManager().permissionDenied("Permission Denied: " + Settings.Secure.INSTALL_NON_MARKET_APPS);
                    return;
                }
            }
            catch (Settings.SettingNotFoundException e) {}

            if (verifyOtherPermissions())
                getUpdateManager().checkUpdate();
        }
    }

    // React to user's response to our request for other permissions.
    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == OTHER_PERMISSIONS_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    getUpdateManager().permissionDenied("Permission Denied: " + permissions[i]);
                    return;
                }
            }

            getUpdateManager().checkUpdate();
        }
    }
}
