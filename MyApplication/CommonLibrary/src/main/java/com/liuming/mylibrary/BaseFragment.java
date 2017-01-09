package com.liuming.mylibrary;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.liuming.mylibrary.dialog.LoadingProgressDialog;

import org.xutils.DbManager;
import org.xutils.x;

/**
 * Created by hp on 2017/1/5.
 */
public abstract class BaseFragment extends Fragment {
    protected DbManager mDbManager;
    protected View mRootView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDbManager = BaseApplication.getInstance().getDbManager();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = x.view().inject(this, inflater, container);
            initView(mRootView);
        } else {
            ViewGroup parent = (ViewGroup) mRootView.getParent();
            if (null != parent)
                parent.removeView(mRootView);
        }
        return mRootView;
    }

    /**
     * 显示加载对话框
     */
    protected void showProgressDialog() {
        LoadingProgressDialog.newInstance(-1)
                .setCompleteListener(new LoadingProgressDialog.CompleteListener() {
                    @Override
                    public void complete(int type, DialogFragment dialog) {
                        dialog.dismiss();
                    }
                })
                .setTitle("提示")
                .setMessage("正在加载中...")
                .showDialog(getActivity().getSupportFragmentManager(), "progress");
    }

    /**
     * 隐藏加载对话框
     */
    protected void hideProgressDialog() {
        LoadingProgressDialog.newInstance(-1).dismiss();
    }

    protected abstract void initView(View mRootView);

    protected abstract void initData();
}
