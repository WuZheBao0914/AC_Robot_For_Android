package cn.edu.uestc.cssl.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import cn.edu.uestc.cssl.activities.R;
import cn.edu.uestc.cssl.delegates.RosFragment;

/*
 *@author xuyang
 *@createTime 2019/2/17 14:44
 *@description 语音识别Fragment
 */
public class VoiceRecognitionFragment extends RosFragment {

    public static VoiceRecognitionFragment newInstance() {

        Bundle args = new Bundle();

        VoiceRecognitionFragment fragment = new VoiceRecognitionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Object setLayout() {
        return R.layout.fragment_voice_recognition;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {

    }

    @Override
    public void shutdown() {

    }
}
