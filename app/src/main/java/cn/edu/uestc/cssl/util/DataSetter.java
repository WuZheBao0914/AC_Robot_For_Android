package cn.edu.uestc.cssl.util;

import org.ros.internal.message.Message;

public interface DataSetter<T extends Message> {

    void setData(T msg, Object object);
}
