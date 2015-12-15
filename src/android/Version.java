package com.vaenow.appupdate.android;

/**
 * Created by LuoWen on 2015/12/14.
 */
public class Version {
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