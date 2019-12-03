package cn.edu.uestc.cssl.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.joanzapata.iconify.IconDrawable;

import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import cn.edu.uestc.ac_ui.icon.AcIcons;
import cn.edu.uestc.android_10.BitmapFromCompressedImage;
import cn.edu.uestc.android_10.view.RosImageView;
import cn.edu.uestc.cssl.activities.R;
import cn.edu.uestc.cssl.activities.RobotController;
import cn.edu.uestc.cssl.delegates.RosFragment;
import sensor_msgs.CompressedImage;

/**
 * @author xuyang
 * @create 2019/1/23 15:39
 **/
public class FaceRecognitionFragment extends RosFragment {

    private static final String TAG = "FaceRecognitionFragment";

    private RosImageView<sensor_msgs.CompressedImage> cameraFaceRecognitionOriginView = null;
    private RosImageView<sensor_msgs.CompressedImage> cameraFaceRecognitionHandledView = null;


    public static FaceRecognitionFragment newInstance() {

        Bundle args = new Bundle();

        FaceRecognitionFragment fragment = new FaceRecognitionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_add_face_for_training, menu);
        menu.findItem(R.id.action_add_face).setIcon(
                new IconDrawable(getContext(), AcIcons.icon_person)
                        .color(Color.BLACK)
                        .actionBarSize()
        );
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_face:
                // todo 如何开启节点
                AddFaceForTrainingFragment addFaceForTrainingFragment
                        = AddFaceForTrainingFragment.newInstance();
                start(addFaceForTrainingFragment);

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
        if (cameraFaceRecognitionOriginView == null && cameraFaceRecognitionHandledView == null) {
            //未处理的图像
            cameraFaceRecognitionOriginView = rootView.findViewById(R.id.camera_face_recognition_origin);
            cameraFaceRecognitionOriginView.setTopicName(getString(R.string.camera_topic_face_recognition_origin));
            cameraFaceRecognitionOriginView.setMessageType(CompressedImage._TYPE);
            cameraFaceRecognitionOriginView.setMessageToBitmapCallable(new BitmapFromCompressedImage());

            //处理后的图像
            cameraFaceRecognitionHandledView = rootView.findViewById(R.id.camera_face_recognition_handled);
            cameraFaceRecognitionHandledView.setTopicName(getString(R.string.camera_topic_face_recognition_handled));
            cameraFaceRecognitionHandledView.setMessageType(CompressedImage._TYPE);
            cameraFaceRecognitionHandledView.setMessageToBitmapCallable(new BitmapFromCompressedImage());
        }
    }

    @Override
    public void initialize(NodeMainExecutor nodeMainExecutor, NodeConfiguration nodeConfiguration) {

        if (nodeConfiguration != null && !isInitialized()) {
            super.initialize(nodeMainExecutor, nodeConfiguration);
            nodeMainExecutor.execute(cameraFaceRecognitionOriginView, nodeConfiguration.setNodeName("android/fragment_camera_view_before"));
            nodeMainExecutor.execute(cameraFaceRecognitionHandledView, nodeConfiguration.setNodeName("android/fragment_camera_view_after"));
            setInitialized(true);
        }
    }

    @Override
    public void shutdown() {
        if (isInitialized()) {
            try {
                nodeMainExecutor.shutdownNodeMain(cameraFaceRecognitionOriginView);
                nodeMainExecutor.shutdownNodeMain(cameraFaceRecognitionHandledView);
                setInitialized(false);
            } catch (Exception e) {
                Log.e(TAG, "nodeMainExecutor为空，shutdown失败");
            }
        }
    }
}
