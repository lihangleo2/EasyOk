package com.lihang.selfmvc.fragment;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lihang.selfmvc.R;
import com.lihang.selfmvc.base.BaseFragment;
import com.lihang.selfmvc.bean.LoginMessage;
import com.lihang.selfmvc.bean.basebean.ParamsBuilder;
import com.lihang.selfmvc.common.PARAMS;
import com.lihang.selfmvc.model.ModelSuperImpl;
import com.lihang.selfmvc.model.PermissionListener;
import com.lihang.selfmvc.utils.LogUtils;
import com.lihang.selfmvc.utils.ToastUtils;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.loader.GlideImageLoader;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.lzy.imagepicker.view.CropImageView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;

/**
 * Created by leo
 * on 2019/8/2.
 */
public class UploadFragment extends BaseFragment implements PermissionListener {
    final int UPLOAD_COMMAND = 99;
    @BindView(R.id.txt_upload)
    TextView txt_upload;
    @BindView(R.id.txt_select)
    TextView txt_select;
    @BindView(R.id.txt_content)
    TextView txt_content;
    @BindView(R.id.img_)
    ImageView img_;
    private StringBuffer sbf;
    private ArrayList<ImageItem> images = null;

    public static Fragment newFragment() {
        UploadFragment fragment = new UploadFragment();
        return fragment;
    }

    @Override
    public int getContentViewId() {
        return R.layout.fragment_upload;
    }

    @Override
    protected void setListener() {
        txt_upload.setOnClickListener(this);
        txt_select.setOnClickListener(this);
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {


        //上传单张图片
//        File file = new File("/storage/emulated/0/atman/1564387709592.jpg");
//        Pair<String, File> map = new Pair<>("file", file);
//        ModelSuperImpl.netWork().uploadPic(ParamsBuilder.build().params(PARAMS.uploadPic("1")).command(UPLOAD_COMMAND),
//                this, map);


        //不同key 上传多张图片
//        File file = new File("/storage/emulated/0/atman/1564387709592.jpg");
//        File file2 = new File("/storage/emulated/0/atman/156438592.jpg");
//        File file3 = new File("/storage/emulated/0/atman/6438592.jpg");
//        Pair<String, File> map = new Pair<>("file", file);
//        Pair<String, File> map2 = new Pair<>("file2", file2);
//        Pair<String, File> map3 = new Pair<>("file3", file3);
//        ModelSuperImpl.netWork().uploadPic(ParamsBuilder.build().params(PARAMS.uploadPic("1")).command(UPLOAD_COMMAND),
//                this, map, map2, map3);


        //同一个key 上传多张图片
//        File file = new File("/storage/emulated/0/atman/1564387709592.jpg");
//        File file2 = new File("/storage/emulated/0/atman/156438592.jpg");
//        File file3 = new File("/storage/emulated/0/atman/6438592.jpg");
//        ArrayList<File> files = new ArrayList<>();
//        files.add(file);
//        files.add(file2);
//        files.add(file3);
//        ModelSuperImpl.netWork().uploadPic(ParamsBuilder.build().params(PARAMS.uploadPic("1")).command(UPLOAD_COMMAND),
//                this, "file",files);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txt_upload:
                ModelSuperImpl.permission().requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
                break;

            case R.id.txt_select:
                ImagePicker imagePicker = ImagePicker.getInstance();
                imagePicker.setStyle(CropImageView.Style.CIRCLE);
                imagePicker.setMultiMode(false);

//                imagePicker.setShowCamera(true);
//                imagePicker.setCrop(true);
//                imagePicker.setSaveRectangle(true);
//                imagePicker.setStyle(CropImageView.Style.CIRCLE);
//                Integer radius = Integer.valueOf("140");
//                radius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, radius, getResources().getDisplayMetrics());
//                imagePicker.setFocusWidth(radius * 2);
//                imagePicker.setFocusHeight(radius * 2);

//                imagePicker.setOutPutX(Integer.valueOf("800"));
//                imagePicker.setOutPutY(Integer.valueOf("800"));

                Intent intent = new Intent(getActivity(), ImageGridActivity.class);
                intent.putExtra(ImageGridActivity.EXTRAS_IMAGES, images);
                startActivityForResult(intent, 100);
                break;
        }
    }

    @Override
    public void onNetCallBack(int command, Object object) {
        switch (command) {
            case UPLOAD_COMMAND:
                String result = (String) object;
                sbf.append(result + "\n");
                txt_content.setText(sbf.toString());
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onbackEvent(LoginMessage message) {
        if (message.getType().equals("UPLOAD")) {
            sbf.append(message.getMessage() + "\n");
            txt_content.setText(sbf.toString());
        }

    }

    @Override
    public void permissionSuccess(int command) {
        sbf = new StringBuffer();
        //上传图片
        File file = new File(images.get(0).path);
        Pair<String, File> map = new Pair<>("file", file);
        ModelSuperImpl.netWork().uploadPic(ParamsBuilder.build().params(PARAMS.uploadPic("1")).command(UPLOAD_COMMAND),
                this, map);
    }

    @Override
    public void permissionFail(int command) {
        ToastUtils.showToast("上传文件需要此权限");
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
            if (images.size() > 0) {
                txt_upload.setEnabled(true);
                Glide.with(UploadFragment.this).load(new File(images.get(0).path)).into(img_);
            }
        }
    }
}
