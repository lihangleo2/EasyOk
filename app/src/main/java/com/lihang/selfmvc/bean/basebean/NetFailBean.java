package com.lihang.selfmvc.bean.basebean;

import java.io.Serializable;

/**
 * Created by leo
 * on 2019/7/31.
 */
public class NetFailBean implements Serializable {
    private String message;

    public NetFailBean(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
