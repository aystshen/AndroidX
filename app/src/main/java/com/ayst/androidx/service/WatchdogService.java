package com.ayst.androidx.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.ayst.androidx.IWatchdogService;
import com.ayst.androidx.supply.Mcu;

public class WatchdogService extends Service {

    private static final String TAG = "WatchdogService";

    private static final int TIMEOUT_MIN = 60;

    private boolean mAlive = true;
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
                    mHeartbeatThread = new Thread(mHeartbeatRunnable);
                    mHeartbeatThread.start();
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

        if (mMcu.watchdogIsOpen()) {
            mHeartbeatThread = new Thread(mHeartbeatRunnable);
            mHeartbeatThread.start();
        } else {
            Log.w(TAG, "onStartCommand, watchdog is off.");
        }

        return super.onStartCommand(intent, flags, startId);
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
