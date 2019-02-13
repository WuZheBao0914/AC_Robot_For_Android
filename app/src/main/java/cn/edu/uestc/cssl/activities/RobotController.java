package cn.edu.uestc.cssl.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;
import org.ros.node.topic.Subscriber;

import cn.edu.uestc.cssl.util.Savable;
import sensor_msgs.CompressedImage;

/**
 * Manages receiving data from, and sending commands to, a connected Robot.
 * todo 感觉这个类没有什么卵用
 * @author xuyang
 * @create 2019/1/15 10:35
 **/
public class RobotController implements NodeMain, Savable {

    // Logcat Tag
    public static final String TAG = "RobotController";

    // The parent Context
    private final MainActivity context;

    // Whether the RobotController has been initialized
    private boolean initialized;

    // Subscriber to Pose data
    private Subscriber<CompressedImage> imageSubscriberBefore;
    private Subscriber<CompressedImage> imageSubscriberAfter;
    // The most recent Pose
    private CompressedImage imageBefore;

    private CompressedImage imageAfter;
    // Lock for synchronizing accessing and receiving the current Pose
    private final Object imageMutex = new Object();
    //处理前图像监听器
    private MessageListener<CompressedImage> imageMessageBeforeReceived;
    //处理后图像监听器
    private MessageListener<CompressedImage> imageMessageAfterReceived;

    // The node connected to the Robot on which data can be sent and received
    private ConnectedNode connectedNode;

    // Bundle ID for pausedPlan
    private static final String PAUSED_PLAN_BUNDLE_ID = "cn.edu.uestc.cssl.activities.RobotController.pausedPlan";
    // Constant for no motion plan
    private static final int NO_PLAN = -1;

    /**
     * Creates a RobotController.
     *
     * @param context The Context the RobotController belongs to.
     */
    public RobotController(MainActivity context) {
        this.context = context;
        this.initialized = false;
    }

    /**
     * Initializes the RobotController.
     *
     * @param nodeMainExecutor  The NodeMainExecutor on which to execute the NodeConfiguration.
     * @param nodeConfiguration The NodeConfiguration to execute
     */
    public void initialize(NodeMainExecutor nodeMainExecutor, NodeConfiguration nodeConfiguration) {
        nodeMainExecutor.execute(this, nodeConfiguration.setNodeName("android/robot_controller"));
    }

    /**
     * Load from a Bundle.
     *
     * @param bundle The Bundle
     */
    @Override
    public void load(@NonNull Bundle bundle) {
    }

    /**
     * Save to a Bundle.
     *
     * @param bundle The Bundle
     */
    @Override
    public void save(@NonNull Bundle bundle) {
    }

    /**
     * @return The default node name for the RobotController
     */
    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("android/robot_controller");
    }

    /**
     * Callback for when the RobotController is connected.
     *
     * @param connectedNode The ConnectedNode the RobotController is connected through
     */
    @Override
    public void onStart(ConnectedNode connectedNode) {
        this.connectedNode = connectedNode;
        initialize();
    }

    /**
     * Initializes the RobotController.
     */
    public void initialize() {
        if (!initialized && connectedNode != null) {

            // Start the topics
//            refreshTopics();

            initialized = true;
        }
    }

    /**
     * Refreshes all topics, recreating them if there topic names have been changed.
     */
    public void refreshTopics() {

        // Get the correct topic names
        String imageTopicBefore =
                context.getString(R.string.camera_topic_face_detection_origin);
        String imageTopicAfter =
                context.getString(R.string.camera_topic_face_detection_handled);


        if (imageSubscriberBefore == null || !imageTopicBefore.equals(imageSubscriberBefore.getTopicName().toString())) {
            if (imageSubscriberBefore != null)
                imageSubscriberBefore.shutdown();

            imageSubscriberBefore = connectedNode.newSubscriber(imageTopicBefore, CompressedImage._TYPE);

            imageSubscriberBefore.addMessageListener(new MessageListener<CompressedImage>() {
                @Override
                public void onNewMessage(CompressedImage image) {
                    setImageBefore(image);
                    synchronized (imageMutex) {
                        if (imageMessageBeforeReceived != null) {
                            imageMessageBeforeReceived.onNewMessage(image);
                        }
                    }
                }
            });
        }

        if (imageSubscriberAfter == null || !imageTopicAfter.equals(imageSubscriberAfter.getTopicName().toString())) {
            if (imageSubscriberAfter != null)
                imageSubscriberAfter.shutdown();

            imageSubscriberAfter = connectedNode.newSubscriber(imageTopicAfter, CompressedImage._TYPE);

            imageSubscriberAfter.addMessageListener(new MessageListener<CompressedImage>() {
                @Override
                public void onNewMessage(CompressedImage image) {
                    setImageAfter(image);
                    synchronized (imageMutex) {
                        if (imageMessageAfterReceived != null) {
                            imageMessageAfterReceived.onNewMessage(image);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onShutdown(Node node) {

    }

    /**
     * Callback for when the shutdown is complete.
     *
     * @param node The Node
     */
    @Override
    public void onShutdownComplete(Node node) {
        this.connectedNode = null;
    }

    /**
     * Callback indicating an error has occurred.
     *
     * @param node      The Node
     * @param throwable The error
     */
    @Override
    public void onError(Node node, Throwable throwable) {
        Log.e(TAG, "", throwable);
    }

    public void setCameraMessageBeforeReceivedListener(MessageListener<CompressedImage> imageMessageBeforeReceived) {
        this.imageMessageBeforeReceived = imageMessageBeforeReceived;
    }

    public void setCameraMessageAfterReceivedListener(MessageListener<CompressedImage> imageMessageAfterReceived) {
        this.imageMessageAfterReceived = imageMessageAfterReceived;
    }


    public CompressedImage getImageBefore() {
        synchronized (imageMutex) {
            return this.imageBefore;
        }

    }

    public void setImageBefore(CompressedImage imageBefore) {
        synchronized (imageMutex) {
            this.imageBefore = imageBefore;
        }
    }

    public CompressedImage getImageAfter() {
        synchronized (imageMutex) {
            return this.imageAfter;
        }
    }

    public void setImageAfter(CompressedImage imageAfter) {
        synchronized (imageMutex) {
            this.imageAfter = imageAfter;
        }
    }
}
