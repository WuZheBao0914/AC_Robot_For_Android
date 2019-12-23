package cn.edu.uestc.cssl.activities;


import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import cn.edu.uestc.ac_core.app.AcRobot;
import cn.edu.uestc.ac_ui.icon.AcIcons;
import cn.edu.uestc.ac_ui.icon.FontACModule;
import cn.edu.uestc.ac_ui.icon.FontACModule1;
import cn.edu.uestc.cssl.delegates.AcDelegate;
import cn.edu.uestc.cssl.fragments.AboutFragment;
import cn.edu.uestc.cssl.fragments.HelpFragment;
import cn.edu.uestc.cssl.fragments.RobotListFragment;
import cn.edu.uestc.cssl.util.RobotStorage;
import me.yokeyword.fragmentation.SupportActivity;
import me.yokeyword.fragmentation.SupportHelper;

@SuppressWarnings("FieldCanBeLocal")
public class MainActivity extends SupportActivity implements
        NavigationView.OnNavigationItemSelectedListener, SensorEventListener {

    // Navigation drawer items  导航栏
    private Toolbar mToolbar;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;

    private ActionBarDrawerToggle mDrawerToggle;

    // Variables for keeping track of Fragments 碎片栈的变量
    public static final int FIRST = 0;
    public static final int SECOND = 1;
    public static final int THIRD = 2;
    private AcDelegate[] fragments = new AcDelegate[3];

    //摇一摇 todo
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 400;

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;


    public AcDelegate[] getFragments() {
        return fragments;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AcRobot.init(this)
                .withIcon(new FontAwesomeModule())
                .withIcon(new FontACModule())
                .withIcon(new FontACModule1())
                .configure();
        // 加载机器人信息
        RobotStorage.load(this);

        setContentView(R.layout.activity_robot_chooser);//设置内容布局


        AcDelegate firstFragment = findFragment(RobotListFragment.class);
        if (firstFragment == null) {//初始化碎片栈
            fragments[FIRST] = RobotListFragment.newInstance();
            fragments[SECOND] = HelpFragment.newInstance();
            fragments[THIRD] = AboutFragment.newInstance();

            loadMultipleRootFragment(R.id.robot_list_container, FIRST,
                    fragments[FIRST],
                    fragments[SECOND],
                    fragments[THIRD]);
        } else {
            // 这里库已经做了Fragment恢复,所有不需要额外的处理了, 不会出现重叠问题

            // 这里我们需要拿到mFragments的引用
            fragments[FIRST] = firstFragment;
            fragments[SECOND] = HelpFragment.newInstance();
            fragments[THIRD] = AboutFragment.newInstance();

        }

        mToolbar = findViewById(R.id.robot_chooser_toolbar);
        setSupportActionBar(mToolbar);

        mDrawerLayout = findViewById(R.id.profileDrawer);

        if (getActionBar() != null) {
            // 打开ActionBar
            getActionBar().setDisplayHomeAsUpEnabled(true);
            // 菜单控制开关点击事件能够响应
            getActionBar().setHomeButtonEnabled(true);
        }

        // 创建菜单控制开关
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                mToolbar, R.string.drawer_open,
                R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                //getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View view) {
                //getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }
        };
        //同步抽屉的指示器状态，只有加这一句抽屉的开关才会改变Toolbar上的NavigationIcon图标
        mDrawerToggle.syncState();
        //设置阴影
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        //添加抽屉监听者，该监听者内部控制了ActionBar的NavigationIcon图标按钮
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                //两者状态同步
                mDrawerToggle.syncState();
            }
        });


        mNavigationView = findViewById(R.id.chooser_navigationView);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.setCheckedItem(R.id.action_info);

        //设置导航菜单图标
        mNavigationView.getMenu().findItem(R.id.action_robot).setIcon(
                new IconDrawable(this, AcIcons.icon_robot2)
                        .color(Color.BLACK)
                        .actionBarSize());
        mNavigationView.getMenu().findItem(R.id.action_help).setIcon(
                new IconDrawable(this, AcIcons.icon_help)
                        .color(Color.BLACK)
                        .actionBarSize());
        mNavigationView.getMenu().findItem(R.id.action_about).setIcon(
                new IconDrawable(this, AcIcons.icon_about)
                        .color(Color.BLACK)
                        .actionBarSize());


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
        mDrawerLayout.closeDrawers();

        switch (item.getItemId()) {
            case R.id.action_about:
                showHideFragment(fragments[THIRD]);

                break;
            case R.id.action_help:
                showHideFragment(fragments[SECOND]);

                break;
            default:
                showHideFragment(fragments[FIRST]);
                break;

        }

        return true;
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
