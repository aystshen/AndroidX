package com.ayst.androidx.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.ayst.androidx.IOtgService;
import com.ayst.androidx.event.MessageEvent;
import com.ayst.androidx.util.AppUtils;
import com.ayst.androidx.util.ShellUtils;

import org.greenrobot.eventbus.EventBus;

public class OtgService extends Service {

    private static final String TAG = "OtgService";
    private static final String FORCE_USB_MODE_FILE = "/sys/bus/platform/drivers/usb20_otg/force_usb_mode";

    private static final String USB_MODE_AUTO = "0";
    private static final String USB_MODE_HOST = "1";
    private static final String USB_MODE_DEVICE = "2";

    public OtgService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mService;
    }

    private final IOtgService.Stub mService = new IOtgService.Stub() {

        @Override
        public boolean setOtgMode(String mode) throws RemoteException {
            ShellUtils.CommandResult result = ShellUtils.execCmd(
                    "echo " + mode + " > " + FORCE_USB_MODE_FILE, true);

            Log.i(TAG, "setOtgMode, result: " + result);

            boolean success = result.errorMsg.isEmpty();
            if (success) {
                AppUtils.setProperty("persist.sys.otg_mode", mode);
                if (TextUtils.equals(USB_MODE_HOST, mode)) {
                    if (TextUtils.equals("1", AppUtils.getProperty(
                            "ro.androidx.watchdog", "0"))) {
                        Log.i(TAG, "setOtgMode, [USB_MODE_HOST] Reopen the watchdog");
                        EventBus.getDefault().post(new MessageEvent(MessageEvent.MSG_OPEN_WATCHDOG));
                    }
                } else {
                    Log.i(TAG, "setOtgMode, [USB_MODE_DEVICE|USB_MODE_AUTO] Close the watchdog");
                    EventBus.getDefault().post(new MessageEvent(MessageEvent.MSG_CLOSE_WATCHDOG));
                }
            }
            return success;
        }

        @Override
        public String getOtgMode() throws RemoteException {
            ShellUtils.CommandResult result = ShellUtils.execCmd(
                    "cat " + FORCE_USB_MODE_FILE, true);

            Log.i(TAG, "getOtgMode, result: " + result);

            return result.successMsg;
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand...");

        String mode = AppUtils.getProperty("persist.sys.otg_mode", "0");
        try {
            mService.setOtgMode(mode);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return super.onStartCommand(intent, flags, startId);
    }
}
