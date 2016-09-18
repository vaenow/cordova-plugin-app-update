package com.vaenow.appupdate.android;

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

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext)
            throws JSONException {

        if (action.equals("checkAppUpdate")) {
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
}
