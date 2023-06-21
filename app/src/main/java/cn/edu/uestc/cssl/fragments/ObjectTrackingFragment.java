package cn.edu.uestc.cssl.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.uestc.android_10.BitmapFromCompressedImage;
import cn.edu.uestc.android_10.view.RosImageView;
import cn.edu.uestc.cssl.activities.R;
import cn.edu.uestc.cssl.delegates.RosFragment;
import cn.edu.uestc.cssl.entity.DetectedObject;
import cn.edu.uestc.cssl.util.DataSetter;
import cn.edu.uestc.cssl.util.Listener;
import cn.edu.uestc.cssl.util.MessageReceiver;
import cn.edu.uestc.cssl.util.Talker;
import sensor_msgs.CompressedImage;


public class ObjectTrackingFragment extends RosFragment implements MessageReceiver,DataSetter<std_msgs.String>{
    private static final java.lang.String TAG = "ObjectTrackingFragment";
    private RosImageView<sensor_msgs.CompressedImage> cameraObjectTrackingHandledView = null;//处理后图像显示控件
    private Listener objectInfoListener = null;//检测到物体信息的接收器
    private LinearLayout objectInfoGroup = null;//物体信息控件组
    private LinearLayout   buttonParent = null;//按钮父级
    private TextView info = null;//提示文本框
    private List<CheckBox> checkBoxs = new ArrayList<CheckBox>();//单选框组列表
    private int modelSignal = 0;//模式信号
    private ArrayList<DetectedObject> detectedObjects = new  ArrayList<DetectedObject>();
    private Talker<std_msgs.String> talker; //发送json数据
    private Map<String,float[]> trakingmap = new HashMap<String, float[]>();
    ArrayList trackingMsg;//机器人目标跟踪需要的信息
    private float x1,x2,y1,y2;//左上角右下角对应的（x，y）坐标

    @Override
    public void setData(std_msgs.String msg, Object object) {
        msg.setData((String) object);
    }
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
//            nodeMainExecutor.execute(compressedImageView, nodeConfiguration.setNodeName("android/fragment_camera_view_after"));
            nodeMainExecutor.execute(talker, nodeConfiguration.setNodeName("android/talker_object_information"));
            //nodeMainExecutor.execute(cameraObjectDetectionHandledView, nodeConfiguration.setNodeName("android/send_fragment_camera_view_after"));
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
        buttonParent = rootView.findViewById(R.id.button_parent);
        trackingMsg =new ArrayList();
//        test = rootView.findViewById(R.id.camera_object_tracking_test);

        info = new TextView(getContext());
        info.setWidth(200);
        info.setGravity(Gravity.CENTER_HORIZONTAL);
        info.setTextSize(20);
        //物体信息接收器实例化，指定话题名、节点名

        objectInfoListener = new Listener("topic_object_tracking_information", "android/listener_object_information",this);
        talker = new Talker<>("topic_objects_modelChg","android/talker_object_information",std_msgs.String._TYPE,this);


        //接收处理后图像的逻辑
        if (cameraObjectTrackingHandledView == null) {
            //处理后的图像
            cameraObjectTrackingHandledView = rootView.findViewById(R.id.camera_object_tracking_handled);//获取控件
            cameraObjectTrackingHandledView.setDrawingCacheEnabled(true);
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
                nodeMainExecutor.shutdownNodeMain(talker);
//                nodeMainExecutor.shutdownNodeMain(talker_image);
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
                if(modelSignal==0){
                    objectInfoGroup.removeAllViews();//移除已有子控件
                    if (msg.equals("no target detected")) {//如果机器人没有捕捉到目标
                        info.setText(msg);
                        objectInfoGroup.addView(info);//添加至面板
                    } else {//如果机器人捕捉到目标
                        detectedObjects = Json2List(msg);//将json字符串转换为DetectedObject的ArrayList
                        for (int i = 0; i < detectedObjects.size(); i++) {
                            CheckBox checkBox = (CheckBox) getLayoutInflater().inflate(R.layout.object_tracking_checkbox, null);
                            checkBox.setGravity(Gravity.CENTER_HORIZONTAL);
                            checkBox.setTextSize(20);
                            checkBoxs.add(checkBox);
                            checkBoxs.get(i).setId(checkBoxs.get(i).getId()+i);
                            checkBoxs.get(i).setText(classes[detectedObjects.get(i).getTargetType()]);
                            checkBoxs.get(i).setChecked(false);
                            checkBoxs.get(i).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    for(int i=0;i<detectedObjects.size();i++){
                                        if(checkBoxs.get(i).isChecked()){
                                            x1=(float)(detectedObjects.get(i).getX1());
                                            x2=(float)(detectedObjects.get(i).getX2());
                                            y1=(float)(detectedObjects.get(i).getY1());
                                            y2=(float)(detectedObjects.get(i).getY2());

                                            float[] topleft;
                                            topleft = new float[]{x1,y1};
                                            float[] bottomright;
                                            bottomright = new float[]{x2,y2};

                                            trakingmap.put("topleft",topleft);
                                            trakingmap.put("bottomright",bottomright);

                                            cameraObjectTrackingHandledView.setDrawingCacheEnabled(true);
                                            Bitmap obmp = Bitmap.createBitmap(cameraObjectTrackingHandledView.getDrawingCache());
                                            cameraObjectTrackingHandledView.setDrawingCacheEnabled(Boolean.FALSE);
                                            final ByteArrayOutputStream os = new ByteArrayOutputStream();
                                            obmp.compress(Bitmap.CompressFormat.PNG, 100, os);
                                            byte [] arr=os.toByteArray();
                                            String trackingResult=Base64.encodeToString(arr,Base64.NO_WRAP);

                                            trackingMsg.add(x1);
                                            trackingMsg.add(y1);
                                            trackingMsg.add(x2);
                                            trackingMsg.add(y2);
                                            trackingMsg.add(1);
                                            trackingMsg.add(trackingResult);

                                            Gson gson = new Gson();
                                            String json = gson.toJson(trackingMsg);
                                            talker.sendMessage(json);
                                            Log.i("ObjectTrackingFragment_",json);

                                            info.setText("当前选择跟踪物体"+checkBoxs.get(i).getText());//设置子控件显示的文本
                                            objectInfoGroup.removeAllViews();
                                            createButton();
                                            objectInfoGroup.addView(info);//添加至面板
                                            modelSignal=1;
                                            break;
                                        }
                                    }
                                }
                            });
                            objectInfoGroup.addView(checkBoxs.get(i),i);
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);  // , 1是可选写的
                            lp.setMargins(0, 20, 0, 0);
                            checkBoxs.get(i).setLayoutParams(lp);
                            checkBoxs.get(i).getLayoutParams().width=500;
                        }
                    }
                } else{
                    int count = buttonParent.getChildCount(),i;
                    for (i = 0; i < count; i++) {
                        View view = buttonParent.getChildAt(i);
                        if(view.getId()==R.id.button_back){
                             break;
                        } 
                    }
                    if(i==count){
                        createButton();
                    }
                }
            }
        });
    }


    public ArrayList<DetectedObject> Json2List(String jsonStr) {
        Gson gson = new Gson();
        Type ListType = new TypeToken<ArrayList<DetectedObject>>(){}.getType();
        ArrayList<DetectedObject> objectList = gson.fromJson(jsonStr, ListType);
        return objectList;
    }


    public void createButton(){
        Button button = (Button) getLayoutInflater().inflate(R.layout.object_tracking_button, null);
        button.setGravity(Gravity.CENTER);
        button.setTextSize(20);
        button.setText("返回");
        buttonParent.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonParent.removeView(v);
                objectInfoGroup.removeView(info);
                trackingMsg.set(4,0);
                Gson gson = new Gson();
                String json = gson.toJson(trackingMsg);
                talker.sendMessage(json);
                trackingMsg.clear();
                modelSignal=0;
            }
        });
    }

}
