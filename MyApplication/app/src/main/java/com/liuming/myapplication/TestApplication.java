package com.liuming.myapplication;

import com.liuming.mylibrary.BaseApplication;

import org.xutils.DbManager;


/**
 * Created by hp on 2017/1/5.
 */

public class TestApplication extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected int getDbVersion() {
        return 0;
    }

    @Override
    protected void upgrade(DbManager db, int oldVersion, int newVersion) {

    }

}
