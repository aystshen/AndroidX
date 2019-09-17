package com.ayst.androidx.action;

import android.content.Context;

public abstract class BaseAction implements Runnable {
    protected Context mContext;
    protected boolean mAlive = true;

    public BaseAction(Context context) {
        mContext = context;
    }

    @Override
    public void run() {

    }

    public void stop() {
        Thread.interrupted();
        mAlive = false;
    }

    public abstract void open();
    public abstract void close();
    public abstract boolean isOpen();
}
