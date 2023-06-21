package cn.edu.uestc.cssl.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.common.collect.Lists;

import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.util.ArrayList;
import java.util.List;

import cn.edu.uestc.cssl.activities.R;
import cn.edu.uestc.cssl.delegates.RosFragment;
import cn.edu.uestc.cssl.util.AbstractListener;
import cn.edu.uestc.cssl.util.DataSetter;
import cn.edu.uestc.cssl.util.Talker;
import std_msgs.String;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TestFragment extends RosFragment implements DataSetter<String> {//项目验收时使用的Fragment，可以不用管这个部分
    private Talker<std_msgs.String> talker;
    private AbstractListener<std_msgs.String> accurate_listener;
    private AbstractListener<std_msgs.String> time_listener;
    private java.lang.String topicName_talker;
    private java.lang.String topicName_listener;
    private java.lang.String topicName_listener_2;
    private Button startBtn;
    private TextView accurate_text;
    private TextView time_text;
    private Handler uiHandler;
    private Spinner database_spinner;
    private NumberPicker numberPicker;
    private int index;
    private int mode;
    public TestFragment() {
        // Required empty public constructor
    }
    // TODO: Rename and change types and number of parameters
    public static TestFragment newInstance() {
        TestFragment fragment = new TestFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void initialize(NodeMainExecutor nodeMainExecutor, NodeConfiguration nodeConfiguration) {
        super.initialize(nodeMainExecutor, nodeConfiguration);
        Log.i("TestFragment","initialize");
        //下面开始初始化本页面的ROS节点，用于远程与机器人通信
        nodeMainExecutor.execute(talker, nodeConfiguration.setNodeName("nodename/"+topicName_talker));//发命令
        nodeMainExecutor.execute(accurate_listener, nodeConfiguration.setNodeName("nodename/"+topicName_listener));//收准确率结果
        nodeMainExecutor.execute(time_listener, nodeConfiguration.setNodeName("nodename/"+topicName_listener_2));//收用时结果
        setInitialized(true);//设置该页面已经初始化
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public Object setLayout() {
        return null;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        Log.i("TestFragment","onBindView");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_test, container, false);
        startBtn = view.findViewById(R.id.btn_start_test);
        database_spinner = view.findViewById(R.id.target_database);
        accurate_text = view.findViewById(R.id.accurate_text);
        time_text = view.findViewById(R.id.testtime_text);

        numberPicker = view.findViewById(R.id.test_NumberPicker);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(10);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBtn.setEnabled(false);
                talker.sendMessage(java.lang.String.valueOf(numberPicker.getValue()*index));
                time_text.setText("测试中...");
            }
        });
        uiHandler = new Handler() {
            public void handleMessage(Message msg) {
                if(msg.what==0){
                    startBtn.setEnabled(true);
                    accurate_text.setText((java.lang.String)msg.obj + "%");
                    if(mode == 2){//语音识别
                        time_text.setText("测试完毕");
                    }
                }
                else if(msg.what ==1){
                    time_text.setText("平均用时："+(java.lang.String)msg.obj + "秒/张");
                }
            }
        };
        return view;
    }

    @Override
    public void setData(String msg, Object object) {
        msg.setData((java.lang.String) object);
    }

    public void setMode(int i){
        mode = i-13;
        List<java.lang.String> nodeName = Lists.newArrayList("testForPose","testForEmotion","testForVoice");
        topicName_talker = "android/"+nodeName.get(mode)+"/start";
        topicName_listener = "android/"+nodeName.get(mode)+"/accResult";
        topicName_listener_2 = "android/"+nodeName.get(mode)+"/timeResult";
        List<Integer> databaselist = Lists.newArrayList(R.array.target_database_pose,R.array.target_database_emotion,R.array.target_database_voice);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getControlApp(), databaselist.get(mode),
                android.R.layout.simple_dropdown_item_1line);
        database_spinner.setAdapter(adapter);

        int[] index_ = {30,50,50};
        index = index_[mode];
        List<java.lang.String> values = new ArrayList();
        for(int temp=1;temp<=10;temp++){
            values.add(java.lang.String.valueOf(temp*index));
        }
        numberPicker.setDisplayedValues(values.toArray(new java.lang.String[values.size()]));

        talker = new Talker<>(topicName_talker,"nodename/"+topicName_talker,std_msgs.String._TYPE,this);
        accurate_listener = new AbstractListener<>(topicName_listener, "nodename/"+topicName_listener, std_msgs.String._TYPE
                , message ->{
            Message msg = uiHandler.obtainMessage();
            msg.what = 0;
            msg.obj = message.getData();
            uiHandler.sendMessage(msg);
        });
        time_listener = new AbstractListener<>(topicName_listener_2, "nodename/"+topicName_listener_2, std_msgs.String._TYPE
                , message ->{
            Message msg = uiHandler.obtainMessage();
            msg.what = 1;
            msg.obj = message.getData();
            uiHandler.sendMessage(msg);
        });
    }
    public void refresh(){
        time_text.setText("等待测试");
        startBtn.setEnabled(true);
        accurate_text.setText("");
    }

}