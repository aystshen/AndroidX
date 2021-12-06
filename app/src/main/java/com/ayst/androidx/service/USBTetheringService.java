package com.ayst.androidx.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.ayst.androidx.util.AppUtils;
import com.ayst.androidx.util.ShellUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class USBTetheringService extends Service {
    private String TAG = "USBTetheringService";
    private Context mContext = null;

    String[] cmds = {
            "ifconfig usb0 up",
            "ifconfig usb0 192.168.42.11",
            "ip route add default via 192.168.42.10",
            "ip rule add from all lookup main pref 9999",
    };
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG,"run called");
            enableUSBteher();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ShellUtils.CommandResult result;
            for (int i = 0; i < cmds.length; i++) {
                result = ShellUtils.execCmd(cmds[i], true);
                Log.i(TAG, cmds[i] + "\n: " + result.toString());
            }
        }
    };

    public USBTetheringService() {

    }
    public int enableUSBteher()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        Method method = getMethod(ConnectivityManager.class,"setUsbTethering",boolean.class);
        if (null == method)
        {
            Log.i(TAG,"callMethod failed");
            return -1;
        }
        int returnCode = 0;
        try {
            returnCode = (Integer)method.invoke(connectivityManager, true);
            Log.i(TAG,"callMethod over");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return  returnCode;
    }
    public Method getMethod(Class clazz, String methodName, Class<?>... parameterTypes) {
        Method method = null;
        try {
            method = clazz.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return method;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        Log.i(TAG,"onCreate");
    }




    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"onStartCommand begin");
        if (TextUtils.equals("1", AppUtils.getProperty(
                "persist.androidx.usbtether", "0"))) {
            Log.i(TAG,"onStartCommand in on persist.androidx.usbtether");
            Thread thread = new Thread(mRunnable);
            thread.start();
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
