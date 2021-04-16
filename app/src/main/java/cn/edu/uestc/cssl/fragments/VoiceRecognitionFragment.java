package cn.edu.uestc.cssl.fragments;

import android.Manifest;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.carlos.voiceline.mylibrary.VoiceLineView;

import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import cn.edu.uestc.cssl.activities.R;
import cn.edu.uestc.cssl.delegates.RosFragment;
import cn.edu.uestc.cssl.util.DataSetter;
import cn.edu.uestc.cssl.util.Listener;
import cn.edu.uestc.cssl.util.MessageReceiver;
import cn.edu.uestc.cssl.util.Talker;
import std_msgs.String;

/*
 *@author xuyang
 *@createTime 2019/2/17 14:44
 *@description 语音识别Fragment
 */
public class VoiceRecognitionFragment extends RosFragment implements Runnable,MessageReceiver,DataSetter<std_msgs.String> {
    private MediaRecorder mediaRecorder;
    private boolean isAlive = true;
    private Button btn;
    private VoiceLineView voiceLineView;
    private File AudioFile;
    private Talker<std_msgs.String> talker; //发送json数据
    private Listener AudioTextListener = null;//检测到物体信息的接收器
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(mediaRecorder==null) return;
            double ratio = (double) mediaRecorder.getMaxAmplitude() / 100;
            double db = 0;// 分贝
            //默认的最大音量是100,可以修改，但其实默认的，在测试过程中就有不错的表现
            //你可以传自定义的数字进去，但需要在一定的范围内，比如0-200，就需要在xml文件中配置maxVolume
            //同时，也可以配置灵敏度sensibility
            if (ratio > 1)
                db = 20 * Math.log10(ratio);
            //只要有一个线程，不断调用这个方法，就可以使波形变化
            //主要，这个方法必须在ui线程中调用
            voiceLineView.setVolume((int) (db));
        }
    };
    @Override
    public void setData(std_msgs.String msg, Object object) {
        msg.setData((java.lang.String) object);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static VoiceRecognitionFragment newInstance() {
        Bundle args = new Bundle();
        VoiceRecognitionFragment fragment = new VoiceRecognitionFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void initialize(NodeMainExecutor nodeMainExecutor, NodeConfiguration nodeConfiguration) {//初始化页面时执行，只要和有ROS通信相关的节点，都需要在此初始化
        if (nodeConfiguration != null && !isInitialized()) {//节点配置不为空且该页面没有被初始化
            super.initialize(nodeMainExecutor, nodeConfiguration);//初始化父类配置
            //下面开始初始化本页面的节点
            nodeMainExecutor.execute(AudioTextListener, nodeConfiguration.setNodeName("android/listener_audiotext_information"));//物体信息接收器初始化，并指定节点名
//            nodeMainExecutor.execute(compressedImageView, nodeConfiguration.setNodeName("android/fragment_camera_view_after"));
            nodeMainExecutor.execute(talker, nodeConfiguration.setNodeName("android/talker_audiofile_information"));
            //nodeMainExecutor.execute(cameraObjectDetectionHandledView, nodeConfiguration.setNodeName("android/send_fragment_camera_view_after"));
            setInitialized(true);//设置该页面已经初始化
        }
    }


    @Override
    public Object setLayout() {
        return R.layout.fragment_voice_recognition;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView){
        requestPermissions(new java.lang.String[]{Manifest.permission.RECORD_AUDIO},100);
        voiceLineView = (VoiceLineView) rootView.findViewById(R.id.voicLine);
        AudioTextListener = new Listener("topic_voice_recognition", "android/listener_audiotext_information",this);
        talker = new Talker<>("topic_rec_Voice","android/talker_audiofile_information",std_msgs.String._TYPE,this);
        btn = rootView.findViewById(R.id.RecordAudio_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    record_audio(view);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        Thread thread = new Thread(this);
        thread.start();
    }
    //录制音频
    public void record_audio(View view) throws Exception{
        AudioFile = new File(getContext().getExternalFilesDir(""),"a.aac");
        CharSequence text = btn.getText();
        if(TextUtils.equals(text,"开始录制")){
            btn.setText("结束录制");
            mediaRecorder = new MediaRecorder();
            //设置音频源为麦克风
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            //设置音频输出文件
            mediaRecorder.setOutputFile(AudioFile.getAbsolutePath());
            try {
                mediaRecorder.prepare();

            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaRecorder.start();
        }else{
            btn.setText("开始录制");
            talker.sendMessage("123");
//            encodeBase64File(AudioFile.getAbsolutePath())
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }
    @Override
    public void onDestroy() {
        isAlive = false;
        mediaRecorder.release();
        mediaRecorder = null;
        super.onDestroy();
    }
    @Override
    public void run() {
        while (isAlive) {
            handler.sendEmptyMessage(0);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void shutdown() {
        if (isInitialized()) {
            try {
                //在Initionalize()方法中初始化的节点，都需要在此shutdown
                nodeMainExecutor.shutdownNodeMain(AudioTextListener);
                nodeMainExecutor.shutdownNodeMain(talker);
//                nodeMainExecutor.shutdownNodeMain(talker_image);
                setInitialized(false);//设置页面未被初始化
            } catch (Exception e) {
                Log.e("hint", "nodeMainExecutor为空，shutdown失败");
            }
        }
    }

    @Override
    public void showMessage(java.lang.String msg) {

    }

    public static java.lang.String encodeBase64File(java.lang.String path) throws Exception {
        File file = new File(path);
        FileInputStream inputFile = new FileInputStream(file);
        byte [] buffer = new byte [( int )file.length()];
        inputFile.read(buffer);
        inputFile.close();
        java.lang.String trackingResult=Base64.encodeToString(buffer,Base64.NO_WRAP);
        return trackingResult;
    }



}
