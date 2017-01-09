package com.liuming.myapplication;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

import com.liuming.mylibrary.utils.StatusBarUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        StatusBarUtil.setColor(this, Color.RED);
        getSupportFragmentManager().beginTransaction().add(R.id.content,new TestFragment()).commitAllowingStateLoss();
    }
}
