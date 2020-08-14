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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OtgService extends Service {

    private static final String TAG = "OtgService";

    private static final int OTG_TYPE_DEFAULT = 0;  // 默认，如：RK3288
    private static final int OTG_TYPE_C = 1;        // TYPE-C
    private static final int OTG_TYPE_USB3 = 2;     // USB3.0
    private static final int OTG_TYPE_3326 = 3;     // RK3326

    private static final String USB_MODE_AUTO = "0";
    private static final String USB_MODE_HOST = "1";
    private static final String USB_MODE_DEVICE = "2";

    private int mCurOtgType = OTG_TYPE_DEFAULT;
    private Otg mCurOtg;
    private static List<Otg> sOtgs = new ArrayList<>();

    static {
        sOtgs.add(new Otg("/sys/bus/platform/drivers/usb20_otg/force_usb_mode",
                "0", "1", "2"));
        sOtgs.add(new Otg("/sys/devices/platform/ff770000.syscon/ff770000.syscon:usb2-phy@e450/otg_mode",
                "peripheral", "host", "otg"));
        sOtgs.add(new Otg("/sys/devices/platform/usb0/dwc3_mode",
                "peripheral", "host", "otg"));
        sOtgs.add(new Otg("/sys/devices/platform/ff2c0000.syscon/ff2c0000.syscon:usb2-phy@100/otg_mode",
                "peripheral", "host", "otg"));
    }

    public OtgService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        String typeStr = AppUtils.getProperty("ro.otg.type", OTG_TYPE_DEFAULT + "");
        int type = Integer.parseInt(typeStr);
        if (type < sOtgs.size()) {
            mCurOtg = sOtgs.get(type);
            mCurOtgType = type;
        } else {
            mCurOtg = sOtgs.get(mCurOtgType);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mService;
    }

    private final IOtgService.Stub mService = new IOtgService.Stub() {

        @Override
        public boolean setOtgMode(String mode) throws RemoteException {
            boolean success = false;
            if (null != mCurOtg) {
                ShellUtils.CommandResult result;
                if (mCurOtgType == OTG_TYPE_USB3
                    && TextUtils.equals(USB_MODE_HOST, mode)) {
                    ShellUtils.execCmd("echo " + mCurOtg.getMode(USB_MODE_AUTO) + " > " + mCurOtg.file, true);
                }
                result = ShellUtils.execCmd(
                        "echo " + mCurOtg.getMode(mode) + " > " + mCurOtg.file, true);

                Log.i(TAG, "setOtgMode, result: " + result);

                success = result.errorMsg.isEmpty();
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
            } else {
                Log.e(TAG, "setOtgMode, mCurOtg is null");
            }
            return success;
        }

        @Override
        public String getOtgMode() throws RemoteException {
            if (null != mCurOtg) {
                ShellUtils.CommandResult result = ShellUtils.execCmd(
                        "cat " + mCurOtg.file, true);

                Log.i(TAG, "getOtgMode, result: " + result);

                return mCurOtg.parseMode(result.successMsg);
            } else {
                Log.e(TAG, "getOtgMode, mCurOtg is null");
            }

            return USB_MODE_AUTO;
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

    private static class Otg {
        String file;
        String autoMode;
        String hostMode;
        String otgMode;

        Otg(String file, String autoMode, String hostMode, String otgMode) {
            this.file = file;
            this.autoMode = autoMode;
            this.hostMode = hostMode;
            this.otgMode = otgMode;
        }

        /**
         * 将客户端传过来的mode转换为平台mode
         *
         * @param mode  客户端mode
         * @return      平台mode
         */
        String getMode(String mode) {
            if (TextUtils.equals(mode, USB_MODE_HOST)) {
                return hostMode;
            } else if (TextUtils.equals(mode, USB_MODE_DEVICE)) {
                return otgMode;
            } else {
                return autoMode;
            }
        }

        /**
         * 将平台mode转接为客户端mode
         *
         * @param mode  平台mode
         * @return      客户端mode
         */
        String parseMode(String mode) {
            if (TextUtils.equals(mode, hostMode)) {
                return USB_MODE_HOST;
            } else if (TextUtils.equals(mode, otgMode)) {
                return USB_MODE_DEVICE;
            } else {
                return USB_MODE_AUTO;
            }
        }

        @Override
        public String toString() {
            return "Otg{" +
                    "file='" + file + '\'' +
                    ", autoMode='" + autoMode + '\'' +
                    ", hostMode='" + hostMode + '\'' +
                    ", otgMode='" + otgMode + '\'' +
                    '}';
        }
    }
}
