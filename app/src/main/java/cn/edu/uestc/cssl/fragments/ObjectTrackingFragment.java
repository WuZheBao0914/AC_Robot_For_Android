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
    private RosImageView<sensor_msgs.CompressedImage> cameraObjectTrackingHandledView = null;//处理后图像显示控件
    private Listener objectInfoListener = null;//检测到物体信息的接收器
    private LinearLayout objectInfoGroup = null;//物体信息控件组

    //物体类别数组
    private String[] classes = {"person", "bicycle", "car", "motorbike", "aeroplane", "bus", "train", "truck", "boat", "traffic light", "fire hydrant", "stop sign", "parking meter", "bench", "bird", "cat", "dog", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe", "backpack", "umbrella", "handbag", "tie", "suitcase", "frisbee", "skis", "snowboard", "sports ball", "kite", "baseball bat", "baseball glove", "skateboard", "surfboard", "tennis racket", "bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple", "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake", "chair", "sofa", "pottedplant", "bed", "diningtable", "toilet", "tvmonitor", "laptop", "mouse", "remote", "keyboard", "cell phone", "microwave", "oven", "toaster", "sink", "refrigerator", "book", "clock", "vase", "scissors", "teddy bear", "hair drier", "toothbrush"};

    public static ObjectTrackingFragment newInstance() {//创建页面时执行
        Bundle args = new Bundle();
        ObjectTrackingFragment fragment = new ObjectTrackingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void initialize(NodeMainExecutor nodeMainExecutor, NodeConfiguration nodeConfiguration) {//初始化页面时执行，只要和有ROS通信相关的节点，都需要在此初始化
        if (nodeConfiguration != null && !isInitialized()) {//节点配置不为空且该页面没有被初始化
            super.initialize(nodeMainExecutor, nodeConfiguration);//初始化父类配置
            //下面开始初始化本页面的节点
            nodeMainExecutor.execute(objectInfoListener, nodeConfiguration.setNodeName("android/listener_object_information"));//物体信息接收器初始化，并指定节点名
            nodeMainExecutor.execute(cameraObjectTrackingHandledView, nodeConfiguration.setNodeName("android/fragment_camera_view_after"));//处理后的图像接收节点初始化，并指定节点名
            setInitialized(true);//设置该页面已经初始化
        }
    }

    @Override
    public Object setLayout() {//指定该java文件对应的xml布局
        return R.layout.fragment_object_tracking;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {//页面执行逻辑
        objectInfoGroup = rootView.findViewById(R.id.objectInfoContainer);//获取物体信息控件组
        //物体信息接收器实例化，指定话题名、节点名
        objectInfoListener = new Listener("topic_objects_information", "android/listener_object_information", this);

        //接收处理后图像的逻辑
        if (cameraObjectTrackingHandledView == null) {
            //处理后的图像
            cameraObjectTrackingHandledView = rootView.findViewById(R.id.camera_object_tracking_handled);//获取控件
            cameraObjectTrackingHandledView.setTopicName(getString(R.string.topicName_of_ObjectDetection_Handled));//设置话题
            cameraObjectTrackingHandledView.setMessageType(CompressedImage._TYPE);//设置控件显示图片的格式
            cameraObjectTrackingHandledView.setMessageToBitmapCallable(new BitmapFromCompressedImage());//将CompressedImage转换为Android可显示的Bitmap格式

        }

    }

    @Override
    public void shutdown() {//页面关闭时执行
        if (isInitialized()) {
            try {
                //在Initionalize()方法中初始化的节点，都需要在此shutdown
                nodeMainExecutor.shutdownNodeMain(objectInfoListener);
                nodeMainExecutor.shutdownNodeMain(cameraObjectTrackingHandledView);
                setInitialized(false);//设置页面未被初始化
            } catch (Exception e) {
                Log.e(TAG, "nodeMainExecutor为空，shutdown失败");
            }
        }
    }

    @Override
    public void showMessage(String msg) {//每次从机器人收到消息后都执行一次，收到的msg为json字符串
        getActivity().runOnUiThread(new Runnable() {//切换至UI线程
            @Override
            public void run() {
                objectInfoGroup.removeAllViews();//移除已有子控件

                if (msg.equals("no target detected")) {//如果机器人没有捕捉到目标
                    TextView child = new TextView(getContext());//new一个TextView
                    child.setText(msg);//设置子控件显示的文本
                    objectInfoGroup.addView(child);//添加至面板
                } else {//如果机器人捕捉到目标
                    ArrayList<DetectedObject> detectedObjects = Json2List(msg);//将json字符串转换为DetectedObject的ArrayList
                    for (int i = 0; i < detectedObjects.size(); i++) {
                        TextView child = new TextView(getContext());//新建子控件
                        String className = classes[detectedObjects.get(i).getTargetType()];//根据下标获取物体类型
                        String printStr = detectedObjects.get(i).toString().replace("typename", className);//生成子控件显示的字符串
                        child.setText(printStr);//设置子控件的文本
                        objectInfoGroup.addView(child);//将子控件添加至布局
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
