package com.vaenow.appupdate.android;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class AuthenticationOptions {

    private static final String[] VALID_AUTH_TYPE = new String[] {"BASIC", "TOKEN"};

    private String authType;
    private String username;
    private String password;
    private String token;
    private String tokenPrefix;

    public AuthenticationOptions(JSONObject options) {
        try {
            this.setAuthType(options.getString("authType"));
            if (options.has("username")) {
                this.setUsername(options.getString("username"));
            }
            if (options.has("password")) {
                this.setPassword(options.getString("password"));
            }
            if (options.has("token")) {
                this.setToken(options.getString("token"));
            }
            if (options.has("tokenPrefix")) {
                this.setTokenPrefix(options.getString("tokenPrefix"));
            }
        } catch (JSONException e){
            // If there is any error then ensure that auth type is unset
            this.setAuthType("");
        }
    }

    /**
     * Flag indicating authentication credentials have been set
     *
     * @return boolean flag indicating if there are authentication credentials
     */
    public boolean hasCredentials() {
        return Arrays.asList(VALID_AUTH_TYPE).contains(authType.toUpperCase());
    }

    public String getEncodedAuthorization() {
        String header = "";

        switch (authType.toUpperCase()) {
            case "TOKEN":
                if (this.tokenPrefix != null && !this.tokenPrefix.isEmpty()) {
                    header = this.tokenPrefix + " ";
                }

                header += this.token;
                break;
            case "BASIC":
                header = "Basic " + Base64.encodeToString((this.username + ":" + this.password)
                        .getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
                break;
        }

        return header;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() { return this.token; }

    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }
}
