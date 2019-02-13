package cn.edu.uestc.cssl.fragments;




import android.support.v4.app.Fragment;

import cn.edu.uestc.cssl.activities.MainActivity;
import cn.edu.uestc.cssl.delegates.AcDelegate;

/**
 * @author xuyang
 * @create 2019/1/15 10:11
 **/
public abstract class SimpleFragment extends AcDelegate {
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
    public MainActivity getControlApp() {
        if (getActivity() instanceof MainActivity)
            return (MainActivity) getActivity();
        else
            return null;
    }
}
