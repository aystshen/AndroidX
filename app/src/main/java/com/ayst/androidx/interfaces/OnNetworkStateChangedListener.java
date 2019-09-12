package com.ayst.androidx.interfaces;

public interface OnNetworkStateChangedListener {
    void onLostConnectivity();

    void onWiFiConnected();

    void onMobileDataConnected();

    void on4GConnected();

    void on3GConnected();

    void on2GConnected();

    void onEthernetConnected();

    void onUnknownConnected();
}
