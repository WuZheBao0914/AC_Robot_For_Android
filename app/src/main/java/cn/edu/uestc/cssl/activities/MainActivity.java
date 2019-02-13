package cn.edu.uestc.cssl.activities;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.joanzapata.iconify.fonts.FontAwesomeModule;

import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;


import java.net.URI;

import cn.edu.uestc.ac_core.app.AcRobot;
import cn.edu.uestc.ac_ui.icon.FontACModule;
import cn.edu.uestc.cssl.delegates.AcDelegate;
import cn.edu.uestc.cssl.fragments.FaceDetectionFragment;
import cn.edu.uestc.cssl.fragments.FaceRecognitionFragment;

public class MainActivity extends ProxyActivity {


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

    private FaceRecognitionFragment fragment;


    // Log tag String
    private static final String TAG = "MainActivity";

    public MainActivity() {
        super(NOTIFICATION_TICKER, NOTIFICATION_TITLE, URI.create("http://192.168.1.108:11311"));
    }


    @Override
    public AcDelegate setRootDelegate() {
        fragment = new FaceRecognitionFragment();
        return fragment;
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

        } catch (Exception e) {
            // Socket problem
            Log.e(TAG, "socket error trying to get networking information from the master uri", e);
        }

        //初始化节点执行者  Todo 后面添加了新的功能该怎么搞
        fragment.initialize(this.nodeMainExecutor, nodeConfiguration);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AcRobot.init(this)
                .withIcon(new FontAwesomeModule())
                .withIcon(new FontACModule())
                .configure();
    }
}
