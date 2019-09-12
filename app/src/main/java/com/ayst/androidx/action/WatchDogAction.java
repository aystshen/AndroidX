package com.ayst.androidx.action;

import android.content.Context;
import android.util.Log;

import com.ayst.androidx.util.AppUtil;
import com.ayst.androidx.util.SPUtils;

public class WatchDogAction extends BaseAction {
    private static final String TAG = "WatchDogAction";
    private static final String KEY_WATCHDOG = "watchdog";

    public WatchDogAction(Context context) {
        super(context);
    }

    @Override
    public void run() {
        super.run();
        while (mAlive) {
            Log.d(TAG, "run...");

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void open() {
        SPUtils.get(mContext).saveData(KEY_WATCHDOG, true);
    }

    @Override
    public void close() {
        SPUtils.get(mContext).saveData(KEY_WATCHDOG, false);
    }

    @Override
    public boolean isOpen() {
        return SPUtils.get(mContext).getData(KEY_WATCHDOG, "1".equals(AppUtil.
                getProperty("ro.androidx.watchdog", "0")));
    }
}
