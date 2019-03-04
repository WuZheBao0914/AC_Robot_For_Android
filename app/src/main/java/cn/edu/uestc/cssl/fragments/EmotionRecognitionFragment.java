package cn.edu.uestc.cssl.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import cn.edu.uestc.cssl.activities.R;
import cn.edu.uestc.cssl.delegates.RosFragment;

/*
 *@author xuyang
 *@createTime 2019/2/17 14:44
 *@description 情绪识别Fragment
 */
public class EmotionRecognitionFragment extends RosFragment {

    public static EmotionRecognitionFragment newInstance() {

        Bundle args = new Bundle();

        EmotionRecognitionFragment fragment = new EmotionRecognitionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Object setLayout() {
        return R.layout.fragment_emotion_recognition;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {

    }

    @Override
    public void shutdown() {

    }
}
