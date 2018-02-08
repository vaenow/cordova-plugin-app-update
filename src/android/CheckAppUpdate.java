package com.vaenow.appupdate.android;

import android.app.Activity;
import android.Manifest;
import android.content.pm.PackageManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

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
            if (verifyPermissions())
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
    public UpdateManager getUpdateManager() throws JSONException {
        if (updateManager == null)
            updateManager = new UpdateManager(cordova.getActivity(), cordova);

        return updateManager;
    }

    //////////
    // Permissions
    //////////

    // Necessary permissions for this plugin.
    private static final int PERMISSIONS_REQUEST_CODE = 0;
    private static String[] PERMISSIONS = {
        Manifest.permission.INTERNET,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.REQUEST_INSTALL_PACKAGES
    };

    // Prompt user for all necessary permissions if we don't already have them all.
    public boolean verifyPermissions() {
        boolean hasAllPermissions = true;
        for (String permission:PERMISSIONS)
            hasAllPermissions = hasAllPermissions && cordova.hasPermission(permission);

        if (hasAllPermissions)
            return true;

        cordova.requestPermissions(this, PERMISSIONS_REQUEST_CODE, PERMISSIONS);
        return false;
    }

    // React to user's response to our request for all necessary permissions.
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        for (int result:grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                getUpdateManager().permissionsDenied();
                return;
            }
        }

        if (requestCode == PERMISSIONS_REQUEST_CODE)
            getUpdateManager().checkUpdate();
    }
}
