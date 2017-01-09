package com.liuming.mylibrary.utils;


import android.util.Log;

import com.liuming.mylibrary.inter.UIProgressRequestListener;
import com.liuming.mylibrary.inter.UIProgressResponseListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ForwardingSink;
import okio.ForwardingSource;
import okio.Okio;
import okio.Sink;
import okio.Source;

/**
 * Created by sll on 2015/3/10.
 */
public class OkHttpHelper {
    private OkHttpClient mOkHttpClient;

    public OkHttpHelper(OkHttpClient mOkHttpClient) {
        this.mOkHttpClient = mOkHttpClient;
    }

    /**
     * 该不会开启异步线程。
     *
     * @param request
     * @return
     * @throws IOException
     */
    public Response execute(Request request) throws IOException {
        return mOkHttpClient.newCall(request).execute();
    }

    /**
     * 开启异步线程访问网络
     *
     * @param request
     * @param responseCallback
     */
    public void enqueue(Request request, Callback responseCallback) {
        mOkHttpClient.newCall(request).enqueue(responseCallback);
    }

    /**
     * 开启异步线程访问网络, 且不在意返回结果（实现空callback）
     *
     * @param request
     */
    public void enqueue(Request request) {
        mOkHttpClient.newCall(request).enqueue(new Callback() {


            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

    public String getStringFromServer(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = execute(request);
        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            throw new IOException("Unexpected code " + response);
        }
    }

    /**
     * 上传文件
     *
     * @param url
     * @param file
     * @param progressListener
     */
    public void httpUpload(String url, long startPoints, File file, UIProgressRequestListener progressListener) {
        mOkHttpClient = new OkHttpClient.Builder().build();
        RequestBody requestBody = new MultipartBody.Builder()
                .addFormDataPart("type", "picture")
                .addFormDataPart("photo", file.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), file))
//                .addPart(Headers.of("Content-Disposition", "form-data; name=\"another\";filename=\"another.dex\"")
//                        , RequestBody.create(MediaType.parse("application/octet-stream"), file))
                .build();
        Request request = new Request.Builder().url(url)
                .header("RANGE", "bytes=" + startPoints + "-")//断点续传
                .post(ProgressHelper
                        .addProgressRequestListener(requestBody, progressListener)).build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });

    }


    /**
     * 下载
     *
     * @param url
     * @param target
     * @param progressListener
     * @throws Exception
     */
    public void httpDownload(String url, final long startPoints, final File target, UIProgressResponseListener progressListener) throws Exception {
        final Request request = new Request.Builder()
                .header("RANGE", "bytes=" + startPoints + "-")//断点下载
                .url(url).build();
        ProgressHelper
                .addProgressResponseListener(mOkHttpClient, progressListener)
                .newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("print", "result:failure");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
//                    BufferedSink sink = Okio.buffer(Okio.sink(target));
//                    sink.writeAll(response.body().source());
//                    sink.flush();
//                    sink.close();
                    save(response, target, startPoints);

                } else {
                    throw new IOException("Unexpected code " + response);
                }
            }
        });
    }

    /**
     * 断点保存下载文件
     *
     * @param response
     * @param target
     * @param startPoints
     * @throws IOException
     */
    private void save(Response response, File target, long startPoints) throws IOException {
        ResponseBody body = response.body();
        FileChannel channelOut = null;
        RandomAccessFile randomAccessFile = new RandomAccessFile(target, "rwd");
        channelOut = randomAccessFile.getChannel();
        MappedByteBuffer mappedBuffer = channelOut.map(FileChannel.MapMode.READ_WRITE, startPoints, body.contentLength());
        InputStream in = response.body().byteStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) != -1) {
            mappedBuffer.put(buffer, 0, len);
        }
        in.close();
        channelOut.close();
        randomAccessFile.close();
    }

    public interface ProgressListener {
        void onProgress(long bytesRead, long contentLength, boolean done);
    }
}

/**
 * 请求体重新包装
 */
class ProgressRequestBody extends RequestBody {

    RequestBody requestBody;
    OkHttpHelper.ProgressListener progressListener;
    BufferedSink bufferedSink;

    public ProgressRequestBody(RequestBody requestBody, OkHttpHelper.ProgressListener progressListener) {
        this.requestBody = requestBody;
        this.progressListener = progressListener;
    }

    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    /**
     * 重写调用实际的响应体的contentLength
     *
     * @return contentLength
     * @throws IOException 异常
     */
    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        if (null == bufferedSink) {
            bufferedSink = Okio.buffer(sink(sink));
        }
        requestBody.writeTo(bufferedSink);
        bufferedSink.flush();
    }

    private Sink sink(BufferedSink sink) {
        return new ForwardingSink(sink) {
            //当前写入字节数
            long bytesWritten = 0L;
            //总字节长度，避免多次调用contentLength()方法
            long contentLength = 0L;

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (contentLength == 0) {
                    contentLength = contentLength();
                }
                Log.e("print", "contentLength:" + contentLength + "bytesWritten:" + bytesWritten);
                bytesWritten += byteCount == -1 ? 0 : byteCount;
                progressListener.onProgress(bytesWritten, contentLength, bytesWritten == contentLength);
            }
        };
    }
}

/**
 * 响应体重新包装
 */
class ProgressResponseBody extends ResponseBody {
    ResponseBody responseBody;
    OkHttpHelper.ProgressListener progressListener;
    //包装完成的BufferedSource
    BufferedSource bufferedSource;

    public ProgressResponseBody(ResponseBody responseBody, OkHttpHelper.ProgressListener progressListener) {
        this.responseBody = responseBody;
        this.progressListener = progressListener;
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (null == bufferedSource) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    private Source source(BufferedSource source) {
        return new ForwardingSource(source) {
            long totalByte;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                totalByte += bytesRead != -1 ? bytesRead : 0;
                progressListener.onProgress(totalByte, contentLength(), bytesRead == -1);
                return bytesRead;
            }
        };
    }
}

class ProgressHelper {
    public static OkHttpClient addProgressResponseListener(OkHttpClient client
            , final OkHttpHelper.ProgressListener progressListener) {
        OkHttpClient clone = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originResponseBody = chain.proceed(chain.request());

                return originResponseBody.newBuilder()
                        .body(new ProgressResponseBody(originResponseBody.body(), progressListener))
                        .build();
            }
        }).build();
        return clone;
    }

    /**
     * 包装请求体用于上传文件的回调
     *
     * @param requestBody             请求体RequestBody
     * @param progressRequestListener 进度回调接口
     * @return 包装后的进度回调请求体
     */
    public static ProgressRequestBody addProgressRequestListener(RequestBody requestBody
            , OkHttpHelper.ProgressListener progressRequestListener) {
        //包装请求体
        return new ProgressRequestBody(requestBody, progressRequestListener);
    }

}
