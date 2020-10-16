package com.ayst.androidx.timertc.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.ayst.androidx.App;
import com.ayst.androidx.timertc.ScheduleConfig;
import com.ayst.androidx.timertc.model.DataSource;
import com.ayst.androidx.timertc.model.ITransformation;
import com.ayst.androidx.timertc.model.SupplyParam;
import com.ayst.androidx.timertc.model.SupplyResult;
import com.ayst.androidx.timertc.model.SupplyTransformation;
import com.ayst.androidx.timertc.service.ResponseService;
import com.google.gson.Gson;



/**
 * 定时器管理器
 */
public class ScheduleManager {


    private static final String TAG = ScheduleManager.class.getSimpleName();
    private SupplyParam mSupplyParam;
    private ITransformation mTransformation;


    private ScheduleManager() {
        mTransformation = SupplyTransformation.get();
    }

    private static final class HOLDER {
        private static final ScheduleManager INSTANCE = new ScheduleManager();
    }

    public static ScheduleManager get() {
        return ScheduleManager.HOLDER.INSTANCE;
    }


    public int updateTimeToRtc() {
        return updateTimeToRtc(DataSource.get().getTask(), false);
    }

    public int updateTimeToRtc(String params) {
        return updateTimeToRtc(params, true);
    }

    /**
     * 更新开关机时间
     *
     * @param params
     * @param fromClient
     * @return
     */
    private int updateTimeToRtc(String params, boolean fromClient) {

        SupplyParam supplyParam = mTransformation.transform(params);

        if (supplyParam == null) { //设置错误，表示该任务设置失败
            return ScheduleConfig.RESULT_CODE_OTHER_ERROR;
        }

        if (supplyParam.getResultCode() != ScheduleConfig.RESULT_CODE_SUCCESS) {
            return supplyParam.getResultCode();
        }

        if (!fromClient) { // 开机自启动
            if (supplyParam.isNoneLoopType()) { // 直接返回，该任务不在启动
                return 0;
            }
        }

        // 取消任务
        cancelAlarmManager(mSupplyParam);

        if (supplyParam.isCancel()) {
            mSupplyParam = null;
            // 清除数据
            DataSource.get().setTask("");
        } else if (supplyParam.isUpdate()) {
            startAlarmManager(supplyParam); // 开始任务
            mSupplyParam = supplyParam;
            // 保存数据
            DataSource.get().setTask(params);
        }

        return ScheduleConfig.RESULT_CODE_SUCCESS;
    }

    public String getTimeRtcStatus() {

        if (mSupplyParam != null) { // 已设置
            SupplyResult supplyResult = new SupplyResult();
            supplyResult.setState(ScheduleConfig.RESULT_CODE_SUCCESS);
            supplyResult.setOff_date(mSupplyParam.getOff_date());
            supplyResult.setOff_time(mSupplyParam.getOff_time());
            supplyResult.setOn_date(mSupplyParam.getOn_date());
            supplyResult.setOn_time(mSupplyParam.getOn_time());
            return new Gson().toJson(supplyResult);
        }
        return ScheduleConfig.RETURN_1000;
    }

    /**
     * 开机定时任务
     *
     * @param supplyParam
     */
    private void startAlarmManager(SupplyParam supplyParam) {
        if (supplyParam == null) {
            Log.e(TAG, "开启任务：SupplyParam is null");
            return;
        }
        Log.e(TAG, "triggerPoint " + supplyParam.getShutTime());
        AlarmManager am = (AlarmManager) App.get().getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, supplyParam.getShutTime(), createIntent(supplyParam));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                am.setExact(AlarmManager.RTC_WAKEUP, supplyParam.getShutTime(), createIntent(supplyParam));
            } else {
                am.set(AlarmManager.RTC_WAKEUP, supplyParam.getShutTime(), createIntent(supplyParam));
            }
            Log.e(TAG, "开启任务成功");
        }
    }

    private void cancelAlarmManager(SupplyParam supplyParam) {
        Log.e(TAG, "取消任务操作：" + supplyParam);
        if (supplyParam == null) {
            Log.e(TAG, "取消任务操作：SupplyParam is null");
        } else {
            AlarmManager am = (AlarmManager) App.get().getSystemService(Context.ALARM_SERVICE);
            PendingIntent pi = createIntent(supplyParam);
            if (am != null) {
                am.cancel(pi);
            }
            pi.cancel();
            Log.e(TAG, "取消任务成功");
        }
    }


    private PendingIntent createIntent(SupplyParam supplyParam) {
        Intent intent = new Intent(App.get(), ResponseService.class);
        return PendingIntent.getService(App.get(), supplyParam.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
