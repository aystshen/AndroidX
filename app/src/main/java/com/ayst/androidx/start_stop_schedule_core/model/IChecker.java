package com.ayst.androidx.start_stop_schedule_core.model;


/**
 * Created by Administrator on 2019-10-24.
 * <p>
 * 检测客户端传进来的json数据的合法性
 */

public interface IChecker<Data extends IData> {

    int check(Data data);
}
