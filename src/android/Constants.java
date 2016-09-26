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
    int VERSION_COMPARE_START = 200; //private 开始对比版本号; start to compare version
    int VERSION_NEED_UPDATE = 201; //检查到需要更新； need update
    int VERSION_UP_TO_UPDATE = 202; //软件是不需要更新；version up to date
    int VERSION_UPDATING = 203; //软件正在更新；version is updating

    /**
     * 版本解析错误
     */
    int VERSION_RESOLVE_FAIL = 301; //版本文件解析错误 version-xml file resolve fail
    int VERSION_COMPARE_FAIL = 302; //版本文件对比错误 version-xml file compare fail

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
