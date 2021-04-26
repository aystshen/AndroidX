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
    private String PROP_ENABLE = "persist.sys.netforward.enable";
    private String PROP_ETH1_IP = "persist.sys.netforward.eth1.ip";
    private String PROP_COMMAND_KEY = "persist.sys.netforward.cmdkey";
    private String CMD_VD28 = "VD28";
    private String mPropCmdKey;
    private String mPropeth1ip = "192.168.1.1";
    private Map<String,  String []> mCMDmap= new HashMap<String, String []>();

    Boolean mForwardONOFF = false;
    public NetForwardService()
    {
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mForwardONOFF = TextUtils.equals("1", AppUtils.getProperty(PROP_ENABLE, "0"));
        mPropCmdKey =  AppUtils.getProperty(PROP_COMMAND_KEY, "VD28");
        mPropeth1ip = AppUtils.getProperty(PROP_ETH1_IP, "192.168.1.1");
        Log.i(TAG,"onCreate mForwardONOFF = "+mForwardONOFF);
        initCommandList();
    }


    void initCommandList()
    {
        String [] mVD28_CommandList = {
                "iptables -F",
                "iptables -X",
                "iptables -t nat -F",
                "busybox ifconfig eth1 up",
                "busybox ifconfig eth1 "+mPropeth1ip,
                "ip rule add from all lookup main pref 9999",
                "echo 1 > /proc/sys/net/ipv4/ip_forward",
                "iptables -t nat -A POSTROUTING -s "+mPropeth1ip+"/255.255.255.0 -o eth0 -j MASQUERADE",
        };
        mCMDmap.put(CMD_VD28,mVD28_CommandList);
        Log.i(TAG,"onCreate initCommandList ");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    private Runnable setForward = new Runnable() {
        @Override
        public void run() {
            ShellUtils.CommandResult result;
            String [] CommandList = mCMDmap.get(mPropCmdKey);
            for (int i = 0 ; i < CommandList.length ; i++)
            {
                result = ShellUtils.execCmd(CommandList[i],true);
                Log.i(TAG, CommandList[i]+"\n=====: " + result.toString());
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mForwardONOFF)
        {
            Thread t = new Thread(setForward);
            t.start();
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
