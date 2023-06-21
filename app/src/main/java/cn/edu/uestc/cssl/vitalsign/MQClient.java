package cn.edu.uestc.cssl.vitalsign;

import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MQClient {

    private static final Logger log = LoggerFactory.getLogger(MQClient.class);

    /**
     * mqtt客户端地址
     */
    private String mqClientConsumerAddress = "";
    /**
     * mqtt host地址
     */
    private String host = "tcp://192.168.8.235";
    /**
     * 连接mqtt的用户名和密码，可以不用
     */
    private String mqttUserName = "";
    private String mqttPassword ="";
    private MqttClient client;
    private MqttConnectOptions options;
    private boolean connect_signal = true;

    private static volatile MQClient mqClient = null;

    private MQClient() {

    }

    /**
     * 创建单例模式，用户长连接
     * @return
     */
    public static MQClient getInstance() {
        if (null == mqClient) {
            synchronized (MQClient.class) {
                if (null == mqClient) {
                    return new MQClient();
                }
            }
        }
        return mqClient;
    }

    /**
     * @author shudj
     * @date 16:00 2020/1/15
     * @description 创建服务
     *
      * @param
     * @return void
     **/
    public void creat2() {

        client = init();
        Log.i("MQClient","init");
        // 创建失败再创建一次
        if (null == client) {
            client = init();
        }
        if(connect_signal != false) {
            Log.i("MQClient","start_connect");
            connect();
        }
    }
    /**
     * 创建MqttClient对象
     * @return
     */
    private MqttClient init() {
        try {
            // 使用当前时间点，确保客户端完全唯一
            connect_signal = true;
            String client = mqClientConsumerAddress + "_" + System.currentTimeMillis();
            return new MqttClient(host, client, new MemoryPersistence());
        } catch (MqttException e) {
            connect_signal = false;
            Log.i("MQClient",String.format("创建MqttClient失败：%s" , e.toString()));
        }
        return null;
    }

    private MqttConnectOptions getOptions() {
        // MQTT的连接设置
        options = new MqttConnectOptions();
        // 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
        options.setCleanSession(true);
        // 设置连接的用户名
        String userName = mqttUserName;
        if (!StringUtils.isBlank(userName)) {
            options.setUserName(userName);
        }
        // 设置连接的密码
        String password = mqttPassword;
        if (!StringUtils.isBlank(password)) {
            options.setPassword(password.toCharArray());
        }
        // 设置超时时间 单位为秒
        options.setConnectionTimeout(3);
        // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
        options.setKeepAliveInterval(20);
        //setWill方法，如果项目中需要知道客户端是否掉线可以调用该方法。设置最终端口的通知消息
        //options.setWill(topic, "close".getBytes(), 2, true);

        return options;

    }

    /**
     * @author shudj
     * @date 14:37 2020/1/15
     * @description  创建连接
     *
      * @param
     * @return void
     **/
    public void connect() {
        try {
            if (null != client) {
//                Log.i("MQClient","连接MQTT服务器");
//                client.connect(getOptions());
                MqttConnectOptions options = getOptions();
                //判断拦截状态，这里注意一下，如果没有这个判断，是非常坑的
                if (!client.isConnected()) {
                    client.connect(options);
                    Log.i("MQClient","连接成功");
                }
//                else {//这里的逻辑是如果连接成功就重新连接
//                    client.disconnect();
//                    client.connect(getOptions());
//                    log.info("MQClient","连接成功");
//                }
            }
        } catch (MqttException e) {
            Log.i("MQClient","连接失败");
//            int count = 0;
//            int three = 3;
//            // 重新连接三次，5秒连接一次
//            while (count < three) {
//                try {
//                    Log.i("MQClient","连接失败，重新连接..."+e);
//                    client.connect(getOptions());
//                    if (client.isConnected()) {
//                        break;
//                    } else {
//                        Thread.sleep(5000);
//                    }
//                } catch (Exception e1) {
//                    Log.i("MQClient","再次连接失败，重新连接..."+e);
//                } finally {
//                    count ++;
//                }
//            }
        }
    }

    public boolean isConnected() {
        return mqClient.isConnected();
    }

    /**
     * @author shudj
     * @date 14:35 2020/1/15
     * @description  订阅消息
     *
      * @param topic 传入主题
     * @return void
     **/
    public void subMessage(String topic, int qos) {
        //订阅消息
        try {
            int[] qoses = {qos};
            String[] topics = {topic};
            client.subscribe(topics, qoses);
        } catch (MqttException e) {
            Log.i("MQClient", String.format("订阅失败：%s", e.toString()));
        }
    }

    public MqttClient getClient() {
        return client;
    }

    /**
         * 发送消息
     * @param topic
     * @param qos
     * @param message
     */
    public void sendMessage(String topic, int qos, String message) {
        try {
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            mqttMessage.setQos(qos);
            client.publish(topic, mqttMessage);
        } catch (MqttException e) {
            log.error("发送消息失败:", e);
        }
    }
}
