package com.ayst.androidx.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.ayst.androidx.service.MainService;
import com.ayst.androidx.util.AppUtil;

/**
 * Test:
 *  adb shell am broadcast -a com.topband.androidx.ACTION_RUN_ALL  --include-stopped-packages com.ayst.androidx
 */
public class ActionReceiver extends BroadcastReceiver {
    private static final String TAG = "ActionReceiver";

    public static final String ACTION_RUN_ALL = "com.topband.androidx.ACTION_RUN_ALL";
    public static final String ACTION_4G_KEEP_LIVE = "com.topband.androidx.ACTION_4G_KEEP_LIVE";
    public static final String ACTION_WATCHDOG = "com.topband.androidx.ACTION_WATCHDOG";

    public static final String EXTRA_ACTION = "action";
    public static final String EXTRA_ACTION_OPEN = "open";
    public static final String EXTRA_ACTION_CLOSE = "close";
    public static final String EXTRA_ACTION_CONFIG = "config";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive, action = " + action);

        Intent actionIntent;
        if (ACTION_RUN_ALL.equals(action)) {
            actionIntent = new Intent(context, MainService.class);
            actionIntent.putExtra(MainService.EXTRA_ACTION, MainService.COMMAND_RUN_ALL_ACTION);
            actionIntent.putExtra(MainService.EXTRA_DELAY, 0);
            AppUtil.startService(context, actionIntent);
        } else if (ACTION_4G_KEEP_LIVE.equals(action)) {
            // 4G 守护
            actionIntent = new Intent(context, MainService.class);
            if (EXTRA_ACTION_OPEN.equals(intent.getStringExtra(EXTRA_ACTION))) {
                actionIntent.putExtra(MainService.EXTRA_ACTION, MainService.COMMAND_ACTION_OPEN_4G_KEEP_LIVE);
            } else if (EXTRA_ACTION_CLOSE.equals(intent.getStringExtra(EXTRA_ACTION))) {
                actionIntent.putExtra(MainService.EXTRA_ACTION, MainService.COMMAND_ACTION_CLOSE_4G_KEEP_LIVE);
            }
            actionIntent.putExtra(MainService.EXTRA_DELAY, 0);
            Bundle bundle = intent.getExtras();
            if (null != bundle) {
                actionIntent.putExtras(bundle);
            }
            AppUtil.startService(context, actionIntent);

        } else if (ACTION_WATCHDOG.equals(action)) {
            // 看门狗
            actionIntent = new Intent(context, MainService.class);
            if (EXTRA_ACTION_OPEN.equals(intent.getStringExtra(EXTRA_ACTION))) {
                actionIntent.putExtra(MainService.EXTRA_ACTION, MainService.COMMAND_ACTION_OPEN_WATCHDOG);
            } else if (EXTRA_ACTION_CLOSE.equals(intent.getStringExtra(EXTRA_ACTION))) {
                actionIntent.putExtra(MainService.EXTRA_ACTION, MainService.COMMAND_ACTION_CLOSE_WATCHDOG);
            } else if (EXTRA_ACTION_CONFIG.equals(intent.getStringExtra(EXTRA_ACTION))) {
                actionIntent.putExtra(MainService.EXTRA_ACTION, MainService.COMMAND_ACTION_CONFIG_WATCHDOG);
            }
            actionIntent.putExtra(MainService.EXTRA_DELAY, 0);
            Bundle bundle = intent.getExtras();
            if (null != bundle) {
                actionIntent.putExtras(bundle);
            }
            AppUtil.startService(context, actionIntent);
        }
    }
}
