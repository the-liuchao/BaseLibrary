package com.liuming.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.liuming.myapplication.bean.Person;
import com.liuming.mylibrary.BaseFragment;
import com.liuming.mylibrary.inter.UIProgressRequestListener;
import com.liuming.mylibrary.inter.UIProgressResponseListener;
import com.liuming.mylibrary.utils.OkHttpHelper;
import com.liuming.mylibrary.widge.TitleBar;

import org.xutils.ex.DbException;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.io.File;
import java.util.List;

import okhttp3.OkHttpClient;

/**
 * Created by hp on 2016/12/15.
 */
@ContentView(value = R.layout.fragment_test)
public class TestFragment extends BaseFragment {
    Uri uri;
    @ViewInject(R.id.show_picture)
    private ImageView showView;
    @ViewInject(R.id.download_progress)
    private SeekBar progressDownload;
    @ViewInject(R.id.upload_progress)
    private SeekBar progressUpload;
    @ViewInject(R.id.titlebar)
    private TitleBar titleBar;

    @Override
    protected void initView(View view) {
        try {
            titleBar.setTitle("标题", View.VISIBLE)
                    .setRightIcon(0, "完成")
                    .setBarClickListener(new TitleBar.BarClickListener() {
                        @Override
                        public void onClick(int position, View v) {
                            if (position == 1) {
                                getActivity().finish();
                            } else if (position == 3) {
                                Toast.makeText(getActivity(), "完成", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            mDbManager.save(new Person(1, "liuchao"));
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initData() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (null != uri) {
            Bitmap bmp = BitmapFactory.decodeFile(uri.getPath());
            showView.setImageBitmap(bmp);
        }
    }

    @Event(value = {R.id.add_picture, R.id.test_download, R.id.test_upload}, type = View.OnClickListener.class)
    private void onClick(View v) {
        OkHttpHelper httpHelper = new OkHttpHelper(new OkHttpClient());
        switch (v.getId()) {
            case R.id.add_picture:
                showProgressDialog();
                try {
                    List<Person> persons = mDbManager.findAll(Person.class);
                    Log.e("print", "persons:" + persons);
                } catch (DbException e) {
                    e.printStackTrace();
                }
//                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                File file = new File(getActivity().getExternalCacheDir() + "/images/");
//                if(!file.exists())
//                    file.mkdirs();
//                File uriFile = new File(file,System.currentTimeMillis()+".jpg");
//                uri = Uri.fromFile(uriFile);
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//                startActivityForResult(intent, 1);
                break;
            case R.id.test_download:

                try {
                    httpHelper.httpDownload("http://b248.photo.store.qq.com/psb?/V11NfQFB1E1HEw/slKBDOp1p3S21CeicNIlnEVvUofZwJDRp.aVcJqfifw!/b/dPgAAAAAAAAA&bo=ywK7A8sCuwMFCSo!&rf=viewer_4"
                            , 0
                            , new File(Environment.getExternalStorageDirectory() + "/guojuan.png")
                            , new UIProgressResponseListener() {
                                @Override
                                public void onUIResponseProgress(long bytesRead, long contentLength, boolean done) {
                                    progressDownload.setMax(100);
                                    progressDownload.setProgress((int) (bytesRead / 100));
                                    if (done)
                                        Toast.makeText(getContext(), "下载完成", Toast.LENGTH_SHORT).show();
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.test_upload:
                httpHelper.httpUpload("http://192.168.1.116:8080/PostTest/PostTestServlet"
                        , 0
                        , new File(Environment.getExternalStorageDirectory() + "/guojuan.png")
                        , new UIProgressRequestListener() {
                            @Override
                            public void onUIRequestProgress(long bytesWrite, long contentLength, boolean done) {
                                progressUpload.setMax(100);
                                progressUpload.setProgress((int) (bytesWrite / 100));
                                if (done)
                                    Toast.makeText(getContext(), "上传完成", Toast.LENGTH_SHORT).show();
                            }
                        });
                break;
        }
    }
}
