package com.vaenow.appupdate.android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.widget.ProgressBar;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by LuoWen on 2015/10/27.
 * <p/>
 * Thanks @coolszy
 */
public class UpdateManager {
    public static final String TAG = "UpdateManager";

    /*
     * 远程的版本文件格式
     *   <update>
     *       <version>2222</version>
     *       <name>name</name>
     *       <url>http://192.168.3.102/android.apk</url>
     *   </update>
     */
    private String updateXmlUrl;
    private JSONObject options;
    private JSONArray args;
    private CordovaInterface cordova;
    private CallbackContext callbackContext;
    private String packageName;
    private Context mContext;
    private MsgBox msgBox;
    private Boolean isDownloading = false;
    private List<Version> queue = new ArrayList<Version>(1);
    private CheckUpdateThread checkUpdateThread;
    private DownloadApkThread downloadApkThread;

    public UpdateManager(Context context, CordovaInterface cordova) {
        this.cordova = cordova;
        this.mContext = context;
        packageName = mContext.getPackageName();
        msgBox = new MsgBox(mContext);
    }

    public UpdateManager(JSONArray args, CallbackContext callbackContext, Context context, JSONObject options) {
        this(args, callbackContext, context, "http://192.168.3.102:8080/update_apk/version.xml", options);
    }

    public UpdateManager(JSONArray args, CallbackContext callbackContext, Context context, String updateUrl, JSONObject options) {
        this.args = args;
        this.callbackContext = callbackContext;
        this.updateXmlUrl = updateUrl;
        this.options = options;
        this.mContext = context;
        packageName = mContext.getPackageName();
        msgBox = new MsgBox(mContext);
    }

    public UpdateManager options(JSONArray args, CallbackContext callbackContext)
            throws JSONException {
        this.args = args;
        this.callbackContext = callbackContext;
        this.updateXmlUrl = args.getString(0);
        this.options = args.getJSONObject(1);
        return this;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case Constants.NETWORK_ERROR:
                    //暂时隐藏错误
                    //msgBox.showErrorDialog(errorDialogOnClick);
                    callbackContext.error(Utils.makeJSON(Constants.NETWORK_ERROR, "network error"));
                    break;
                case Constants.VERSION_COMPARE_START:
                    compareVersions();
                    break;
                case Constants.DOWNLOAD_CLICK_START:
                    emitNoticeDialogOnClick();
                    break;
                case Constants.DOWNLOAD_FINISH:
                    isDownloading = false;
                    break;
                case Constants.VERSION_UPDATING:
                    callbackContext.success(Utils.makeJSON(Constants.VERSION_UPDATING, "success, version updating."));
                    break;
                case Constants.VERSION_NEED_UPDATE:
                    callbackContext.success(Utils.makeJSON(Constants.VERSION_NEED_UPDATE, "success, need date."));
                    break;
                case Constants.VERSION_UP_TO_UPDATE:
                    callbackContext.success(Utils.makeJSON(Constants.VERSION_UP_TO_UPDATE, "success, up to date."));
                    break;
                case Constants.VERSION_COMPARE_FAIL:
                    callbackContext.error(Utils.makeJSON(Constants.VERSION_COMPARE_FAIL, "version compare fail"));
                    break;
                case Constants.VERSION_RESOLVE_FAIL:
                    callbackContext.error(Utils.makeJSON(Constants.VERSION_RESOLVE_FAIL, "version resolve fail"));
                    break;
                case Constants.REMOTE_FILE_NOT_FOUND:
                    callbackContext.error(Utils.makeJSON(Constants.REMOTE_FILE_NOT_FOUND, "remote file not found"));
                    break;
                default:
                    callbackContext.error(Utils.makeJSON(Constants.UNKNOWN_ERROR, "unknown error"));
            }

        }
    };

    /**
     * 检测软件更新
     */
    public void checkUpdate() {
        LOG.d(TAG, "checkUpdate..");

        checkUpdateThread = new CheckUpdateThread(mContext, mHandler, queue, packageName, updateXmlUrl, options);
        this.cordova.getThreadPool().execute(checkUpdateThread);
        //new Thread(checkUpdateThread).start();
    }

    /**
     * 对比版本号
     */
    private void compareVersions() {
        Version version = queue.get(0);
        int versionCodeLocal = version.getLocal();
        int versionCodeRemote = version.getRemote();

        boolean skipPromptDialog = false;
        try {
            skipPromptDialog = options.getBoolean("skipPromptDialog");
        } catch (JSONException e) {}

        boolean skipProgressDialog = false;
        try {
            skipProgressDialog = options.getBoolean("skipProgressDialog");
        } catch (JSONException e) {}

        //比对版本号
        //检查软件是否有更新版本
        if (versionCodeLocal < versionCodeRemote) {
            if (isDownloading) {
                msgBox.showDownloadDialog(null, null, null, !skipProgressDialog);
                mHandler.sendEmptyMessage(Constants.VERSION_UPDATING);
            } else {
                LOG.d(TAG, "need update");
                if (skipPromptDialog) {
                    mHandler.sendEmptyMessage(Constants.DOWNLOAD_CLICK_START);
                } else {
                    // 显示提示对话框
                    msgBox.showNoticeDialog(noticeDialogOnClick);
                    mHandler.sendEmptyMessage(Constants.VERSION_NEED_UPDATE);
                }
            }
        } else {
            mHandler.sendEmptyMessage(Constants.VERSION_UP_TO_UPDATE);
            // Do not show Toast
            //Toast.makeText(mContext, getString("update_latest"), Toast.LENGTH_LONG).show();
        }
    }

    private OnClickListener noticeDialogOnClick = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            mHandler.sendEmptyMessage(Constants.DOWNLOAD_CLICK_START);
        }
    };

    private void emitNoticeDialogOnClick() {
        isDownloading = true;

        boolean skipProgressDialog = false;
        try {
            skipProgressDialog = options.getBoolean("skipProgressDialog");
        } catch (JSONException e) {}

        // 显示下载对话框
        Map<String, Object> ret = msgBox.showDownloadDialog(
                downloadDialogOnClickNeg,
                downloadDialogOnClickPos,
                downloadDialogOnClickNeu,
                !skipProgressDialog);

        // 下载文件
        downloadApk((AlertDialog) ret.get("dialog"), (ProgressBar) ret.get("progress"));
    }

    /**
     * 手动安装
     * Download again
     */
    private OnClickListener downloadDialogOnClickNeu = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            //Implemented in DownloadHandler.java
        }
    };
    /**
     * 重新下载
     * Download again
     */
    private OnClickListener downloadDialogOnClickPos = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            mHandler.sendEmptyMessage(Constants.DOWNLOAD_CLICK_START);
        }
    };
    /**
     * 转到后台更新
     * Update in background
     */
    private OnClickListener downloadDialogOnClickNeg = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            // 设置取消状态
            //downloadApkThread.cancelBuildUpdate();
        }
    };

    private OnClickListener errorDialogOnClick = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    };

    /**
     * 下载apk文件
     *
     * @param mProgress
     * @param mDownloadDialog
     */
    private void downloadApk(AlertDialog mDownloadDialog, ProgressBar mProgress) {
        LOG.d(TAG, "downloadApk" + mProgress);

        // 启动新线程下载软件
        downloadApkThread = new DownloadApkThread(mContext, mHandler, mProgress, mDownloadDialog, checkUpdateThread.getMHashMap(), options);
        this.cordova.getThreadPool().execute(downloadApkThread);
        // new Thread(downloadApkThread).start();
    }

}