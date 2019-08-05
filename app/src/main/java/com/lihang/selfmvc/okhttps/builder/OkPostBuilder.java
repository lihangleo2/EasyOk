package com.lihang.selfmvc.okhttps.builder;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.lihang.selfmvc.R;
import com.lihang.selfmvc.base.MyApplication;
import com.lihang.selfmvc.bean.LoginMessage;
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
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by leo
 * on 2019/7/23.
 * post builder
 */
public class OkPostBuilder {
    private int type = 0;
    private MediaType mediaType;
    private static final int TYPE_PARAMS = 1;
    private static final int TYPE_JSON = 2;
    //json请求方式的mediaType
    private final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json;charset=utf-8");

    /**
     * 下面是解析参数，包括成功后 解析type
     */
    private String url;
    private String tag;
    private Map<String, String> headers;
    //post 键值对参数 (参数类型有很多，如键值对，string,byte,file,json等，这里主要封装2中json和键值对)
    private Map<String, String> params;
    //json 参数
    private String json;
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

    public OkPostBuilder() {
        this.okHttpClient = EasyOk.getInstance().getOkHttpClient();
        this.context = MyApplication.getContext();
        this.mDelivery = EasyOk.getInstance().getmDelivery();
    }


    public OkPostBuilder build() {
        Request.Builder mBuilder = new Request.Builder();
        validParams();
        mBuilder.url(url);
        LogUtils.i("网络请求", "请求接口 ==> " + url);
        EventBus.getDefault().post(new LoginMessage("请求接口 ==> " + url, "POST"));

        if (!TextUtils.isEmpty(tag)) {
            mBuilder.tag(tag);
        }

        if (headers != null) {
            mBuilder.headers(appendHeaders(headers));
        }
        RequestBody requestBody = null;
        switch (type) {
            case TYPE_PARAMS:
                FormBody.Builder formBody = new FormBody.Builder();
                LogUtils.i("网络请求", "请求参数  键值对 ==> " + GsonUtil.ser(params));
                EventBus.getDefault().post(new LoginMessage("请求参数  键值对 ==> " + GsonUtil.ser(params), "POST"));

                addParams(formBody, params);
                requestBody = formBody.build();
                break;
            case TYPE_JSON:
                LogUtils.i("网络请求", "请求参数  json ==> " + json);
                EventBus.getDefault().post(new LoginMessage("请求参数  json ==> " + json, "POST"));

                requestBody = RequestBody.create(mediaType != null ? mediaType : MEDIA_TYPE_JSON, json);
                break;
        }
        //这里的.post是区分get请求的关键步骤
        mBuilder.post(requestBody);

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

                if (currentAgainCount < tryAgainCount && tryAgainCount > 0) { // 如果超时并未超过指定次数，则重新连接
                    currentAgainCount++;
                    okHttpClient.newCall(call.request()).enqueue(this);
                    return;
                }

                removeOnceTag();
                if (resultMyCall != null) {
                    mDelivery.post(new Runnable() {
                        @Override
                        public void run() {
                            resultMyCall.onAfter();
                            String errorMsg;
                            if (e instanceof ConnectException) {
                                errorMsg = context.getString(R.string.network_unknow);
                            } else if (e instanceof SocketTimeoutException) {
                                errorMsg = context.getString(R.string.network_overtime);
                            } else {
                                errorMsg = context.getString(R.string.server_error);
                            }
                            resultMyCall.onError(errorMsg);
                        }
                    });

                }
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                removeOnceTag();
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
                            //这里解析出错，是int值写成了string类型。如果没错，即使错也会给个空值
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
            LogUtils.i("网络请求", "请求方式 ==> POST");
            EventBus.getDefault().post(new LoginMessage("请求方式 ==> POST", "POST"));
            LogUtils.i("网络请求", "请求开始");
            EventBus.getDefault().post(new LoginMessage("请求开始", "POST"));

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
                EventBus.getDefault().post(new LoginMessage("请求code ==> " + response.code(), "POST"));

                String result = response.body().string();
                LogUtils.i("网络请求", result);
                resultMyCall.onSuccess(result);
                EventBus.getDefault().post(new LoginMessage("----------------------------- 请求结束 -----------------------------", "POST"));
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


    public OkPostBuilder onlyOneNet(boolean onlyOneNet) {
        this.onlyOneNet = onlyOneNet;
        return this;
    }


    public OkPostBuilder tryAgainCount(int tryAgainCount) {
        this.tryAgainCount = tryAgainCount;
        return this;
    }


    public OkPostBuilder url(String url) {
        this.url = url;
        return this;
    }

    public OkPostBuilder tag(String tag) {
        this.tag = tag;
        return this;
    }


    public OkPostBuilder headers(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public OkPostBuilder params(Map<String, String> params) {
        this.params = params;
        return this;
    }

    public OkPostBuilder json(String json) {
        this.json = json;
        return this;
    }


    //拼接头部参数
    public Headers appendHeaders(Map<String, String> headers) {
        Headers.Builder headerBuilder = new Headers.Builder();
        if (headers == null || headers.isEmpty()) return null;

        for (String key : headers.keySet()) {
            headerBuilder.add(key, headers.get(key));
        }
        return headerBuilder.build();
    }

    //键值对拼接的参数
    private void addParams(FormBody.Builder builder, Map<String, String> params) {
        if (builder == null) {
            throw new IllegalArgumentException("builder can not be null .");
        }

        if (params != null && !params.isEmpty()) {
            for (String key : params.keySet()) {
                builder.add(key, params.get(key));
            }
        }
    }


    //判断参数方式只能是一个
    protected void validParams() {
        int count = 0;
        if (params != null) {
            type = TYPE_PARAMS;
            count++;
        }

        if (json != null) {
            type = TYPE_JSON;
            count++;
        }

        if (count <= 0 || count > 1) {
            throw new IllegalArgumentException("the params must has one and only one .");
        }
    }

}
