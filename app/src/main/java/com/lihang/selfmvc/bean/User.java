
package com.lihang.selfmvc.bean;

import java.io.Serializable;

public class User implements Serializable {

    private String userName;
    private String lockScreenPassword;
    private int loginErrorTimes;

    public int getLoginErrorTimes() {
        return loginErrorTimes;
    }

    public void setLoginErrorTimes(int loginErrorTimes) {
        this.loginErrorTimes = loginErrorTimes;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLockScreenPassword() {
        return lockScreenPassword;
    }

    public void setLockScreenPassword(String lockScreenPassword) {
        this.lockScreenPassword = lockScreenPassword;
    }
}
