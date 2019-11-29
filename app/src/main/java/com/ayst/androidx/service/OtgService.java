package com.ayst.androidx.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.ayst.androidx.IOtgService;
import com.ayst.androidx.util.AppUtils;
import com.ayst.androidx.util.ShellUtils;

public class OtgService extends Service {

    private static final String TAG = "OtgService";
    private static final String FORCE_USB_MODE_FILE = "/sys/bus/platform/drivers/usb20_otg/force_usb_mode";

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
