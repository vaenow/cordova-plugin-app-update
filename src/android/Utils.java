package com.vaenow.appupdate.android;

import org.json.JSONException;
import org.json.JSONObject;
import android.os.Handler;

import javax.net.ssl.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by LuoWen on 16/7/22.
 */
public class Utils {

    static SSLContext ctxAllowAll;

    static {
        try {
            ctxAllowAll = SSLContext.getInstance("TLS");
            ctxAllowAll.init(new KeyManager[0], new TrustManager[] {new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }}, new SecureRandom());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }
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

    static HttpURLConnection openConnection(String path, Handler handler, AuthenticationOptions authentication, boolean sslCheck) {
        HttpURLConnection conn = null;

        try {
            URL url = new URL(path);
            conn = (HttpURLConnection) url.openConnection();

            if (conn instanceof HttpsURLConnection && !sslCheck) {
                ((HttpsURLConnection) conn).setSSLSocketFactory(ctxAllowAll.getSocketFactory());
                ((HttpsURLConnection) conn).setHostnameVerifier((hostname, session) -> true);
            }

            if (authentication.hasCredentials()) {
                conn.setRequestProperty("Authorization", authentication.getEncodedAuthorization());
            }

            conn.setDoInput(true);
            conn.connect();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            handler.sendEmptyMessage(Constants.REMOTE_FILE_NOT_FOUND);
        } catch (IOException e) {
            e.printStackTrace();
            handler.sendEmptyMessage(Constants.NETWORK_ERROR);
        }

        return conn;
    }

}
