package com.ayst.androidx.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ayst.androidx.service.ModemService;
import com.ayst.androidx.service.WatchdogService;

public class BootCompleteReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompleteReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive, action = " + action);
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            context.startService(new Intent(context, ModemService.class));
            context.startService(new Intent(context, WatchdogService.class));
        }
    }
}
