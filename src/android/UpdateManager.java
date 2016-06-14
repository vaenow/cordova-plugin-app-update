package com.vaenow.appupdate.android;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.widget.ProgressBar;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.LOG;
import org.json.JSONArray;

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
    private JSONArray args;
    private CallbackContext callbackContext;
    private String packageName;
    private Context mContext;
    private MsgBox msgBox;
    private Boolean isDownloading = false;

    private List<Version> queue = new ArrayList<Version>(1);

    private CheckUpdateThread checkUpdateThread;
    private DownloadApkThread downloadApkThread;


    public UpdateManager(JSONArray args, CallbackContext callbackContext, Context context) {
        this(args, callbackContext, context, "http://192.168.3.102:8080/update_apk/version.xml");
    }

    public UpdateManager(JSONArray args, CallbackContext callbackContext, Context context, String updateUrl) {
        this.args = args;
        this.callbackContext = callbackContext;
        this.updateXmlUrl = updateUrl;
        this.mContext = context;
        packageName = mContext.getPackageName();
        msgBox = new MsgBox(mContext);
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case Constants.NETWORK_ERROR:
                    //暂时隐藏错误
                    //msgBox.showErrorDialog(errorDialogOnClick);
                    break;
                case Constants.VERSION_COMPARE_START:
                    compareVersions();
                    break;
                case Constants.VERSION_COMPARE_SUCCESS:
                    callbackContext.success();
                    break;
                case Constants.VERSION_COMPARE_FAIL:
//                    callbackContext.error(Constants.VERSION_COMPARE_FAIL);
                    break;
            }
        }
    };

    /**
     * 检测软件更新
     */
    public void checkUpdate() {
        LOG.d(TAG, "checkUpdate..");
        checkUpdateThread = new CheckUpdateThread(mContext, mHandler, queue, packageName, updateXmlUrl);
        new Thread(checkUpdateThread).start();
    }

    /**
     * 对比版本号
     */
    private void compareVersions() {
        Version version = queue.get(0);
        int versionCodeLocal = version.getLocal();
        int versionCodeRemote = version.getRemote();

        //比对版本号
        //检查软件是否有更新版本
        if (versionCodeLocal != versionCodeRemote) {
            if(isDownloading) {
                msgBox.showDownloadDialog(null);
            } else {
                LOG.d(TAG, "need update");
                // 显示提示对话框
                msgBox.showNoticeDialog(noticeDialogOnClick);
            }
        } else {
            // Do not show Toast
            //Toast.makeText(mContext, getString("update_latest"), Toast.LENGTH_LONG).show();
        }
    }

    private OnClickListener noticeDialogOnClick = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            isDownloading = true;
            // 显示下载对话框
            Map<String, Object> ret = msgBox.showDownloadDialog(downloadDialogOnClick);
            // 下载文件
            downloadApk((Dialog)ret.get("dialog"), (ProgressBar)ret.get("progress"));
        }
    };

    private OnClickListener downloadDialogOnClick = new OnClickListener() {
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
     * @param mProgress
     * @param mDownloadDialog
     */
    private void downloadApk(Dialog mDownloadDialog, ProgressBar mProgress) {
        LOG.d(TAG, "downloadApk" + mProgress);

        // 启动新线程下载软件
        downloadApkThread = new DownloadApkThread(mContext, mProgress, mDownloadDialog, checkUpdateThread.getMHashMap());
        new Thread(downloadApkThread).start();
    }

}