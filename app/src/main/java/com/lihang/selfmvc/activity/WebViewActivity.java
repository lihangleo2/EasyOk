package com.lihang.selfmvc.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.lihang.selfmvc.R;
import com.lihang.selfmvc.base.BaseActivity;
import com.lihang.selfmvc.utils.ToastUtils;

import butterknife.BindView;


/**
 * Created by leo2 on 2017/10/31.
 * webView最终版，解决各种不可见问题，
 */

public class WebViewActivity extends BaseActivity {
    @BindView(R.id.webView)
    WebView webView;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    private String urlStr;


    @Override
    public int getContentViewId() {
        return R.layout.activity_webview;
    }

    @Override
    public void setListener() {
    }

    @Override
    protected void processLogic() {
        urlStr = "https://github.com/lihangleo2";
        //声明WebSettings子类
        WebSettings webSettings = webView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        //设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        webSettings.setDomStorageEnabled(true);

        //缩放操作
        webSettings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件

        //其他细节操作
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); //开启缓存LOAD_CACHE_ELSE_NETWORK//LOAD_NO_CACHE关闭缓存
        webSettings.setAllowFileAccess(true); //设置可以访问文件
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //解决webView不加载图片
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }


        ////步骤3. 复写shouldOverrideUrlLoading()方法，使得打开网页时不调用系统浏览器， 而是在本WebView中显示
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                try {
                    if (url.startsWith("http:") || url.startsWith("https:")) {
                        view.loadUrl(url);
                        return false;
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                        return true;
                    }
                } catch (Exception e) {
                    return true;
                }

            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                ToastUtils.showToast("网页加载失败");

            }



            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            //webView加载结束后的动作
            @Override
            public void onPageFinished(WebView view, String url) {
                //设定加载结束的操作
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }


            }
        });


        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (progressBar.getVisibility() == View.GONE && newProgress != 100) {
                    //点击webView内部，进度条可见
                    progressBar.setVisibility(View.VISIBLE);
                }

                if (newProgress == 100 && progressBar.getVisibility() == View.VISIBLE) {
                    //bug:不走finish所以不能设置不可见
                    progressBar.setVisibility(View.GONE);
                }


                if (progressBar != null) {
                    progressBar.setProgress(newProgress);
                }
            }
        });


        webView.loadUrl(urlStr);
        //js互调相关
        webView.addJavascriptInterface(new CrosswalkInterface(this), "JSbridge");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();// 返回前一个页面
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.destroy();
        }
    }

    @Override
    public void onNetCallBack(int command, Object object) {

    }

    public class CrosswalkInterface {
        Context context;

        public CrosswalkInterface(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void exit() {
            finish();
        }
    }

}
