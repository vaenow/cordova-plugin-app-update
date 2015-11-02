package com.xuexiq.appupdate;

import android.app.Activity;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by LuoWen on 2015/10/27.
 */
public class CheckAppUpdate extends CordovaPlugin {
    public static final String TAG = "CheckAppUpdate";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext)
            throws JSONException {
        LOG.d(TAG, "execute");

        Activity activity = this.cordova.getActivity();
        if (action.equals("checkAppUpdate")) {
            LOG.d(TAG, "action.equals");
            LOG.d(TAG, "args " + args);
            LOG.d(TAG, "args.length() " + args.length());

            if (args.length() == 0) {
                new UpdateManager(activity).checkUpdate();
            } else {
                String updateUrl = args.getString(0);
                LOG.d(TAG, "updateUrl " + updateUrl);
                LOG.d(TAG, "updateUrl.isEmpty() " + updateUrl.isEmpty());
                new UpdateManager(activity, updateUrl).checkUpdate();
            }
            callbackContext.success();
            return true;
        }
        callbackContext.error("no such method: " + action);
        return false;
    }
}
