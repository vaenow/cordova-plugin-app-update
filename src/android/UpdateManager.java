package com.xuexiq.appupdate;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import org.apache.cordova.LOG;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by LuoWen on 2015/10/27.
 *
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

    private String package_name;
    private Resources resources;

    /* 下载中 */
    private static final int DOWNLOAD = 1;
    /* 下载结束 */
    private static final int DOWNLOAD_FINISH = 2;
    /* 保存解析的XML信息 */
    HashMap<String, String> mHashMap;
    /* 下载保存路径 */
    private String mSavePath;
    /* 记录进度条数量 */
    private int progress;
    /* 是否取消更新 */
    private boolean cancelUpdate = false;

    private Context mContext;
    /* 更新进度条 */
    private ProgressBar mProgress;
    private Dialog mDownloadDialog;



    private BlockingQueue<Version> queue = new ArrayBlockingQueue<Version>(1);

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // 正在下载
                case DOWNLOAD:
                    // 设置进度条位置
                    mProgress.setProgress(progress);
                    break;
                case DOWNLOAD_FINISH:
                    // 安装文件
                    installApk();
                    break;
                default:
                    break;
            }
        }
    };

    public UpdateManager(Context context) {
        this(context, "http://192.168.3.102:8080/update_apk/version.xml");
    }

    public UpdateManager(Context context, String updateUrl) {
        this.updateXmlUrl = updateUrl;
        this.mContext = context;
        package_name = mContext.getPackageName();
        resources = mContext.getResources();
    }

    /**
     * 检测软件更新
     */
    public void checkUpdate() {
        LOG.d(TAG, "checkUpdate..");

        new Thread(new CheckUpdateThread()).start();
        //new CheckUpdateThread().start();

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
            Toast.makeText(mContext, getString("soft_update_no")/*R.string.soft_update_no*/, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 显示软件更新对话框
     */
    private void showNoticeDialog() {
        LOG.d(TAG, "showNoticeDialog");
        // 构造对话框
        AlertDialog.Builder builder = new Builder(mContext);
        builder.setTitle(getString("soft_update_title")/*R.string.soft_update_title*/);
        builder.setMessage(getString("soft_update_info")/*R.string.soft_update_info*/);
        // 更新
        builder.setPositiveButton(getString("soft_update_updatebtn")/*R.string.soft_update_updatebtn*/, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // 显示下载对话框
                showDownloadDialog();
            }
        });
        // 稍后更新
        // builder.setNegativeButton(getString("soft_update_later")/*R.string.soft_update_later*/, new OnClickListener() {
        //     @Override
        //     public void onClick(DialogInterface dialog, int which) {
        //         dialog.dismiss();
        //     }
        // });
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
        builder.setTitle(getString("soft_updating")/*R.string.soft_updating*/);
        // 给下载对话框增加进度条
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(getLayout("appupdate_progress")/*R.layout.appupdate_progress*/, null);

        mProgress = (ProgressBar) v.findViewById(getId("update_progress")/*R.id.update_progress*/);
        builder.setView(v);
        // 取消更新
        builder.setNegativeButton(getString("soft_update_cancel")/*R.string.soft_update_cancel*/, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // 设置取消状态
                cancelUpdate = true;
            }
        });
        mDownloadDialog = builder.create();
        mDownloadDialog.show();
        // 现在文件
        downloadApk();
    }

    /**
     * 下载apk文件
     */
    private void downloadApk() {
        LOG.d(TAG, "downloadApk");

        // 启动新线程下载软件
//        new DownloadApkThread().start();
        new Thread(new DownloadApkThread()).start();
    }

    /**
     * 下载文件线程
     */
    private class DownloadApkThread implements Runnable {
        private String TAG = "DownloadApkThread";

        @Override
        public void run() {
            try {
                // 判断SD卡是否存在，并且是否具有读写权限
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    // 获得存储卡的路径
                    String sdpath = Environment.getExternalStorageDirectory() + "/";
                    mSavePath = sdpath + "download";
                    URL url = new URL(mHashMap.get("url"));
                    // 创建连接
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    // 获取文件大小
                    int length = conn.getContentLength();
                    // 创建输入流
                    InputStream is = conn.getInputStream();

                    File file = new File(mSavePath);
                    // 判断文件目录是否存在
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    File apkFile = new File(mSavePath, mHashMap.get("name"));
                    FileOutputStream fos = new FileOutputStream(apkFile);
                    int count = 0;
                    // 缓存
                    byte buf[] = new byte[1024];
                    // 写入到文件中
                    do {
                        int numread = is.read(buf);
                        count += numread;
                        // 计算进度条位置
                        progress = (int) (((float) count / length) * 100);
                        // 更新进度
                        mHandler.sendEmptyMessage(DOWNLOAD);
                        if (numread <= 0) {
                            // 下载完成
                            mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
                            break;
                        }
                        // 写入文件
                        fos.write(buf, 0, numread);
                    } while (!cancelUpdate);// 点击取消就停止下载.
                    fos.close();
                    is.close();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 取消下载对话框显示
            mDownloadDialog.dismiss();
        }
    }

    private int getId(String name) {
        return resources.getIdentifier(name, "id", package_name);
    }
    private int getString(String name) {
        return resources.getIdentifier(name, "string", package_name);
    }
    private int getLayout(String name) {
        return resources.getIdentifier(name, "layout", package_name);
    }

    /**
     * 安装APK文件
     */
    private void installApk() {
        File apkfile = new File(mSavePath, mHashMap.get("name"));
        if (!apkfile.exists()) {
            return;
        }
        // 通过Intent安装APK文件
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        mContext.startActivity(i);
    }

    private class CheckUpdateThread implements Runnable {

        private String TAG = "UpdateManager @ CheckUpdateThread";

        @Override
        public void run() {
            //Looper.prepare(); //java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()

            int versionCodeLocal = getVersionCodeLocal(mContext); // 获取当前软件版本
            int versionCodeRemote = getVersionCodeRemote();  //获取服务器当前软件版本

            //给阻塞式队列添加值
            Version version = new Version(versionCodeLocal, versionCodeRemote);
            try {
                queue.put(version);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         * 通过url返回文件
         *
         * @param path
         * @return
         */
        private InputStream returnFileIS(String path) {
            LOG.d(TAG, "returnFileIS..");

            URL url = null;
            InputStream is = null;
            try {
                url = new URL(path);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                LOG.d(TAG, "url " + url);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();//利用HttpURLConnection对象,我们可以从网络中获取网页数据.
                LOG.d(TAG, "conn.setDoInput" + conn);
                conn.setDoInput(true);
                LOG.d(TAG, "conn.connect");
                conn.connect();
                LOG.d(TAG, "conn.getInputStream");
                is = conn.getInputStream(); //得到网络返回的输入流
                LOG.d(TAG, "is " + is);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            return is;
        }

        /**
         * 获取软件版本号
         *
         * It's weird, I don't know why.
         * <pre>
         * versionName -> versionCode
         * 0.0.1    ->  12
         * 0.3.4    ->  3042
         * 3.2.4    ->  302042
         * 12.234.221 -> 1436212
         * </pre>
         * @param context
         * @return
         */
        private int getVersionCodeLocal(Context context) {
            LOG.d(TAG, "getVersionCode..");

            int versionCode = 0;
            try {
                // 获取软件版本号，对应AndroidManifest.xml下android:versionCode
                versionCode = context.getPackageManager().getPackageInfo(package_name, 0).versionCode;
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            return versionCode;
        }

        /**
         * 获取服务器软件版本号
         *
         * @return
         */
        private int getVersionCodeRemote() {
            int versionCodeRemote = 0;

            InputStream is = returnFileIS(updateXmlUrl);
            // 解析XML文件。 由于XML文件比较小，因此使用DOM方式进行解析
            ParseXmlService service = new ParseXmlService();
            try {
                mHashMap = service.parseXml(is);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (null != mHashMap) {
                versionCodeRemote = Integer.valueOf(mHashMap.get("version"));
            }

            return versionCodeRemote;
        }
    }


    private static class Version {
        private int local;
        private int remote;

        public Version(int local, int remote) {
            this.local = local;
            this.remote = remote;
        }

        public int getLocal() {
            return local;
        }

        public int getRemote() {
            return remote;
        }
    }
}