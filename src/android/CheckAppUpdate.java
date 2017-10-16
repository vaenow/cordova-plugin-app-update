package com.vaenow.appupdate.android;

import android.app.Activity;
import android.Manifest;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by LuoWen on 2015/10/27.
 */
public class CheckAppUpdate extends CordovaPlugin {
    public static final String TAG = "CheckAppUpdate";

    private UpdateManager updateManager = null;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE };

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext)
            throws JSONException {

        if (action.equals("checkAppUpdate")) {
            verifyStoragePermissions();
            getUpdateManager(args, callbackContext).checkUpdate();
            return true;
        }
        callbackContext.error(Utils.makeJSON(Constants.NO_SUCH_METHOD, "no such method: " + action));
        return false;
    }

    public UpdateManager getUpdateManager(JSONArray args, CallbackContext callbackContext)
            throws JSONException {

        if (this.updateManager == null) {
            this.updateManager = new UpdateManager(this.cordova.getActivity(), this.cordova);
        }

        return this.updateManager.options(args, callbackContext);
    }

    public void verifyStoragePermissions() {
        // Check if we have write permission
        // and if we don't prompt the user
        if (!cordova.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            cordova.requestPermissions(this, REQUEST_EXTERNAL_STORAGE, PERMISSIONS_STORAGE);
        }
    }
}
