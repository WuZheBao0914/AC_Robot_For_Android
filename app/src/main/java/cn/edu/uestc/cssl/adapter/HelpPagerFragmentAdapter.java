package cn.edu.uestc.cssl.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import cn.edu.uestc.cssl.fragments.HelpFaqFragment;
import cn.edu.uestc.cssl.fragments.HelpUsingFragment;

/*
 *@author xuyang
 *@createTime 2019/2/17 13:14
 *@description 帮助Fragment Pager 适配器
 */
public class HelpPagerFragmentAdapter extends FragmentPagerAdapter {
    private String[] mTitles;

    public HelpPagerFragmentAdapter(FragmentManager fm, String... titles) {
        super(fm);
        mTitles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return HelpUsingFragment.newInstance();
        } else if (position == 1) {
            return HelpUsingFragment.newInstance();
        } else {
            return HelpFaqFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        return mTitles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }
}
