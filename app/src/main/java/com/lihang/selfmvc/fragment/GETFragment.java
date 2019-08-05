package com.lihang.selfmvc.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;
import com.lihang.selfmvc.R;
import com.lihang.selfmvc.base.BaseFragment;
import com.lihang.selfmvc.bean.LoginMessage;
import com.lihang.selfmvc.bean.basebean.ParamsBuilder;
import com.lihang.selfmvc.common.PARAMS;
import com.lihang.selfmvc.model.ModelSuperImpl;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import butterknife.BindView;


/**
 * Created by leo
 * on 2019/1/9.
 */
public class GETFragment extends BaseFragment {
    final int GANK_COMMAND = 99;
    @BindView(R.id.txt_get)
    TextView txt_get;
    @BindView(R.id.txt_content)
    TextView txt_content;
    private StringBuffer sbf;

    public static Fragment newFragment() {
        GETFragment fragment = new GETFragment();
        return fragment;
    }

    @Override
    public int getContentViewId() {
        return R.layout.fragment_get;
    }

    @Override
    protected void setListener() {
        txt_get.setOnClickListener(this);
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txt_get:
                sbf = new StringBuffer();
                ModelSuperImpl.netWork().gankGet(ParamsBuilder.build().params(PARAMS.gank("android"))
                        .command(GANK_COMMAND), this);
                break;
        }
    }

    @Override
    public void onNetCallBack(int command, Object object) {
        switch (command) {
            case GANK_COMMAND:
                String result = (String) object;
                sbf.append(result + "\n");
                txt_content.setText(sbf.toString());
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onbackEvent(LoginMessage message) {
        if (message.getType().equals("GET")) {
            sbf.append(message.getMessage() + "\n");
            txt_content.setText(sbf.toString());
        }

    }


}
