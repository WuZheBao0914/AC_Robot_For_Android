package cn.edu.uestc.cssl.util;

import org.apache.commons.logging.Log;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

public class Listener extends AbstractNodeMain {
    private String topicName;
    private String nodeName;
    private MessageReceiver messageReceiver;

    public Listener(java.lang.String topicName, java.lang.String nodeName,
                    MessageReceiver messageReceiver) {
        this.topicName = topicName;
        this.nodeName = nodeName;
        this.messageReceiver = messageReceiver;
    }

    public GraphName getDefaultNodeName() {
        return GraphName.of(this.nodeName);
    }

    public void onStart(ConnectedNode connectedNode) {
        final Log log = connectedNode.getLog();
        Subscriber<std_msgs.String> subscriber = connectedNode.newSubscriber(this.topicName, std_msgs.String._TYPE);
        subscriber.addMessageListener(message -> {
            log.info("I heard: \"" + message.getData() + "\"");
            messageReceiver.showMessage(message.getData());

        });
    }
}
