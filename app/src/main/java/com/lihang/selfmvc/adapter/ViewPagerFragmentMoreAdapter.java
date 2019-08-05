package com.lihang.selfmvc.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.lihang.selfmvc.fragment.DownFileFragment;
import com.lihang.selfmvc.fragment.GETFragment;
import com.lihang.selfmvc.fragment.POSTFragment;
import com.lihang.selfmvc.fragment.ShadowFragemnt;
import com.lihang.selfmvc.fragment.UploadFragment;


/**
 * Created by Administrator on 2018/1/19.
 * 这是多fragment的Adapter
 */

public class ViewPagerFragmentMoreAdapter extends FragmentStatePagerAdapter {

    private String[] arr;

    public ViewPagerFragmentMoreAdapter(FragmentManager fm, String[] arr) {
        super(fm);
        this.arr = arr;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return GETFragment.newFragment();
        } else if (position == 1) {
            return DownFileFragment.newFragment();
        } else if (position == 2) {
            return POSTFragment.newFragment();
        } else if (position == 3) {
            return UploadFragment.newFragment();
        } else {
            return ShadowFragemnt.newFragment();
        }
    }

    @Override
    public int getCount() {
        return arr != null ? arr.length : 0;
    }
}
