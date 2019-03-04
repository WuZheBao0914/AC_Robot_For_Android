package cn.edu.uestc.cssl.activities;

import android.content.Context;
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

import com.joanzapata.iconify.IconDrawable;

import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.net.URI;

import cn.edu.uestc.ac_ui.icon.AcIcons;
import cn.edu.uestc.android_10.AppCompatRosActivity;
import cn.edu.uestc.cssl.delegates.RosFragment;
import cn.edu.uestc.cssl.entity.RobotInfo;
import cn.edu.uestc.cssl.fragments.BaseInfomationFragment;
import cn.edu.uestc.cssl.fragments.EmotionRecognitionFragment;
import cn.edu.uestc.cssl.fragments.FaceDetectionFragment;
import cn.edu.uestc.cssl.fragments.FaceRecognitionFragment;
import cn.edu.uestc.cssl.fragments.MapBuildFragment;
import cn.edu.uestc.cssl.fragments.SettingFragment;
import cn.edu.uestc.cssl.fragments.TrackBarycenterFragment;
import cn.edu.uestc.cssl.fragments.TrackBonesFragment;
import cn.edu.uestc.cssl.fragments.VoiceRecognitionFragment;
import de.hdodenhof.circleimageview.CircleImageView;
import me.yokeyword.fragmentation.SupportHelper;

@SuppressWarnings("FieldCanBeLocal")
public class RobotController extends AppCompatRosActivity implements
        NavigationView.OnNavigationItemSelectedListener, SensorEventListener {
    // Logcat Tag
    public static final String TAG = "RobotController";

    //当前机器人信息
    public static RobotInfo ROBOT_INFO = null;

    public static RobotController controller = null;

    /**
     * Notification ticker for the App
     */
    public static final String NOTIFICATION_TICKER = "ROS Control";
    /**
     * Notification title for the App
     */
    public static final String NOTIFICATION_TITLE = "ROS Control";

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
    private RosFragment[] fragments = new RosFragment[9];
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

        fragment = findFragment(BaseInfomationFragment.class);
        if (fragment == null) {
            fragments[FIRST] = BaseInfomationFragment.newInstance();
            fragments[SECOND] = MapBuildFragment.newInstance();
            fragments[THIRD] = EmotionRecognitionFragment.newInstance();
            fragments[FOURTH] = VoiceRecognitionFragment.newInstance();
            fragments[FIFTH] = FaceDetectionFragment.newInstance();
            fragments[SIXTH] = FaceRecognitionFragment.newInstance();
            fragments[SEVENTH] = TrackBarycenterFragment.newInstance();
            fragments[EIGHTH] = TrackBonesFragment.newInstance();
            fragments[NINTH] = SettingFragment.newInstance();

            loadMultipleRootFragment(R.id.container, FIRST,
                    fragments[FIRST],
                    fragments[SECOND],
                    fragments[THIRD],
                    fragments[FOURTH],
                    fragments[FIFTH],
                    fragments[SIXTH],
                    fragments[SEVENTH],
                    fragments[EIGHTH],
                    fragments[NINTH]);
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
            fragments[SEVENTH] = TrackBarycenterFragment.newInstance();
            fragments[EIGHTH] = TrackBonesFragment.newInstance();
            fragments[NINTH] = SettingFragment.newInstance();
        }



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
        toggle.syncState();

        //同步抽屉的指示器状态，只有加这一句抽屉的开关才会改变Toolbar上的NavigationIcon图标
        toggle.syncState();
        //设置阴影
        mDrawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        //添加抽屉监听者，该监听者内部控制了ActionBar的NavigationIcon图标按钮
        mDrawer.addDrawerListener(toggle);

        mDrawer.post(new Runnable() {
            @Override
            public void run() {
                //两者状态同步
                toggle.syncState();
            }
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

        switch (item.getItemId()) {

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
            case R.id.action_track_barycenter:
                skip(SEVENTH, R.string.action_track_barycenter);
                break;
            case R.id.action_track_bones:
                skip(EIGHTH, R.string.action_track_bones);
                break;
            case R.id.action_settings:
                skip(NINTH, R.string.action_settings);
                break;
            default:
                skip(FIRST, R.string.action_info);
                break;
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
        fragment = fragments[index];
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
}
