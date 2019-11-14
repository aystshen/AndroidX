package com.ayst.androidx.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.ayst.androidx.IModemService;
import com.ayst.androidx.supply.Modem;
import com.ayst.androidx.interfaces.OnNetworkStateChangedListener;
import com.ayst.androidx.receiver.NetworkReceiver;
import com.ayst.androidx.util.NetworkUtils;
import com.ayst.androidx.util.SPUtils;

public class ModemService extends Service {

    private static final String TAG = "ModemService";

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
    private static final int[] CHECK_PERIOD = {60 * 1000, 5 * 60 * 1000, 30 * 60 * 1000, 60 * 60 * 1000};

    private boolean mAlive = true;
    private boolean isMobileNetwork = true;
    private int mResetModemCnt = 0;
    private Modem mModem;
    private NetworkReceiver mNetworkReceiver;
    private Thread mKeepLiveThread;

    public ModemService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mModem = new Modem(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mService;
    }

    private final IModemService.Stub mService = new IModemService.Stub() {
        @Override
        public boolean open4gKeepLive() throws RemoteException {
            Log.i(TAG, "open4gKeepLive");
            if (!keepLiveIsOpen()) {
                mKeepLiveThread = new Thread(mKeepLiveRunnable);
                mKeepLiveThread.start();

                SPUtils.get(ModemService.this).saveData(SPUtils.KEY_4G_KEEP_LIVE, true);
            }
            return true;
        }

        @Override
        public boolean close4gKeepLive() throws RemoteException {
            Log.i(TAG, "close4gKeepLive");
            if (keepLiveIsOpen()) {
                mAlive = false;
                if (null != mKeepLiveThread) {
                    mKeepLiveThread.interrupt();
                }

                SPUtils.get(ModemService.this).saveData(SPUtils.KEY_4G_KEEP_LIVE, false);
            }
            return true;
        }

        @Override
        public boolean keepLiveIsOpen() throws RemoteException {
            return ModemService.this.keepLiveIsOpen();
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand...");

        if (keepLiveIsOpen()) {
            mKeepLiveThread = new Thread(mKeepLiveRunnable);
            mKeepLiveThread.start();
        } else {
            Log.w(TAG, "onStartCommand, 4g keep live is off.");
        }

        return Service.START_REDELIVER_INTENT;
    }

    private boolean keepLiveIsOpen() {
        return SPUtils.get(this).getData(SPUtils.KEY_4G_KEEP_LIVE, false);
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
        registerReceiver(mNetworkReceiver, filter);
    }

    private void unregisterNetworkReceiver() {
        if (null != mNetworkReceiver) {
            unregisterReceiver(mNetworkReceiver);
        }
    }

    private Runnable mKeepLiveRunnable = new Runnable() {
        @Override
        public void run() {
            mAlive = true;
            registerNetworkReceiver();
            while (mAlive) {
                try {
                    Thread.sleep(CHECK_PERIOD[mResetModemCnt]);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Log.i(TAG, "run...");

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
                        mModem.reset();
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
            unregisterNetworkReceiver();
        }
    };
}
