package com.ayst.androidx.action;

import android.content.Context;
import android.util.Log;

import com.ayst.androidx.util.AppUtil;
import com.ayst.androidx.util.SPUtils;

public class KeepLive4GAction extends BaseAction {
    private static final String TAG = "KeepLive4GAction";
    private static final String KEY_4G = "4g_keep_live";

    public KeepLive4GAction(Context context) {
        super(context);
    }

    @Override
    public void run() {
        super.run();
        while (mAlive) {
            Log.d(TAG, "run...");

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void open() {
        SPUtils.get(mContext).saveData(KEY_4G, true);
    }

    @Override
    public void close() {
        SPUtils.get(mContext).saveData(KEY_4G, false);
    }

    @Override
    public boolean isOpen() {
        return SPUtils.get(mContext).getData(KEY_4G, "1".equals(AppUtil.
                getProperty("ro.androidx.4g_keep_live", "0")));
    }
}
