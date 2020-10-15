package com.ayst.androidx.start_stop_schedule_core.model;

import android.text.TextUtils;
import android.util.Log;

import com.ayst.androidx.start_stop_schedule_core.ScheduleConfig;


import java.util.concurrent.TimeUnit;

import static com.ayst.androidx.start_stop_schedule_core.ScheduleConfig.SCHEDU_TAG;

/**
 * 请求数据检查
 */
public class SupplyChecker implements IChecker<SupplyParam> {


    @Override
    public int check(SupplyParam data) {

        if (data.isUpdate()) {
            return checkUpdate(data);
        } else if (data.isCancel()) {
            return checkCancel(data);
        }
        return ScheduleConfig.RESULT_CODE_CMD_ERROR; // cmd指令错误
    }

    private int checkCancel(SupplyParam data) {
        Log.i(SCHEDU_TAG, "checkCancel：" + data);
        return ScheduleConfig.RESULT_CODE_SUCCESS;
    }


    /**
     * 检查更新时间指令数据正确性
     */
    private int checkUpdate(SupplyParam data) {

        if (data.isDayLoopType()) {
            return checkDayLoopType(data);
        } else if (data.isNoneLoopType()) {
            return checkNoneLoopType(data);
        }

        return ScheduleConfig.RESULT_CODE_DATA_TIME_ERROR;
    }

    /**
     * 不允许开关机时间相同
     */
    private int checkDayLoopType(SupplyParam data) {
        if (data.getBootTime() == data.getShutTime()) {
            return ScheduleConfig.RESULT_CODE_DATA_TIME_ERROR;
        }
        return ScheduleConfig.RESULT_CODE_SUCCESS;
    }


    /**
     * 1、关机时间至少要比当前时间推迟5分钟
     * 2、开机时间与关机时间至少间隔5分钟
     */
    private int checkNoneLoopType(SupplyParam data) {

        if (TextUtils.isEmpty(data.getOff_time()) || TextUtils.isEmpty(data.getOn_time())) {
            return ScheduleConfig.RESULT_CODE_DATA_TIME_ERROR;
        }

        Log.i("SupplyChecker", "data.getShutTime():" + data.getShutTime());
        Log.i("SupplyChecker", "System.currentTimeMillis():" + System.currentTimeMillis());
        Log.i("SupplyChecker", "data.getShutTime() - System.currentTimeMillis():" + (data.getShutTime() - System.currentTimeMillis()));
        Log.i("SupplyChecker", "AppConfig.MINUTE_5_MILLIS:" + ScheduleConfig.MINUTE_5_MILLIS);
        Log.i("SupplyChecker", "AppConfig.MINUTE_4_MILLIS:" + TimeUnit.MINUTES.toMillis(4));

        // 设置的时间小于当前时间
        if (System.currentTimeMillis() > data.getShutTime()) {
            return ScheduleConfig.RESULT_CODE_LESS;
        }
        // TODO 毫秒数(精确度在4-5之间)
        if (data.getShutTime() - System.currentTimeMillis() < ScheduleConfig.MINUTE_5_MILLIS) { // 关机时间至少要比当前时间推迟5分钟
            return ScheduleConfig.RESULT_CODE_TIME_INTERVAL_ERROR;
        }

        if (data.getBootTime() - data.getShutTime() < ScheduleConfig.MINUTE_5_MILLIS) { // 开机时间与关机时间间隔小于5分钟
            return ScheduleConfig.RESULT_CODE_TIME_INTERVAL_ERROR;
        }

        return ScheduleConfig.RESULT_CODE_SUCCESS;
    }


}
