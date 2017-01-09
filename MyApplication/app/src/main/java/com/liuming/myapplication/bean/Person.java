package com.liuming.myapplication.bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * Created by hp on 2017/1/5.
 */

@Table(name = "person")
public class Person {
    @Column(name = "ID",
            isId = true)
    private int id;
    @Column(name = "mId",property = "NOT NULL UNIQUE")
    private int mId;
    @Column(name = "mName")
    private String mName;

    public Person() {
    }

    public Person(int mId, String mName) {
        this.mId = mId;
        this.mName = mName;
    }

    public int getmId() {
        return mId;
    }

    public void setmId(int mId) {
        this.mId = mId;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    @Override
    public String toString() {
        return "Person{" +
                "mId=" + mId +
                ", mName='" + mName + '\'' +
                '}';
    }
}
