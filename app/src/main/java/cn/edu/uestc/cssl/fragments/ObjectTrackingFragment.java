package cn.edu.uestc.cssl.fragments;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.app.Activity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;
import org.ros.node.topic.Subscriber;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.edu.uestc.android_10.BitmapFromCompressedImage;
import cn.edu.uestc.android_10.view.RosImageView;
import cn.edu.uestc.cssl.activities.MainActivity;
import cn.edu.uestc.cssl.activities.R;
import cn.edu.uestc.cssl.activities.RobotController;
import cn.edu.uestc.cssl.delegates.RosFragment;


import cn.edu.uestc.cssl.entity.DetectedObject;
import cn.edu.uestc.cssl.util.Listener;
import cn.edu.uestc.cssl.util.ListenerTest;
import cn.edu.uestc.cssl.util.MessageReceiver;
import sensor_msgs.CompressedImage;


public class ObjectTrackingFragment extends RosFragment implements MessageReceiver {
    private static final java.lang.String TAG = "ObjectTrackingFragment";
    private RosImageView<sensor_msgs.CompressedImage> cameraObjectTrackingHandledView = null;
    private Listener objectInfoListener = null;
    private LinearLayout objectInfoGroup = null;

    private String[] classes = {"person", "bicycle", "car", "motorbike", "aeroplane", "bus", "train", "truck", "boat", "traffic light", "fire hydrant", "stop sign", "parking meter", "bench", "bird", "cat", "dog", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe", "backpack", "umbrella", "handbag", "tie", "suitcase", "frisbee", "skis", "snowboard", "sports ball", "kite", "baseball bat", "baseball glove", "skateboard", "surfboard", "tennis racket", "bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple", "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake", "chair", "sofa", "pottedplant", "bed", "diningtable", "toilet", "tvmonitor", "laptop", "mouse", "remote", "keyboard", "cell phone", "microwave", "oven", "toaster", "sink", "refrigerator", "book", "clock", "vase", "scissors", "teddy bear", "hair drier", "toothbrush"};

    public static ObjectTrackingFragment newInstance() {
        Bundle args = new Bundle();

        ObjectTrackingFragment fragment = new ObjectTrackingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void initialize(NodeMainExecutor nodeMainExecutor, NodeConfiguration nodeConfiguration) {
        if (nodeConfiguration != null && !isInitialized()) {
            super.initialize(nodeMainExecutor, nodeConfiguration);
            nodeMainExecutor.execute(objectInfoListener, nodeConfiguration.setNodeName("android/listener_object_information"));
            nodeMainExecutor.execute(cameraObjectTrackingHandledView, nodeConfiguration.setNodeName("android/fragment_camera_view_after"));//设置节点名
            setInitialized(true);
        }
    }

    @Override
    public Object setLayout() {
        return R.layout.fragment_object_tracking;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        objectInfoGroup = rootView.findViewById(R.id.objectInfoContainer);
        objectInfoListener = new Listener("topic_objects_information", "android/listener_object_information", this);
        if (cameraObjectTrackingHandledView == null) {
            //处理后的图像
            cameraObjectTrackingHandledView = rootView.findViewById(R.id.camera_object_tracking_handled);
            cameraObjectTrackingHandledView.setTopicName(getString(R.string.topicName_of_ObjectDetection_Handled));//设置话题
            cameraObjectTrackingHandledView.setMessageType(CompressedImage._TYPE);
            cameraObjectTrackingHandledView.setMessageToBitmapCallable(new BitmapFromCompressedImage());

        }

    }

    @Override
    public void shutdown() {
        if (isInitialized()) {
            try {
                nodeMainExecutor.shutdownNodeMain(objectInfoListener);
                nodeMainExecutor.shutdownNodeMain(cameraObjectTrackingHandledView);
                setInitialized(false);
            } catch (Exception e) {
                Log.e(TAG, "nodeMainExecutor为空，shutdown失败");
            }
        }
    }

    @Override
    public void showMessage(String msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                objectInfoGroup.removeAllViews();
                if (msg.equals("no target detected")) {
                    TextView child = new TextView(getContext());
                    child.setText(msg);
                    objectInfoGroup.addView(child);
                } else {
                    ArrayList<DetectedObject> detectedObjects = Json2List(msg);
                    for (int i = 0; i < detectedObjects.size(); i++) {
                        TextView child = new TextView(getContext());
                        String className = classes[detectedObjects.get(i).getTargetType()];
                        String printStr = detectedObjects.get(i).toString().replace("typename", className);
                        child.setText(printStr);
                        objectInfoGroup.addView(child);
                    }
                }

            }
        });
    }

    public ArrayList<DetectedObject> Json2List(String jsonStr) {
        ArrayList<DetectedObject> objectList = new ArrayList<>();
        JSONArray jsonArray = JSONArray.parseArray(jsonStr);
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            DetectedObject temp = JSON.parseObject(obj.toJSONString(), DetectedObject.class);
            objectList.add(temp);
        }
        return objectList;
    }

}
