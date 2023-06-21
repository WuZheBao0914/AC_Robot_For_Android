package cn.edu.uestc.cssl.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;

import org.ros.android.BitmapFromCompressedImage;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Objects;

import cn.edu.uestc.cssl.activities.R;
import cn.edu.uestc.cssl.activities.TestActivity2;
import cn.edu.uestc.cssl.delegates.RosFragment;
import cn.edu.uestc.cssl.util.AbstractListener;
import cn.edu.uestc.cssl.util.BitmapUtils;
import cn.edu.uestc.cssl.util.DataSetter;
import cn.edu.uestc.cssl.util.SelectPicPopupWindow;
import cn.edu.uestc.cssl.util.Talker;
import sensor_msgs.CompressedImage;
import std_msgs.String;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TargetSeekingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TargetSeekingFragment extends RosFragment implements DataSetter<std_msgs.String> {

    private AbstractListener<CompressedImage> target_Image_Listener;
    private AbstractListener<std_msgs.String> mode_change_Listener;
    private Talker<std_msgs.String> talker;//用于发送消息至机器人
    private Talker<std_msgs.String> patrolModeTalker;//用于发送消息至机器人
    private java.lang.String filename;//发送到机器人端的照片名称
    private status mode;
    private boolean selectFromAlbum;//是否从相册选取图片
    private Button btn_Select_Image;//调用相册选定图像按钮
    private Button btn_Send_Image;//发送图像按钮
    private Button btn_start_mission;
    private Spinner spinner_Target_Class;
    private ImageView imageView;//展示选定图像
    private ImageView anim_View;//简单的动画
    private TextView message_TextView;
    private final int SELECT_PIC = 105;//onActivityResult请求码
    private final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 200;//权限请求码
    private Bitmap bmap;
    private Handler uiHandler;
    public enum status{idle,picture_selected,waiting,working};

    public TargetSeekingFragment() {
        // Required empty public constructor
    }

    public static TargetSeekingFragment newInstance() {//Fragment初始化
        TargetSeekingFragment fragment = new TargetSeekingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void initialize(NodeMainExecutor nodeMainExecutor, NodeConfiguration nodeConfiguration) {
        super.initialize(nodeMainExecutor, nodeConfiguration);

        //下面开始初始化本页面的ROS节点，用于远程与机器人通信
        nodeMainExecutor.execute(talker, nodeConfiguration.setNodeName("android/fragment_target_seeking/start_seeking"));//发图片
        nodeMainExecutor.execute(patrolModeTalker, nodeConfiguration.setNodeName("android/fragment_target_seeking/patrol_mode"));//开始任务
        nodeMainExecutor.execute(mode_change_Listener, nodeConfiguration.setNodeName("android/fragment_target_seeking/mode_change"));//收指令
        nodeMainExecutor.execute(target_Image_Listener, nodeConfiguration.setNodeName("android/fragment_target_seeking_image/target_image"));//收图片
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
        return R.layout.fragment_target_seeking;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_target_seeking, null);
        btn_Select_Image = view.findViewById(R.id.select_picture);
        btn_Send_Image = view.findViewById(R.id.send_picture);
        btn_start_mission =view.findViewById(R.id.start_mission);
        imageView = view.findViewById(R.id.selected_picture);
        anim_View = view.findViewById(R.id.image_rotate);
        anim_View.setImageResource(R.drawable.add_face);
        message_TextView = view.findViewById(R.id.targetseeking_progress_message);

        spinner_Target_Class = view.findViewById(R.id.target_class);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getControlApp(), R.array.target_class,
                android.R.layout.simple_dropdown_item_1line);
        spinner_Target_Class.setAdapter(adapter);

        spinner_Target_Class.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                return;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });

        uiHandler = new Handler() {
            public void handleMessage(Message msg) {
                if(msg.what==0){
                    set_Image((Bitmap)msg.obj);
                    set_status(status.idle);
                    Toast.makeText(getControlApp(),"找到了目标!",Toast.LENGTH_LONG);
                }
            }
        };

        patrolModeTalker = new Talker<>("set_patrol_mode","android/fragment_target_seeking/patrol_mode",std_msgs.String._TYPE,this);
        talker = new Talker<>("topic_getFeatures_image","android/fragment_target_seeking/start_seeking",std_msgs.String._TYPE,this);
        target_Image_Listener =new AbstractListener<>("topic_getTargetImage", "android/fragment_target_seeking_image/target_image",CompressedImage._TYPE
                , message ->{
            if(mode==status.working) {
                Log.i("TargetSeekingFragment", "get_image!");
                BitmapFromCompressedImage bitmapFromCompressedImage = new BitmapFromCompressedImage();
                bmap = bitmapFromCompressedImage.call(message);
                BitmapUtils.saveBitmap("resultImage.jpg","faceimage/result/", bmap, Objects.requireNonNull(getContext()));

                Message msg = uiHandler.obtainMessage();
                msg.what = 0;
                msg.obj = bmap;
                uiHandler.sendMessage(msg);
            }

        });
        mode_change_Listener = new AbstractListener<>("topic_getFeatures_result","android/fragment_target_seeking/mode_change", std_msgs.String._TYPE
        ,message ->{
            if(message.getData().equals("2") && mode == status.waiting){//对于std_msgs类型，需要调用getData转换为String，对照见Listener
                set_status(status.working);
            }
            if(message.getData().equals("0") && mode == status.working){//对于std_msgs类型，需要调用getData转换为String，对照见Listener
                set_status(status.idle);
            }
        });

        btn_Select_Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermissions(
                        new java.lang.String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);//动态申请权限
                SelectImage();
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("image/*");
//                startActivityForResult(intent, SELECT_PIC);
            }
        });
        btn_Send_Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mode == status.picture_selected) {
                    if (imageView.getDrawable() != null) {
//                        Bitmap obmp = Bitmap.createBitmap(imageView.getDrawingCache());
                        Log.i("TargetSeekingFragment","send_image!");
                        final ByteArrayOutputStream os = new ByteArrayOutputStream();
                        bmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                        byte[] arr = os.toByteArray();
                        java.lang.String originimage = Base64.encodeToString(arr, Base64.NO_WRAP);

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("image", originimage);
                        jsonObject.put("featureId", filename);
                        talker.sendMessage(jsonObject.toString());
                        Log.i("TargetSeekingFragment",jsonObject.toString());
                        set_status(status.waiting);
                    } else {
                        Toast.makeText(getControlApp(), "发送失败：要发送的图片为空", Toast.LENGTH_LONG);
                    }
                }
                else if(mode == status.waiting){
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("mode","1");
                    jsonObject.put("featureId",filename);
                    jsonObject.put("type",spinner_Target_Class.getSelectedItem().toString());
                    patrolModeTalker.sendMessage(jsonObject.toString());
                    set_status(status.working);
                }
            }
        });
        btn_start_mission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                patrolModeTalker.sendMessage("1");
                set_status(status.working);
            }
        });
        set_status(status.idle);

        return view;
    }

    @Override
    public void shutdown() {
        nodeMainExecutor.shutdownNodeMain(talker);
        nodeMainExecutor.shutdownNodeMain(target_Image_Listener);
        nodeMainExecutor.shutdownNodeMain(mode_change_Listener);
    }

    @Override
    public void setData(String msg, Object object) {
            msg.setData((java.lang.String) object);
    }

    private void SelectImage() {
         Intent intent = new Intent(getControlApp(), SelectPicPopupWindow.class);
         startActivityForResult(intent, SELECT_PIC);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("TargetSeekingFragment","onActivityResult");
        Log.i("TargetSeekingFragment",java.lang.String.valueOf(resultCode));
        if (data == null) {
            Log.i("TargetSeekingFragment","datanull");
            return;
        }
        switch (resultCode) {
            case 3://相册选取图片
                java.lang.String image_Path = data.getStringExtra("image_url");
                Log.i("TargetSeekingFragment",image_Path);
                if(image_Path!=null) {

                    selectFromAlbum = true;
                    filename = getFileName(image_Path);
                    bmap = BitmapFactory.decodeFile(image_Path);
                    imageView.setImageBitmap(bmap);
                    set_status(status.waiting);//默认使用相册时选择的是已经注册过的图片
                }
                break;
            case 4://拍照接收图片
                java.lang.String image_Path_ = data.getStringExtra("image_url");
                Log.i("TargetSeekingFragment",image_Path_);
                if(image_Path_!=null) {
                    selectFromAlbum = false;
                    bmap = BitmapFactory.decodeFile(image_Path_);
                    imageView.setImageBitmap(bmap);
                    File tempfile = new File(image_Path_);
                    filename = spinner_Target_Class.getSelectedItem().toString() + "_" + System.currentTimeMillis();
                    File file = BitmapUtils.saveBitmap(filename + ".jpg",
                            "faceimage/", bmap, getControlApp());

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//根据SDK号指示手机更新对应目录（手机比较傻，即便有照片加入也不会自动更新相册，需要提醒它一下）
                        Intent mediaScanIntent = new Intent(
                                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Uri contentUri = Uri.fromFile(file); //out is your output file
                        mediaScanIntent.setData(contentUri);
                        getControlApp().sendBroadcast(mediaScanIntent);
                    } else {
                        getControlApp().sendBroadcast(new Intent(
                                Intent.ACTION_MEDIA_MOUNTED,
                                Uri.parse("file://"
                                        + Environment.getExternalStorageDirectory())));
                    }

                    tempfile.delete();
                    set_status(status.picture_selected);
                }
                break;
            case 5://根据用户决定进入特定的选取图像方式
                java.lang.String select_mode = data.getStringExtra("result");
                switch (select_mode){
                    case "-1":
                        break;
                    case "0":
                        Intent intent1 = new Intent(getControlApp(),TestActivity2.class);
                        intent1.putExtra("mode","0");
                        startActivityForResult(intent1,SELECT_PIC);
                        break;
                    case "1":
                        Intent intent2 = new Intent(getControlApp(),TestActivity2.class);
                        intent2.putExtra("mode","1");
                        startActivityForResult(intent2,SELECT_PIC);
                        break;
                    default:
                        break;
                }
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull java.lang.String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS:
                if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    Toast.makeText(this.getContext(),"oops",Toast.LENGTH_LONG);
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
        }
    }
    private void set_status(status sta){
        switch(sta){
            case idle://空闲的状态
                mode = status.idle;
                spinner_Target_Class.setEnabled(true);
                btn_Select_Image.setEnabled(true);
                btn_Send_Image.setText("尚未选择图片");
                message_TextView.setText("");
                set_Anim(false);
                break;
            case picture_selected://已选择图片的状态
                mode = status.picture_selected;
                spinner_Target_Class.setEnabled(true);
                btn_Select_Image.setEnabled(true);
                btn_Send_Image.setEnabled(true);
                btn_Send_Image.setText("确定目标");
                message_TextView.setText("");
                set_Anim(false);
                break;
            case waiting://已发送图片，机器人等待任务开始命令
                mode = status.waiting;
                spinner_Target_Class.setEnabled(false);
                btn_Select_Image.setEnabled(true);
                btn_Send_Image.setEnabled(true);
                btn_Send_Image.setText("开始任务");
                set_Anim(false);
//                message_TextView.setText("准备任务中...");
//                set_Anim(true);
                break;
            case working://确定拍照的图片为可寻找对象，机器人标的寻物进行中，手机端等待寻物结果的状态
                mode = status.working;
                spinner_Target_Class.setEnabled(false);
                btn_Select_Image.setEnabled(false);
                btn_Send_Image.setEnabled(false);
                btn_start_mission.setEnabled(false);
                message_TextView.setText("任务进行中...");
                set_Anim(true);
                break;
            default:
                break;
        }
    }
    private void set_Anim(boolean i){//设置图片旋转动画，不重要
        if(i){
            anim_View.setImageResource(R.drawable.add_face);
            Animation animation = AnimationUtils.loadAnimation(getControlApp(), R.anim.rotate);
            LinearInterpolator interpolator = new LinearInterpolator();
            animation.setInterpolator(interpolator);
            anim_View.startAnimation(animation);//開始动画
        }
        else{
            anim_View.setImageDrawable(null);
            anim_View.clearAnimation();
        }
    }
    private void set_Image(Bitmap bmap){
        imageView.setImageBitmap(bmap);
    }
    public java.lang.String getFileName(java.lang.String pathandname) {
        /**
         * 仅保留文件名不保留后缀
         */
        int start = pathandname.lastIndexOf("/");
        int end = pathandname.lastIndexOf(".");
        if (start != -1 && end != -1) {
            return pathandname.substring(start + 1, end);
        } else {
            return null;
        }
    }
}