package cn.edu.uestc.cssl.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.view.View;

import com.joanzapata.iconify.widget.IconButton;

import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import cn.edu.uestc.android_10.BitmapFromCompressedImage;
import cn.edu.uestc.android_10.view.RosImageView;
import cn.edu.uestc.cssl.activities.R;
import cn.edu.uestc.cssl.activities.RobotController;
import cn.edu.uestc.cssl.delegates.RosFragment;
import cn.edu.uestc.cssl.util.Talker;
import sensor_msgs.CompressedImage;

/**
 * @author xuyang
 * @create 2019/1/23 16:01
 **/
public class AddFaceForTrainingFragment extends RosFragment {

    private static final String TAG = "FaceForTrainingFragment";

    private RosImageView<sensor_msgs.CompressedImage> kinectRealtimeImageView;
    private IconButton btnAddFace;
    private TextInputEditText textAddFace;
    private Talker talker;


    public static AddFaceForTrainingFragment newInstance() {

        Bundle args = new Bundle();

        AddFaceForTrainingFragment fragment = new AddFaceForTrainingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Object setLayout() {
        return R.layout.fragment_add_face_for_training;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        Log.i(TAG,"先执行界面初始化");
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
                if ("".equals(name) || name.length() == 0) {
                    textAddFace.setError("姓名不能为空");
                }else {
                    talker.sendMessage(name);
                }
            }
        });
        talker = new Talker(getString(R.string.topicName_of_add_face_for_face_recognition),
                getString(R.string.nodeName_of_add_face_for_face_recognition));
        RobotController.initFragment(this);
    }

    @Override
    public void initialize(NodeMainExecutor nodeMainExecutor, NodeConfiguration nodeConfiguration) {
        Log.i(TAG,"先执行初始化");
        super.initialize(nodeMainExecutor, nodeConfiguration);
        if (nodeConfiguration != null) {
            nodeMainExecutor.execute(kinectRealtimeImageView, nodeConfiguration.setNodeName("android/fragment_camera_view_before"));
            nodeMainExecutor.execute(talker,nodeConfiguration.setNodeName(talker.getDefaultNodeName()));
        }
    }

    @Override
    public void shutdown() {
        try {
            nodeMainExecutor.shutdownNodeMain(kinectRealtimeImageView);
        } catch (NullPointerException e) {
            Log.e(TAG, "nodeMainExecutor为空，shutdown失败");
        }
    }
}
