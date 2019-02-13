package cn.edu.uestc.cssl.util;

import org.apache.commons.logging.Log;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import std_msgs.String;

public class Listener extends AbstractNodeMain {
    public Listener() {
    }

    public GraphName getDefaultNodeName() {
        return GraphName.of("rosjava_tutorial_pubsub/listener");
    }

    public void onStart(ConnectedNode connectedNode) {
        final Log log = connectedNode.getLog();
        Subscriber<String> subscriber = connectedNode.newSubscriber("chatter", "std_msgs/String");
        subscriber.addMessageListener(new MessageListener<String>() {
            public void onNewMessage(String message) {
                log.info("I heard: \"" + message.getData() + "\"");
            }
        });
    }
}
