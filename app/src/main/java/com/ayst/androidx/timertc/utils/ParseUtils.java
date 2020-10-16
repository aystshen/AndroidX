package com.ayst.androidx.timertc.utils;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;


import com.ayst.androidx.timertc.ScheduleConfig;
import com.ayst.androidx.timertc.model.SupplyParam;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;


public class ParseUtils {

    private static final String TAG = "ParseUtils";

    /**
     * 解析json数据
     */
    public static SupplyParam parse(String params) {

        if (TextUtils.isEmpty(params)) {

            return null;
        }

        SupplyParam supplyParam = new SupplyParam();

        try {
            JSONObject json = new JSONObject(params);

            int cmd = json.getInt(ScheduleConfig.FIELD_CMD);
            String onData = json.optString(ScheduleConfig.FIELD_ON_DATE);
            String onTime = json.optString(ScheduleConfig.FIELD_ON_TIME);
            String offData = json.optString(ScheduleConfig.FIELD_OFF_DATE);
            String offTime = json.optString(ScheduleConfig.FIELD_OFF_TIME);

            supplyParam.setCmd(cmd);
            supplyParam.setOff_date(offData);
            supplyParam.setOff_time(offTime);
            supplyParam.setOn_date(onData);
            supplyParam.setOn_time(onTime);

            fix(supplyParam);

            return supplyParam;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static void fix(SupplyParam supplyParam) {

        int type = ConvertUtils.convert2Type(supplyParam.getOff_date(), supplyParam.getOn_date());
        supplyParam.setType(type);

        long[] times = new long[2];

        if (supplyParam.isNoneLoopType()) {
            fixNoneLoopType(supplyParam, times);
        } else if (supplyParam.isDayLoopType()) {
            fixDayLoopType(supplyParam, times);
        }
        supplyParam.setBootTime(times[0]);
        supplyParam.setShutTime(times[1]);

    }

    private static void fixDayLoopType(@NonNull SupplyParam supplyParam, @NonNull long[] times) {

        long off = ConvertUtils.convertDayLoopType2Millis(supplyParam.getOff_time());
        long on = ConvertUtils.convertDayLoopType2Millis(supplyParam.getOn_time());
        // 当前时间小于关机时间，则关机时间就是原始关机时间
        // 当前时间大与关机时间，则关机时间为明天同一时间

        long millis = System.currentTimeMillis();

        while (millis > off) {
            off += ScheduleConfig.DAY_MILLIS;
        }

        // 关机时间小于开机时间，则开机时间就是原始开机时间
        // 关机时间大于开机时间，则开机时间就是明天同一时间
        while (off > on) {
            on += ScheduleConfig.DAY_MILLIS;
        }


        times[0] = on;
        times[1] = off;

        Log.e(TAG, "fixDayLoopType： " + Arrays.toString(times));
    }

    private static void fixNoneLoopType(@NonNull SupplyParam supplyParam, @NonNull long[] times) {

        long off = ConvertUtils.convertNoneLoopType2Millis(supplyParam.getOff_date(), supplyParam.getOff_time());
        long on = ConvertUtils.convertNoneLoopType2Millis(supplyParam.getOn_date(), supplyParam.getOn_time());
        times[0] = on;
        times[1] = off;
        Log.e(TAG, "fixDayLoopType： " + Arrays.toString(times));
    }
}
