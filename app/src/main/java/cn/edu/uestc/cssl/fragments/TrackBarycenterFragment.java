package cn.edu.uestc.cssl.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import cn.edu.uestc.android_10.BitmapFromCompressedImage;
import cn.edu.uestc.android_10.view.RosImageView;
import cn.edu.uestc.cssl.activities.R;
import cn.edu.uestc.cssl.activities.RobotController;
import cn.edu.uestc.cssl.delegates.RosFragment;
import cn.edu.uestc.cssl.util.DataSetter;
import cn.edu.uestc.cssl.util.Talker;
import sensor_msgs.CompressedImage;
import std_msgs.String;

/*
 *@author xuyang
 *@createTime 2019/2/17 14:46
 *@description 质心追踪Fragment
 */
public class TrackBarycenterFragment extends RosFragment implements DataSetter<String> {

    private static final java.lang.String TAG = "TrackBarycenterFragment";


    private RosImageView<CompressedImage> cameraView = null;
    private Talker<String> talker;
    private java.lang.String commandString;

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
    public void initialize(NodeMainExecutor nodeMainExecutor, NodeConfiguration nodeConfiguration) {
        if (nodeConfiguration != null && !isInitialized()) {
            super.initialize(nodeMainExecutor, nodeConfiguration);
            nodeMainExecutor.execute(talker, nodeConfiguration.setNodeName(talker.getDefaultNodeName()));
//            nodeMainExecutor.execute(cameraView, nodeConfiguration.setNodeName(getString(R.string.nodeName_of_KinectCamera)));
            setInitialized(true);
        }
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        cameraView = rootView.findViewById(R.id.camera_rawimage_trackbarycenter);
        talker = new Talker<>(getString(R.string.topicName_of_TrackBartcenter), getString(R.string.nodeName_of_TrackBartcenter), String._TYPE, this);

        if(cameraView == null){
            cameraView = rootView.findViewById(R.id.cameraview);
            cameraView.setTopicName(getString(R.string.topicName_of_KinectCamera));
            cameraView.setMessageType(CompressedImage._TYPE);
            cameraView.setMessageToBitmapCallable(new BitmapFromCompressedImage());
        }
        RobotController.initFragment(this);
    }

    @Override
    public void shutdown() {
        if (isInitialized()) {
            try {
                nodeMainExecutor.shutdownNodeMain(talker);
//                nodeMainExecutor.shutdownNodeMain(cameraView);
                setInitialized(false);
            } catch (Exception e) {
                Log.e(TAG, "nodeMainExecutor为空，shutdown失败");
            }
        }
    }

    @Override
    public void setData(String msg, Object object) {
        msg.setData((java.lang.String) object);
    }
}
