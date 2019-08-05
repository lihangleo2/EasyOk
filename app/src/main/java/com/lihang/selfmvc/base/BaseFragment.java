package com.lihang.selfmvc.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.lihang.selfmvc.model.NetWorkListener;
import com.lihang.selfmvc.okhttps.EasyOk;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by leo
 * on 2019/7/3.
 */
public abstract class BaseFragment extends Fragment implements View.OnClickListener,NetWorkListener {
    //获取当前fragment布局文件
    public abstract int getContentViewId();

    //设置监听事件
    protected abstract void setListener();

    //处理逻辑业务
    protected abstract void processLogic(Bundle savedInstanceState);

    protected View mContentView;
    private Unbinder mUnbinder;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 避免多次从xml中加载布局文件
        if (mContentView == null) {
            setContentView(getContentViewId());
            setListener();
            processLogic(savedInstanceState);
        } else {
            ViewGroup parent = (ViewGroup) mContentView.getParent();
            if (parent != null) {
                parent.removeView(mContentView);
            }
        }
        return mContentView;
    }

    protected void setContentView(@LayoutRes int layoutResID) {
        mContentView = LayoutInflater.from(getActivity()).inflate(layoutResID, null);
        mUnbinder = ButterKnife.bind(this, mContentView);
        EventBus.getDefault().register(this);
    }


    //简单跳转
    public void transfer(Class<?> clazz) {
        Intent intent = new Intent(getActivity(), clazz);
        startActivity(intent);
    }


    //快速获取textView 或 EditText上文字内容
    public String getStringByUI(View view) {
        if (view instanceof EditText) {
            return ((EditText) view).getText().toString().trim();
        } else if (view instanceof TextView) {
            return ((TextView) view).getText().toString().trim();
        }
        return "";
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
        //退出一个页面。取消这个页面所有正在进行的网络请求
        EasyOk.getInstance().cancleOkhttpTag(this.getClass().toString());
        EventBus.getDefault().unregister(this);
    }


}
