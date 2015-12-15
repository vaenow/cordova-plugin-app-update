package com.vaenow.appupdate.android;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import org.apache.cordova.LOG;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

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
    private String packageName;
    private Resources resources;
    private Context mContext;
    private Dialog mDownloadDialog;
    /* 更新进度条 */
    private ProgressBar mProgress;

    private BlockingQueue<Version> queue = new ArrayBlockingQueue<Version>(1);

    private CheckUpdateThread checkUpdateThread;
    private DownloadApkThread downloadApkThread;


    public UpdateManager(Context context) {
        this(context, "http://192.168.3.102:8080/update_apk/version.xml");
    }

    public UpdateManager(Context context, String updateUrl) {
        this.updateXmlUrl = updateUrl;
        this.mContext = context;
        packageName = mContext.getPackageName();
        resources = mContext.getResources();
    }

    /**
     * 检测软件更新
     */
    public void checkUpdate() {
        LOG.d(TAG, "checkUpdate..");
        checkUpdateThread = new CheckUpdateThread(mContext, queue, packageName, updateXmlUrl);
        new Thread(checkUpdateThread).start();

        //阻塞式队列
        Version version = null;
        try {
            version = queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int versionCodeLocal = version.getLocal();
        int versionCodeRemote = version.getRemote();

        //比对版本号
        //检查软件是否有更新版本
        if (versionCodeLocal != versionCodeRemote) {
            LOG.d(TAG, "need update");
            // 显示提示对话框
            showNoticeDialog();
        } else {
            // Do not show Toast
            //Toast.makeText(mContext, getString("soft_update_no"), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 显示软件更新对话框
     */
    private void showNoticeDialog() {
        LOG.d(TAG, "showNoticeDialog");
        // 构造对话框
        AlertDialog.Builder builder = new Builder(mContext);
        builder.setTitle(getString("soft_update_title"));
        builder.setMessage(getString("soft_update_info"));
        // 更新
        builder.setPositiveButton(getString("soft_update_updatebtn"), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // 显示下载对话框
                showDownloadDialog();
            }
        });
        Dialog noticeDialog = builder.create();
        noticeDialog.show();
    }

    /**
     * 显示软件下载对话框
     */
    private void showDownloadDialog() {
        LOG.d(TAG, "showDownloadDialog");

        // 构造软件下载对话框
        AlertDialog.Builder builder = new Builder(mContext);
        builder.setTitle(getString("soft_updating"));
        // 给下载对话框增加进度条
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(getLayout("appupdate_progress"), null);

        mProgress = (ProgressBar) v.findViewById(getId("update_progress"));
        builder.setView(v);
        // 取消更新
        builder.setNegativeButton(getString("soft_update_cancel"), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // 设置取消状态
                downloadApkThread.cancelBuildUpdate();
            }
        });
        mDownloadDialog = builder.create();
        mDownloadDialog.show();
        // 下载文件
        downloadApk();
    }

    /**
     * 下载apk文件
     */
    private void downloadApk() {
        LOG.d(TAG, "downloadApk" + mProgress);

        // 启动新线程下载软件
        downloadApkThread = new DownloadApkThread(mContext, mProgress, mDownloadDialog, checkUpdateThread.getMHashMap());
        new Thread(downloadApkThread).start();
    }

    private int getId(String name) {
        return resources.getIdentifier(name, "id", packageName);
    }

    private int getString(String name) {
        return resources.getIdentifier(name, "string", packageName);
    }

    private int getLayout(String name) {
        return resources.getIdentifier(name, "layout", packageName);
    }

}