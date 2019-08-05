package com.lihang.selfmvc.model;

import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Pair;

import com.google.gson.reflect.TypeToken;
import com.lihang.selfmvc.bean.User;
import com.lihang.selfmvc.bean.basebean.ParamsBuilder;
import com.lihang.selfmvc.bean.basebean.ResponModel;
import com.lihang.selfmvc.common.SystemConst;
import com.lihang.selfmvc.okhttps.okcallback.OnDownloadListener;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.util.ArrayList;

import io.reactivex.functions.Consumer;

/**
 * Created by leo
 * on 2019/7/31.
 */
public class ModelSuperImpl extends ModelBase {
    private static final ModelSuperImpl ourInstance = new ModelSuperImpl();

    public static ModelSuperImpl netWork() {
        return ourInstance;
    }

    public static ModelPermissionImpl permission() {
        return new ModelPermissionImpl();
    }

    private ModelSuperImpl() {

    }

    //模拟搜索方法,外面参数过多，如果在多个页面都用到这个网络请求，把重复的写在里面
    public void gankGet(ParamsBuilder paramsBuilder, NetWorkListener netWorkListener) {
        paramsBuilder.url(SystemConst.GANK_GET)
//                .type(new TypeToken<ResponModel<String>>() {
//                }.getType())
        ;
        sendOkHttpGet(paramsBuilder, netWorkListener);
    }


    public void gankPost(ParamsBuilder paramsBuilder, NetWorkListener netWorkListener) {
        paramsBuilder.url(SystemConst.GANK_POST)
//                .type(new TypeToken<ResponModel<User>>() {
//                }.getType())
        ;
        sendOkHttpPost(paramsBuilder, netWorkListener);
    }


    public void downApk(ParamsBuilder paramsBuilder, OnDownloadListener onDownloadListener) {
        paramsBuilder.url(SystemConst.QQ_APK);
        sendOkHttpDownload(paramsBuilder, onDownloadListener);
    }


    //不同file 不同key
    public void uploadPic(ParamsBuilder paramsBuilder, NetWorkListener netWorkListener, Pair<String, File>... files) {
        paramsBuilder.url(SystemConst.UPLOAD_PIC)
//                .type(new TypeToken<ResponModel<String>>() {
//                }.getType())
        ;
        sendOkHttpUpload(paramsBuilder, netWorkListener, files);
    }

    //同一key 不同file
    public void uploadPic(ParamsBuilder paramsBuilder, NetWorkListener netWorkListener, String key, ArrayList<File> files) {
        paramsBuilder.url(SystemConst.UPLOAD_PIC)
//                .type(new TypeToken<ResponModel<String>>() {
//                }.getType())
        ;
        sendOkHttpUpload(paramsBuilder, netWorkListener, key, files);
    }


}
