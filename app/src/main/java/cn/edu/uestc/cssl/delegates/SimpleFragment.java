package cn.edu.uestc.cssl.delegates;


import android.widget.Toast;

import cn.edu.uestc.cssl.activities.MainActivity;
import cn.edu.uestc.cssl.activities.R;
import cn.edu.uestc.cssl.activities.RobotController;
import cn.edu.uestc.cssl.delegates.AcDelegate;
import me.yokeyword.fragmentation.SupportActivity;

/**
 * @author xuyang
 * @create 2019/1/15 10:11
 **/
public abstract class SimpleFragment extends AcDelegate {

    // 再点一次退出程序时间设置
    private static final long WAIT_TIME = 2000L;
    private long TOUCH_TIME = 0;

    /**
     * Shows the Fragment, making it visible.
     */
    public void show(){
        getFragmentManager()
                .beginTransaction()
                .show(this)
                .commit();
    }

    /**
     * Hides the Fragment, making it invisible.
     */
    public void hide(){
        getFragmentManager()
                .beginTransaction()
                .hide(this)
                .commit();
    }

    /**
     * Convenience method to get the current activity as a ControlApp.
     * @return The current activity casted to a ControlApp if it is one and null otherwise
     */
    public SupportActivity getControlApp() {
        if (getActivity() instanceof MainActivity) {
            return (MainActivity) getActivity();
        } else if (getActivity() instanceof RobotController) {
            return (RobotController) getActivity();
        } else {
            return null;
        }
    }

    /**
     * 处理回退事件
     *
     * @return
     */
    @Override
    public boolean onBackPressedSupport() {
        if (getActivity() instanceof RobotController) {
            return super.onBackPressedSupport();
        }
        if (System.currentTimeMillis() - TOUCH_TIME < WAIT_TIME) {
            _mActivity.finish();
        } else {
            TOUCH_TIME = System.currentTimeMillis();
            Toast.makeText(_mActivity, R.string.press_again_exit, Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}
