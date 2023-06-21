package cn.edu.uestc.cssl.util;

import android.graphics.Bitmap;
import android.util.Log;

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import cn.edu.uestc.android_10.MessageCallable;

public class AbstractListener<T> extends AbstractNodeMain{
    private java.lang.String topicName;
    private java.lang.String nodeName;
    private ListenerCallBack<T> listenerCallBack;
    private java.lang.String message_Type;
    private MessageCallable<Bitmap, T> callable = null;

    public AbstractListener(java.lang.String topicName, java.lang.String nodeName,ListenerCallBack<T> listenerCallBack) {
        this.topicName = topicName;
        this.nodeName = nodeName;
        this.listenerCallBack = listenerCallBack;
    }
    public AbstractListener(java.lang.String topicName, java.lang.String nodeName,java.lang.String message_Type,ListenerCallBack<T> listenerCallBack) {
        this.topicName = topicName;
        this.nodeName = nodeName;
        this.listenerCallBack = listenerCallBack;
        this.message_Type = message_Type;
    }
    public void setMessageToBitmapCallable(MessageCallable<Bitmap, T> callable) {
        this.callable = callable;
    }

    public void setMessage_Type(java.lang.String message_Type) {
        this.message_Type = message_Type;
    }

    public boolean callableExist(){
        return callable!=null;
    }
    public GraphName getDefaultNodeName() {
        return GraphName.of(this.nodeName);
    }

    public void onStart(ConnectedNode connectedNode) {
        Log.i("AbstractListener","onStart!!");
        Subscriber<T> subscriber = connectedNode.newSubscriber(this.topicName, message_Type);
        subscriber.addMessageListener(message -> {
            Log.i("AbstractListener","get_image!");
            listenerCallBack.OnCall(message);
        });
    }
}