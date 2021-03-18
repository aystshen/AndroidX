package com.ayst.androidx.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.ayst.androidx.util.AppUtils;

public class AppLaunchService extends Service {
    private static final String TAG = "AppLaunchService";

    private String[] mLaunchApps;

    public AppLaunchService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand...");

        mLaunchApps = loadApps();

        if (null != mLaunchApps && mLaunchApps.length > 0) {
            registerInstallBroadcastReceiver();
        } else {
            Log.w(TAG, "onStartCommand, There is no application to be launched.");
        }

        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        if (null != mLaunchApps && mLaunchApps.length > 0) {
            unregisterInstallBroadcastReceiver();
        }
        super.onDestroy();
    }

    private String[] loadApps() {
        String value = AppUtils.getProperty("ro.launchapps", "");
        if (!TextUtils.isEmpty(value)) {
            return value.split(",");
        }

        return null;
    }

    private void registerInstallBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addDataScheme("package");
        this.registerReceiver(mInstallBroadcastReceiver, intentFilter);
    }

    private void unregisterInstallBroadcastReceiver() {
        this.unregisterReceiver(mInstallBroadcastReceiver);
    }

    private BroadcastReceiver mInstallBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null
                    && (TextUtils.equals(Intent.ACTION_PACKAGE_ADDED, intent.getAction())
                    || TextUtils.equals(Intent.ACTION_PACKAGE_REPLACED, intent.getAction()))) {
                if (intent.getData() != null) {
                    String packageName = intent.getData().getSchemeSpecificPart();
                    Log.i(TAG, "mInstallBroadcastReceiver, installed-------->" + packageName);

                    if (mLaunchApps != null && mLaunchApps.length > 0) {
                        for (String app : mLaunchApps) {
                            if (TextUtils.equals(packageName, app)) {
                                AppUtils.startApp(context, packageName);
                                Log.i(TAG, "mInstallBroadcastReceiver, launched-------->" + packageName);
                            }
                        }
                    }
                }
            }
        }
    };
}
