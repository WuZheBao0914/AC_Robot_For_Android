package cn.edu.uestc.cssl.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.ContentFrameLayout;

import java.net.URI;

import cn.edu.uestc.android_10.AppCompatRosActivity;
import cn.edu.uestc.cssl.delegates.AcDelegate;

/**
 * @author xuyang
 * @create 2019/1/21 16:51
 **/
public abstract class ProxyActivity extends AppCompatRosActivity {

    public ProxyActivity(String notificationTicker, String notificationTitle, URI customMasterUri) {
        super(notificationTicker, notificationTitle, customMasterUri);
    }

    public abstract AcDelegate setRootDelegate();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initContainer(savedInstanceState);
    }

    private void initContainer(@Nullable Bundle savedInstanceState) {
        final ContentFrameLayout container = new ContentFrameLayout(this);
        container.setId(R.id.delegate_container);

        setContentView(container);
        if (savedInstanceState == null) {
            loadRootFragment(R.id.delegate_container, setRootDelegate());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
        System.runFinalization();
    }
}
