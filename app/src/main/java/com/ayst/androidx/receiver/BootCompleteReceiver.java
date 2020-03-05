package com.ayst.androidx.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ayst.androidx.service.Log2fileService;
import com.ayst.androidx.service.ModemService;
import com.ayst.androidx.service.OtgService;
import com.ayst.androidx.service.WatchdogService;
import com.ayst.androidx.util.AppUtils;

public class BootCompleteReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompleteReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive, action = " + action);
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            context.startService(new Intent(context, Log2fileService.class));
            context.startService(new Intent(context, ModemService.class));
            context.startService(new Intent(context, WatchdogService.class));
            context.startService(new Intent(context, OtgService.class));

            if ("1".equals(AppUtils.getProperty("persist.sys.intercept_key", "0"))) {
                AppUtils.openAccessibilityService(context);
            } else {
                AppUtils.closeAccessibilityService(context);
            }
        }
    }
}
