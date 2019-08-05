package com.lihang.selfmvc.okhttps.builder;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.lihang.selfmvc.base.MyApplication;
import com.lihang.selfmvc.okhttps.EasyOk;
import com.lihang.selfmvc.okhttps.okcallback.OnDownloadListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by leo
 * on 2019/7/29.
 */
public class OkDownloadBuilder {
    //断点续传的长度
    private long currentLength;
    private String url;
    private String tag;
    //文件路径(不包括文件名)
    private String path;
    //文件名
    private String fileName;
    //是否开启断点续传
    private boolean resume;

    private boolean onlyOneNet;//只允许一个在当前下载线程中

    /**
     * okHttpUtils里单例里唯一
     */
    private OkHttpClient okHttpClient;
    private Context context;
    private Handler mDelivery;

    /**
     * 每次请求网络生成的请求request
     */
    private Request.Builder mBuilder;

    public OkDownloadBuilder() {
        this.okHttpClient = EasyOk.getInstance().getOkHttpClient();
        this.context = MyApplication.getContext();
        this.mDelivery = EasyOk.getInstance().getmDelivery();
    }

    public OkDownloadBuilder build() {
        mBuilder = new Request.Builder();
        mBuilder.url(url);
        if (!TextUtils.isEmpty(tag)) {
            mBuilder.tag(tag);
        }
        //这里只要断点上传，总会走缓存。。所以强制网络下载
        mBuilder.cacheControl(CacheControl.FORCE_NETWORK);
        return this;
    }


    public void removeOnceTag() {
        if (onlyOneNet) {
            if (!TextUtils.isEmpty(tag)) {
                EasyOk.getInstance().getOnesTag().remove(tag);
            } else {
                EasyOk.getInstance().getOnesTag().remove(url);
            }
        }
    }

    public void enqueue(final OnDownloadListener listener) {
        if (onlyOneNet) {
            if (!TextUtils.isEmpty(tag)) {
                if (EasyOk.getInstance().getOnesTag().contains(tag)) {
                    return;
                }
                EasyOk.getInstance().getOnesTag().add(tag);
            } else {
                if (EasyOk.getInstance().getOnesTag().contains(url)) {
                    return;
                }
                EasyOk.getInstance().getOnesTag().add(url);
            }
        }

        if (resume) {
            File exFile = new File(path, fileName);
            if (exFile.exists()) {
                currentLength = exFile.length();
                mBuilder.header("RANGE", "bytes=" + currentLength + "-");
            }
        }
        Request okHttpRequest = mBuilder.build();
        okHttpClient.newCall(okHttpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                removeOnceTag();
                //下载失败监听回调
                mDelivery.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onDownloadFailed(e);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                removeOnceTag();
                InputStream is = null;
                byte[] buf = new byte[1024];
                int len = 0;
                FileOutputStream fos = null;

                //储存下载文件的目录
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                final File file = new File(dir, fileName);

                try {
                    is = response.body().byteStream();
                    //总长度
                    final long total;
                    //如果当前长度就等于要下载的长度，那么此文件就是下载好的文件
                    //前提是这里是默认下载的同意文件，要判断是否可以断点续传，最好在开启网络的时候判断是否是同意版本号
                    if (currentLength == response.body().contentLength()) {
                        mDelivery.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onDownloadSuccess(file);
                            }
                        });
                        return;
                    }
                    if (resume) {
                        total = response.body().contentLength() + currentLength;
                    } else {
                        total = response.body().contentLength();
                    }
                    mDelivery.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onDownLoadTotal(total);
                        }
                    });
                    if (resume) {
                        //这个方法是文件开始拼接
                        fos = new FileOutputStream(file, true);
                    } else {
                        //这个是不拼接，从头开始
                        fos = new FileOutputStream(file);
                    }
                    long sum;
                    if (resume) {
                        sum = currentLength;
                    } else {
                        sum = 0;
                    }
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        final int progress = (int) (sum * 1.0f / total * 100);
                        //下载中更新进度条
                        mDelivery.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onDownloading(progress);
                            }
                        });

                    }
                    fos.flush();
                    //下载完成
                    mDelivery.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onDownloadSuccess(file);
                        }
                    });

                } catch (final Exception e) {
                    mDelivery.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onDownloadFailed(e);
                        }
                    });
                } finally {

                    try {
                        if (is != null) {
                            is.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {

                    }

                }


            }
        });

    }


    public OkDownloadBuilder onlyOneNet(boolean onlyOneNet) {
        this.onlyOneNet = onlyOneNet;
        return this;
    }


    public OkDownloadBuilder path(String path) {
        this.path = path;
        return this;
    }


    public OkDownloadBuilder fileName(String fileName) {
        this.fileName = fileName;
        return this;
    }


    public OkDownloadBuilder url(String url) {
        this.url = url;
        return this;
    }

    public OkDownloadBuilder tag(String tag) {
        this.tag = tag;
        return this;
    }

    public OkDownloadBuilder resume(boolean resume) {
        this.resume = resume;
        return this;
    }


}
