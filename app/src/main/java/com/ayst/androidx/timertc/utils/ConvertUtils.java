package com.ayst.androidx.timertc.utils;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.ayst.androidx.timertc.ScheduleConfig;


import java.text.ParseException;


public class ConvertUtils {

    public static final String TAG = ConvertUtils.class.getSimpleName();

    /**
     * @param time 11:54
     * @return 该日期该时间点对应的毫秒数
     */
    public static long convertDayLoopType2Millis(String time) {
        if (TextUtils.isEmpty(time)) {
            return 0;
        }
        String date = ScheduleConfig.DATE_FORMAT.format(System.currentTimeMillis());
        Log.e(TAG, "DayLoop date：" + date);

        time = time.replace(":", "-");
        Log.e(TAG, "DayLoop time：" + time);

        String target = date + "-" + time;
        Log.e(TAG, "DayLoop time：" + time);
        Log.e(TAG, "DayLoop target = " + target);

        try {
            long targetMillis = ScheduleConfig.DATE_TIME_FORMAT.parse(target).getTime();
            Log.e(TAG, "DayLoop time：" + time);
            Log.e(TAG, "DayLoop targetMillis = " + targetMillis);
            return targetMillis;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * @param date 1923-11-21
     * @param time 11:54
     * @return 该日期该时间点对应的毫秒数
     */
    public static long convertNoneLoopType2Millis(@Nullable String date, String time) {
        if (TextUtils.isEmpty(time)) {
            return 0;
        }
        time = time.replace(":", "-");
        Log.e(TAG, "NoneLoop time：" + time);
        String target = date + "-" + time;
        Log.e(TAG, "NoneLoop target = " + target);
        try {
            return ScheduleConfig.DATE_TIME_FORMAT.parse(target).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * off_date 和 on_date 都为null表示种类2，否则表示种类1
     *
     * @param offDate 1923-11-21
     * @param onDate  1923-11-21
     * @return 该日期该时间点对应的毫秒数
     */
    public static int convert2Type(@Nullable String offDate, @Nullable String onDate) {

        if (TextUtils.isEmpty(offDate) && TextUtils.isEmpty(onDate)) {
            return ScheduleConfig.TYPE_DAY_LOOP;
        }

        if (!TextUtils.isEmpty(offDate) && !TextUtils.isEmpty(onDate)) {
            return ScheduleConfig.TYPE_NONE_LOOP;
        }

        return ScheduleConfig.TYPE_ERROR_LOOP;
    }

}
