package com.ayst.androidx.start_stop_schedule_core.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.ayst.androidx.ITimeRTCService;
import com.ayst.androidx.start_stop_schedule_core.manager.ScheduleManager;


/**
 * 提供与外部应用通信的AIDL服务
 */
public class SupplyService extends Service {

    private static final String TAG = "SupplyService";

    public SupplyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "客户端来连接");
        return new SupplyBinder();
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
