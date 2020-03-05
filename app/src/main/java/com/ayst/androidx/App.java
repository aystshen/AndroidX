package com.ayst.androidx;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import com.ayst.androidx.supply.Mcu;
import com.ayst.androidx.util.AppUtils;
import com.ayst.androidx.util.SPUtils;

public class App extends Application {
    private static final String TAG = "App";

    private Mcu mMcu;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "AndroidX, version: " + AppUtils.getVersionName(this));

        mMcu = new Mcu(this);

        if (AppUtils.isFirstRun(this)) {
            loadDefaultConfig();
        }
    }

    private void loadDefaultConfig() {
        Log.i(TAG, "loadDefaultConfig");

        boolean watchdog = TextUtils.equals("1", AppUtils.getProperty("ro.androidx.watchdog", "0"));
        boolean keepLive4g = TextUtils.equals("1", AppUtils.getProperty("ro.androidx.4g_keep_live", "0"));
        boolean log2file = TextUtils.equals("1", AppUtils.getProperty("ro.androidx.log2file", "1"));
        Log.i(TAG, "loadDefaultConfig, watchdog=" + watchdog);
        Log.i(TAG, "loadDefaultConfig, keepLive4g=" + keepLive4g);
        Log.i(TAG, "loadDefaultConfig, log2file=" + log2file);

        if (watchdog) {
            mMcu.openWatchdog();
        } else {
            mMcu.closeWatchdog();
        }

        SPUtils.get(this).saveData(SPUtils.KEY_4G_KEEP_LIVE, keepLive4g);
        SPUtils.get(this).saveData(SPUtils.KEY_LOG2FILE, log2file);
    }
}