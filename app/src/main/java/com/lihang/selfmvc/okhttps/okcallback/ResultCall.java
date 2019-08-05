package com.lihang.selfmvc.okhttps.okcallback;

/**
 * Created by leo
 * on 2019/7/31.
 * 用于封装
 */
public interface ResultCall {
    //请求网络之前，一般展示loading
    void onBefore();
    //请求网络结束，消失loading
    void onAfter();
    //网络请求失败,返回失败原因
    void onError(String message);
    //网络请求成功，但有可能不是200
    void onSuccess(String response);
    //监听上传图片的进度(目前支持图片上传)
     void inProgress(float progress);

}
