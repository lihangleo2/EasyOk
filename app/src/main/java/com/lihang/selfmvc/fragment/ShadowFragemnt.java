package com.lihang.selfmvc.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.lihang.selfmvc.R;
import com.lihang.selfmvc.base.BaseFragment;
import com.lihang.selfmvc.bean.LoginMessage;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by leo
 * on 2019/8/2.
 */
public class ShadowFragemnt extends BaseFragment {

    public static Fragment newFragment() {
        ShadowFragemnt fragment = new ShadowFragemnt();
        return fragment;
    }

    @Override
    public int getContentViewId() {
        return R.layout.fragment_shadow;
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onNetCallBack(int command, Object object) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onbackEvent(LoginMessage message) {

    }
}
