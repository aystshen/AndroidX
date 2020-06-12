package com.ayst.androidx.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.ayst.androidx.IKeyInterceptService;
import com.ayst.androidx.util.AppUtils;
import com.ayst.androidx.util.SPUtils;

public class KeyInterceptService extends Service {
    private final static String TAG = "KeyInterceptService";

    public KeyInterceptService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mService;
    }

    private final IKeyInterceptService.Stub mService = new IKeyInterceptService.Stub() {

        @Override
        public void openKeyIntercept() throws RemoteException {
            Log.i(TAG, "openKeyIntercept");

            AppUtils.openAccessibilityService(getApplicationContext());
            SPUtils.get(KeyInterceptService.this)
                    .saveData(SPUtils.KEY_KEY_INTERCEPT, true);
        }

        @Override
        public void closeKeyIntercept() throws RemoteException {
            Log.i(TAG, "closeKeyIntercept");

            AppUtils.closeAccessibilityService(getApplicationContext());
            SPUtils.get(KeyInterceptService.this)
                    .saveData(SPUtils.KEY_KEY_INTERCEPT, false);
        }

        @Override
        public boolean isOpen() throws RemoteException {
            return SPUtils.get(KeyInterceptService.this)
                    .getData(SPUtils.KEY_KEY_INTERCEPT, false);
        }
    };
}
