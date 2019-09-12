package com.ayst.androidx.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import com.ayst.androidx.interfaces.OnNetworkStateChangedListener;
import com.ayst.androidx.util.NetworkUtils;

public class NetworkReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkReceiver";

    private OnNetworkStateChangedListener mListener;

    public NetworkReceiver(OnNetworkStateChangedListener listener) {
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "action: " + action);

        switch (action) {
            case ConnectivityManager.CONNECTIVITY_ACTION:
                boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
                if (noConnectivity) {
                    if (mListener != null) {
                        mListener.onLostConnectivity();
                    }
                } else {
                    if (NetworkUtils.isMobileData()) {
                        if (mListener != null) {
                            mListener.onMobileDataConnected();
                        }
                    } else if (NetworkUtils.isWifiConnected()) {
                        if (mListener != null) {
                            mListener.onWiFiConnected();
                        }
                    } else if (NetworkUtils.isEthernet()) {
                        if (mListener != null) {
                            mListener.onEthernetConnected();
                        }
                    } else {
                        if (mListener != null) {
                            mListener.onUnknownConnected();
                        }
                    }
                }
                break;
        }
    }
}
