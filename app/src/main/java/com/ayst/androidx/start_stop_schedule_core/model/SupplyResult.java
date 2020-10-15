package com.ayst.androidx.start_stop_schedule_core.model;


/**
 * 定时请求响应
 */
public class SupplyResult {

    /**
     * state : 0
     * error : error sting
     * off_date : 2019-07-03
     * off_time : 2019-07-03
     * on_date : 2019-07-03
     * on_time : 2019-07-03
     */

    private int state;
//    private String error;
    private String off_date;
    private String off_time;
    private String on_date;
    private String on_time;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

//    public String getError() {
//        return error;
//    }
//
//    public void setError(String error) {
//        this.error = error;
//    }

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
}
