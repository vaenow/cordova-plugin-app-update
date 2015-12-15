package com.vaenow.appupdate;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import org.apache.cordova.LOG;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

/**
 * Created by LuoWen on 2015/12/14.
 */
public class CheckUpdateThread implements Runnable {
    private String TAG = "CheckUpdateThread";

    /* 保存解析的XML信息 */
    HashMap<String, String> mHashMap;
    private Context mContext;
    private BlockingQueue<Version> queue;
    private String packageName;
    private String updateXmlUrl;

    private void setMHashMap(HashMap<String, String> mHashMap) {
        this.mHashMap = mHashMap;
    }

    public HashMap<String, String> getMHashMap() {
        return mHashMap;
    }

    public CheckUpdateThread(Context mContext, BlockingQueue<Version> queue, String packageName, String updateXmlUrl) {
        this.mContext = mContext;
        this.queue = queue;
        this.packageName = packageName;
        this.updateXmlUrl = updateXmlUrl;
    }

    @Override
    public void run() {
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
            LOG.d(TAG, "url" + url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();//利用HttpURLConnection对象,我们可以从网络中获取网页数据.
            LOG.d(TAG, "conn" + conn);
            conn.setDoInput(true);
            LOG.d(TAG, " conn.setDoInput(true);");
            conn.connect();
            LOG.d(TAG, "conn.connect();");
            is = conn.getInputStream(); //得到网络返回的输入流
            LOG.d(TAG, "is" + is);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return is;
    }

    /**
     * 获取软件版本号
     * <p/>
     * It's weird, I don't know why.
     * <pre>
     * versionName -> versionCode
     * 0.0.1    ->  12
     * 0.3.4    ->  3042
     * 3.2.4    ->  302042
     * 12.234.221 -> 1436212
     * </pre>
     *
     * @param context
     * @return
     */
    private int getVersionCodeLocal(Context context) {
        LOG.d(TAG, "getVersionCode..");

        int versionCode = 0;
        try {
            // 获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode = context.getPackageManager().getPackageInfo(packageName, 0).versionCode;
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
            setMHashMap(service.parseXml(is));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != getMHashMap()) {
            versionCodeRemote = Integer.valueOf(getMHashMap().get("version"));
        }

        return versionCodeRemote;
    }
}