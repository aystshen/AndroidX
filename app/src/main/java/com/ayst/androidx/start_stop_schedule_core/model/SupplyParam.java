package com.ayst.androidx.start_stop_schedule_core.model;

import android.text.TextUtils;

import com.ayst.androidx.start_stop_schedule_core.ScheduleConfig;


/**
 * 定时请求参数
 */
public class SupplyParam implements IData {


    /**
     * 格式：
     * cmd : 1
     * off_date : 2019-07-03
     * off_time : 11:20
     * on_date : 2019-07-03
     * on_time : 11:20
     */
    private int cmd;
    private String off_date;
    private String off_time;
    private String on_date;
    private String on_time;

    /**
     * 定时类型
     */
    private int type;
    private int resultCode = ScheduleConfig.RESULT_CODE_SUCCESS;

    private long bootTime;
    private long shutTime;

    public String getOff_date() {
        return off_date;
    }

    public void setOff_date(String off_date) {
        this.off_date = off_date;
    }

    public String getOff_time() {
        return off_time;
    }

    public void setOff_time(String off_time) {
        this.off_time = off_time;
    }

    public String getOn_date() {
        return on_date;
    }

    public void setOn_date(String on_date) {
        this.on_date = on_date;
    }

    public String getOn_time() {
        return on_time;
    }

    public void setOn_time(String on_time) {
        this.on_time = on_time;
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public long getBootTime() {
        return bootTime;
    }

    public void setBootTime(long bootTime) {
        this.bootTime = bootTime;
    }

    public long getShutTime() {
        return shutTime;
    }

    public void setShutTime(long shutTime) {
        this.shutTime = shutTime;
    }

    public boolean isDayLoopType() {
        return TextUtils.isEmpty(off_date) && TextUtils.isEmpty(on_date);
    }

    public boolean isNoneLoopType() {
        return !TextUtils.isEmpty(off_date) && !TextUtils.isEmpty(on_date);
    }

    public boolean isUpdate() {
        return cmd == ScheduleConfig.CMD_CODE_SET;
    }

    public boolean isCancel() {
        return cmd == ScheduleConfig.CMD_CODE_CANCEL;
    }

    @Override
    public int hashCode() {
        return (off_time + on_time).hashCode();
    }

    @Override
    public String toString() {
        return "SupplyParam{" +
                "cmd=" + cmd +
                ", off_date='" + off_date + '\'' +
                ", off_time='" + off_time + '\'' +
                ", on_date='" + on_date + '\'' +
                ", on_time='" + on_time + '\'' +
                ", type=" + type +
                ", resultCode=" + resultCode +
                ", bootTime=" + bootTime +
                ", shutTime=" + shutTime +
                '}';
    }
}
