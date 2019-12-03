package cn.edu.uestc.cssl.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.widget.IconButton;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import cn.edu.uestc.ac_ui.icon.AcIcons;
import cn.edu.uestc.android_10.BitmapFromCompressedImage;
import cn.edu.uestc.android_10.BitmapFromImage;
import cn.edu.uestc.android_10.view.RosImageView;
import cn.edu.uestc.cssl.activities.R;
import cn.edu.uestc.cssl.activities.RobotController;
import cn.edu.uestc.cssl.delegates.RosFragment;
import cn.edu.uestc.cssl.util.Listener;
import cn.edu.uestc.cssl.util.MessageReceiver;
import cn.edu.uestc.cssl.util.Talker;
import sensor_msgs.CompressedImage;
import sensor_msgs.Image;

public class PoseEstimationFragment extends RosFragment {

    private static final String TAG = "PoseEstimationFragment";

    private RosImageView<sensor_msgs.CompressedImage> cameraPoseEstimationOriginView = null;
    private RosImageView<sensor_msgs.CompressedImage> cameraPoseEstimationHandledView = null;

    public static PoseEstimationFragment newInstance() {

        Bundle args = new Bundle();

        PoseEstimationFragment fragment = new PoseEstimationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void initialize(NodeMainExecutor nodeMainExecutor, NodeConfiguration nodeConfiguration) {
        if (nodeConfiguration != null && !isInitialized()) {
            super.initialize(nodeMainExecutor, nodeConfiguration);
            nodeMainExecutor.execute(cameraPoseEstimationOriginView, nodeConfiguration.setNodeName("android/fragment_camera_view_before"));
            nodeMainExecutor.execute(cameraPoseEstimationHandledView, nodeConfiguration.setNodeName("android/fragment_camera_view_after"));
            setInitialized(true);
        }
    }

    @Override
    public Object setLayout() {
        return R.layout.fragment_pose_estimation;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {

        if (cameraPoseEstimationOriginView == null && cameraPoseEstimationHandledView == null) {
            //未处理的图像
            cameraPoseEstimationOriginView = rootView.findViewById(R.id.camera_pose_estimation_origin);
            cameraPoseEstimationOriginView.setTopicName(getString(R.string.camera_topic_pose_estimation_origin));
            cameraPoseEstimationOriginView.setMessageType(CompressedImage._TYPE);
            cameraPoseEstimationOriginView.setMessageToBitmapCallable(new BitmapFromCompressedImage());

            //处理后的图像
            cameraPoseEstimationHandledView = rootView.findViewById(R.id.camera_pose_estimation_handled);
            cameraPoseEstimationHandledView.setTopicName(getString(R.string.camera_topic_pose_estimation_handled));
            cameraPoseEstimationHandledView.setMessageType(CompressedImage._TYPE);
            cameraPoseEstimationHandledView.setMessageToBitmapCallable(new BitmapFromCompressedImage());

        }
    }

    @Override
    public void shutdown() {
        if (isInitialized()) {
            try {
                nodeMainExecutor.shutdownNodeMain(cameraPoseEstimationOriginView);
                nodeMainExecutor.shutdownNodeMain(cameraPoseEstimationHandledView);
                setInitialized(false);
            } catch (Exception e) {
                Log.e(TAG, "nodeMainExecutor为空，shutdown失败");
            }
        }
    }

}
