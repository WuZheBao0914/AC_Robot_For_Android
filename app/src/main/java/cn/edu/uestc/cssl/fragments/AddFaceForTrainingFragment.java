package cn.edu.uestc.cssl.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.joanzapata.iconify.widget.IconButton;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import cn.edu.uestc.android_10.BitmapFromCompressedImage;
import cn.edu.uestc.android_10.view.RosImageView;
import cn.edu.uestc.cssl.activities.R;
import cn.edu.uestc.cssl.activities.RobotController;
import cn.edu.uestc.cssl.delegates.RosFragment;
import cn.edu.uestc.cssl.util.Listener;
import cn.edu.uestc.cssl.util.MessageReceiver;
import cn.edu.uestc.cssl.util.Talker;
import sensor_msgs.CompressedImage;

/**
 * @author xuyang
 * @create 2019/1/23 16:01
 **/
public class AddFaceForTrainingFragment extends RosFragment implements MessageReceiver {

    private static final String TAG = "FaceForTrainingFragment";
    private View view;
    private RosImageView<sensor_msgs.CompressedImage> kinectRealtimeImageView;
    private IconButton btnAddFace;
    private TextInputEditText textAddFace;
    private Talker talker;
    private Listener listener;
    private QMUITipDialog waitDialog;

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
        view = rootView;

        Log.i(TAG, "先执行界面初始化");
        kinectRealtimeImageView = rootView.findViewById(R.id.kinectRealtimeImageView);
        kinectRealtimeImageView.setTopicName(getString(R.string.camera_topic_face_recognition_realtime));
        kinectRealtimeImageView.setMessageType(CompressedImage._TYPE);
        kinectRealtimeImageView.setMessageToBitmapCallable(new BitmapFromCompressedImage());

        textAddFace = rootView.findViewById(R.id.add_face_name);

        btnAddFace = rootView.findViewById(R.id.btn_acq_face);
        btnAddFace.setOnClickListener(view -> {
            String name = textAddFace.getText().toString();
            if ("".equals(name) || name.length() == 0) {
                textAddFace.setError("姓名不能为空");
            } else {
                talker.sendMessage(name);
                waitDialog.show();
            }
        });
        talker = new Talker(getString(R.string.topicName_of_add_face_for_face_recognition),
                getString(R.string.nodeName_of_add_face_for_face_recognition));
        listener = new Listener(getString(R.string.topicName_of_result_of_add_face_for_face_recognition),
                "addFaceResultNode", this);
        waitDialog = new QMUITipDialog.Builder(getControlApp())
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord("添加数据中...")
                .create();
        //初始化Node
        RobotController.initFragment(this);
    }

    @Override
    public void initialize(NodeMainExecutor nodeMainExecutor, NodeConfiguration nodeConfiguration) {
        Log.i(TAG, "先执行初始化");
        super.initialize(nodeMainExecutor, nodeConfiguration);
        if (nodeConfiguration != null) {
            nodeMainExecutor.execute(kinectRealtimeImageView, nodeConfiguration.setNodeName("android/fragment_camera_view_before"));
            nodeMainExecutor.execute(talker, nodeConfiguration.setNodeName(talker.getDefaultNodeName()));
            nodeMainExecutor.execute(listener, nodeConfiguration.setNodeName(listener.getDefaultNodeName()));
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

    @Override
    public void showMessage(String msg) {
        //隐藏加载提示框
        waitDialog.dismiss();
        //数据添加失败
        if (msg == null || "".equals(msg) || msg.indexOf("失败") > 0) {
            final QMUITipDialog tipDialog = new QMUITipDialog.Builder(getControlApp())
                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                    .setTipWord(msg)
                    .create();
            tipDialog.show();
            view.postDelayed(tipDialog::dismiss, 1500);
        } else {
            //数据添加成功
            final QMUITipDialog tipDialog = new QMUITipDialog.Builder(getControlApp())
                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                    .setTipWord(msg)
                    .create();
            tipDialog.show();
            view.postDelayed(tipDialog::dismiss, 1500);
        }


    }
}
