package cn.edu.uestc.cssl.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import cn.edu.uestc.cssl.activities.R;
import cn.edu.uestc.cssl.delegates.RosFragment;

/*
 *@author xuyang
 *@createTime 2019/2/17 14:46
 *@description 质心追踪Fragment
 */
public class TrackBarycenterFragment extends RosFragment {

    public static TrackBarycenterFragment newInstance() {

        Bundle args = new Bundle();

        TrackBarycenterFragment fragment = new TrackBarycenterFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Object setLayout() {
        return R.layout.fragment_track_barycenter;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {

    }

    @Override
    public void shutdown() {

    }
}
