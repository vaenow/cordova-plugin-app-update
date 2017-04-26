package com.vaenow.appupdate.android;

/**
 * Created by kendami on 2017/2/22.
 */

public class BaseModel {
    public boolean status;
    public boolean success;
    public int code;
    public String msg;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static class Error {
        public int code;
        public int http_code;
        public String message;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public int getHttp_code() {
            return http_code;
        }

        public void setHttp_code(int http_code) {
            this.http_code = http_code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
