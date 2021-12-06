package com.ayst.androidx.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.ayst.androidx.util.AppUtils;
import com.ayst.androidx.util.ShellUtils;

import java.util.HashMap;
import java.util.Map;

public class NetForwardService extends Service {
    private String TAG = "NetForwardService";
    private String CMD_VD28 = "VD28";
    private String mCmdKey = CMD_VD28;
    private String mIp = "192.168.1.1";
    private Map<String, String[]> mCmdMap = new HashMap<String, String[]>();

    private Runnable mNetForwardRunnable = new Runnable() {
        @Override
        public void run() {
            ShellUtils.CommandResult result;
            String[] cmds = mCmdMap.get(mCmdKey);
            for (int i = 0; i < cmds.length; i++) {
                result = ShellUtils.execCmd(cmds[i], true);
                Log.i(TAG, cmds[i] + "\n: " + result.toString());
            }
        }
    };

    public NetForwardService() {}

    @Override
    public void onCreate() {
        super.onCreate();
        mCmdKey = AppUtils.getProperty("persist.androidx.netforward.key", mCmdKey);
        mIp = AppUtils.getProperty("persist.androidx.netforward.ip", mIp);
        Log.i(TAG, "mCmdKey = " + mCmdKey + "mIp = " + mIp);
        initCmds();
    }

    void initCmds() {
        String[] vd28 = {
                "iptables -F",
                "iptables -X",
                "iptables -t nat -F",
                "busybox ifconfig eth1 up",
                "busybox ifconfig eth1 " + mIp,
                "ip rule add from all lookup main pref 9999",
                "echo 1 > /proc/sys/net/ipv4/ip_forward",
                "iptables -t nat -A POSTROUTING -s " + mIp + "/255.255.255.0 -o eth0 -j MASQUERADE",
        };
        mCmdMap.put(CMD_VD28, vd28);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (TextUtils.equals("1", AppUtils.getProperty(
                "persist.androidx.netforward", "0"))) {
            Thread thread = new Thread(mNetForwardRunnable);
            thread.start();
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
