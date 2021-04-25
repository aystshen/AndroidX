package com.ayst.androidx.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.ayst.androidx.service.AppEnableService;
import com.ayst.androidx.service.AppLaunchService;
import com.ayst.androidx.service.Log2fileService;
import com.ayst.androidx.service.ModemService;
import com.ayst.androidx.service.NetForwardService;
import com.ayst.androidx.service.OtgService;
import com.ayst.androidx.service.TimeRTCService;
import com.ayst.androidx.service.WatchdogService;
import com.ayst.androidx.util.AppUtils;
import com.ayst.androidx.util.SPUtils;

public class BootCompleteReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompleteReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive, action = " + action);

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            Log.i(TAG, "开机自启");
            context.startService(new Intent(context, AppLaunchService.class));
            context.startService(new Intent(context, AppEnableService.class));
            context.startService(new Intent(context, Log2fileService.class));
            context.startService(new Intent(context, ModemService.class));
            context.startService(new Intent(context, WatchdogService.class));
            context.startService(new Intent(context, OtgService.class));
            context.startService(new Intent(context, TimeRTCService.class));
            context.startService(new Intent(context, NetForwardService.class));

            if (TextUtils.equals("1", AppUtils.getProperty(
                    "persist.androidx.key_intercept",
                    "0"))) {
                AppUtils.openAccessibilityService(context);
            } else {
                AppUtils.closeAccessibilityService(context);
            }
        }
    }
}
