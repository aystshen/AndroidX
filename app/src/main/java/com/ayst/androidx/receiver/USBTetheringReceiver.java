package com.ayst.androidx.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.text.TextUtils;
import android.util.Log;

import com.ayst.androidx.util.AppUtils;
import com.ayst.androidx.util.ShellUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class USBTetheringReceiver extends BroadcastReceiver {

    private static final String TAG = "UsbStatusReceiver";
    /**
     * Broadcast Action: A sticky broadcast for USB state change events when in device mode.
     * This is a sticky broadcast for clients that includes USB connected/disconnected state,
     * <ul>
     * <li> {@link #USB_CONNECTED} boolean indicating whether USB is connected or disconnected.
     * <li> {@link #USB_CONFIGURED} boolean indicating whether USB is configured. currently zero if not configured, one for configured.
     * <li> {@link #USB_FUNCTION_ADB} boolean extra indicating whether the adb function is enabled
     * <li> {@link #USB_FUNCTION_RNDIS} boolean extra indicating whether the RNDIS ethernet function is enabled
     * <li> {@link #USB_FUNCTION_MTP} boolean extra indicating whether the MTP function is enabled
     * <li> {@link #USB_FUNCTION_PTP} boolean extra indicating whether the PTP function is enabled
     * <li> {@link #USB_FUNCTION_PTP} boolean extra indicating whether the accessory function is enabled
     * <li> {@link #USB_FUNCTION_AUDIO_SOURCE} boolean extra indicating whether the audio source function is enabled
     * <li> {@link #USB_FUNCTION_MIDI} boolean extra indicating whether the MIDI function is enabled
     * </ul>
     */

    public static final String ACTION_USB_STATE = "android.hardware.usb.action.USB_STATE";

    /**
     * Boolean extra indicating whether USB is connected or disconnected.
     * Used in extras for the {@link #ACTION_USB_STATE} broadcast.
     */
    public static final String USB_CONNECTED = "connected";

    /**
     * Boolean extra indicating whether USB is configured.
     * Used in extras for the {@link #ACTION_USB_STATE} broadcast.
     */
    public static final String USB_CONFIGURED = "configured";

    /**
     * Boolean extra indicating whether confidential user data, such as photos, should be
     * made available on the USB connection. This variable will only be set when the user
     * has explicitly asked for this data to be unlocked.
     * Used in extras for the {@link #ACTION_USB_STATE} broadcast.
     */
    public static final String USB_DATA_UNLOCKED = "unlocked";


    public static final String USB_FUNCTION_NONE = "none";

    /**
     * Name of the adb USB function.
     * Used in extras for the {@link #ACTION_USB_STATE} broadcast
     */
    public static final String USB_FUNCTION_ADB = "adb";

    /**
     * Name of the RNDIS ethernet USB function.
     * Used in extras for the {@link #ACTION_USB_STATE} broadcast
     */
    public static final String USB_FUNCTION_RNDIS = "rndis";

    /**
     * Name of the MTP USB function.
     * Used in extras for the {@link #ACTION_USB_STATE} broadcast
     */
    public static final String USB_FUNCTION_MTP = "mtp";

    /**
     * Name of the PTP USB function.
     * Used in extras for the {@link #ACTION_USB_STATE} broadcast
     */
    public static final String USB_FUNCTION_PTP = "ptp";

    /**
     * Name of the audio source USB function.
     * Used in extras for the {@link #ACTION_USB_STATE} broadcast
     */
    public static final String USB_FUNCTION_AUDIO_SOURCE = "audio_source";

    /**
     * Name of the MIDI USB function.
     * Used in extras for the {@link #ACTION_USB_STATE} broadcast
     */
    public static final String USB_FUNCTION_MIDI = "midi";

    Context mContext = null;
    String[] cmds = {
            "ifconfig usb0 up",
            "ifconfig usb0 192.168.42.11",
            "ip route add default via 192.168.42.10",
            "ip rule add from all lookup main pref 9999",
    };

    @Override
    public void onReceive(Context context, Intent intent) {

        mContext = context;
        if (!TextUtils.equals("1", AppUtils.getProperty(
                "persist.androidx.usbtether", "0"))) {
            return;
        }
        String action = intent.getAction();
        Log.i(TAG, "action = " + intent.getAction());
        switch (action) {
            case ACTION_USB_STATE:
                boolean connected = intent.getBooleanExtra(USB_CONNECTED, false);
                Log.i(TAG, "connected : " + connected);
                boolean configured = intent.getBooleanExtra(USB_CONFIGURED, false);
                Log.i(TAG, "configured : " + configured);
                boolean function_adb = intent.getBooleanExtra(USB_FUNCTION_ADB, false);
                Log.i(TAG, "function_adb : " + function_adb);
                boolean function_rndis = intent.getBooleanExtra(USB_FUNCTION_RNDIS, false);
                Log.i(TAG, "function_rndis : " + function_rndis);
                boolean function_mtp = intent.getBooleanExtra(USB_FUNCTION_MTP, false);
                Log.i(TAG, "function_mtp : " + function_mtp);
                boolean function_ptp = intent.getBooleanExtra(USB_FUNCTION_PTP, false);
                Log.i(TAG, "usb_function_ptp : " + function_ptp);
                boolean function_audio_source = intent.getBooleanExtra(USB_FUNCTION_AUDIO_SOURCE, false);
                Log.i(TAG, "function_audio_source : " + function_audio_source);
                boolean function_midi = intent.getBooleanExtra(USB_FUNCTION_MIDI, false);
                Log.i(TAG, "function_midi : " + function_midi);
                if (connected && !function_rndis) {
                    try {
                        enableUSBteher();
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    initIp();
                }
                break;
            case Intent.ACTION_BOOT_COMPLETED:

                try {
                    Thread.sleep(1000);
                    enableUSBteher();
                    Thread.sleep(1000);
                    initIp();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            default:
                Log.i(TAG,"default");
        }
    }

    public void initIp() {
        ShellUtils.CommandResult result;
        for (int i = 0; i < cmds.length; i++) {
            result = ShellUtils.execCmd(cmds[i], true);
            Log.i(TAG, cmds[i] + "\n: " + result.toString());
        }
    }

    public int enableUSBteher() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        Method method = getMethod(ConnectivityManager.class, "setUsbTethering", boolean.class);
        if (null == method) {
            Log.i(TAG, "callMethod failed");
            return -1;
        }
        int returnCode = 0;
        try {
            returnCode = (Integer) method.invoke(connectivityManager, true);
            Log.i(TAG, "callMethod over");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return returnCode;
    }

    public Method getMethod(Class clazz, String methodName, Class<?>... parameterTypes) {
        Method method = null;
        try {
            method = clazz.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return method;
    }

}
