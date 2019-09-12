package com.ayst.androidx.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.ayst.androidx.R;
import com.ayst.androidx.action.BaseAction;
import com.ayst.androidx.action.KeepLive4GAction;
import com.ayst.androidx.action.WatchDogAction;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class MainService extends Service {
    public static final String TAG = "MainService";

    public static final String ID = "com.ayst.androidx.MainService";
    public static final String NAME = "Androidx";

    public static final int COMMAND_NULL = 0;
    public static final int COMMAND_RUN_ALL_ACTION = 1001;
    public static final int COMMAND_ACTION_OPEN_WATCHDOG = 1002;
    public static final int COMMAND_ACTION_CLOSE_WATCHDOG = 1003;
    public static final int COMMAND_ACTION_CONFIG_WATCHDOG = 1004;
    public static final int COMMAND_ACTION_OPEN_4G_KEEP_LIVE = 1005;
    public static final int COMMAND_ACTION_CLOSE_4G_KEEP_LIVE = 1006;

    public static final String EXTRA_ACTION = "action";
    public static final String EXTRA_DELAY = "delay";

    public static final int DEFAULT_DELAY_TIME = 3000;

    private WorkHandler mWorkHandler;
    private ThreadPoolExecutor mThreadPoolExecutor;
    private HashMap<String, BaseAction> mActiveActions = new HashMap<>();

    public MainService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        HandlerThread workThread = new HandlerThread("MainService: workThread");
        workThread.start();
        mWorkHandler = new WorkHandler(workThread.getLooper());

        mThreadPoolExecutor = new ThreadPoolExecutor(3, 10, 1, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(100));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand...");
        startForeground();
        if (intent == null) {
            return Service.START_NOT_STICKY;
        }

        int command = intent.getIntExtra(EXTRA_ACTION, COMMAND_NULL);
        int delayTime = intent.getIntExtra(EXTRA_DELAY, DEFAULT_DELAY_TIME);
        Bundle bundle = intent.getExtras();

        Log.d(TAG, "onStartCommand, command=" + command + " delayTime=" + delayTime);
        if (command == COMMAND_NULL) {
            return Service.START_NOT_STICKY;
        }

        Message msg = new Message();
        msg.what = command;
        msg.obj = bundle;
        mWorkHandler.sendMessageDelayed(msg, delayTime);

        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        for (BaseAction action : mActiveActions.values()) {
            action.stop();
        }
        mActiveActions.clear();
        mThreadPoolExecutor.shutdown();
    }

    private class WorkHandler extends Handler {
        WorkHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case COMMAND_RUN_ALL_ACTION:
                    runAllActions();
                    break;
            }
        }
    }

    private void runAction(BaseAction action) {
        if (action.isOpen()) {
            mThreadPoolExecutor.execute(action);
            mActiveActions.put(action.NAME, action);
        }
    }

    private void runAllActions() {
        for (BaseAction action : mActiveActions.values()) {
            action.stop();
        }
        mActiveActions.clear();

        runAction(new KeepLive4GAction(this));
        runAction(new WatchDogAction(this));
    }

    /**
     * Android O(8.1) 开始，启动Service不再允许使用context.startService(intent)，
     * 而改用context.startForegroundService(intent)，并且启动Service后必须调用
     * startForeground()接口，否则Service将被系统强制结束掉。
     */
    private void startForeground() {
        Notification.Builder builder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(ID, NAME, NotificationManager.IMPORTANCE_HIGH);
            chan.enableLights(true);
            chan.setLightColor(Color.RED);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(chan);
            }
            builder = new Notification.Builder(this, ID);
        } else {
            builder = new Notification.Builder(this.getApplicationContext());
        }
        Notification notification = builder.setContentTitle("AndroidX")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .build();
        startForeground(1, notification);
    }
}
