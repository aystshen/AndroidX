package com.ayst.androidx.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.IBinder;
import android.os.IMcuService;
import android.os.RemoteException;

import java.lang.reflect.Method;

/**
 * Created by Administrator on 2018/11/6.
 */

public class Mcu {
    private IMcuService mMcuService;

    @SuppressLint("WrongConstant")
    public Mcu(Context context) {
        Method method = null;
        try {
            method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
            IBinder binder = (IBinder) method.invoke(null, new Object[]{"mcu"});
            mMcuService = IMcuService.Stub.asInterface(binder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Heartbeat
     */
    public void heartbeat() {
        if (null != mMcuService) {
            try {
                mMcuService.heartbeat();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Set the boot countdown
     * @param time (unit: second)
     * @return <0：error
     */
    public int setUptime(int time) {
        if (null != mMcuService) {
            try {
                return mMcuService.setUptime(time);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * Enable watchdog
     * @return <0：error
     */
    public int openWatchdog() {
        if (null != mMcuService) {
            try {
                return mMcuService.openWatchdog();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * Disable watchdog
     * @return <0：error
     */
    public int closeWatchdog() {
        if (null != mMcuService) {
            try {
                return mMcuService.closeWatchdog();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * Set watchdog over time duration
     * @param duration (unit: second)
     * @return <0：error
     */
    public int setWatchdogDuration(int duration) {
        if (null != mMcuService) {
            try {
                return mMcuService.setWatchdogDuration(duration);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }
}
