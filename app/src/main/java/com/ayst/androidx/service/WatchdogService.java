package com.ayst.androidx.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.ayst.androidx.IWatchdogService;
import com.ayst.androidx.event.MessageEvent;
import com.ayst.androidx.supply.Mcu;
import com.ayst.androidx.util.AppUtils;
import com.ayst.androidx.util.FileUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;

public class WatchdogService extends Service {

    private static final String TAG = "WatchdogService";

    private static final int TIMEOUT_MIN = 60;

    /**
     * Recovery upgrade status storage file
     */
    private static final String RECOVERY_DIR = "/cache/recovery";
    private static final File WATCHDOG_FLAG_FILE = new File(RECOVERY_DIR + "/last_watchdog_flag");

    private boolean mAlive = true;
    private boolean mHeartbeat = true;
    private int mTimeout;
    private Mcu mMcu;
    private Thread mHeartbeatThread;

    public WatchdogService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMcu = new Mcu(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mService;
    }

    private final IWatchdogService.Stub mService = new IWatchdogService.Stub() {
        @Override
        public boolean openWatchdog() throws RemoteException {
            Log.i(TAG, "openWatchdog");
            if (!watchdogIsOpen()) {
                if (mMcu.openWatchdog() == 0) {
                    if (mHeartbeat) {
                        mHeartbeatThread = new Thread(mHeartbeatRunnable);
                        mHeartbeatThread.start();
                    }
                } else {
                    Log.e(TAG, "openWatchdog, failed.");
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean closeWatchdog() throws RemoteException {
            Log.i(TAG, "closeWatchdog");
            if (watchdogIsOpen()) {
                if (mMcu.closeWatchdog() == 0) {
                    mAlive = false;
                    if (null != mHeartbeatThread) {
                        mHeartbeatThread.interrupt();
                    }
                } else {
                    Log.e(TAG, "closeWatchdog, failed.");
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean setWatchdogTimeout(int timeout) throws RemoteException {
            Log.i(TAG, "setWatchdogTimeout, timeout=" + timeout);
            if (timeout >= TIMEOUT_MIN) {
                if (mMcu.setWatchdogDuration(timeout) == 0) {
                    mTimeout = timeout;
                } else {
                    Log.e(TAG, "setWatchdogTimeout, failed.");
                    return false;
                }
            } else {
                Log.e(TAG, "setWatchdogTimeout, the timeout must be greater than " + TIMEOUT_MIN + "s.");
                return false;
            }
            return true;
        }

        @Override
        public int getWatchdogTimeout() throws RemoteException {
            return mMcu.getWatchdogDuration();
        }

        @Override
        public boolean watchdogIsOpen() throws RemoteException {
            return mMcu.watchdogIsOpen();
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand...");

        mHeartbeat = !TextUtils.equals("2", AppUtils.getProperty(
                "ro.androidx.watchdog", "0"));

        EventBus.getDefault().register(this);

        if (mMcu.watchdogIsOpen()) {
            if (mHeartbeat) {
                mHeartbeatThread = new Thread(mHeartbeatRunnable);
                mHeartbeatThread.start();
            }
        } else {
            Log.w(TAG, "onStartCommand, watchdog is off.");
            checkLastFlag();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.getMessage()) {
            case MessageEvent.MSG_OPEN_WATCHDOG:
                try {
                    mService.openWatchdog();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case MessageEvent.MSG_CLOSE_WATCHDOG:
                try {
                    mService.closeWatchdog();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
        }
    };

    private void checkLastFlag() {
        String flag = null;
        try {
            flag = FileUtils.readFile(WATCHDOG_FLAG_FILE);
        } catch (IOException e) {
            Log.w(TAG, "checkLastFlag, " + e.getMessage());
        }
        Log.i(TAG, "checkLastFlag, flag = " + flag);

        if (!TextUtils.isEmpty(flag)) {
            String[] array = flag.split("\\$");
            for (String param : array) {
                if (param.startsWith("watchdog")) {
                    String value = param.substring(param.indexOf('=') + 1);
                    Log.i(TAG, "checkLastFlag, watchdog=" + value);

                    if (TextUtils.equals("true", value)) {
                        try {
                            mService.openWatchdog();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
                WATCHDOG_FLAG_FILE.delete();
            }
        }
    }

    private Runnable mHeartbeatRunnable = new Runnable() {
        @Override
        public void run() {
            mAlive = true;
            mTimeout = mMcu.getWatchdogDuration();
            while (mAlive) {
                Log.i(TAG, "run...");

                mMcu.heartbeat();

                try {
                    Thread.sleep(mTimeout >= TIMEOUT_MIN ? mTimeout / 3 * 1000 : 30 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
}
