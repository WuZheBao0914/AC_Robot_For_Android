package cn.edu.uestc.cssl.util;

import org.ros.node.ConnectedNode;

public interface ListenerCallBack<T> {
    public void OnCall(T msg);
}
