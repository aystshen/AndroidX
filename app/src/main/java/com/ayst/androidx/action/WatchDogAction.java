package com.ayst.androidx.action;

import android.content.Context;
import android.util.Log;

import com.ayst.androidx.helper.Mcu;
import com.ayst.androidx.util.AppUtil;

public class WatchDogAction extends BaseAction {
    private static final String TAG = "WatchDogAction";

    private static final int TIMEOUT_MIN = 60;

    private Mcu mMcu;
    private int mTimeout;

    public WatchDogAction(Context context) {
        super(context);
        mMcu = new Mcu(context);
    }

    @Override
    public void run() {
        super.run();
        mTimeout = mMcu.getWatchdogDuration();
        while (mAlive) {
            Log.d(TAG, "run...");

            sendHeartbeat();

            try {
                Thread.sleep(mTimeout >= TIMEOUT_MIN ? mTimeout/3*1000 : 30*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void open() {
        mMcu.openWatchdog();
    }

    @Override
    public void close() {
        mMcu.closeWatchdog();
    }

    @Override
    public boolean isOpen() {
        if (AppUtil.isFirstRun(mContext)) {
            if ("1".equals(AppUtil.getProperty("persist.androidx.watchdog", "0"))) {
                Log.i(TAG, "isOpen, The first run, it opens by default.");
                open();
            } else {
                Log.i(TAG, "isOpen, The first run, it closes by default.");
                close();
            }
        }
        return mMcu.watchdogIsOpen();
    }

    private void sendHeartbeat() {
        mMcu.heartbeat();
    }

    public void setTimeout(int timeout) {
        if (timeout >= TIMEOUT_MIN) {
            if (mMcu.setWatchdogDuration(timeout) >= 0) {
                mTimeout = timeout;
            }
        } else {
            Log.w(TAG, "setTimeout, The timeout is less than the minimum.");
        }
    }
}
