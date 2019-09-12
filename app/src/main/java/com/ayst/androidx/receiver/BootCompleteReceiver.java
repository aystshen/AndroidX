package com.ayst.androidx.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ayst.androidx.service.MainService;
import com.ayst.androidx.util.AppUtil;

public class BootCompleteReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompleteReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive, action = " + action);
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            Intent serviceIntent = new Intent(context, MainService.class);
            serviceIntent.putExtra(MainService.EXTRA_ACTION, MainService.COMMAND_RUN_ALL_ACTION);
            serviceIntent.putExtra(MainService.EXTRA_DELAY, 60000);
            AppUtil.startService(context, serviceIntent);
        }
    }
}
