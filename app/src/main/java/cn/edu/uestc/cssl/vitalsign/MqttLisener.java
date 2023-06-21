package cn.edu.uestc.cssl.vitalsign;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public interface MqttLisener {
    public void OnCall(MqttMessage mqttMessage);
}
