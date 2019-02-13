package cn.edu.uestc.android_10;


import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import java.net.URI;

/**
 * @author xuyang
 * @create 2019/1/22 9:57
 **/
public abstract class AppCompatRosActivity extends RosActivity {
    private AppCompatDelegate mDelegate;

    public AppCompatRosActivity() {
        super("", "");
    }

    protected AppCompatRosActivity(String notificationTicker, String notificationTitle) {
        super(notificationTicker, notificationTitle);
    }

    protected AppCompatRosActivity(String notificationTicker, String notificationTitle, URI customMasterUri) {
        super(notificationTicker, notificationTitle, customMasterUri);
    }

    protected AppCompatRosActivity(String notificationTicker, String notificationTitle, Class<?> activity, int requestCode) {
        super(notificationTicker, notificationTitle, activity, requestCode);
    }

    protected void onCreate(Bundle savedInstanceState) {
        this.getDelegate().installViewFactory();
        this.getDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        this.getDelegate().onPostCreate(savedInstanceState);
    }

    public ActionBar getSupportActionBar() {
        return this.getDelegate().getSupportActionBar();
    }

    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        this.getDelegate().setSupportActionBar(toolbar);
    }

    public MenuInflater getMenuInflater() {
        return this.getDelegate().getMenuInflater();
    }

    public void setContentView(@LayoutRes int layoutResID) {
        this.getDelegate().setContentView(layoutResID);
    }

    public void setContentView(View view) {
        this.getDelegate().setContentView(view);
    }

    public void setContentView(View view, LayoutParams params) {
        this.getDelegate().setContentView(view, params);
    }

    public void addContentView(View view, LayoutParams params) {
        this.getDelegate().addContentView(view, params);
    }

    protected void onPostResume() {
        super.onPostResume();
        this.getDelegate().onPostResume();
    }

    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        this.getDelegate().setTitle(title);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.getDelegate().onConfigurationChanged(newConfig);
    }

    protected void onStop() {
        super.onStop();
        this.getDelegate().onStop();
    }

    protected void onDestroy() {
        super.onDestroy();
        this.getDelegate().onDestroy();
    }

    public void invalidateOptionsMenu() {
        this.getDelegate().invalidateOptionsMenu();
    }

    public AppCompatDelegate getDelegate() {
        if (this.mDelegate == null) {
            this.mDelegate = AppCompatDelegate.create(this, (AppCompatCallback) null);
        }

        return this.mDelegate;
    }
}


