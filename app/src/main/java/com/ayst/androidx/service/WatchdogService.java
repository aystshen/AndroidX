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

    /**
     * 保存关机或升级前看门狗状态，以便重启后恢复。
     */
    private static final File RECOVERY_DIR = new File("/cache/recovery");
    private static final File WATCHDOG_FLAG_FILE = new File(RECOVERY_DIR, "/last_watchdog_flag");

    private static final int TIMEOUT_MIN = 60;

    private static final int STATE_OFF = 0;
    private static final int STATE_ON = 1;
    private static final int STATE_AUTO = 2;
    private int mState = STATE_AUTO;

    private boolean mAlive = true;
    private int mTimeout;
    private Mcu mMcu;
    private Thread mHeartbeatThread;

    /**
     * 看门狗心跳线程
     */
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

    private final IWatchdogService.Stub mService = new IWatchdogService.Stub() {
        /**
         * 打开看门狗
         *
         * @return true:成功 false:失败
         * @throws RemoteException
         */
        @Override
        public boolean openWatchdog() throws RemoteException {
            if (WatchdogService.this.openWatchdog()) {
                AppUtils.setProperty("persist.androidx.watchdog",
                        String.valueOf(STATE_ON));
                return true;
            }
            return false;
        }

        /**
         * 关闭看门狗
         *
         * @return true:成功 false:失败
         * @throws RemoteException
         */
        @Override
        public boolean closeWatchdog() throws RemoteException {
            if (WatchdogService.this.closeWatchdog()) {
                AppUtils.setProperty("persist.androidx.watchdog",
                        String.valueOf(STATE_OFF));
                return true;
            }
            return false;
        }

        /**
         * 设置看门狗超时时间
         *
         * @param timeout 超时时间
         * @return true:成功 false:失败
         * @throws RemoteException
         */
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
                Log.e(TAG, "setWatchdogTimeout, the timeout must be greater than "
                        + TIMEOUT_MIN + "s.");
                return false;
            }
            return true;
        }

        /**
         * 获取看门狗超时时间
         *
         * @return 超时时间
         * @throws RemoteException
         */
        @Override
        public int getWatchdogTimeout() throws RemoteException {
            return mMcu.getWatchdogDuration();
        }

        /**
         * 判断看门狗是否打开
         *
         * @return true:打开 false:关闭
         * @throws RemoteException
         */
        @Override
        public boolean watchdogIsOpen() throws RemoteException {
            return mMcu.watchdogIsOpen();
        }
    };

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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand...");

        EventBus.getDefault().register(this);

        mState = Integer.parseInt(AppUtils.getProperty("persist.androidx.watchdog",
                String.valueOf(STATE_AUTO)));
        if (mState == STATE_ON) {
            Log.d(TAG, "onStartCommand, watchdog is on.");
            openWatchdog();
        } else if (mState == STATE_OFF) {
            Log.d(TAG, "onStartCommand, watchdog is off.");
            closeWatchdog();
        } else if (mState == STATE_AUTO) {
            Log.d(TAG, "onStartCommand, watchdog is auto.");
            if (mMcu.watchdogIsOpen()) {
                if (mHeartbeatThread == null) {
                    mHeartbeatThread = new Thread(mHeartbeatRunnable);
                    mHeartbeatThread.start();
                }
            } else if (checkWatchdogFlag()) {
                openWatchdog();
            }
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
                openWatchdog();
                break;
            case MessageEvent.MSG_CLOSE_WATCHDOG:
                closeWatchdog();
                break;
        }
    }

    /**
     * 打开看门狗
     *
     * @return true:成功 false:失败
     */
    private boolean openWatchdog() {
        Log.i(TAG, "openWatchdog");
        if (mMcu.openWatchdog() == 0) {
            if (mHeartbeatThread == null) {
                mHeartbeatThread = new Thread(mHeartbeatRunnable);
                mHeartbeatThread.start();
            }
            return true;
        } else {
            Log.e(TAG, "openWatchdog, failed.");
            return false;
        }
    }

    /**
     * 关闭看门狗
     *
     * @return true:成功 false:失败
     */
    private boolean closeWatchdog() {
        Log.i(TAG, "closeWatchdog");
        if (mMcu.closeWatchdog() == 0) {
            mAlive = false;
            if (mHeartbeatThread != null) {
                mHeartbeatThread.interrupt();
                mHeartbeatThread = null;
            }
            return true;
        } else {
            Log.e(TAG, "closeWatchdog, failed.");
            return false;
        }
    }

    /**
     * 读取上一次关机或升级前的看门狗状态
     *
     * @return true:开 false:关
     */
    private boolean checkWatchdogFlag() {
        // 检查 last_flag
        String flag = null;
        try {
            flag = FileUtils.readFile(WATCHDOG_FLAG_FILE);
            WATCHDOG_FLAG_FILE.delete();
        } catch (IOException e) {
            Log.w(TAG, "checkWatchdogFlag, " + e.getMessage());
        }
        Log.i(TAG, "checkWatchdogFlag, watchdog flag:" + flag);

        return TextUtils.equals(flag, "watchdog=true");
    }
}