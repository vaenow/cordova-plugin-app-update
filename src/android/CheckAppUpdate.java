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

            UpdateManager updateManager = new UpdateManager(activity);
            // 检查软件更新
            updateManager.checkUpdate();
            /*Intent i = activity.getIntent();
            if (i.hasExtra(Intent.EXTRA_TEXT)) {
                callbackContext.success(i.getStringExtra(Intent.EXTRA_TEXT));
            } else {
                callbackContext.error("");
            }*/
            return true;
        }
        return false;
    }
}
