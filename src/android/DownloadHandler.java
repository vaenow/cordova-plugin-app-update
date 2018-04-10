package com.vaenow.appupdate.android;

import org.apache.cordova.BuildHelper;

import android.app.AlertDialog;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.support.v4.content.FileProvider;
import java.io.File;
import java.util.HashMap;

import org.apache.cordova.LOG;

/**
 * Created by LuoWen on 2015/12/14.
 */
public class DownloadHandler extends Handler {
    private String TAG = "DownloadHandler";

    private Context mContext;
    /* 更新进度条 */
    private ProgressBar mProgress;
    /* 记录进度条数量 */
    private int progress;
    /* 下载保存路径 */
    private String mSavePath;
    /* 保存解析的XML信息 */
    private HashMap<String, String> mHashMap;
    private MsgHelper msgHelper;
    private AlertDialog mDownloadDialog;

    public DownloadHandler(Context mContext, ProgressBar mProgress, AlertDialog mDownloadDialog, String mSavePath, HashMap<String, String> mHashMap) {
        this.msgHelper = new MsgHelper(mContext.getPackageName(), mContext.getResources());
        this.mDownloadDialog = mDownloadDialog;
        this.mContext = mContext;
        this.mProgress = mProgress;
        this.mSavePath = mSavePath;
        this.mHashMap = mHashMap;
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            // 正在下载
            case Constants.DOWNLOAD:
                // 设置进度条位置
                mProgress.setProgress(progress);
                break;
            case Constants.DOWNLOAD_FINISH:
                updateMsgDialog();
                // 安装文件
                installApk();
                break;
            default:
                break;
        }
    }

    public void updateProgress(int progress) {
        this.progress = progress;
    }

    public void updateMsgDialog() {
        mDownloadDialog.setTitle(msgHelper.getString(MsgHelper.DOWNLOAD_COMPLETE_TITLE));
        if (mDownloadDialog.isShowing()) {
            mDownloadDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.GONE); //Update in background
            mDownloadDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setVisibility(View.VISIBLE); //Install Manually
            mDownloadDialog.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.VISIBLE); //Download Again

            mDownloadDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(downloadCompleteOnClick);
        }
    }

    private OnClickListener downloadCompleteOnClick = new OnClickListener() {
        @Override
        public void onClick(View view) {
            installApk();
        }
    };

    /**
     * 安装APK文件
     */
    private void installApk() {
        LOG.d(TAG, "Installing APK");

        File apkFile = new File(mSavePath, mHashMap.get("name")+".apk");
        if (!apkFile.exists()) {
            LOG.e(TAG, "Could not find APK: " + mHashMap.get("name"));
            return;
        }

        LOG.d(TAG, "APK Filename: " + apkFile.toString());

        // 通过Intent安装APK文件
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            LOG.d(TAG, "Build SDK Greater than or equal to Nougat");
            String applicationId = (String) BuildHelper.getBuildConfigValue((Activity) mContext, "APPLICATION_ID");
            Uri apkUri = FileProvider.getUriForFile(mContext, applicationId + ".appupdate.provider", apkFile);
            Intent i = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            i.setData(apkUri);
            i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            mContext.startActivity(i);
        }else{
            LOG.d(TAG, "Build SDK less than Nougat");
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setDataAndType(Uri.parse("file://" + apkFile.toString()), "application/vnd.android.package-archive");
            mContext.startActivity(i);
        }

    }
}
