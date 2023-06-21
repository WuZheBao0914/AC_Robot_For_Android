package cn.edu.uestc.cssl.util;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import cn.edu.uestc.cssl.activities.R;
import cn.edu.uestc.cssl.activities.RobotController;
import cn.edu.uestc.cssl.activities.TestActivity2;

public class SelectPicPopupWindow extends Activity implements OnClickListener{

    private Button btn_take_photo, btn_pick_photo, btn_cancel;
    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_select_image);
        btn_take_photo = (Button) this.findViewById(R.id.btn_take_photo);
        btn_pick_photo = (Button) this.findViewById(R.id.btn_pick_photo);
        btn_cancel = (Button) this.findViewById(R.id.btn_cancel);
        layout=(LinearLayout)findViewById(R.id.pop_layout);

        //添加选择窗口范围监听可以优先获取触点，即不再执行onTouchEvent()函数，点击其他地方时执行onTouchEvent()函数销毁Activity
        layout.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                Toast.makeText(getApplicationContext(), "提示：点击窗口外部关闭窗口！",
                        Toast.LENGTH_SHORT).show();
            }
        });
        //添加按钮监听
        btn_cancel.setOnClickListener(this);
        btn_pick_photo.setOnClickListener(this);
        btn_take_photo.setOnClickListener(this);
    }

    //实现onTouchEvent触屏函数但点击屏幕时销毁本Activity
    @Override
    public boolean onTouchEvent(MotionEvent event){
        finish();
        return true;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_take_photo:
                Intent take_photo = new Intent(SelectPicPopupWindow.this, RobotController.class);
                take_photo.putExtra("result","0");
                setResult(5, take_photo);
                finish();
                break;
            case R.id.btn_pick_photo:
                Intent pick_photo = new Intent(SelectPicPopupWindow.this, RobotController.class);
                pick_photo.putExtra("result","1");
                setResult(5, pick_photo);
                break;
            case R.id.btn_cancel:
                Intent btn_cancel = new Intent(SelectPicPopupWindow.this, RobotController.class);
                btn_cancel.putExtra("result","-1");
                setResult(5, btn_cancel);
                break;
            default:
                break;
        }
        finish();
    }

}
