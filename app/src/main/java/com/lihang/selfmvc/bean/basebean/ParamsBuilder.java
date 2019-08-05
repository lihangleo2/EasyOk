package com.lihang.selfmvc.bean.basebean;

import android.content.Context;

import java.lang.reflect.Type;
import java.util.HashMap;

/**
 * Created by leo
 * on 2019/7/11.
 */
public class ParamsBuilder {
    //请求网络的url(必填)
    private String url;
    //网络回调的int值(必填)
    private int command;
    //网络返回的type类型(选填)不填，则会返回string类型
    private Type type;
    //网络请求需要带的头部信息(选填，不填为null)
    private HashMap<String, String> heads;
    //网络请求需要带的参数(选填，不填为null)
    private HashMap<String, String> params;
    //网络loading需要带的文字信息(选填，不填为null)
    private String loadMessage;
    //是否显示网络loading(默认为显示loading)
    private boolean isShowDialog = true;
    //网络请求的tag,可根据tag取消网络请求(选填，不填：默认当前宿主类名，退出后自动取消)
    private String tag;
    //是否重写网络问题还是超时问题对回调进行一个重写
    //如果是true,则在回调的时候可对那部分额外操作，除了弹提示还可以做别的操作
    //(选填，不填：重写不了且只弹提示)
    private boolean overrideError;
    //json上传要带的参数
    private String json;
    //网络接口code=200, 但没有成功，此用户已关注
    //需要重写带true，重写可以写逻辑包括弹提示
    //不需要重写只弹提示
    private boolean successErrorOverrid;

    //离线缓存时间  单位秒
    private int cacheOfflineTime;
    //有网络请求时缓存最大时间
    private int cacheOnlineTime;
    //多次点击按钮，只进行一次联网请求
    //场景：网络还在loading，又点了一次请求，那么不发送新请求，只显示loading
    private boolean onlyOneNet = true;
    //联网失败，重试次数
    private int tryAgainCount;
    //如果是在网络请求接口回调不是activity,也不是fragment，用于传context
    //用于showdialog
    private Context context;


    /**
     * 下载文件才用的到
     */
    private String path;
    private String fileName;
    //是否开启断点续传,要注意的是开启断点续传，要保证下载的是同一文件
    //默认是不开启断点续传，除非判断要下载文件和当前未下载文件属于同一文件
    //如果不是那么重新下载，会清掉之前的文件。
    private boolean resume;

    public ParamsBuilder path(String path) {
        this.path = path;
        return this;
    }

    public ParamsBuilder fileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public ParamsBuilder resume(boolean resume) {
        this.resume = resume;
        return this;
    }


    public static ParamsBuilder build() {
        return new ParamsBuilder();
    }

    public ParamsBuilder cacheOnlineTime(int cacheOnlineTime) {
        this.cacheOnlineTime = cacheOnlineTime;
        return this;
    }


    public ParamsBuilder cacheOfflineTime(int cacheOfflineTime) {
        this.cacheOfflineTime = cacheOfflineTime;
        return this;
    }

    public ParamsBuilder tryAgainCount(int tryAgainCount) {
        this.tryAgainCount = tryAgainCount;
        return this;
    }


    public ParamsBuilder onlyOneNet(boolean onlyOneNet) {
        this.onlyOneNet = onlyOneNet;
        return this;
    }


    public ParamsBuilder context(Context context) {
        this.context = context;
        return this;
    }


    public ParamsBuilder successErrorOverrid(boolean successErrorOverrid) {
        this.successErrorOverrid = successErrorOverrid;
        return this;
    }

    public ParamsBuilder overrideError(boolean overrideError) {
        this.overrideError = overrideError;
        return this;
    }

    public ParamsBuilder json(String json) {
        this.json = json;
        return this;
    }


    public ParamsBuilder tag(String tag) {
        this.tag = tag;
        return this;
    }

    public ParamsBuilder isShowDialog(boolean isShowDialog) {
        this.isShowDialog = isShowDialog;
        return this;
    }


    public ParamsBuilder loadMessage(String loadMessage) {
        this.loadMessage = loadMessage;
        return this;
    }


    public ParamsBuilder params(HashMap<String, String> params) {
        this.params = params;
        return this;
    }


    public ParamsBuilder heads(HashMap<String, String> heads) {
        this.heads = heads;
        return this;
    }


    public ParamsBuilder type(Type type) {
        this.type = type;
        return this;
    }

    public ParamsBuilder command(int command) {
        this.command = command;
        return this;
    }


    public ParamsBuilder url(String url) {
        this.url = url;
        return this;
    }


    /**
     * get and set 方法
     */
    public int getCacheOfflineTime() {
        return cacheOfflineTime;
    }

    public void setCacheOfflineTime(int cacheOfflineTime) {
        this.cacheOfflineTime = cacheOfflineTime;
    }

    public int getCacheOnlineTime() {
        return cacheOnlineTime;
    }

    public void setCacheOnlineTime(int cacheOnlineTime) {
        this.cacheOnlineTime = cacheOnlineTime;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public boolean isOnlyOneNet() {
        return onlyOneNet;
    }

    public void setOnlyOneNet(boolean onlyOneNet) {
        this.onlyOneNet = onlyOneNet;
    }

    public int getTryAgainCount() {
        return tryAgainCount;
    }

    public void setTryAgainCount(int tryAgainCount) {
        this.tryAgainCount = tryAgainCount;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public HashMap<String, String> getHeads() {
        return heads;
    }

    public void setHeads(HashMap<String, String> heads) {
        this.heads = heads;
    }

    public HashMap<String, String> getParams() {
        return params;
    }

    public void setParams(HashMap<String, String> params) {
        this.params = params;
    }

    public String getLoadMessage() {
        return loadMessage;
    }

    public void setLoadMessage(String loadMessage) {
        this.loadMessage = loadMessage;
    }

    public boolean isShowDialog() {
        return isShowDialog;
    }

    public void setShowDialog(boolean showDialog) {
        isShowDialog = showDialog;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public boolean isOverrideError() {
        return overrideError;
    }

    public void setOverrideError(boolean overrideError) {
        this.overrideError = overrideError;
    }

    public boolean isSuccessErrorOverrid() {
        return successErrorOverrid;
    }

    public void setSuccessErrorOverrid(boolean successErrorOverrid) {
        this.successErrorOverrid = successErrorOverrid;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isResume() {
        return resume;
    }

    public void setResume(boolean resume) {
        this.resume = resume;
    }
}
