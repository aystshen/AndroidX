package com.ayst.androidx.timertc;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


/**
 * 定时相关配置
 */
public class ScheduleConfig {



    /**
     * 时间格式化
     */
    public static SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.getDefault());
    public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    /**
     * 更新定时开关机结果码
     */
    public static final int RESULT_CODE_SUCCESS = 0;
    public static final int RESULT_CODE_CMD_ERROR = 1001;
    public static final int RESULT_CODE_DATA_TIME_ERROR = 1002;
    public static final int RESULT_CODE_LESS = 1003;
    public static final int RESULT_CODE_TIME_INTERVAL_ERROR = 1004;
    public static final int RESULT_CODE_OTHER_ERROR = 1005;


    /**
     * 开关机类型
     */
    public static final int TYPE_NONE_LOOP = 1; // 不循环
    public static final int TYPE_DAY_LOOP = 2; // 每天循环
    public static final int TYPE_ERROR_LOOP = -1; // 每天循环

    /**
     * 返回结果状态码
     */


    public static final int CMD_CODE_SET = 1;
    public static final int CMD_CODE_CANCEL = 0;


    /**
     * 字段名称
     */
    public static final String FIELD_CMD = "cmd";
    public static final String FIELD_ON_DATE = "on_date";
    public static final String FIELD_ON_TIME = "on_time";
    public static final String FIELD_OFF_DATE = "off_date";
    public static final String FIELD_OFF_TIME = "off_time";


    /**
     * 一天的总毫秒数
     */
    public static final long DAY_MILLIS = TimeUnit.DAYS.toMillis(1);

    /**
     * 一周的总毫秒数
     */
    public static final long WEEK_MILLIS = TimeUnit.DAYS.toMillis(7);
    /**
     * 一周的总毫秒数
     */
    public static final long MINUTE_5_MILLIS = TimeUnit.MINUTES.toMillis(5);


    public static final String SP_KEY = "SP_KEY";


    /**
     * getTimeRtcStatus 返回值
     */
    public static final String RETURN_1000 = "{\"state\":1000}";

}
