package cn.edu.uestc.cssl.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import cn.edu.uestc.cssl.activities.R;
import cn.edu.uestc.cssl.delegates.RosFragment;

/*
 *@author xuyang
 *@createTime 2019/2/17 14:43
 *@description 地图构建Fragment
 */
public class MapBuildFragment extends RosFragment {

    public static MapBuildFragment newInstance() {

        Bundle args = new Bundle();

        MapBuildFragment fragment = new MapBuildFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Object setLayout() {
        return R.layout.fragment_map_build;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {

    }

    @Override
    public void shutdown() {

    }
}
