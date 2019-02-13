package cn.edu.uestc.cssl.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;


import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import cn.edu.uestc.android_10.BitmapFromCompressedImage;
import cn.edu.uestc.android_10.view.RosImageView;
import cn.edu.uestc.cssl.activities.R;
import sensor_msgs.CompressedImage;

/**
 * @author xuyang
 * @create 2019/1/15 10:33
 **/
public class FaceDetectionFragment extends RosFragment {

    private RosImageView<CompressedImage> cameraFaceDetectionOriginView;
    private RosImageView<sensor_msgs.CompressedImage> cameraFaceDetectionHandledView;



    public FaceDetectionFragment() {
    }



    @Override
    public Object setLayout() {
        return R.layout.fragment_face_detection;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        //未处理的图像
        cameraFaceDetectionOriginView = rootView.findViewById(R.id.camera_face_detection_origin);
        cameraFaceDetectionOriginView.setTopicName(getString(R.string.camera_topic_face_detection_origin));
        cameraFaceDetectionOriginView.setMessageType(CompressedImage._TYPE);
        cameraFaceDetectionOriginView.setMessageToBitmapCallable(new BitmapFromCompressedImage());

        //处理后的图像
        cameraFaceDetectionHandledView = rootView.findViewById(R.id.camera_face_detection_handled);
        cameraFaceDetectionHandledView.setTopicName(getString(R.string.camera_topic_face_detection_handled));
        cameraFaceDetectionHandledView.setMessageType(CompressedImage._TYPE);
        cameraFaceDetectionHandledView.setMessageToBitmapCallable(new BitmapFromCompressedImage());

    }

    @Override
    public void initialize(NodeMainExecutor nodeMainExecutor, NodeConfiguration nodeConfiguration) {
        super.initialize(nodeMainExecutor, nodeConfiguration);
        if (nodeConfiguration != null) {
            nodeMainExecutor.execute(cameraFaceDetectionOriginView, nodeConfiguration.setNodeName("android/camera_node_face_detection_origin"));
            nodeMainExecutor.execute(cameraFaceDetectionHandledView, nodeConfiguration.setNodeName("android/camera_node_face_detection_handled"));
        }
    }

    @Override
    void shutdown() {
        nodeMainExecutor.shutdownNodeMain(cameraFaceDetectionOriginView);
        nodeMainExecutor.shutdownNodeMain(cameraFaceDetectionHandledView);
    }
}
