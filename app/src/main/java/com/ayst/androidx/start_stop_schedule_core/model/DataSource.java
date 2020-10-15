package com.ayst.androidx.start_stop_schedule_core.model;


import com.ayst.androidx.App;
import com.ayst.androidx.start_stop_schedule_core.ScheduleConfig;
import com.ayst.androidx.util.SPUtils;

/**
 * Created by Administrator on 2019-10-25.
 */

public class DataSource {

    private DataSource() {
    }

    private static final class HOLDER {
        private static final DataSource INSTANCE = new DataSource();
    }

    public static DataSource get() {
        return DataSource.HOLDER.INSTANCE;
    }

    public void setTask(String params){
        SPUtils.get(App.get()).saveData(ScheduleConfig.SP_KEY, params);
    }

    public String getTask(){
        return SPUtils.get(App.get()).getData(ScheduleConfig.SP_KEY,null);
    }
}
