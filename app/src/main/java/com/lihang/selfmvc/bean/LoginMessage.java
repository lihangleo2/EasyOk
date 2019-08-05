package com.lihang.selfmvc.bean;

import java.io.Serializable;

/**
 * Created by leo
 * on 2019/8/2.
 */
public class LoginMessage implements Serializable {
    private String message;
    private String type;

    public LoginMessage(String message, String type) {
        this.message = message;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
