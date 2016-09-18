package com.vaenow.appupdate.android;

/**
 * Created by LuoWen on 2015/12/14.
 */
public interface Constants {
    /* 下载中 */
    int DOWNLOAD = 1;
    /* 下载结束 */
    int DOWNLOAD_FINISH = 2;
    /* 点击开始下载按钮*/
    int DOWNLOAD_CLICK_START = 3;

    /**
     * 对比版本号
     */
    int VERSION_COMPARE_START = 200;
    int VERSION_NEED_UPDATE = 201;
    int VERSION_UP_TO_UPDATE = 202;
    int VERSION_UPDATING = 203;

    /**
     * 版本解析错误
     */
    int VERSION_RESOLVE_FAIL = 301;
    int VERSION_COMPARE_FAIL = 302;

    /**
     * 网络错误
     */
    int REMOTE_FILE_NOT_FOUND = 404;
    int NETWORK_ERROR = 405;

    /**
     * 没有相应的方法
     */
    int NO_SUCH_METHOD = 501;

    /**
     * 未知错误
     */
    int UNKNOWN_ERROR = 901;

}
