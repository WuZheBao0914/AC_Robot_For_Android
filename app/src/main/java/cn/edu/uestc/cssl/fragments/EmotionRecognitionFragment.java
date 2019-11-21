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
import cn.edu.uestc.cssl.delegates.RosFragment;
import sensor_msgs.CompressedImage;

/*
 *@author xuyang
 *@createTime 2019/2/17 14:44
 *@description 情绪识别Fragment
 */
public class EmotionRecognitionFragment extends RosFragment {

    private static final String TAG = "EmotionRecognitionFragm";

    private RosImageView<CompressedImage> cameraExpressionRecognitionOriginView = null;
    private RosImageView<sensor_msgs.CompressedImage> cameraExpressionRecognitionHandledView = null;

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
        if (cameraExpressionRecognitionOriginView == null && cameraExpressionRecognitionHandledView == null) {
            //未处理的图像
            cameraExpressionRecognitionOriginView = rootView.findViewById(R.id.camera_face_detection_origin);
            cameraExpressionRecognitionOriginView.setTopicName(getString(R.string.camera_topic_face_detection_origin));
            cameraExpressionRecognitionOriginView.setMessageType(CompressedImage._TYPE);
            cameraExpressionRecognitionOriginView.setMessageToBitmapCallable(new BitmapFromCompressedImage());

            //处理后的图像
            cameraExpressionRecognitionHandledView = rootView.findViewById(R.id.camera_expression_recognition_handled);
            cameraExpressionRecognitionHandledView.setTopicName(getString(R.string.camera_topic_expression_recognition_handled));
            cameraExpressionRecognitionHandledView.setMessageType(CompressedImage._TYPE);
            cameraExpressionRecognitionHandledView.setMessageToBitmapCallable(new BitmapFromCompressedImage());
        }
    }

    @Override
    public void initialize(NodeMainExecutor nodeMainExecutor, NodeConfiguration nodeConfiguration) {

        if (nodeConfiguration != null && !isInitialized()) {
            super.initialize(nodeMainExecutor, nodeConfiguration);
            nodeMainExecutor.execute(cameraExpressionRecognitionOriginView, nodeConfiguration.setNodeName("android/fragment_camera_view_before"));
            nodeMainExecutor.execute(cameraExpressionRecognitionHandledView, nodeConfiguration.setNodeName("android/fragment_camera_view_after"));
            setInitialized(true);
        }
    }

    @Override
    public void shutdown() {
        if (isInitialized()) {
            try {
                nodeMainExecutor.shutdownNodeMain(cameraExpressionRecognitionOriginView);
                nodeMainExecutor.shutdownNodeMain(cameraExpressionRecognitionHandledView);
                setInitialized(false);
            } catch (Exception e) {
                Log.e(TAG, "nodeMainExecutor为空，shutdown失败");
            }
        }
    }
}
