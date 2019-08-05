package com.lihang.selfmvc.common;

import com.lihang.selfmvc.utils.GsonUtil;

import java.util.HashMap;

/**
 * Created by leo on 2017/9/13.
 * 键值对上传类
 */

public class PARAMS {
    public static String pageSize = "10";

    /**
     * 登录
     */
    public static String login(String userName, String password) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("userName", userName);
        map.put("password", password);
        map.put("userType", 1);
        return GsonUtil.ser(map);
    }


    public static HashMap<String, String> gank(String en_name) {
        HashMap<String, String> map = new HashMap<>();
        map.put("en_name", en_name);
        return map;
    }

    public static HashMap<String, String> gankPost(String url, String desc, String who, String type, String debug) {
        HashMap<String, String> map = new HashMap<>();
        map.put("url", url);
        map.put("desc", desc);
        map.put("who", who);
        map.put("type", type);
        map.put("debug", debug);
        return map;
    }


    /**
     * 获取验证码
     */
    public static HashMap<String, String> getSmscode(String mobile) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("mobile", mobile);
        return map;
    }


    /**
     * 脸搜上传图片
     */
    public static HashMap<String, String> uploadImage(String keyName, String mimeType, String pathType) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("keyName", keyName);
        map.put("mimeType", mimeType);
        map.put("pathType", pathType);
        return map;
    }

    /**
     * 脸搜上传图片
     */
    public static HashMap<String, String> uploadPic(String sequence) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("sequence", sequence);
        return map;
    }
}
