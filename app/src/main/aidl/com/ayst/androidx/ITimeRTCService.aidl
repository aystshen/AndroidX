// ITimeRTCService.aidl
package com.ayst.androidx;

// Declare any non-default types here with import statements

interface ITimeRTCService {
     /**
        * @param param json格式字符串
        *
        * {
        *    "cmd":1,
        *    "off_date":"2019-07-03",
        *    "off_time":"11:30",
        *    "on_date":"2019-07-03",
        *    "on_time":"12:00"
        * }
        *
        * 其中：
        * cmd取值有二：
        *  1：表示设置定时开关机
        *  0：表示取消定时开关机（此操作其余参数可为空）
        *
        * off_date：关机日期
        * off_time：关机时间点
        * on_date：开机日期
        * on_time：开机时间点
        *
        *
        * @return 设置状态响应
        */
       int updateTimeToRtc(String param);


       /**
        *
        * {
        *    "state":0,
        *    "error":"error string",
        *    "off_date":"2019-07-03",
        *    "off_time":"11:30",
        *    "on_date":"2019-07-03",
        *    "on_time":"12:00"
        * }
        */
       String getTimeRtcStatus();
}
