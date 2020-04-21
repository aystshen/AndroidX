package com.ayst.androidx.service;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.ayst.androidx.util.AppUtils;

import java.util.ArrayList;

public class AppEnableService extends Service {
    private static final String TAG = "AppEnableService";

    private String[] mEnableApps;
    private Thread mEnableThread;

    public AppEnableService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand...");

        mEnableApps = loadApps();

        if (null != mEnableApps && mEnableApps.length > 0) {
            mEnableThread = new Thread(mEnableRunnable);
            mEnableThread.start();
        } else {
            Log.w(TAG, "onStartCommand, There is no application to be enabled.");
        }

        return Service.START_REDELIVER_INTENT;
    }

    private String[] loadApps() {
        String value = AppUtils.getProperty("ro.enableapps", "");
        if (!TextUtils.isEmpty(value)) {
            return value.split(",");
        }

        return null;
    }

    private Runnable mEnableRunnable = new Runnable() {
        @Override
        public void run() {
            PackageManager pm = getPackageManager();
            for (String pkgName : mEnableApps) {
                if (!TextUtils.isEmpty(pkgName)
                && pkgName.contains(".")) {
                    try {
                        pm.setApplicationEnabledSetting(
                                pkgName,
                                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                                0);
                    } catch (Exception e) {
                        Log.e(TAG, "setApplicationEnabledSetting, " + e.getMessage());
                    }

                    Log.i(TAG, pkgName + " Enabled.");
                }
            }
        }
    };
}
