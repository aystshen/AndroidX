package com.ayst.androidx.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.ayst.androidx.IKeyInterceptService;
import com.ayst.androidx.util.AppUtils;

public class KeyInterceptService extends Service {

    public KeyInterceptService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mService;
    }

    private final IKeyInterceptService.Stub mService = new IKeyInterceptService.Stub() {

        @Override
        public void openKeyIntercept() throws RemoteException {
            AppUtils.openAccessibilityService(getApplicationContext());
            AppUtils.setProperty("persist.sys.intercept_key", "1");
        }

        @Override
        public void closeKeyIntercept() throws RemoteException {
            AppUtils.closeAccessibilityService(getApplicationContext());
            AppUtils.setProperty("persist.sys.intercept_key", "0");
        }

        @Override
        public boolean isOpen() throws RemoteException {
            return TextUtils.equals("1", AppUtils.getProperty(
                    "persist.sys.intercept_key", "0"));
        }
    };
}
