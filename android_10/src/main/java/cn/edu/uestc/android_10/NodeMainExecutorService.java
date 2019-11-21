/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package cn.edu.uestc.android_10;

import com.google.common.base.Preconditions;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import org.ros.RosCore;
import org.ros.concurrent.ListenerGroup;
import org.ros.concurrent.SignalRunnable;
import org.ros.exception.RosRuntimeException;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeListener;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;

import java.net.URI;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */


public class NodeMainExecutorService extends Service implements NodeMainExecutor {
  private static final String TAG = "NodeMainExecutorService";
  private static final int ONGOING_NOTIFICATION = 1;
  static final String ACTION_START = "org.ros.android.ACTION_START_NODE_RUNNER_SERVICE";
  static final String ACTION_SHUTDOWN = "org.ros.android.ACTION_SHUTDOWN_NODE_RUNNER_SERVICE";
  static final String EXTRA_NOTIFICATION_TITLE = "org.ros.android.EXTRA_NOTIFICATION_TITLE";
  static final String EXTRA_NOTIFICATION_TICKER = "org.ros.android.EXTRA_NOTIFICATION_TICKER";
  private final NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
  private final IBinder binder = new NodeMainExecutorService.LocalBinder();
  private final ListenerGroup<NodeMainExecutorServiceListener> listeners;
  private Handler handler;
  private WakeLock wakeLock;
  private WifiLock wifiLock;
  private RosCore rosCore;
  private URI masterUri;
  private String rosHostname = null;

  public NodeMainExecutorService() {
    this.listeners = new ListenerGroup(this.nodeMainExecutor.getScheduledExecutorService());
  }

  @SuppressLint("InvalidWakeLockTag")
  public void onCreate() {
    this.handler = new Handler();
    PowerManager powerManager = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
    this.wakeLock = powerManager.newWakeLock(1, "NodeMainExecutorService");
    this.wakeLock.acquire();
    int wifiLockType = 1;

    try {
      wifiLockType = WifiManager.class.getField("WIFI_MODE_FULL_HIGH_PERF").getInt((Object)null);
    } catch (Exception var4) {
      Log.w("NodeMainExecutorService", "Unable to acquire high performance wifi lock.");
    }

    WifiManager wifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
    this.wifiLock = wifiManager.createWifiLock(wifiLockType, "NodeMainExecutorService");
    this.wifiLock.acquire();
  }

  public void execute(NodeMain nodeMain, NodeConfiguration nodeConfiguration, Collection<NodeListener> nodeListeneners) {
    this.nodeMainExecutor.execute(nodeMain, nodeConfiguration, nodeListeneners);
  }

  public void execute(NodeMain nodeMain, NodeConfiguration nodeConfiguration) {
    this.execute(nodeMain, nodeConfiguration, (Collection)null);
  }

  public ScheduledExecutorService getScheduledExecutorService() {
    return this.nodeMainExecutor.getScheduledExecutorService();
  }

  public void shutdownNodeMain(NodeMain nodeMain) {
    this.nodeMainExecutor.shutdownNodeMain(nodeMain);
  }

  public void shutdown() {
    this.handler.post(new Runnable() {
      public void run() {
        AlertDialog.Builder builder = new AlertDialog.Builder(NodeMainExecutorService.this);
        builder.setMessage("Continue shutting down?");
        builder.setPositiveButton("Shutdown", new OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            NodeMainExecutorService.this.forceShutdown();
          }
        });
        builder.setNegativeButton("Cancel", new OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
          }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setType(2003);
        alertDialog.show();
      }
    });
  }

  public void forceShutdown() {
    this.signalOnShutdown();
    this.stopForeground(true);
    this.stopSelf();
  }

  public void addListener(NodeMainExecutorServiceListener listener) {
    this.listeners.add(listener);
  }

  public void removeListener(NodeMainExecutorServiceListener listener) {
    this.listeners.remove(listener);
  }

  private void signalOnShutdown() {
    this.listeners.signal(new SignalRunnable<NodeMainExecutorServiceListener>() {
      public void run(NodeMainExecutorServiceListener nodeMainExecutorServiceListener) {
        nodeMainExecutorServiceListener.onShutdown(NodeMainExecutorService.this);
      }
    });
  }

  public void onDestroy() {
    this.toast("Shutting down...");
    this.nodeMainExecutor.shutdown();
    if (this.rosCore != null) {
      this.rosCore.shutdown();
    }

    if (this.wakeLock.isHeld()) {
      this.wakeLock.release();
    }

    if (this.wifiLock.isHeld()) {
      this.wifiLock.release();
    }

    super.onDestroy();
  }

  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent.getAction() == null) {
      return Service.START_NOT_STICKY;
    } else {
      if (intent.getAction().equals("org.ros.android.ACTION_START_NODE_RUNNER_SERVICE")) {
        Preconditions.checkArgument(intent.hasExtra("org.ros.android.EXTRA_NOTIFICATION_TICKER"));
        Preconditions.checkArgument(intent.hasExtra("org.ros.android.EXTRA_NOTIFICATION_TITLE"));
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Intent notificationIntent = new Intent(this, NodeMainExecutorService.class);
        notificationIntent.setAction("org.ros.android.ACTION_SHUTDOWN_NODE_RUNNER_SERVICE");
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, notificationIntent, 0);
        // todo  屏蔽掉前台通知
//        Notification notification = builder.setContentIntent(pendingIntent).setSmallIcon(R.mipmap.icon).setTicker(intent.getStringExtra("org.ros.android.EXTRA_NOTIFICATION_TICKER")).setWhen(System.currentTimeMillis()).setContentTitle(intent.getStringExtra("org.ros.android.EXTRA_NOTIFICATION_TITLE")).setAutoCancel(true).setContentText("Tap to shutdown.").build();
//        this.startForeground(1, notification);
      }

      if (intent.getAction().equals("org.ros.android.ACTION_SHUTDOWN_NODE_RUNNER_SERVICE")) {
        this.shutdown();
      }

      return Service.START_NOT_STICKY;
    }
  }

  public IBinder onBind(Intent intent) {
    return this.binder;
  }

  public URI getMasterUri() {
    return this.masterUri;
  }

  public void setMasterUri(URI uri) {
    this.masterUri = uri;
  }

  public void setRosHostname(String hostname) {
    this.rosHostname = hostname;
  }

  public String getRosHostname() {
    return this.rosHostname;
  }

  /** @deprecated */
  @Deprecated
  public void startMaster() {
    this.startMaster(true);
  }

  public void startMaster(boolean isPrivate) {
    AsyncTask<Boolean, Void, URI> task = new AsyncTask<Boolean, Void, URI>() {
      protected URI doInBackground(Boolean[] params) {
        NodeMainExecutorService.this.startMasterBlocking(params[0]);
        return NodeMainExecutorService.this.getMasterUri();
      }
    };
    task.execute(new Boolean[]{isPrivate});

    try {
      task.get();
    } catch (InterruptedException var4) {
      throw new RosRuntimeException(var4);
    } catch (ExecutionException var5) {
      throw new RosRuntimeException(var5);
    }
  }

  private void startMasterBlocking(boolean isPrivate) {
    if (isPrivate) {
      this.rosCore = RosCore.newPrivate();
    } else if (this.rosHostname != null) {
      this.rosCore = RosCore.newPublic(this.rosHostname, 11311);
    } else {
      this.rosCore = RosCore.newPublic(11311);
    }

    this.rosCore.start();

    try {
      this.rosCore.awaitStart();
    } catch (Exception var3) {
      throw new RosRuntimeException(var3);
    }

    this.masterUri = this.rosCore.getUri();
  }

  public void toast(final String text) {
    this.handler.post(new Runnable() {
      public void run() {
        Toast.makeText(NodeMainExecutorService.this, text, Toast.LENGTH_SHORT).show();
      }
    });
  }

  class LocalBinder extends Binder {
    LocalBinder() {
    }

    NodeMainExecutorService getService() {
      return NodeMainExecutorService.this;
    }
  }
}

