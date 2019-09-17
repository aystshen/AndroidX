package com.ayst.androidx.action;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.util.Log;

import com.ayst.androidx.helper.Modem;
import com.ayst.androidx.interfaces.OnNetworkStateChangedListener;
import com.ayst.androidx.receiver.NetworkReceiver;
import com.ayst.androidx.util.AppUtil;
import com.ayst.androidx.util.NetworkUtils;
import com.ayst.androidx.util.SPUtils;

public class KeepLive4GAction extends BaseAction {
    private static final String TAG = "KeepLive4GAction";
    private static final String KEY_4G = "4g_keep_live";

    /*
     * 检查网络时重试间隔
     */
    private static final int RETRY_INTERVAL = 10 * 1000; // 10 seconds

    /*
     * 检查网络时重试次数
     */
    private static final int RETRY_MAX = 3;

    /**
     * 每隔多长时间检查一次网络
     * 4G被复位0次：每1分钟检查一次网络
     * 4G被复位1次：每5分钟检查一次网络
     * 4G被复位2次：每30分钟检查一次网络
     * 4G被复位3次：每60分钟检查一次网络
     * 4G被复位4次以上：退出线程
     */
    private static final int[] CHECK_PERIOD = {60*1000, 5*60*1000, 30*60*1000, 60*60*1000};

    private Modem mModem;
    private NetworkReceiver mNetworkReceiver;
    private boolean isMobileNetwork = true;
    private int mResetModemCnt = 0;

    public KeepLive4GAction(Context context) {
        super(context);
        mModem = new Modem(context);
    }

    @Override
    public void run() {
        super.run();
        registerNetworkReceiver();
        while (mAlive) {
            try {
                Thread.sleep(CHECK_PERIOD[mResetModemCnt]);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Log.d(TAG, "run...");

            if (isMobileNetwork) {
                int retry = 0;
                int lostCnt = 0;
                while (retry++ < RETRY_MAX) {
                    boolean available = NetworkUtils.isAvailable();
                    Log.i(TAG, "run, Check the network is "
                            + (available ? "working" : "no working")
                            + ", retry " + retry);
                    if (available) {
                        lostCnt = 0;
                    } else {
                        lostCnt++;
                    }
                    try {
                        Thread.sleep(RETRY_INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (lostCnt >= RETRY_MAX) {
                    resetModem();
                    mResetModemCnt++;
                    Log.i(TAG, "run, Reset modem: " + mResetModemCnt);
                    if (mResetModemCnt >= CHECK_PERIOD.length) {
                        Log.i(TAG, "run, Exit thread because reset too many times.");
                        break;
                    }
                } else {
                    mResetModemCnt = 0;
                }
            } else {
                mResetModemCnt = 0;
            }
        }
    }

    @Override
    public void stop() {
        if (null != mNetworkReceiver) {
            mContext.unregisterReceiver(mNetworkReceiver);
        }
        super.stop();
    }

    @Override
    public void open() {
        SPUtils.get(mContext).saveData(KEY_4G, true);
    }

    @Override
    public void close() {
        SPUtils.get(mContext).saveData(KEY_4G, false);
    }

    @Override
    public boolean isOpen() {
        return SPUtils.get(mContext).getData(KEY_4G, "1".equals(AppUtil.
                getProperty("ro.androidx.4g_keep_live", "0")));
    }

    private void registerNetworkReceiver() {
        mNetworkReceiver = new NetworkReceiver(new OnNetworkStateChangedListener() {
            @Override
            public void onLostConnectivity() {
                Log.d(TAG, "onLostConnectivity");
            }

            @Override
            public void onMobileDataConnected() {
                Log.d(TAG, "onMobileDataConnected");
                isMobileNetwork = true;
            }

            @Override
            public void on4GConnected() {
                Log.d(TAG, "on4GConnected");
                isMobileNetwork = true;
            }

            @Override
            public void on3GConnected() {
                Log.d(TAG, "on3GConnected");
                isMobileNetwork = true;
            }

            @Override
            public void on2GConnected() {
                Log.d(TAG, "on2GConnected");
                isMobileNetwork = true;
            }

            @Override
            public void onWiFiConnected() {
                Log.d(TAG, "onWiFiConnected");
                isMobileNetwork = false;
            }

            @Override
            public void onEthernetConnected() {
                Log.d(TAG, "onEthernetConnected");
                isMobileNetwork = false;
            }

            @Override
            public void onUnknownConnected() {
                Log.d(TAG, "onUnknownConnected");
                isMobileNetwork = false;
            }
        });
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mContext.registerReceiver(mNetworkReceiver, filter);
    }

    private void resetModem() {
        mModem.reset();
    }
}
