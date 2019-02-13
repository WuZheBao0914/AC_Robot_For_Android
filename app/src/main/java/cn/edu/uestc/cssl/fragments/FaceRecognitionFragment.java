package cn.edu.uestc.cssl.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import cn.edu.uestc.android_10.BitmapFromCompressedImage;
import cn.edu.uestc.android_10.view.RosImageView;
import cn.edu.uestc.cssl.activities.R;
import sensor_msgs.CompressedImage;

/**
 * @author xuyang
 * @create 2019/1/23 15:39
 **/
public class FaceRecognitionFragment extends RosFragment {
    private RosImageView<sensor_msgs.CompressedImage> cameraFaceRecognitionOriginView;
    private RosImageView<sensor_msgs.CompressedImage> cameraFaceRecognitionHandledView;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_add_face_for_training, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_face:
                    getSupportDelegate().start(new AddFaceForTrainingFragment());
                break;
        }
        return true;
    }

    @Override
    public Object setLayout() {
        return R.layout.fragment_face_recognition;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        setHasOptionsMenu(true);

        //未处理的图像
        cameraFaceRecognitionOriginView = rootView.findViewById(R.id.camera_face_recognition_origin);
        cameraFaceRecognitionOriginView.setTopicName(getString(R.string.camera_topic_face_detection_origin));
        cameraFaceRecognitionOriginView.setMessageType(CompressedImage._TYPE);
        cameraFaceRecognitionOriginView.setMessageToBitmapCallable(new BitmapFromCompressedImage());

        //处理后的图像
        cameraFaceRecognitionHandledView = rootView.findViewById(R.id.camera_face_recognition_handled);
        cameraFaceRecognitionHandledView.setTopicName(getString(R.string.camera_topic_face_detection_handled));
        cameraFaceRecognitionHandledView.setMessageType(CompressedImage._TYPE);
        cameraFaceRecognitionHandledView.setMessageToBitmapCallable(new BitmapFromCompressedImage());
    }

    @Override
    public void initialize(NodeMainExecutor nodeMainExecutor, NodeConfiguration nodeConfiguration) {
        super.initialize(nodeMainExecutor, nodeConfiguration);
        if (nodeConfiguration != null) {
            nodeMainExecutor.execute(cameraFaceRecognitionOriginView, nodeConfiguration.setNodeName("android/fragment_camera_view_before"));
            nodeMainExecutor.execute(cameraFaceRecognitionHandledView, nodeConfiguration.setNodeName("android/fragment_camera_view_after"));
        }
    }

    @Override
    void shutdown() {
        nodeMainExecutor.shutdownNodeMain(cameraFaceRecognitionOriginView);
        nodeMainExecutor.shutdownNodeMain(cameraFaceRecognitionHandledView);
    }
}
