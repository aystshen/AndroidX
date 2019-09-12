package com.ayst.androidx.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.IBinder;
import android.os.IModemService;
import android.os.RemoteException;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created by Administrator on 2018/11/6.
 */

public class Modem {
    private IModemService mModemService;

    @SuppressLint("WrongConstant")
    public Modem(Context context) {
        Method method = null;
        try {
            method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
            IBinder binder = (IBinder) method.invoke(null, new Object[]{"modem"});
            mModemService = IModemService.Stub.asInterface(binder);
            if (mModemService == null) {
                Log.i("Modem", "mModemService is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int powerOn() {
        if (null != mModemService) {
            try {
                return mModemService.powerOn();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public int powerOff() {
        if (null != mModemService) {
            try {
                return mModemService.powerOff();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public int reset() {
        if (null != mModemService) {
            try {
                Log.i("Modem", "reset");
                return mModemService.reset();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public int wakeup() {
        if (null != mModemService) {
            try {
                return mModemService.wakeup();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public int sleep() {
        if (null != mModemService) {
            try {
                return mModemService.sleep();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public boolean isWakeup() {
        if (null != mModemService) {
            try {
                return mModemService.isWakeup();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean isPowerOn() {
        if (null != mModemService) {
            try {
                return mModemService.isPowerOn();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
