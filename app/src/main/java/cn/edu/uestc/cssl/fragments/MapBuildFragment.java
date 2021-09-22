package cn.edu.uestc.cssl.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import org.ros.address.InetAddressFactory;
import org.ros.android.view.visualization.layer.CameraControlListener;
import org.ros.android.view.visualization.layer.LaserScanLayer;
import org.ros.android.view.visualization.layer.Layer;
import cn.edu.uestc.cssl.view.ViewControlLayer;

import org.ros.android.view.visualization.layer.OccupancyGridLayer;
import org.ros.android.view.visualization.layer.RobotLayer;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;
import org.ros.android.view.visualization.VisualizationView;
import org.ros.time.NtpTimeProvider;
import org.ros.time.TimeProvider;
import org.ros.time.WallTimeProvider;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

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
public class MapBuildFragment extends RosFragment implements DataSetter<geometry_msgs.Twist> {
    private static final String TAG = "MapBuildFragment";
    private JoyStickView joyStickView;
    private VisualizationView mapView;//显示地图构建结果
    private ViewGroup mainLayout;//主布局，即主要显示界面
    private ViewGroup sideLayout;//边布局，即底边显示界面
    private volatile Talker<Twist> talker;
    private RosImageView<CompressedImage> cameraView = null;
    private ViewControlLayer viewControlLayer;//控制布局类，负责切换主/次视图、缩放旋转平移等实际操作
    private View rootView;//存储View内控件内容

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

            precondition();//基本布局绑定 以及 回调函数设置
            nodeMainExecutor.execute(talker, nodeConfiguration.setNodeName(talker.getDefaultNodeName()));
            nodeMainExecutor.execute(mapView,nodeConfiguration.setNodeName("MapViewNode"));
            nodeMainExecutor.execute(cameraView, nodeConfiguration.setNodeName(getString(R.string.nodeName_of_KinectCamera)));
            setInitialized(true);
        }
    }

    public static MapBuildFragment newInstance() {
        Bundle args = new Bundle();
        MapBuildFragment fragment = new MapBuildFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public Object setLayout() {
        return R.layout.fragment_map_build;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {

        this.rootView = rootView;
        if(cameraView == null){
            cameraView = rootView.findViewById(R.id.cameraview);
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
                nodeMainExecutor.shutdownNodeMain(mapView);
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

    public void precondition(){//预处理
        talker = new Talker<>(getString(R.string.topicName_of_Joystick_Control), getString(R.string.nodeName_of_Joystick_Control), Twist._TYPE, this);

        joyStickView = rootView.findViewById(R.id.joystick);
        mainLayout = rootView.findViewById(R.id.map_build_main_layout);
        sideLayout = rootView.findViewById(R.id.map_build_side_layout);
        mapView = rootView.findViewById(R.id.visualizationView);

        viewControlLayer = new ViewControlLayer(getContext(),nodeMainExecutor.getScheduledExecutorService(),cameraView,
                mapView,mainLayout,sideLayout);
        LaserScanLayer laserScanLayer = new LaserScanLayer("scan");
        OccupancyGridLayer occupancyGridLayer = new OccupancyGridLayer("map");
        RobotLayer robotLayer = new RobotLayer("map");

        mapView.onCreate(new ArrayList<Layer>());
        mapView.getCamera().jumpToFrame("map");
        mapView.addLayer(viewControlLayer);
        mapView.addLayer(occupancyGridLayer);
        mapView.addLayer(robotLayer);
        mapView.addLayer(laserScanLayer);


        mapView.init(nodeMainExecutor);

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
    }
}
