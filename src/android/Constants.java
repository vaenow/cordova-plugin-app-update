package com.vaenow.appupdate.android;

/**
 * Created by LuoWen on 2015/12/14.
 */
public interface Constants {
    /* 下载中 */
    public static final int DOWNLOAD = 1;
    /* 下载结束 */
    public static final int DOWNLOAD_FINISH = 2;

    /**
     * 对比版本号
     */
    public static final int VERSION_COMPARE_START = 200;
    public static final int VERSION_COMPARE_SUCCESS = 201;
    public static final int VERSION_COMPARE_FAIL = 202;

    /**
     * 版本解析错误
     */
    public static final int VERSION_RESOLVE_FAIL = 301;

    /**
     * 网络错误
     */
    public static final int NETWORK_ERROR = 404;

}
