package cn.edu.uestc.cssl.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.util.Timer;
import java.util.TimerTask;

import cn.edu.uestc.android_10.BitmapFromCompressedImage;
import cn.edu.uestc.android_10.view.RosImageView;
import cn.edu.uestc.cssl.activities.R;
import cn.edu.uestc.cssl.activities.RobotController;
import cn.edu.uestc.cssl.delegates.RosFragment;
import cn.edu.uestc.cssl.util.DataSetter;
import cn.edu.uestc.cssl.util.Talker;
import cn.edu.uestc.cssl.view.JoyStickView;
import geometry_msgs.Twist;
import sensor_msgs.CompressedImage;


/*
 *@author xuyang
 *@createTime 2019/2/17 14:43
 *@description 地图构建Fragment
 */
public class SemanticSegmentationFragment extends RosFragment implements DataSetter<Twist> {
    private static final String TAG = "SemanticSegmentationFragment";
    private JoyStickView joyStickView;
    private volatile Talker<Twist> talker;
    private Button btn_start_segmentation;
    private RosImageView<CompressedImage> cameraView = null;
    private int mode;

    private String[] DIRECTION_STATE = {"CENTER", "LEFT", "LEFT_UP", "UP", "RIGHT_UP", "RIGHT", "RIGHT_DOWN", "DOWN", "LEFT_DOWN"};


    // Timer for periodically publishing velocity commands
    private Timer publisherTimer;
    // Indicates when a velocity command should be published
    private boolean publishVelocity;

    // Contains the current velocity plan to be published
    private Twist currentVelocityCommand;



    @Override
    public void initialize(NodeMainExecutor nodeMainExecutor, NodeConfiguration nodeConfiguration) {//initialize后于onBindView执行
        if (nodeConfiguration != null && !isInitialized()) {
            super.initialize(nodeMainExecutor, nodeConfiguration);

            nodeMainExecutor.execute(talker, nodeConfiguration.setNodeName(talker.getDefaultNodeName()));
            nodeMainExecutor.execute(cameraView, nodeConfiguration.setNodeName(getString(R.string.nodeName_of_KinectCamera)));
            setInitialized(true);
        }
    }

    public static SemanticSegmentationFragment newInstance() {
        Bundle args = new Bundle();
        SemanticSegmentationFragment fragment = new SemanticSegmentationFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public Object setLayout() {
        return R.layout.fragment_semantic_segmentation;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mode = 0;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {

        joyStickView = rootView.findViewById(R.id.ss_joystick);
        btn_start_segmentation = (Button)rootView.findViewById(R.id.ss_btn_mission);

        if(cameraView == null){
            cameraView = rootView.findViewById(R.id.ss_cameraview);
            cameraView.setTopicName("/camera/rgb/image_color/compressed");
            cameraView.setMessageType(CompressedImage._TYPE);
            cameraView.setMessageToBitmapCallable(new BitmapFromCompressedImage());
        }
        RobotController.initFragment(this);


        publisherTimer = new Timer();
        publisherTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isInitialized() && talker.getPublisher() != null
                        && currentVelocityCommand != null) {
                    talker.sendMessage(currentVelocityCommand);
                }
            }
        }, 0, 80);

        talker = new Talker<>(getString(R.string.topicName_of_Joystick_Control), getString(R.string.nodeName_of_Joystick_Control), Twist._TYPE, this);

        joyStickView.setListener(new JoyStickView.JoyStickListener() {
            @Override
            public void onMove(JoyStickView joyStick, double angle, double power, int direction) {

                double contactTheta = (int) joyStick.getAngleDegrees();//触点与控件中心点的角度
                double normorlizedMagnitude = ((int) joyStick.getPower()) * 1.0 / 100.0;//触点与控件中心点的归一化后的距离（范围在[0,1]之间）
                double linearVelocityX = -1 * normorlizedMagnitude * Math.cos(contactTheta * Math.PI / 180);//X方向上的线速度[-1,1]之间
                double linearVelocityY = normorlizedMagnitude * Math.sin(contactTheta * Math.PI / 180);//Y方向上的线速度[-1,1]之间
                double linearVelocityZ = -1 * normorlizedMagnitude * Math.sin((contactTheta + 90) * Math.PI / 180);


                currentVelocityCommand = talker.getPublisher().newMessage();

                switch (DIRECTION_STATE[joyStick.getDirection() + 1]) {
                    case "UP": case "DOWN":
                        forceVelocity(linearVelocityY, 0, 0);
                        break;
                    case "LEFT": case "RIGHT":
                        forceVelocity(0, 0, linearVelocityZ);
                        break;
                    case "LEFT_UP": case "LEFT_DOWN":case "RIGHT_UP": case "RIGHT_DOWN":
                        forceVelocity(linearVelocityY, 0, linearVelocityZ);
                        break;
                    default:
                        stop();
                }
            }

            @Override
            public void onTap() {

            }

            @Override
            public void onDoubleTap() {

            }
        });
        btn_start_segmentation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mode == 0) {
                    btn_start_segmentation.setText("停止分割");
                    mode = 1;
                }
                else {
                    btn_start_segmentation.setText("开始分割");
                    mode = 0;
                }
            }
        });
    }

    @Override
    public void setData(Twist msg, Object object) {
        msg.setAngular(((Twist) object).getAngular());
        msg.setLinear(((Twist) object).getLinear());
    }

    @Override
    public void shutdown() {
        if (isInitialized()) {
            try {
                nodeMainExecutor.shutdownNodeMain(talker);
                nodeMainExecutor.shutdownNodeMain(cameraView);
                setInitialized(false);
            } catch (Exception e) {
                Log.e(TAG, "nodeMainExecutor为空，shutdown失败");
            }
        }
    }

    public void publishVelocity(double linearVelocityX, double linearVelocityY, double angularVelocityZ) {
        if (currentVelocityCommand != null) {

            float scale = 1.0f;

            currentVelocityCommand.getLinear().setX(linearVelocityX * scale);
            currentVelocityCommand.getLinear().setY(-linearVelocityY * scale);
            currentVelocityCommand.getLinear().setZ(0.0);
            currentVelocityCommand.getAngular().setX(0.0);
            currentVelocityCommand.getAngular().setY(0.0);
            currentVelocityCommand.getAngular().setZ(-angularVelocityZ);
        } else {
            Log.w("Emergency Stop", "currentVelocityCommand is null");
        }
    }

    public void stop() {

        publishVelocity = false;
        publishVelocity(0.0, 0.0, 0.0);

    }

    public void forceVelocity(double linearVelocityX, double linearVelocityY,
                              double angularVelocityZ) {
        publishVelocity = true;
        publishVelocity(linearVelocityX, linearVelocityY, angularVelocityZ);
    }

}
