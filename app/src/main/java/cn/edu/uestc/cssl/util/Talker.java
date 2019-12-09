package cn.edu.uestc.cssl.util;

import org.ros.internal.message.Message;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

public class Talker<T extends Message> extends AbstractNodeMain {

    private String topicName;
    private String nodeName;
    private String topicType;
    private DataSetter<T> setter;


    private Publisher<T> publisher;

    /**
     *
     * @param topicName
     * @param nodeName
     * @param topicType
     * @param setter
     */
    public Talker(String topicName, String nodeName, String topicType, DataSetter<T> setter) {
        this.topicName = topicName;
        this.nodeName = nodeName;
        this.topicType = topicType;
        this.setter = setter;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of(this.nodeName);
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {

        publisher = connectedNode.newPublisher(this.topicName, topicType);
    }

    public void sendMessage(Object msg) {
        T message = publisher.newMessage();
        setter.setData(message, msg);
        publisher.publish(message);
    }
}
