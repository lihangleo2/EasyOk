package com.lihang.selfmvc.okhttps.builder;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.lihang.selfmvc.R;
import com.lihang.selfmvc.base.MyApplication;
import com.lihang.selfmvc.bean.LoginMessage;
import com.lihang.selfmvc.okhttps.Interceptor.NetCacheInterceptor;
import com.lihang.selfmvc.okhttps.Interceptor.OfflineCacheInterceptor;
import com.lihang.selfmvc.okhttps.EasyOk;
import com.lihang.selfmvc.okhttps.okcallback.ResultCall;
import com.lihang.selfmvc.okhttps.okcallback.ResultMyCall;
import com.lihang.selfmvc.utils.GsonUtil;
import com.lihang.selfmvc.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by leo
 * on 2019/7/22.
 * get builder
 */
public class OkGetBuilder {
    /**
     * 下面是解析参数，包括成功后 解析type
     */
    private String url;
    private String tag;
    private Map<String, String> headers;
    private Map<String, String> params;
    private boolean onlyOneNet;
    private int tryAgainCount;
    private int currentAgainCount;
    /**
     * okHttpUtils里单例里唯一
     */
    private OkHttpClient okHttpClient;
    private Context context;
    private Handler mDelivery;

    /**
     * 每次请求网络生成的请求request
     */
    private Request okHttpRequest;

    public OkGetBuilder() {
        this.okHttpClient = EasyOk.getInstance().getOkHttpClient();
        this.context = MyApplication.getContext();
        this.mDelivery = EasyOk.getInstance().getmDelivery();
    }


    public OkGetBuilder build() {
        //头部的builder
//        okHttpRequest = new Request.Builder().url(appendParams(url, params)).tag(tag).headers(appendHeaders(headers)).build();
        Request.Builder mBuilder = new Request.Builder();
        if (params != null) {
            mBuilder.url(appendParams(url, params));
        } else {
            LogUtils.i("网络请求", "请求接口 ==>> " + url);
            EventBus.getDefault().post(new LoginMessage("请求接口 ==>> " + url,"GET"));

            mBuilder.url(url);
        }

        if (!TextUtils.isEmpty(tag)) {
            mBuilder.tag(tag);
        }

        if (headers != null) {
            mBuilder.headers(appendHeaders(headers));
        }

        okHttpRequest = mBuilder.build();
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


    //非封装单独使用
    public void enqueue(final ResultMyCall resultMyCall) {
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

        if (resultMyCall != null) {
            mDelivery.post(new Runnable() {
                @Override
                public void run() {
                    resultMyCall.onBefore();
                }
            });
        }

        okHttpClient.newCall(okHttpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                if (e instanceof SocketException) {

                } else {
                    //如果在重连的情况下，是主动取消网络是java.net.SocketException: Socket closed
                    if (currentAgainCount < tryAgainCount && tryAgainCount > 0) { // 如果超时并未超过指定次数，则重新连接
                        currentAgainCount++;
                        okHttpClient.newCall(call.request()).enqueue(this);
                        return;
                    }
                }

                removeOnceTag();
                if (resultMyCall != null) {
                    mDelivery.post(new Runnable() {
                        @Override
                        public void run() {
                            resultMyCall.onAfter();
                            String errorMsg;
                            if (e instanceof SocketException) {

                            } else {
                                if (e instanceof ConnectException) {
                                    errorMsg = context.getString(R.string.network_unknow);
                                } else if (e instanceof SocketTimeoutException) {
                                    errorMsg = context.getString(R.string.network_overtime);
                                } else {
                                    errorMsg = context.getString(R.string.server_error);
                                }
                                resultMyCall.onError(errorMsg);
                            }


                        }
                    });

                }
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                removeOnceTag();
                //网络请求成功
                if (response.isSuccessful()) {
                    if (resultMyCall != null) {
                        String result = response.body().string();
                        Object successObject = null;
                        try {
                            if (resultMyCall.getType() == null) {
                                successObject = result;
                            } else {
                                successObject = GsonUtil.deser(result, resultMyCall.getType());
                            }

                        } catch (Throwable e) {
                            mDelivery.post(new Runnable() {
                                @Override
                                public void run() {
                                    resultMyCall.onAfter();
                                    resultMyCall.onError("数据解析出错了");
                                }
                            });
                            return;
                        }

                        if (successObject == null) {
                            successObject = result;
                        }

                        final Object finalSuccessObject = successObject;
                        mDelivery.post(new Runnable() {
                            @Override
                            public void run() {
                                resultMyCall.onAfter();
                                resultMyCall.onSuccess(finalSuccessObject);
                            }
                        });

                    }
                } else {
                    //接口请求确实成功了，code 不是 200
                    if (resultMyCall != null) {
                        final String errorMsg = response.body().string();
                        mDelivery.post(new Runnable() {
                            @Override
                            public void run() {
                                resultMyCall.onAfter();
                                resultMyCall.onError(errorMsg);
                            }
                        });
                    }
                }

            }
        });
    }


    //要想封装，解析数据必须交到外面处理。
    public void enqueue(final ResultCall resultMyCall) {
        if (resultMyCall != null) {
            LogUtils.i("网络请求", "请求方式 ==> GET");
            EventBus.getDefault().post(new LoginMessage("请求方式 ==> GET","GET"));
            LogUtils.i("网络请求", "请求开始");
            EventBus.getDefault().post(new LoginMessage("请求开始","GET"));
            mDelivery.post(new Runnable() {
                @Override
                public void run() {
                    resultMyCall.onBefore();
                }
            });
        }
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



        okHttpClient.newCall(okHttpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                if (e instanceof SocketException) {

                } else {
                    //如果在重连的情况下，是主动取消网络是java.net.SocketException: Socket closed
                    if (currentAgainCount < tryAgainCount && tryAgainCount > 0) { // 如果超时并未超过指定次数，则重新连接
                        currentAgainCount++;
                        okHttpClient.newCall(call.request()).enqueue(this);
                        return;
                    }
                }

                removeOnceTag();
                if (resultMyCall != null) {
                    mDelivery.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            String errorMsg;
                            if (e instanceof SocketException) {

                            } else {
                                if (e instanceof ConnectException) {
                                    errorMsg = context.getString(R.string.network_unknow);
                                } else if (e instanceof SocketTimeoutException) {
                                    errorMsg = context.getString(R.string.network_overtime);
                                } else {
                                    errorMsg = context.getString(R.string.server_error);
                                }
                                LogUtils.i("网络请求", "请求失败原因 ==> " + e.toString());
                                resultMyCall.onError(errorMsg);
                            }

                            LogUtils.i("网络请求", "----------------------------- 请求结束 -----------------------------");
                            resultMyCall.onAfter();

                        }
                    }, 50);

                }
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                removeOnceTag();
                //网络请求成功
                LogUtils.i("网络请求", "请求code ==> " + response.code());
                EventBus.getDefault().post(new LoginMessage("请求code ==> " + response.code(),"GET"));

                String result = response.body().string();
                LogUtils.i("网络请求", result);
                resultMyCall.onSuccess(result);
                EventBus.getDefault().post(new LoginMessage("----------------------------- 请求结束 -----------------------------","GET"));
                LogUtils.i("网络请求", "----------------------------- 请求结束 -----------------------------");
                mDelivery.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        resultMyCall.onAfter();
                    }
                }, 50);

            }
        });
    }


    //亲测，post不支持缓存。有更熟悉者，望告知
    public OkGetBuilder cacheOnlineTime(int onlineTime) {
        //默认是不设置缓存(设置缓存后，在有网的情况下，在有效期内不会请求网络，只会请求缓存)
        //注意设置缓存后，在缓存有效期不能更改的。因为缓存文件里有有效期标识
        if (onlineTime != 0) {
            NetCacheInterceptor.getInstance().setOnlineTime(onlineTime);
        }
        return this;
    }


    public OkGetBuilder cacheOfflineTime(int offlineTime) {
        //默认不设置离线缓存(设置离线缓存后，在没有网络的情况下，在有效期内会直接读缓存。过了有效期会请求网络。当然是失败，
        // 注意前提是离线。有网络了会走上面的，请确认清楚概念)
        if (offlineTime != 0) {
            OfflineCacheInterceptor.getInstance().setOfflineCacheTime(offlineTime);
        }
        return this;
    }


    public OkGetBuilder url(String url) {
        this.url = url;
        return this;
    }

    public OkGetBuilder tryAgainCount(int tryAgainCount) {
        this.tryAgainCount = tryAgainCount;
        return this;
    }


    public OkGetBuilder onlyOneNet(boolean onlyOneNet) {
        this.onlyOneNet = onlyOneNet;
        return this;
    }


    public OkGetBuilder tag(String tag) {
        this.tag = tag;
        return this;
    }


    public OkGetBuilder headers(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public OkGetBuilder params(Map<String, String> params) {
        this.params = params;
        return this;
    }


    private Headers appendHeaders(Map<String, String> headers) {
        Headers.Builder headerBuilder = new Headers.Builder();
        if (headers == null || headers.isEmpty()) return null;

        for (String key : headers.keySet()) {
            headerBuilder.add(key, headers.get(key));
        }
        return headerBuilder.build();
    }


    //get 参数拼在url后面
    private String appendParams(String url, Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        if (url.indexOf("?") == -1) {
            sb.append(url + "?");
        } else {
            sb.append(url + "&");
        }

        if (params != null && !params.isEmpty()) {
            for (String key : params.keySet()) {
                sb.append(key).append("=").append(params.get(key)).append("&");
            }
        }

        sb = sb.deleteCharAt(sb.length() - 1);
        LogUtils.i("网络请求", "请求接口 ==>> " + sb.toString());
        EventBus.getDefault().post(new LoginMessage("请求接口 ==>> " + sb.toString(),"GET"));
        return sb.toString();
    }


}
