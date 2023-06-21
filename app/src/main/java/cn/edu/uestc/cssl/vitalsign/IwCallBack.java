package cn.edu.uestc.cssl.vitalsign;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 发布消息的回调类
 * <p>
 * 必须实现MqttCallback的接口并实现对应的相关接口方法CallBack 类将实现 MqttCallBack。
 * 每个客户机标识都需要一个回调实例。在此示例中，构造函数传递客户机标识以另存为实例数据。
 * 在回调中，将它用来标识已经启动了该回调的哪个实例。
 * 必须在回调类中实现三个方法：
 * <p>
 * public void messageArrived(MqttTopic topic, MqttMessage message)接收已经预订的发布。
 * <p>
 * public void connectionLost(Throwable cause)在断开连接时调用。
 * <p>
 * public void deliveryComplete(MqttDeliveryToken token))
 * 接收到已经发布的 QoS 1 或 QoS 2 消息的传递令牌时调用。
 * 由 MqttClient.connect 激活此回调。
 */

/**
 * @author : shudj
 * mqtt订阅消息后，回执处理
 */
public class IwCallBack implements MqttCallback {

    private Logger log = LoggerFactory.getLogger(IwCallBack.class);
    private MQClient mqClient = MQClient.getInstance();
    private MqttLisener mqttLisener;
    /**
     * 订阅的主题
     */
    private String topic = "";
    public IwCallBack(MqttLisener mqttLisener){
        this.mqttLisener = mqttLisener;
    }
    @Override
    public void connectionLost(Throwable cause) {
        log.error("mqtt重连", cause);
        // 连接丢失后，一般在这里面进行重连
        mqClient.connect();
        // 重连后重新订阅一次
        if (mqClient.isConnected()) {
            mqClient.subMessage(topic, 1);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        /**
         * 获取到mqtt传输过来的消息，多线程处理解析消息，避免阻塞
         */
//        Log.i("callbacklistener", message.toString());
//        Log.i("callbacklistener",message.toString());
        mqttLisener.OnCall(message);
    }

}