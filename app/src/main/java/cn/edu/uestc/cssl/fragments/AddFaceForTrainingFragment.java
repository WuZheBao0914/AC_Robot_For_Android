package cn.edu.uestc.cssl.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.view.View;

import com.joanzapata.iconify.widget.IconButton;

import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import cn.edu.uestc.android_10.BitmapFromCompressedImage;
import cn.edu.uestc.android_10.view.RosImageView;
import cn.edu.uestc.cssl.activities.R;
import sensor_msgs.CompressedImage;

/**
 * @author xuyang
 * @create 2019/1/23 16:01
 **/
public class AddFaceForTrainingFragment extends RosFragment {

    private RosImageView<sensor_msgs.CompressedImage> kinectRealtimeImageView;
    private IconButton btnAddFace;
    private TextInputEditText textAddFace;

    @Override
    public Object setLayout() {
        return R.layout.fragment_add_face_for_training;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        kinectRealtimeImageView = rootView.findViewById(R.id.kinectRealtimeImageView);
        kinectRealtimeImageView.setTopicName(getString(R.string.camera_topic_face_recognition_realtime));
        kinectRealtimeImageView.setMessageType(CompressedImage._TYPE);
        kinectRealtimeImageView.setMessageToBitmapCallable(new BitmapFromCompressedImage());

        textAddFace = rootView.findViewById(R.id.add_face_name);

        btnAddFace = rootView.findViewById(R.id.btn_acq_face);
        btnAddFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = textAddFace.getText().toString();
                if (name == null || name == "" || name.length() == 0) {
                    textAddFace.setError("姓名不能为空");
                }
            }
        });
    }

    @Override
    public void initialize(NodeMainExecutor nodeMainExecutor, NodeConfiguration nodeConfiguration) {
        super.initialize(nodeMainExecutor, nodeConfiguration);
        if (nodeConfiguration != null) {
            nodeMainExecutor.execute(kinectRealtimeImageView, nodeConfiguration.setNodeName("android/fragment_camera_view_before"));
        }
    }

    @Override
    void shutdown() {
        nodeMainExecutor.shutdownNodeMain(kinectRealtimeImageView);
    }
}
