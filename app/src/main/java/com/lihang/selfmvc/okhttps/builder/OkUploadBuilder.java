package com.lihang.selfmvc.okhttps.builder;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Pair;

import com.lihang.selfmvc.R;
import com.lihang.selfmvc.base.MyApplication;
import com.lihang.selfmvc.bean.LoginMessage;
import com.lihang.selfmvc.okhttps.EasyOk;
import com.lihang.selfmvc.okhttps.okcallback.ResultCall;
import com.lihang.selfmvc.okhttps.okcallback.ResultMyCall;
import com.lihang.selfmvc.okhttps.request.CountingRequestBody;
import com.lihang.selfmvc.utils.GsonUtil;
import com.lihang.selfmvc.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.FileNameMap;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URLConnection;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by leo
 * on 2019/7/29.
 * 文件上传 builder
 */
public class OkUploadBuilder {

    private Pair<String, File>[] files;
    private String url;
    private String tag;
    //post 键值对参数 (参数类型有很多，如键值对，string,byte,file,json等，这里主要封装2中json和键值对)
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
    private RequestBody requestBody;

    public OkUploadBuilder() {
        this.okHttpClient = EasyOk.getInstance().getOkHttpClient();
        this.context = MyApplication.getContext();
        this.mDelivery = EasyOk.getInstance().getmDelivery();
    }


    public OkUploadBuilder build() {
        //这是文件上传添加的参数 和 requestBody
        MultipartBody.Builder mBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if (params == null) {
            //如果参数为空
            addFils(mBuilder);
        } else {
            if (!params.isEmpty()) {
                for (String key : params.keySet()) {
                    mBuilder.addFormDataPart(key, params.get(key));
                }
            }
            addFils(mBuilder);
        }
        requestBody = mBuilder.build();
        return this;
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

        /**
         * 返回上传进度移动这里，因为要用我们resultCall把进度返回出去
         * */
        //这是正常的请求头
        Request.Builder mRquesBuilder = new Request.Builder();
        mRquesBuilder.url(url);
        if (!TextUtils.isEmpty(tag)) {
            mRquesBuilder.tag(tag);
        }

        //进项这部操作才能监听进度，来自鸿洋okHttpUtils
        RequestBody requestBodyProgress = new CountingRequestBody(requestBody, new CountingRequestBody.Listener() {
            @Override
            public void onRequestProgress(final long bytesWritten, final long contentLength) {
                LogUtils.i("我这里是进度吗", bytesWritten + "=======" + contentLength);
                mDelivery.post(new Runnable() {
                    @Override
                    public void run() {
                        resultMyCall.inProgress(bytesWritten * 1.0f / contentLength);
                    }
                });

            }
        });

        mRquesBuilder.post(requestBodyProgress);
        Request okHttpRequest = mRquesBuilder.build();

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
                                //如果200code后，status 1表示发布成功，2表示发布失败。bean对象一致，
                                //因为解析的问题如果数据类型不出错，是不会解析错误的。
                                //如果2失败只是弹个提示的话，可以在你的baseActivity里进行二次封装，
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


    public void enqueue(final ResultCall resultMyCall) {
        if (resultMyCall != null) {
            LogUtils.i("网络请求", "上传图片");
            EventBus.getDefault().post(new LoginMessage("上传图片","UPLOAD"));
            LogUtils.i("网络请求", "请求开始");
            EventBus.getDefault().post(new LoginMessage("请求开始","UPLOAD"));

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

        /**
         * 返回上传进度移动这里，因为要用我们resultCall把进度返回出去
         * */
        //这是正常的请求头
        Request.Builder mRquesBuilder = new Request.Builder();
        mRquesBuilder.url(url);
        if (!TextUtils.isEmpty(tag)) {
            mRquesBuilder.tag(tag);
        }
        LogUtils.i("网络请求", "请求接口 ==> " + url);
        EventBus.getDefault().post(new LoginMessage("请求接口 ==> " + url,"UPLOAD"));
        if (params != null) {
            LogUtils.i("网络请求", "请求参数 ==> " + GsonUtil.ser(params));
            EventBus.getDefault().post(new LoginMessage("请求参数 ==> " + GsonUtil.ser(params),"UPLOAD"));

        }

        //进项这部操作才能监听进度，来自鸿洋okHttpUtils
        RequestBody requestBodyProgress = new CountingRequestBody(requestBody, new CountingRequestBody.Listener() {
            @Override
            public void onRequestProgress(final long bytesWritten, final long contentLength) {
                mDelivery.post(new Runnable() {
                    @Override
                    public void run() {
                        resultMyCall.inProgress(bytesWritten * 1.0f / contentLength);
                        LogUtils.i("网络请求", "请求进度 ==> " + bytesWritten * 1.0f / contentLength);
                    }
                });

            }
        });

        mRquesBuilder.post(requestBodyProgress);
        Request okHttpRequest = mRquesBuilder.build();
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
                                EventBus.getDefault().post(new LoginMessage("请求失败原因 ==> " + e.toString(),"UPLOAD"));

                                resultMyCall.onError(errorMsg);
                            }

                            EventBus.getDefault().post(new LoginMessage("----------------------------- 请求结束 -----------------------------","UPLOAD"));
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
                EventBus.getDefault().post(new LoginMessage("请求code ==> " + response.code(),"UPLOAD"));

                String result = response.body().string();
                LogUtils.i("网络请求", result);
                EventBus.getDefault().post(new LoginMessage(result,"UPLOAD"));

                resultMyCall.onSuccess(result);
                LogUtils.i("网络请求", "----------------------------- 请求结束 -----------------------------");
                EventBus.getDefault().post(new LoginMessage("----------------------------- 请求结束 -----------------------------","UPLOAD"));

                mDelivery.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        resultMyCall.onAfter();
                    }
                }, 50);

            }
        });
    }


    public OkUploadBuilder onlyOneNet(boolean onlyOneNet) {
        this.onlyOneNet = onlyOneNet;
        return this;
    }


    public OkUploadBuilder tryAgainCount(int tryAgainCount) {
        this.tryAgainCount = tryAgainCount;
        return this;
    }


    public OkUploadBuilder files(Pair<String, File>... files) {
        this.files = files;
        return this;
    }

    public OkUploadBuilder url(String url) {
        this.url = url;
        return this;
    }

    public OkUploadBuilder tag(String tag) {
        this.tag = tag;
        return this;
    }


    public OkUploadBuilder params(Map<String, String> params) {
        this.params = params;
        return this;
    }


    public void addFils(MultipartBody.Builder mBuilder) {
        if (files != null) {
            RequestBody fileBody = null;
            for (int i = 0; i < files.length; i++) {
                if (files[i] != null) {
                    Pair<String, File> filePair = files[i];
                    String fileKeyName = filePair.first;
                    File file = filePair.second;
                    String fileName = file.getName();
                    fileBody = RequestBody.create(MediaType.parse(guessMimeType(fileName)), file);
                    mBuilder.addPart(Headers.of("Content-Disposition",
                            "form-data; name=\"" + fileKeyName + "\"; filename=\"" + fileName + "\""),
                            fileBody);
                }
            }
        }
    }


    private String guessMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(path);
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
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

}
