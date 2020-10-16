package com.ayst.androidx.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.ayst.androidx.ITimeRTCService;
import com.ayst.androidx.timertc.manager.ScheduleManager;


/**
 * 提供与外部应用通信的AIDL服务
 */
public class TimeRTCService extends Service {

    private static final String TAG = TimeRTCService.class.getSimpleName();

    public TimeRTCService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "客户端连接");
        return new SupplyBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "TimeRTCService-->服务启动");
        //启动定时开关机服务
        int toRtc = ScheduleManager.get().updateTimeToRtc();
        Log.e(TAG, "toRtc:" + toRtc);
        return Service.START_REDELIVER_INTENT;//重启
    }

    private static class SupplyBinder extends ITimeRTCService.Stub {

        @Override
        public int updateTimeToRtc(String param) throws RemoteException {
            Log.e(TAG, "updateTimeToRtc：" + param);
            return ScheduleManager.get().updateTimeToRtc(param);
        }

        @Override
        public String getTimeRtcStatus() throws RemoteException {
            return ScheduleManager.get().getTimeRtcStatus();
        }
    }
}
