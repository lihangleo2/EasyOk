## EasyOk 一个看的懂的okhttp网络请求封装，本项目基于mvc开发模式进行二次封装

* 支持get请求
* 支持post请求
* 支持上传文件 
* 支持下载文件和断点续传  
* 有网络时，支持缓存（连接网络时的有效期）
* 断开网络，支持离线缓存（离线缓存有效期） 
* 多次请求同一url，在网络还在请求时，是否只请求一次
* 支持请求失败,自动重连

### 自己封装okhttp只是加深理解（如果你有更好的想法,欢迎issues），网上已经有很优秀的封装，本库借鉴了以下项目：
>  [okgo:https : //github.com/jeasonlzy/okhttp-OkGo](https://github.com/jeasonlzy/okhttp-OkGo)  
[okhttputils : https://github.com/hongyangAndroid/okhttputils](https://github.com/hongyangAndroid/okhttputils) 

## 友情链接
ui中再遇到阴影时,跟Ui小姐姐说,阴影部分别担心，我自己来
> 阴影布局，不管你是什么控件，放进阴影布局即刻享受你想要的阴影  
地址：[https://github.com/lihangleo2/ShadowLayout](https://github.com/lihangleo2/ShadowLayout)

有多种效果
> 一款多效果智能登录按钮,也可用于点赞动画等
地址：[https://github.com/lihangleo2/SmartLoadingView](https://github.com/lihangleo2/SmartLoadingView)

## 演示（最好打开权限，保证一切正常）
|get请求|post请求|上传文件|
|:---:|:---:|:---:|
|![](https://github.com/lihangleo2/EasyOk/blob/master/get.gif)|![](https://github.com/lihangleo2/EasyOk/blob/master/post.gif)|![](https://github.com/lihangleo2/EasyOk/blob/master/upload.gif)
|下载文件|
|![](https://github.com/lihangleo2/EasyOk/blob/master/download.gif)|

本项目做了基于mvc二次封装了，利于开发，可能不利于理解，下面做些简单介绍和用法
我会倒是用博客把okhttp和mvc封装分开来写（如果你有更好的理解，和更便捷的方式，麻烦告知。谢谢）

## get请求
```java
//这些是全部方法，没有用到的不使用
EasyOk.get().url("http://gank.io/api/xiandu/category/wow")
                .tag("cancleTag")
                //内部已经做了null处理，请求头部
                //.headers(paramsBuilder.getHeads())
                //内部已经做了null处理，请求参数
                //.params(paramsBuilder.getParams())
                //（默认不缓存）有网络的情况下，设置缓存有效期
                //.cacheOfflineTime(paramsBuilder.getCacheOfflineTime())
                //无网络的时候,设置离线缓存有效期
                //.cacheOnlineTime(paramsBuilder.getCacheOnlineTime())
                //默认只请求一次，多次请求同一url是否只请求一次
                //.onlyOneNet(paramsBuilder.isOnlyOneNet())
                //默认不重连，设置后，联网失败自动重连次数
                //.tryAgainCount(paramsBuilder.getTryAgainCount())
                //这里用抽象类，为了封装项目里ResultCall是接口类。
                //抽象类在这里有很大的好处，你可以把统一操作展示loading和关闭loading放在before和after里。如果不需要重写，甚至都不用实现方法，他会走super里的。
                .build().enqueue(new ResultMyCall<T>() {
            @Override
            public void onBefore() {
                super.onBefore();

            }

            @Override
            public void onAfter() {
                super.onAfter();

            }


            @Override
            public void onError(String errorMessage) {
                super.onError(errorMessage);

            }

            @Override
            public void onSuccess(Object response) {
                super.onSuccess(response);
               //如果你再new ResultMyCall的时候带了泛型，那么这里只需要
               //T bean = (T)response ;
               //如果没有带泛型，那么默认返回的string类型，
               //Sring bean = (String)response;
            }
        });

```

## post请求
post请求其实和get差不多。只不过内部和get区别就在于 mBuilder.post(requestBody);加上这区就是post请求了。还有几点要注意：
* post有多种requestBody，常用的有 键值对，json。其他的大家可以去上面2个优秀封装中查看
* 对于okhttp缓存，很明确一般用于不怎么变化的接口。所以post一般不用，本人亲测过后，缓存确实本来就不支持post



## 上传文件
这里是简单展示，未封装的是ResultMyCall，封装用到的接口是ResultCall。未封装是抽象类，不实现方法会默认走父类的。大部分和post一样。不同的是有个上传进度inprogress（float progress）;这里只是展示简单使用:
```java
        File file = new File("文件路径");
        Pair<String, File> map = new Pair<>("file", file);
        EasyOk.upload().url("url")
                .tag("upload")
                //.files(map)
                //.params(paramsBuilder.getParams())
                .build()
                .enqueue(new ResultMyCall() {
                    @Override
                    public void onBefore() {
                        super.onBefore();
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                    }

                    @Override
                    public void inProgress(float progress) {
                        super.inProgress(progress);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        super.onError(errorMessage);
                    }

                    @Override
                    public void onSuccess(Object response) {
                        super.onSuccess(response);
                    }
                });

```

如果使用了封装，如下，你可以看出特别方便：
* 要请求网络只需调用
```java
ModelSuperImpl.netWork().gankGet(ParamsBuilder.build().params(PARAMS.gank("android"))
                        .command(GANK_COMMAND), this);
//this是网络请求的回调，你只需要实现NetWorkListener接口，就能拿到网络回调。唯一不同的是这里要解析的类型要通过.type带进去，如果不传那么回到就是
//string类型。如果传了Object强壮一下就行了。如.type(new TypeToken<ResponModel<User>>() {}.getType()),具体看我博客
//这里把数据解析，网络请求失败，和虽然code=200,但请求接口失败如，关注失败。都封装在ModelBase里。如果不重写会走父类默认方法，要重写具体看博客
@Override
    public void onNetCallBack(int command, Object object) {
        switch (command) {
            case GANK_COMMAND:
                //这里是用于区分不同网络请求，如果你某个页面只有一个请求，那么这个指令可以忽略，请求的时候.command也可以忽略
                break;
        }
    }
//配合baseActivity和baseFragment使用，更是方便
```

## 下载文件
```jave
//下载文件只需要，this是OnDownloadListener,拿到网络回调，实现这个接口即刻
ModelSuperImpl.netWork().downApk(ParamsBuilder.build().path(path)
                        .fileName(fileName).tag("downApk").resume(true), this);

//这个接口如下
public interface OnDownloadListener {
    /**
     * 下载成功之后的文件
     */
    void onDownloadSuccess(File file);

    void onDownLoadTotal(long total);

    /**
     * 下载进度
     */
    void onDownloading(int progress);

    /**
     * 下载异常信息
     */
    void onDownloadFailed(Exception e);
}
                        
                        
```

可能有人发现了url去拿了，外面调用接口，我只传入了参数，把url什么的都封装在了ModelSuperImpl里了。这样的话不同页面请求同一网络请求，你只需要直接调用，传参数即可。
#### 看到这里给个star吧,后续出2篇博客
* okhttp简单封装（基于二次封装）
* mvc开发

