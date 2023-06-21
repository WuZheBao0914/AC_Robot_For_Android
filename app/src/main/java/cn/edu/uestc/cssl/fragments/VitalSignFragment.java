package cn.edu.uestc.cssl.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.common.collect.Lists;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.edu.uestc.cssl.activities.R;
import cn.edu.uestc.cssl.delegates.RosFragment;
import cn.edu.uestc.cssl.vitalsign.IwCallBack;
import cn.edu.uestc.cssl.vitalsign.LineChart_Datasource;
import cn.edu.uestc.cssl.vitalsign.MQClient;
import cn.edu.uestc.cssl.vitalsign.MqttLisener;

public class VitalSignFragment extends RosFragment {

    private TextView view_blood_oxygen;//血氧
    private TextView view_blood_Pressure;//血压
    private TextView view_heart_Rate;//心率
    private TextView view_sleep_Status;//睡眠状态
    private TextView view_Tempreature;//体温
    private List<List<LineChart_Datasource>> lineChartDatasourceList;//数据源，邻接表
    private List<List<LineDataSet>> lineDataSetList;//从数据源转化为图表适配数据，邻接表
    private List<LineChart> lineChartList;//图表列表
    private List<TextView> textViewList;//信息视图列表
    private MQClient mqttClient;
    private int count;
    private static List<String> nameList= Lists.newArrayList("Blood_Oxygen","Heart_Ratio","Blood_Pressure","Temprature");

    public VitalSignFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static VitalSignFragment newInstance() {
        VitalSignFragment fragment = new VitalSignFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void initialize(NodeMainExecutor nodeMainExecutor, NodeConfiguration nodeConfiguration) {
        super.initialize(nodeMainExecutor, nodeConfiguration);
        set_iwCallback();//创建MQTT连接及接收回调函数
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Object setLayout() {
        return null;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_vital_sign, null);
        //绑定控件
        view_blood_oxygen = view.findViewById(R.id.graph_father1_data_text_left);
        view_heart_Rate = view.findViewById(R.id.graph_father2_data_text);
        view_sleep_Status = view.findViewById(R.id.graph_father3_data_text);
        view_blood_Pressure = view.findViewById(R.id.graph_father4_data_text);
        view_Tempreature = view.findViewById(R.id.graph_father5_data_text);

        lineChartDatasourceList = new ArrayList();
        lineDataSetList = new ArrayList();
        lineChartList = new ArrayList();
        textViewList = new ArrayList();

        lineChartList.add(view.findViewById(R.id.lineChart_BloodOxygen));
        lineChartList.add(view.findViewById(R.id.lineChart_HeartRatio));
        lineChartList.add(view.findViewById(R.id.lineChart_BloodPressure));
        lineChartList.add(view.findViewById(R.id.lineChart_Temprature));

        textViewList.add(view.findViewById(R.id.graph_father1_left));//血氧
        textViewList.add(view.findViewById(R.id.graph_father2_left));//心跳
        textViewList.add(view.findViewById(R.id.graph_father4_left));//血压
        textViewList.add(view.findViewById(R.id.graph_father5_left));//体温

        for (int i = 0; i < 4; i++) {
            create_Chart(i);
        }



        return view;
    }

    @Override
    public void shutdown() {

    }

    private void FakeData(){
        float detect_value[] = new float[2];
        java.lang.String blood_Pressure = new java.lang.String();
        for(int i=0;i<10;i++){
            float f = 99+(float)Math.random();
            BigDecimal b = new BigDecimal(f);
            float value = b.setScale(1,BigDecimal.ROUND_HALF_UP).floatValue();
            updateChart(value,0);//血氧
            view_blood_oxygen.setText(String.valueOf(value));
            detect_value[0]=value;
            textViewList.get(0).setText("血氧-"+anomaly_Detection(detect_value,0));

            f=(float)(75+5*Math.random());
            b = new BigDecimal(f);
            value = b.setScale(0,BigDecimal.ROUND_HALF_UP).floatValue();
            detect_value[0]=value;
            textViewList.get(1).setText("心率-"+anomaly_Detection(detect_value,1));
            view_heart_Rate.setText(String.valueOf(value));
            updateChart(value,1);//心率

            blood_Pressure="";
            f=(float)(60+15*Math.random());
            b = new BigDecimal(f);
            value = b.setScale(0,BigDecimal.ROUND_HALF_UP).floatValue();
            updateChart(value,2);//血压
            detect_value[0]=value;
            blood_Pressure += String.valueOf(value) + "/";//拼接字符串

            f=(float)(90+45*Math.random());
            b = new BigDecimal(f);
            value = b.setScale(0,BigDecimal.ROUND_HALF_UP).floatValue();
            detect_value[1]=value;
            blood_Pressure += String.valueOf(value);
            view_blood_Pressure.setText(blood_Pressure);
            textViewList.get(2).setText("血压-"+anomaly_Detection(detect_value,2));
            updateChart(value,2);//血压


            f=(float)(36+1*Math.random());
            b = new BigDecimal(f);
            value = b.setScale(1,BigDecimal.ROUND_HALF_UP).floatValue();

            detect_value[0]=value;
            view_Tempreature.setText(String.valueOf(value));
            textViewList.get(3).setText("体温-"+anomaly_Detection(detect_value,3));
            updateChart(value,3);//体温
        }
        view_sleep_Status.setText(checkSleepStatus("00"));
    }

    public String getSpecificPack(JSONObject jsonObject,String macId){//获取特定MacId的pack包
        jsonObject = jsonObject.getJSONObject("comType");
        jsonObject = jsonObject.getJSONObject("content");
        if(jsonObject.getString("type").equals("ReportBordcastAll")){
            String MacId;
            jsonObject = jsonObject.getJSONObject("data");
            for(int i=1;;i++){
                if(jsonObject.containsKey("mac"+i)){
                    MacId = jsonObject.getString("mac"+i);
                    if(MacId.equals(macId)){
//                        Log.i("callbacklistener",MacId);
                        return jsonObject.getString("pack"+i);
                    }
                }
                else {
//                    Log.i("callbacklistener","null");
                    return "null";
                }
            }
        }
        else{
            return "null";
        }
    }

    public String checkSleepStatus(String sleepStatus){//根据体征数据转换成对应的状态文本
        switch (sleepStatus){
            case "00":
                return "清醒";
            case "01":
                return "浅度睡眠";
            case "02":
                return "深度睡眠";
            case "FF":
                return "未检测";
            default:
                return "错误";
        }
    }

    public void updateChart(float y,int chart_Number) {//更新图表数据
        if(chart_Number==2){//血压时
            int change_id = lineChartDatasourceList.get(chart_Number).get(0).get_yDataList().size()<=
                    lineChartDatasourceList.get(chart_Number).get(1).get_yDataList().size()
                    ?0:1;
            lineChartDatasourceList.get(chart_Number).get(change_id).add_NewData(y);
            lineDataSetList.get(chart_Number).get(change_id).notifyDataSetChanged();
            lineChartList.get(chart_Number).setData(new LineData(lineDataSetList.get(chart_Number).get(0),lineDataSetList.get(chart_Number).get(1)));
        }
        else{
            lineChartDatasourceList.get(chart_Number).get(0).add_NewData(y);
            lineDataSetList.get(chart_Number).get(0).notifyDataSetChanged();
            lineChartList.get(chart_Number).setData(new LineData(lineDataSetList.get(chart_Number).get(0)));
        }
        lineChartList.get(chart_Number).invalidate();
    }

    public void create_Chart(int type){//图表绑定数据源，设置图表样式
        lineChartDatasourceList.add(new ArrayList());
        lineChartDatasourceList.get(type).add(new LineChart_Datasource());
        lineDataSetList.add(new ArrayList());
        lineDataSetList.get(type).add(new LineDataSet(lineChartDatasourceList.get(type).get(0).get_yDataList(),nameList.get(type)));//读者请根据namelist确定数据集对应体征
        set_LinedataSet(lineDataSetList.get(type).get(0),type);
        set_Linechart(lineChartList.get(type),type);
        if(type==2){//血压时
            lineChartDatasourceList.get(type).add(new LineChart_Datasource());
            lineDataSetList.get(type).add(new LineDataSet(lineChartDatasourceList.get(type).get(1).get_yDataList(),nameList.get(type)+"_high"));
            set_LinedataSet(lineDataSetList.get(type).get(1),5);
        }
    }

    public void set_LinedataSet(LineDataSet origin_set,int type){//设置图表样式(使用LineDataSet来调整Chart样式-不知道为啥)
        if(type == 5){
            origin_set.setColor(Color.YELLOW);
            origin_set.setCircleColor(Color.YELLOW);
        }
        else{
            origin_set.setColor(Color.GREEN);
            origin_set.setCircleColor(Color.GREEN);
        }


        origin_set.setDrawCircles(false);
        origin_set.setDrawFilled(false);
        origin_set.setDrawValues(false);
        origin_set.setLineWidth(1f);
        origin_set.setCircleRadius(3f);
        origin_set.setDrawCircleHole(false);
    }

    public void set_Linechart(LineChart lineChart,int type){//设置图表样式
        lineChart.setBackgroundColor(Color.BLACK);
        XAxis xAxis = lineChart.getXAxis();
        YAxis yAxis = lineChart.getAxisLeft();
        Legend legend = lineChart.getLegend();
        legend.setEnabled(false);
        xAxis.setEnabled(false);
        xAxis.setTextColor(Color.WHITE);
        yAxis.setTextColor(Color.WHITE);
        lineChart.getDescription().setEnabled(false);
    }

    public String anomaly_Detection(float value[], int type){//异常检测
        switch(type){
            case 0://血氧
                if(value[0]<=90) return "血氧过低";
                return "正常";
            case 1://心率
                if(value[0]>=100) return "心动过快";
                if(value[0]<=60) return "心动过慢";
                return "正常";
            case 2://血压
                if(value[0]>=140&&value[0]<=149&&value[1]>=90&&value[1]<=94) return "临界高血压";
                if(value[0]>=140&&value[0]<=159&&value[1]>=90&&value[1]<=99) return "一级高血压";
                if(value[0]>=160&&value[0]<=179&&value[1]>=100&&value[1]<=109) return "二级高血压";
                if(value[0]>=180&&value[1]>=110) return "三级高血压";
                if(value[0]>=140&&value[1]<=90) return "单纯收缩期高血压";
                if(value[0]>=140&&value[0]<=149&&value[1]<=90) return "临界收缩期高血压";
                if(value[0]<=90&&value[1]<=60) return "低血压";
                return "正常";
            case 3://体温
                if(value[0]>37) return "高温";
                if(value[1]<35) return "低温";
                return "正常";
            default:
                return "";
        }
    }
    private void set_iwCallback(){
        mqttClient = MQClient.getInstance();
        mqttClient.creat2();
        count = 0;
        IwCallBack iwCallBack = new IwCallBack(new MqttLisener() {//接收广播数据时对应的回调操作
            @Override
            public void OnCall(MqttMessage mqttMessage) {
                String msg = mqttMessage.toString();
                Log.i("callbacklistener",msg);
                String blood_Pressure = new String();
                float detect_value[] = new float[2];
                JSONObject jsonObject = JSONObject.parseObject(msg);
                msg = getSpecificPack(jsonObject,"E6DBA1971921");
                if(!msg.equals("null")){
                    float value;
                    int bias = 20;
                    Log.i("callbacklistener",msg.substring(bias));
                    Log.i("callbacklistener","count:"+String.valueOf(count++));
                    Integer tool;

                    tool = Integer.parseInt(msg.substring(bias+16, bias+18), 16);//血氧
                    value = tool.intValue();
                    detect_value[0]=value;
                    view_blood_oxygen.setText(String.valueOf(value));
                    textViewList.get(0).setText("血氧-"+anomaly_Detection(detect_value,0));
                    updateChart(value,0);

                    tool = Integer.parseInt(msg.substring(bias+0, bias+2), 16);//心率
                    value = tool.intValue();
                    detect_value[0]=value;
                    textViewList.get(1).setText("心率-"+anomaly_Detection(detect_value,1));
                    view_heart_Rate.setText(String.valueOf(value));
                    updateChart(value,1);

                    view_sleep_Status.setText(checkSleepStatus(msg.substring(bias+10,bias+12)));//睡眠状态

                    tool = Integer.parseInt(msg.substring(bias+12, bias+14), 16);//舒张压（低压）
                    value = tool.intValue();
                    updateChart(value,2);
                    detect_value[0]=value;
                    blood_Pressure += String.valueOf(value) + "/";//拼接字符串

                    tool = Integer.parseInt(msg.substring(bias+14, bias+16), 16);//收缩压（高压）
                    value = tool.intValue();
                    updateChart(value,2);
                    detect_value[1]=value;
                    blood_Pressure += String.valueOf(value);
                    view_blood_Pressure.setText(blood_Pressure);
                    textViewList.get(2).setText("血压-"+anomaly_Detection(detect_value,2));

                    tool = Integer.parseInt(msg.substring(bias+34, bias+36), 16);//体温整数
                    value = tool.intValue();
                    tool = Integer.parseInt(msg.substring(bias+36, bias+38), 16);//体温小数
                    value += tool.floatValue()/100;
                    detect_value[0]=value;
                    view_Tempreature.setText(String.valueOf(value));
                    textViewList.get(3).setText("体温-"+anomaly_Detection(detect_value,3));
                    updateChart(value,3);
                }
            }
        });
        mqttClient.getClient().setCallback(iwCallBack);//设置回调函数
        String topic = "sys/cloud/8cd49500125e";
//        String topic = "sys/cloud/8cd495001281";
        mqttClient.subMessage(topic.toLowerCase(), 1);//订阅话题
    }
}