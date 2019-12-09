package com.ayst.androidx.event;

import android.os.Bundle;

/**
 * Created by Administrator on 2018/3/30.
 */

public class MessageEvent {
    public static final int MSG_NONE = 0;
    public static final int MSG_OPEN_WATCHDOG = 1001;
    public static final int MSG_CLOSE_WATCHDOG = 1002;

    private int message = MSG_NONE;
    private Bundle bundle = null;

    public MessageEvent(int message) {
        this.message = message;
    }

    public int getMessage() {
        return message;
    }

    public void setMessage(int message) {
        this.message = message;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }
}
