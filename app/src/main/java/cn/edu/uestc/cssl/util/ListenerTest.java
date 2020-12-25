package cn.edu.uestc.cssl.util;

import org.apache.commons.logging.Log;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import std_msgs.String;

public class ListenerTest extends AbstractNodeMain {
    private java.lang.String topicName;
    private java.lang.String nodeName;

    public ListenerTest(java.lang.String topicName, java.lang.String nodeName) {
        this.topicName = topicName;
        this.nodeName = nodeName;
    }

    public GraphName getDefaultNodeName() {
        return GraphName.of(this.nodeName);
    }

    public void onStart(ConnectedNode connectedNode) {
        final Log log = connectedNode.getLog();
        Subscriber<String> subscriber = connectedNode.newSubscriber(this.topicName, std_msgs.String._TYPE);
        subscriber.addMessageListener(message -> {
            log.info("I heard: \"" + message.getData() + "\"");

        });
    }
}
