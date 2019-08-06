package com.lihang.selfmvc.activity;

import android.Manifest;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import com.gyf.barlibrary.ImmersionBar;
import com.lihang.ShadowLayout;
import com.lihang.selfmvc.R;
import com.lihang.selfmvc.adapter.ViewPagerFragmentMoreAdapter;
import com.lihang.selfmvc.base.BaseActivity;
import com.lihang.selfmvc.model.ModelSuperImpl;
import com.lihang.selfmvc.model.PermissionListener;
import com.lihang.selfmvc.utils.ToastUtils;
import com.lihang.selfmvc.utils.networks.NetStateChangeObserver;
import com.lihang.selfmvc.utils.networks.NetStateChangeReceiver;
import com.lihang.selfmvc.utils.networks.NetworkType;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.UIUtil;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.badge.BadgePagerTitleView;
import butterknife.BindView;

/**
 * Created by leo
 * on 2019/8/1.
 */
public class MainActivity extends BaseActivity implements NetStateChangeObserver, PermissionListener {
    @BindView(R.id.magic_indicator)
    MagicIndicator magicIndicator;
    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.shadow_permission)
    ShadowLayout shadow_permission;
    @BindView(R.id.text_quanx)
    TextView text_quanx;
    String[] titles = {"GET请求", "下载文件", "POST请求", "上传文件", "阴影控件"};
    private ViewPagerFragmentMoreAdapter adapter;
    private CommonNavigator commonNavigator;
    //沉浸式状态栏
    protected ImmersionBar mImmersionBar;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 11:
                    ModelSuperImpl.permission().requestPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
                    break;
            }
        }
    };

    @Override
    public int getContentViewId() {
        return R.layout.activity_main;
    }

    @Override
    public void setListener() {
        shadow_permission.setOnClickListener(this);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                if (i == 4) {
                    text_quanx.setText("我的github");
                } else {
                    text_quanx.setText("请打开权限,保证功能正常");
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    @Override
    protected void processLogic() {
        //延迟请求权限
        mHandler.sendEmptyMessageDelayed(11, 1200);
        //注册广播
        NetStateChangeReceiver.registerReceiver(this);
        mImmersionBar = ImmersionBar.with(this)
                .statusBarColor(R.color.shape2)
                //布局是否在状态栏下
                .fitsSystemWindows(true);
        mImmersionBar.init();
        //主动设置viewPager缓存
        viewPager.setOffscreenPageLimit(6);
        adapter = new ViewPagerFragmentMoreAdapter(getSupportFragmentManager(), titles);
        viewPager.setAdapter(adapter);
        initMagicIndicator();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.shadow_permission:
                if (getStringByUI(text_quanx).equals("我的github")) {
                    transfer(WebViewActivity.class);
                } else {
                    ModelSuperImpl.permission().requestPermission(MainActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
                }
                break;
        }
    }

    @Override
    public void onNetCallBack(int command, Object object) {
    }


    private void initMagicIndicator() {
        magicIndicator.setBackgroundColor(getResources().getColor(R.color.shape2));
        commonNavigator = new CommonNavigator(MainActivity.this);
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return titles == null ? 0 : titles.length;
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                final BadgePagerTitleView badgePagerTitleView = new BadgePagerTitleView(context);

                SimplePagerTitleView simplePagerTitleView = new ColorTransitionPagerTitleView(context);
                simplePagerTitleView.setText(titles[index]);
                simplePagerTitleView.setNormalColor(getResources().getColor(R.color.unselect));
                simplePagerTitleView.setSelectedColor(getResources().getColor(R.color.white));
                simplePagerTitleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
                simplePagerTitleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewPager.setCurrentItem(index);
                    }
                });

                badgePagerTitleView.setInnerPagerTitleView(simplePagerTitleView);
                return badgePagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator indicator = new LinePagerIndicator(context);
                indicator.setMode(LinePagerIndicator.MODE_EXACTLY);
                indicator.setLineHeight(UIUtil.dip2px(context, 3));
                indicator.setLineWidth(UIUtil.dip2px(context, 15));
                indicator.setRoundRadius(UIUtil.dip2px(context, 3));
                indicator.setStartInterpolator(new AccelerateInterpolator());
                indicator.setEndInterpolator(new DecelerateInterpolator(2.0f));
                indicator.setColors(getResources().getColor(R.color.pink));
                return indicator;
            }
        });
        magicIndicator.setNavigator(commonNavigator);
        ViewPagerHelper.bind(magicIndicator, viewPager);

    }


    @Override
    protected void onResume() {
        super.onResume();
        //绑定观察者
        NetStateChangeReceiver.registerObserver(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //解绑观察者
        NetStateChangeReceiver.unRegisterObserver(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mImmersionBar.destroy();
        //注释广播
        NetStateChangeReceiver.unRegisterReceiver(this);
    }


    @Override
    public void onNetDisconnected() {
        //断开网络连接回调
        ToastUtils.showToast("网络断开!");
    }

    @Override
    public void onNetConnected(NetworkType networkType) {
        //连接网络回调
        ToastUtils.showToast("网络已连接" + networkType.toString());
    }

    @Override
    public void permissionSuccess(int command) {

    }

    @Override
    public void permissionFail(int command) {
        ToastUtils.showToast("为了功能正常，请打开相关权限");
    }
}
