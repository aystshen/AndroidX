// IWatchdogService.aidl
package com.ayst.androidx;

// Declare any non-default types here with import statements

interface IWatchdogService {
    boolean openWatchdog();
    boolean closeWatchdog();
    boolean setWatchdogTimeout(int timeout);
    int getWatchdogTimeout();
    boolean watchdogIsOpen();
}
