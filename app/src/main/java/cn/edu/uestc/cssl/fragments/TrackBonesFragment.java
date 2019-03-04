package cn.edu.uestc.cssl.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import cn.edu.uestc.cssl.activities.R;
import cn.edu.uestc.cssl.delegates.RosFragment;

/*
 *@author xuyang
 *@createTime 2019/2/17 14:45
 *@description 骨骼追踪Fragment
 */
public class TrackBonesFragment extends RosFragment {

    public static TrackBonesFragment newInstance() {

        Bundle args = new Bundle();

        TrackBonesFragment fragment = new TrackBonesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Object setLayout() {
        return R.layout.fragment_track_bones;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {

    }

    @Override
    public void shutdown() {

    }
}
