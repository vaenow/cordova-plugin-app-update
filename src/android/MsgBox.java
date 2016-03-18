package com.vaenow.appupdate.android;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import org.apache.cordova.LOG;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by LuoWen on 2016/1/20.
 */
public class MsgBox{
    public static final String TAG = "MsgBox";
    private Context mContext;
    private String packageName;
    private Resources resources;

    private Dialog noticeDialog;
    private Dialog downloadDialog;
    private ProgressBar downloadDialogProgress;
    private Dialog errorDialog;

    public MsgBox(Context mContext) {
        this.mContext = mContext;
        packageName = mContext.getPackageName();
        resources = mContext.getResources();
    }

    /**
     * 显示软件更新对话框
     * @param onClickListener
     */
    public Dialog showNoticeDialog(OnClickListener onClickListener) {
        if (noticeDialog == null) {
            LOG.d(TAG, "showNoticeDialog");
            // 构造对话框
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(getString("update_title"));
            builder.setMessage(getString("update_message"));
            // 更新
            builder.setPositiveButton(getString("update_update_btn"), onClickListener);
            noticeDialog = builder.create();
        }

        if (!noticeDialog.isShowing()) noticeDialog.show();
        
        noticeDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        return noticeDialog;
    }



    /**
     * 显示软件下载对话框
     * @param onClickListener
     */
    public Map<String, Object> showDownloadDialog(OnClickListener onClickListener) {
        if(downloadDialog == null) {
            LOG.d(TAG, "showDownloadDialog");

            // 构造软件下载对话框
            AlertDialog.Builder builder = new Builder(mContext);
            builder.setTitle(getString("updating"));
            // 给下载对话框增加进度条
            final LayoutInflater inflater = LayoutInflater.from(mContext);
            View v = inflater.inflate(getLayout("appupdate_progress"), null);

            /* 更新进度条 */
            downloadDialogProgress = (ProgressBar) v.findViewById(getId("update_progress"));
            builder.setView(v);
            // 取消更新
            //builder.setNegativeButton(getString("update_cancel"), onClickListener);
            //转到后台更新
            builder.setNegativeButton(getString("update_bg"), onClickListener);
            downloadDialog = builder.create();
        }

        if(!downloadDialog.isShowing()) downloadDialog.show();

        downloadDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失

        Map<String, Object> ret = new HashMap<String, Object>();
        ret.put("dialog", downloadDialog);
        ret.put("progress", downloadDialogProgress);
        return ret;
    }

    /**
     * 错误提示窗口
     * @param errorDialogOnClick
     */
    public Dialog showErrorDialog(OnClickListener errorDialogOnClick) {
        if(this.errorDialog == null) {
            LOG.d(TAG, "initErrorDialog");
            // 构造对话框
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(getString("update_error_title"));
            builder.setMessage(getString("update_error_message"));
            // 更新
            builder.setPositiveButton(getString("update_error_yes_btn"), errorDialogOnClick);
            errorDialog = builder.create();
        }

        if(!errorDialog.isShowing()) errorDialog.show();

        return errorDialog;
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
