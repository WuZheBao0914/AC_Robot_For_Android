package cn.edu.uestc.cssl.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.alibaba.fastjson.JSONObject;

import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.util.Arrays;
import java.util.List;

import cn.edu.uestc.cssl.activities.R;
import cn.edu.uestc.cssl.delegates.RosFragment;
import cn.edu.uestc.cssl.util.DataSetter;
import cn.edu.uestc.cssl.util.Talker;
import std_msgs.String;

/*
 *@author xuyang
 *@createTime 2019/2/17 14:42
 *@description 基本信息Fragment
 */
public class BaseInfomationFragment extends RosFragment implements DataSetter<String> {
    private Switch switch_RGBD;
    private Switch switch_Ridar;
    private Switch switch_Server;
    private Switch switch_Lower_Computer;
    private ProgressDialog progressDialog;

    private boolean needSendCommand;
    private Talker<std_msgs.String> talker;

    private enum Device{RGBD,Ridar,Server,Chassis};
    public static BaseInfomationFragment newInstance() {

        Bundle args = new Bundle();

        BaseInfomationFragment fragment = new BaseInfomationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void initialize(NodeMainExecutor nodeMainExecutor, NodeConfiguration nodeConfiguration) {
        super.initialize(nodeMainExecutor, nodeConfiguration);

//        nodeMainExecutor.execute(talker, nodeConfiguration.setNodeName("android/base_information/init_device"));//发图片
        setInitialized(true);
    }

    @Override
    public Object setLayout() {
        return R.layout.fragment_base_infomation;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
//        talker = new Talker<>("topic_Send_Command","android/base_information/init_device",std_msgs.String._TYPE,this);
        needSendCommand = true;

        switch_RGBD = rootView.findViewById(R.id.switch_rgbd);
        switch_Ridar = rootView.findViewById(R.id.switch_ridar);
        switch_Server = rootView.findViewById(R.id.switch_roscore);
        switch_Lower_Computer = rootView.findViewById(R.id.switch_chassis);
        switch_Ridar.setChecked(true);
        switch_Ridar.setEnabled(false);
        switch_Server.setChecked(true);
        switch_Server.setEnabled(false);
        switch_Lower_Computer.setChecked(true);
        switch_Lower_Computer.setEnabled(false);
        switch_RGBD.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sendCommand(Device.RGBD,isChecked);
            }
        });
        switch_Ridar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sendCommand(Device.Ridar,isChecked);
            }
        });
        switch_Server.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sendCommand(Device.Server,isChecked);
            }
        });
        switch_Lower_Computer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sendCommand(Device.Chassis,isChecked);
            }
        });
    }

    @Override
    public void shutdown() {

    }
    public void set_Switch_Values(List<Boolean> values){
        needSendCommand = false;
        switch_RGBD.setChecked(values.get(0));
        switch_Ridar.setChecked(values.get(1));
        switch_Server.setChecked(values.get(2));
        switch_Lower_Computer.setChecked(values.get(3));
        needSendCommand = true;
    }
    public List<Boolean> get_Switch_Values(){
        List<Boolean> values = Arrays.asList(switch_RGBD.isChecked(),switch_Ridar.isChecked(),switch_Server.isChecked(),switch_Lower_Computer.isChecked());
        return values;
    }
    public void set_Talker(Talker<std_msgs.String> talker_){
        talker = talker_;

    }

    @Override
    public void setData(String msg, Object object) {
        msg.setData((java.lang.String) object);
    }
    public void sendCommand(Device device,boolean isinit){
        if(needSendCommand) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("object", device.ordinal());
            if (!isinit) {
                jsonObject.put("mode", "close_device");
            } else if (isinit) {
                jsonObject.put("mode", "init_device");
            }
            talker.sendMessage(jsonObject.toString());
            progressDialog = progressDialog.show(getControlApp(), "Connecting", "Connecting to robot"
                    , true, true);
        }
    }
    public void closeProgressDialog(){
        progressDialog.dismiss();
    }
    public void enableAllSwitch(){
        switch_RGBD.setEnabled(true);
        switch_Ridar.setEnabled(true);
        switch_Lower_Computer.setEnabled(true);
    }
    public void recover_RGBD_Status(){
        if(switch_RGBD.isChecked()){
            sendCommand(Device.RGBD,true);
        }
    }
}
