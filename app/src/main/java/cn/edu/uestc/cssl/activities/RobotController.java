package cn.edu.uestc.cssl.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.EntypoIcons;
import com.joanzapata.iconify.fonts.MaterialCommunityIcons;

import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.net.URI;
import java.util.List;

import cn.edu.uestc.ac_ui.icon.AcIcons;
import cn.edu.uestc.android_10.AppCompatRosActivity;
import cn.edu.uestc.cssl.delegates.RosFragment;
import cn.edu.uestc.cssl.entity.RobotInfo;
import cn.edu.uestc.cssl.fragments.BaseInfomationFragment;
import cn.edu.uestc.cssl.fragments.EmotionRecognitionFragment;
import cn.edu.uestc.cssl.fragments.FaceDetectionFragment;
import cn.edu.uestc.cssl.fragments.FaceRecognitionFragment;
import cn.edu.uestc.cssl.fragments.MapBuildFragment;
import cn.edu.uestc.cssl.fragments.ObjectTrackingFragment;
import cn.edu.uestc.cssl.fragments.PoseEstimationFragment;
import cn.edu.uestc.cssl.fragments.SemanticSegmentationFragment;
import cn.edu.uestc.cssl.fragments.SettingFragment;
import cn.edu.uestc.cssl.fragments.TargetSeekingFragment;
import cn.edu.uestc.cssl.fragments.TestFragment;
import cn.edu.uestc.cssl.fragments.TrackBarycenterFragment;
import cn.edu.uestc.cssl.fragments.TrackBonesFragment;
import cn.edu.uestc.cssl.fragments.VitalSignFragment;
import cn.edu.uestc.cssl.fragments.VoiceRecognitionFragment;
import cn.edu.uestc.cssl.util.DataSetter;
import cn.edu.uestc.cssl.util.Listener;
import cn.edu.uestc.cssl.util.MessageReceiver;
import cn.edu.uestc.cssl.util.Talker;
import de.hdodenhof.circleimageview.CircleImageView;
import me.yokeyword.fragmentation.SupportHelper;
import std_msgs.String;

@SuppressWarnings("FieldCanBeLocal")
public class RobotController extends AppCompatRosActivity implements
        MessageReceiver,NavigationView.OnNavigationItemSelectedListener, SensorEventListener, DataSetter<String> {
    // Logcat Tag
    public static final java.lang.String TAG = "RobotController";

    //当前机器人信息
    public static RobotInfo ROBOT_INFO = null;

    public static RobotController controller;

    /**
     * Notification ticker for the App
     */
    public static final java.lang.String NOTIFICATION_TICKER = "ROS Control";
    /**
     * Notification title for the App
     */
    public static final java.lang.String NOTIFICATION_TITLE = "ROS Control";

    // NodeMainExecutor encapsulating the Robot's connection
    private NodeMainExecutor nodeMainExecutor;
    // The NodeConfiguration for the connection
    private NodeConfiguration nodeConfiguration;

    // Variables for keeping track of Fragments 碎片栈的变量
    public static final int FIRST = 0;
    public static final int SECOND = 1;
    public static final int THIRD = 2;
    public static final int FOURTH = 3;
    public static final int FIFTH = 4;
    public static final int SIXTH = 5;
    public static final int SEVENTH = 6;
    public static final int EIGHTH = 7;
    public static final int NINTH = 8;
    public static final int TENTH = 9;
    public static final int ELEVENTH = 10;
    public static final int TWELFTH = 11;
    public static final int THIRTEEN = 12;
    public static final int FOURTEEN = 13;
    public static final int FIFTEEN = 14;
    public static final int SIXTEEN = 15;
    public static final int SEVENTEEN = 16;
    private RosFragment[] fragments = new RosFragment[17];
    private RosFragment fragment = null;


    //导航栏
    private Toolbar mToolbar;
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle toggle;
    private NavigationView mNavigationView;
    // NavigationView上的头像
    private CircleImageView mImgNav;

    //页面副标题
    private TextView subtitle;

    //摇一摇 todo
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 400;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    //发送启动设备命令
    private Talker<std_msgs.String> talker;
    private ProgressDialog progressDialog;
    enum Function{BaseInfomation,MapBuild,EmotionRecognition,VoiceRecognition,FaceDetection,FaceRecognition,PoseEstimation,TrackBarycenter
    ,TrackBones,Setting,ObjectTracking,VitalSign,TargetSeeking,TestPose,TestEmotion,TestVoice,None};
    private int former_index;
    private int former_id;
    private List<Boolean> switchValues;
    private Listener resultListener;


    public RobotController() {
        super(NOTIFICATION_TICKER, NOTIFICATION_TITLE, URI.create(ROBOT_INFO.getMasterUri()));
        controller = this;
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        try {
            java.net.Socket socket = new java.net.Socket(getMasterUri().getHost(), getMasterUri().getPort());
            java.net.InetAddress local_network_address = socket.getLocalAddress();
            socket.close();

            this.nodeMainExecutor = nodeMainExecutor;
            this.nodeConfiguration =
                    NodeConfiguration.newPublic(local_network_address.getHostAddress(), getMasterUri());
            if (fragment != null) {
                //初始化节点执行者
                fragment.initialize(this.nodeMainExecutor, nodeConfiguration);
            }
            this.nodeMainExecutor.execute(talker, this.nodeConfiguration.setNodeName("android/base_information/init_device"));
            this.nodeMainExecutor.execute(resultListener, nodeConfiguration.setNodeName("android/listener_initialization"));
//            关闭所有设备
            Thread.sleep(500);
            Log.i("RobotController----","closeall");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("mode","close_All");
            jsonObject.put("object",1);
            talker.sendMessage(jsonObject.toString());
        } catch (Exception e) {
            // Socket problem
            Log.e(TAG, "socket error trying to get networking information from the master uri", e);
        }
    }

    /**
     * 用于初始化启动的Fragment
     *
     * @param fragment
     */
    public static void initFragment(RosFragment fragment) {
        if (fragment != null) {
            //初始化节点执行者  Todo 后面添加了新的功能该怎么搞
            fragment.initialize(controller.nodeMainExecutor, controller.nodeConfiguration);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot_controller);

        progressDialog = new ProgressDialog(this);
        fragment = findFragment(BaseInfomationFragment.class);
        if (fragment == null) {
            fragments[FIRST] = BaseInfomationFragment.newInstance();
            fragments[SECOND] = MapBuildFragment.newInstance();
            fragments[THIRD] = EmotionRecognitionFragment.newInstance();
            fragments[FOURTH] = VoiceRecognitionFragment.newInstance();
            fragments[FIFTH] = FaceDetectionFragment.newInstance();
            fragments[SIXTH] = FaceRecognitionFragment.newInstance();
            fragments[SEVENTH] = PoseEstimationFragment.newInstance();
            fragments[EIGHTH] = TrackBarycenterFragment.newInstance();
            fragments[NINTH] = TrackBonesFragment.newInstance();
            fragments[TENTH] = SettingFragment.newInstance();
            fragments[ELEVENTH] = ObjectTrackingFragment.newInstance();
            fragments[TWELFTH] = VitalSignFragment.newInstance();
            fragments[THIRTEEN] = TargetSeekingFragment.newInstance();
            fragments[FOURTEEN] = TestFragment.newInstance();
            fragments[FIFTEEN] = TestFragment.newInstance();
            fragments[SIXTEEN] = TestFragment.newInstance();
            fragments[SEVENTEEN] = SemanticSegmentationFragment.newInstance();
            loadMultipleRootFragment(R.id.container, FIRST,
                    fragments[FIRST],
                    fragments[SECOND],
                    fragments[THIRD],
                    fragments[FOURTH],
                    fragments[FIFTH],
                    fragments[SIXTH],
                    fragments[SEVENTH],
                    fragments[EIGHTH],
                    fragments[NINTH],
                    fragments[TENTH],
                    fragments[ELEVENTH],
                    fragments[TWELFTH],
                    fragments[THIRTEEN],
                    fragments[FOURTEEN],
                    fragments[FIFTEEN],
                    fragments[SIXTEEN],
                    fragments[SEVENTEEN]
            );
            fragment = fragments[FIRST];
        } else {
            // 这里库已经做了Fragment恢复,所有不需要额外的处理了, 不会出现重叠问题

            // 这里我们需要拿到mFragments的引用
            fragments[FIRST] = fragment;
            fragments[SECOND] = MapBuildFragment.newInstance();
            fragments[THIRD] = EmotionRecognitionFragment.newInstance();
            fragments[FOURTH] = VoiceRecognitionFragment.newInstance();
            fragments[FIFTH] = FaceDetectionFragment.newInstance();
            fragments[SIXTH] = FaceRecognitionFragment.newInstance();
            fragments[SEVENTH] = PoseEstimationFragment.newInstance();
            fragments[EIGHTH] = TrackBarycenterFragment.newInstance();
            fragments[NINTH] = TrackBonesFragment.newInstance();
            fragments[TENTH] = SettingFragment.newInstance();
            fragments[ELEVENTH] = ObjectTrackingFragment.newInstance();
            fragments[TWELFTH] = VitalSignFragment.newInstance();
            fragments[THIRTEEN] = TargetSeekingFragment.newInstance();
            fragments[FOURTEEN] = TestFragment.newInstance();//姿态测试
            fragments[FIFTEEN] = TestFragment.newInstance();//情绪测试
            fragments[SIXTEEN] = TestFragment.newInstance();//语音测试
            fragments[SEVENTEEN] = SemanticSegmentationFragment.newInstance();//语义分割
        }
        //发送启动设备命令及启动功能命令的talker，启动功能在RobotController，启动设备在BaseInformationFragment
        talker = new Talker<>("topic_Send_Command","android/base_information/init_device",std_msgs.String._TYPE,this);
        BaseInfomationFragment base_fragment = (BaseInfomationFragment) fragments[FIRST];
        base_fragment.set_Talker(talker);
        resultListener = new Listener("topic_objects_information", "android/listener_initialization",this);
        former_index = FIRST;
        former_id = R.id.action_info;

        mToolbar = findViewById(R.id.robot_controller_toolbar);
        subtitle = findViewById(R.id.subtitle);
        //设置toolbar的title
        mToolbar.setTitle(ROBOT_INFO.getName());
        subtitle.setText(R.string.action_info);
        setSupportActionBar(mToolbar);

        mDrawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);

        //同步抽屉的指示器状态，只有加这一句抽屉的开关才会改变Toolbar上的NavigationIcon图标
        toggle.syncState();
        //设置阴影
        mDrawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        //添加抽屉监听者，该监听者内部控制了ActionBar的NavigationIcon图标按钮
        mDrawer.addDrawerListener(toggle);

        mDrawer.post(() -> {
            //两者状态同步
            toggle.syncState();

        });

        mNavigationView = findViewById(R.id.navigationView);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.setCheckedItem(R.id.action_info);

        //设置导航菜单图标
        mNavigationView.getMenu().findItem(R.id.action_face_detection).setIcon(
                new IconDrawable(this, AcIcons.icon_face_detection)
                        .color(Color.BLACK)
                        .actionBarSize());
        mNavigationView.getMenu().findItem(R.id.action_face_recognition).setIcon(
                new IconDrawable(this, AcIcons.icon_face_recognition)
                        .color(Color.BLACK)
                        .actionBarSize());
        // todo 换成姿态估计的图标
        mNavigationView.getMenu().findItem(R.id.action_pose).setIcon(
                new IconDrawable(this, AcIcons.icon_track_bones)
                        .color(Color.BLACK)
                        .actionBarSize());
        mNavigationView.getMenu().findItem(R.id.action_info).setIcon(
                new IconDrawable(this, AcIcons.icon_info)
                        .color(Color.BLACK)
                        .actionBarSize());
        mNavigationView.getMenu().findItem(R.id.action_map).setIcon(
                new IconDrawable(this, AcIcons.icon_map)
                        .color(Color.BLACK)
                        .actionBarSize());
        mNavigationView.getMenu().findItem(R.id.action_emotion).setIcon(
                new IconDrawable(this, AcIcons.icon_emotion)
                        .color(Color.BLACK)
                        .actionBarSize());
        mNavigationView.getMenu().findItem(R.id.action_voice).setIcon(
                new IconDrawable(this, AcIcons.icon_voice)
                        .color(Color.BLACK)
                        .actionBarSize());
        mNavigationView.getMenu().findItem(R.id.action_settings).setIcon(
                new IconDrawable(this, AcIcons.icon_settings)
                        .color(Color.BLACK)
                        .actionBarSize());
        mNavigationView.getMenu().findItem(R.id.action_track_barycenter).setIcon(
                new IconDrawable(this, AcIcons.icon_track_barycenter)
                        .color(Color.BLACK)
                        .actionBarSize());
        mNavigationView.getMenu().findItem(R.id.action_track_bones).setIcon(
                new IconDrawable(this, AcIcons.icon_track_bones)
                        .color(Color.BLACK)
                        .actionBarSize());
        mNavigationView.getMenu().findItem(R.id.action_object_tracking).setIcon(
                new IconDrawable(this, EntypoIcons.entypo_hair_cross)
                        .color(Color.BLACK)
                        .actionBarSize());
        mNavigationView.getMenu().findItem(R.id.action_vital_sign).setIcon(
                new IconDrawable(this, EntypoIcons.entypo_heart)
                        .color(Color.BLACK)
                        .actionBarSize());
        mNavigationView.getMenu().findItem(R.id.action_target_seeking).setIcon(
                new IconDrawable(this, MaterialCommunityIcons.mdi_account_search)
                        .color(Color.BLACK)
                        .actionBarSize());
        mNavigationView.getMenu().findItem(R.id.action_test_pose).setIcon(
                new IconDrawable(this, AcIcons.icon_track_bones)
                        .color(Color.RED)
                        .actionBarSize());
        mNavigationView.getMenu().findItem(R.id.action_test_emotion).setIcon(
                new IconDrawable(this, AcIcons.icon_emotion)
                        .color(Color.RED)
                        .actionBarSize());
        mNavigationView.getMenu().findItem(R.id.action_test_voice).setIcon(
                new IconDrawable(this, AcIcons.icon_voice)
                        .color(Color.RED)
                        .actionBarSize());
        mNavigationView.getMenu().findItem(R.id.action_semantic_segmentation).setIcon(
                new IconDrawable(this, EntypoIcons.entypo_tree)
                        .color(Color.BLACK)
                        .actionBarSize());

        LinearLayout llNavHeader = (LinearLayout) mNavigationView.getHeaderView(0);

        mImgNav = llNavHeader.findViewById(R.id.imageView);
        mImgNav.setImageDrawable(
                new IconDrawable(this, AcIcons.icon_robot)
                        .color(Color.BLACK)
                        .actionBarSize());
        llNavHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.closeDrawer(GravityCompat.START);
                mDrawer.postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        goLogin();
                    }
                }, 250);
            }
        });

        //摇一摇 todo
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setCheckable(true);
        item.setChecked(true);
        //关闭侧边菜单栏
        mDrawer.closeDrawers();
        Log.i("RobotController----",java.lang.String.valueOf(former_index) +","+item.getItemId());
        if(former_id != item.getItemId()) {
            switch (item.getItemId()) {
                case R.id.action_object_tracking:
                    skip(ELEVENTH, R.string.action_object_tracking);
                    break;
                case R.id.action_map:
                    skip(SECOND, R.string.action_map);
                    break;
                case R.id.action_emotion:
                    skip(THIRD, R.string.action_emotion);
                    break;
                case R.id.action_voice:
                    skip(FOURTH, R.string.action_voice);
                    break;
                case R.id.action_face_detection:
                    skip(FIFTH, R.string.action_face_detection);
                    break;
                case R.id.action_face_recognition:
                    skip(SIXTH, R.string.action_face_recognition);
                    break;
                case R.id.action_pose:
                    skip(SEVENTH, R.string.action_pose);
                    break;
                case R.id.action_track_barycenter:
                    skip(EIGHTH, R.string.action_track_barycenter);
                    break;
                case R.id.action_track_bones:
                    skip(NINTH, R.string.action_track_bones);
                    break;
                case R.id.action_settings:
                    skip(TENTH, R.string.action_settings);
                    break;
                case R.id.action_vital_sign:
                    skip(TWELFTH, R.string.action_vital_sign);
                    break;
                case R.id.action_target_seeking:
                    skip(THIRTEEN, R.string.action_target_seeking);
                    break;
                case R.id.action_test_pose:
                    skip(FOURTEEN, R.string.action_test_pose);
                    break;
                case R.id.action_test_emotion:
                    skip(FIFTEEN, R.string.action_test_emotion);
                    break;
                case R.id.action_test_voice:
                    skip(SIXTEEN, R.string.action_test_voice);
                    break;
                case R.id.action_semantic_segmentation:
                    skip(SEVENTEEN, R.string.action_semantic_segmentation);
                    break;
                default:
                    skip(FIRST, R.string.action_info);
                    break;
            }
            former_id = item.getItemId();
        }
        return true;
    }

    /**
     * 导航栏菜单跳转
     *
     * @param index
     * @param resId
     */
    private void skip(int index, int resId) {
        // shutdown当前fragment
        if (fragment.isInitialized()) {
            if(former_index != index){
                sendFunctionMessage(transformIndexToId(former_index),false);
            }
            if(former_index == FIRST){
                BaseInfomationFragment base_fragment = (BaseInfomationFragment) fragments[FIRST];
                switchValues = base_fragment.get_Switch_Values();
            }
            if(former_index == EIGHTH){
                BaseInfomationFragment base_fragment = (BaseInfomationFragment) fragments[FIRST];
                base_fragment.recover_RGBD_Status();
            }
            // 异步关闭，增加流畅度
            synchronized (this) {
                RosFragment temp = fragment;
                new Thread(temp::shutdown).start();
            }
        }
        fragment = fragments[index];
        if(index == FIRST){
            BaseInfomationFragment base_fragment = (BaseInfomationFragment) fragments[FIRST];
            base_fragment.set_Switch_Values(switchValues);
        }
        if(index >= FOURTEEN&&index <= SIXTEEN){
            TestFragment testFragment = (TestFragment) fragments[index];
            testFragment.setMode(index);
            testFragment.refresh();
        }
        if(index == EIGHTH){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("mode","close_All");
            jsonObject.put("object",1);
            talker.sendMessage(jsonObject.toString());
        }
        sendFunctionMessage(transformIndexToId(index),true);
        former_index = index;
        //用于初始化fragment
        initFragment(fragment);

        subtitle.setText(resId);

        showHideFragment(fragment);

    }

    //todo
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    SupportHelper.showFragmentStackHierarchyView(this);
                }

                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    //todo
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        nodeMainExecutor.shutdownNodeMain(resultListener);
        nodeMainExecutor.shutdownNodeMain(talker);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data == null){
            Log.i("RobotController----","data == null");
        }
        fragment.onActivityResult(requestCode,resultCode,data);
    }

    @Override
    public void setData(std_msgs.String msg, Object object) {
        msg.setData((java.lang.String) object);
    }
    public void sendFunctionMessage(Function func,boolean isinit) {
        Log.i("RobotController----", func.toString());
        Log.i("RobotController----", java.lang.String.valueOf(isinit));
        if (func != Function.None) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("object", func.ordinal());
            if (!isinit) {
                jsonObject.put("mode", "close_function");
            } else if (isinit) {
                jsonObject.put("mode", "init_function");
            }
            if (talker != null && jsonObject != null) {
                Log.i("RobotController----", jsonObject.toString());
                talker.sendMessage(jsonObject.toString());
                if (isinit) {
                    if (func != Function.MapBuild && func != Function.TrackBarycenter) {
                        progressDialog = progressDialog.show(this, "Connecting", "Connecting to robot"
                                , true, true);
                    }
                }
            }
        }
    }
    public Function transformIndexToId(int index){
        if(index == FIRST || index == TWELFTH || index == TENTH){
            return Function.None;
        }
        return Function.values()[index];
    }

    @Override
    public void showMessage(java.lang.String msg) {
        Log.i("RobotController_",msg);
        com.alibaba.fastjson.JSONArray jsonArray = null;
        jsonArray =  com.alibaba.fastjson.JSONArray.parseArray(msg);
        java.lang.String type = jsonArray.getString(0);
        Log.i("RobotController_",type);
        if(type.equals("function")){
            progressDialog.dismiss();
        }
        else if(type.equals("device")){
            BaseInfomationFragment base_fragment = (BaseInfomationFragment) fragments[FIRST];
            base_fragment.closeProgressDialog();
        }
        else if(type.equals("function_all")){
            BaseInfomationFragment base_fragment = (BaseInfomationFragment) fragments[FIRST];
            base_fragment.enableAllSwitch();
        }
    }
}
