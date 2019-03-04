package cn.edu.uestc.cssl.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;

import cn.edu.uestc.cssl.activities.R;
import cn.edu.uestc.cssl.adapter.HelpPagerFragmentAdapter;
import cn.edu.uestc.cssl.delegates.SimpleFragment;

/*
 *@author xuyang
 *@createTime 2019/2/17 0:25
 *@description 帮助Fragment
 */
public class HelpFragment extends SimpleFragment {

    private TabLayout mTab;
    private ViewPager mViewPager;

    public static HelpFragment newInstance() {

        Bundle args = new Bundle();

        HelpFragment fragment = new HelpFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Object setLayout() {
        return R.layout.fragment_help;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {


        mTab = rootView.findViewById(R.id.tab);
        mViewPager = rootView.findViewById(R.id.pager);

        mTab.addTab(mTab.newTab());
        mTab.addTab(mTab.newTab());
        mTab.addTab(mTab.newTab());
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
        mViewPager.setAdapter(new HelpPagerFragmentAdapter(getChildFragmentManager()
                , getString(R.string.setup), getString(R.string.using), getString(R.string.faq)));
        mTab.setupWithViewPager(mViewPager);
    }
}

