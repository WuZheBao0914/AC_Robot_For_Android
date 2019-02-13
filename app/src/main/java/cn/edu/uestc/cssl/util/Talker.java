package cn.edu.uestc.cssl.util;

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

public class Talker extends AbstractNodeMain {

    private String topicName;
    private String nodeName;

    private static Publisher<std_msgs.String> publisher;

    /**
     *
     * @param topicName
     * @param nodeName
     */
    public Talker(String topicName, String nodeName) {
        this.topicName = topicName;
        this.nodeName = nodeName;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of(this.nodeName);
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {

        publisher = connectedNode.newPublisher(this.topicName, std_msgs.String._TYPE);
    }

    public void sendMessage(String msg) {
        std_msgs.String str = publisher.newMessage();
        str.setData(msg);
        publisher.publish(str);
    }
}
