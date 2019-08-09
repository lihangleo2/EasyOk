package com.lihang.selfmvc.okhttps;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import com.lihang.selfmvc.okhttps.Interceptor.NetCacheInterceptor;
import com.lihang.selfmvc.okhttps.Interceptor.OfflineCacheInterceptor;
import com.lihang.selfmvc.okhttps.builder.OkDownloadBuilder;
import com.lihang.selfmvc.okhttps.builder.OkGetBuilder;
import com.lihang.selfmvc.okhttps.builder.OkPostBuilder;
import com.lihang.selfmvc.okhttps.builder.OkUploadBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

/**
 * Created by leo
 * on 2019/7/22.
 * 鸿样大神 和 okGo 已经封装的很好了。
 * 自己写只是加强理解，主要是学习、
 */
public class EasyOk {

    private static EasyOk easyOk;
    private OkHttpClient okHttpClient;
    //这个handler的作用是把子线程切换主线程。在后面接口中的具体实现，就不需要用handler去回调了
    private Handler mDelivery;
    //防止网络重复请求的tagList;
    private ArrayList<String> onesTag;

    private EasyOk() {
        onesTag = new ArrayList<>();
        mDelivery = new Handler(Looper.getMainLooper());
        okHttpClient = new OkHttpClient.Builder()
                //设置缓存文件路径，和文件大小
                .cache(new Cache(new File(Environment.getExternalStorageDirectory() + "/okhttp_cache/"), 50 * 1024 * 1024))
                .hostnameVerifier(new HostnameVerifier() {//证书信任
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                })
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                //这里是网上对cookie的封装 github : https://github.com/franmontiel/PersistentCookieJar
                //如果你的项目没有遇到cookie管理或者你想通过网络拦截自己存储，那么可以删除persistentcookiejar包
//                .cookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(MyApplication.getContext())))
                .addInterceptor(OfflineCacheInterceptor.getInstance())
                .addNetworkInterceptor(NetCacheInterceptor.getInstance())
                .build();
    }


    public static EasyOk getInstance() {
        if (easyOk == null) {
            synchronized (EasyOk.class) {
                if (easyOk == null) {
                    easyOk = new EasyOk();
                }
            }
        }
        return easyOk;
    }


    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public Handler getmDelivery() {
        return mDelivery;
    }

    public ArrayList<String> getOnesTag() {
        return onesTag;
    }


    public static OkGetBuilder get() {
        return new OkGetBuilder();
    }

    public static OkPostBuilder post() {
        return new OkPostBuilder();
    }

    public static OkUploadBuilder upload() {
        return new OkUploadBuilder();
    }

    public static OkDownloadBuilder download() {
        return new OkDownloadBuilder();
    }

    //tag取消网络请求
    public void cancleOkhttpTag(String tag) {
        Dispatcher dispatcher = okHttpClient.dispatcher();
        synchronized (dispatcher) {
            //请求列表里的，取消网络请求
            for (Call call : dispatcher.queuedCalls()) {
                if (tag.equals(call.request().tag())) {
                    call.cancel();
                }
            }
            //正在请求网络的，取消网络请求
            for (Call call : dispatcher.runningCalls()) {
                if (tag.equals(call.request().tag())) {
                    call.cancel();
                }
            }
        }
    }

}
