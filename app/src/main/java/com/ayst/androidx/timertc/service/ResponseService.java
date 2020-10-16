package com.ayst.androidx.timertc.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ayst.androidx.App;
import com.ayst.androidx.timertc.model.DataSource;
import com.ayst.androidx.timertc.model.SupplyParam;
import com.ayst.androidx.timertc.utils.ParseUtils;


/**
 * 响应服务
 */
public class ResponseService extends IntentService {

    private static final String TAG = ResponseService.class.getSimpleName();

    public ResponseService() {
        this("TaskService");
    }

    public ResponseService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        calculateNextPowerOn();
    }

    private void calculateNextPowerOn() {
        String params = DataSource.get().getTask();
        SupplyParam supplyParam = ParseUtils.parse(params);
        if (supplyParam != null) {
            long interval = supplyParam.getBootTime() - supplyParam.getShutTime();
            Log.e(TAG, "interval: " + interval);
            if (interval < 1) {
                Log.e(TAG, "calculateNextPowerOn: 时间间隔过小");
            } else {
                int state = App.get().setUptime((int) interval / 1000);
                Log.e(TAG, "setUptime():" + state);
                if (state >= 0) {
                    shutdown(); // 关机
                }
            }
        }
    }

    /**
     * 关机
     */
    public static void shutdown() {
        Intent intent = new Intent("com.android.internal.intent.action.REQUEST_SHUTDOWN");
        intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        App.get().startActivity(intent);
    }
}
