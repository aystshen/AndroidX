package com.ayst.androidx;

import android.app.Application;
import android.util.Log;

import com.ayst.androidx.supply.Mcu;
import com.ayst.androidx.util.AppUtils;

public class App extends Application {
    private static final String TAG = "App";

    private Mcu mMcu;
    private static App sInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;

        // 退出开机动画
        AppUtils.setProperty("service.bootanim.tb.exit", "1");

        mMcu = new Mcu(this);

        Log.i(TAG, "AndroidX-->Start, version: " + AppUtils.getVersionName(this));
    }

    /**
     * 获取全局上下文
     *
     * @return
     */
    public static App get() {
        return sInstance;
    }


    /**
     * 定时开机
     *
     * @param time
     * @return
     */
    public int setUptime(int time) {
        if (mMcu != null) {
            return mMcu.setUptime(time);
        }
        return -1;
    }
}