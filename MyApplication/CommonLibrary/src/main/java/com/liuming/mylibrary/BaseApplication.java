package com.liuming.mylibrary;

import android.app.Application;

import org.xutils.DbManager;
import org.xutils.x;

/**
 * XUtils3.3.4
 * Created by hp on 2017/1/5.
 */

public abstract class BaseApplication extends Application {
    private static BaseApplication instance;

    private DbManager.DaoConfig mDaoConfig;
    private DbManager mDbManager;

    public static BaseApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        initDataBase();
        instance = this;
    }

    private void initDataBase() {
        mDaoConfig = new DbManager.DaoConfig()
                .setDbName("xutils.db")                                             //数据库名称
//                .setDbDir(Environment.getDataDirectory())                           //不设置dbDir时, 默认存储在app的私有目录.
                .setDbVersion(getDbVersion())                                       //设置版本号
                .setDbOpenListener(new DbManager.DbOpenListener() {
                    @Override
                    public void onDbOpened(DbManager db) {
                        db.getDatabase().enableWriteAheadLogging();                 //开启WAL, 对写入加速提升巨大
                    }
                })
                .setDbUpgradeListener(new DbManager.DbUpgradeListener() {           //数据库升级
                    @Override
                    public void onUpgrade(DbManager db, int oldVersion, int newVersion) {
                        // db.addColumn(...);
                        // db.dropTable(...);
                        // ...
                        // or
                        // db.dropDb();
                        upgrade(db, oldVersion, newVersion);
                    }
                });
        mDbManager = x.getDb(mDaoConfig);
    }

    public DbManager getDbManager() {
        return mDbManager;
    }

    /**
     * 本地数据库版本
     *
     * @return
     */
    protected abstract int getDbVersion();

    /**
     * 数据库更新
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    protected abstract void upgrade(DbManager db, int oldVersion, int newVersion);
}
