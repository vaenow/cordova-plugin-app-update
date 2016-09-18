package com.vaenow.appupdate.android;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by LuoWen on 16/7/22.
 */
public class Utils {

    static JSONObject makeJSON(int code, Object msg) {
        JSONObject json = new JSONObject();

        try {
            json.put("code", code);
            json.put("msg", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }
}
