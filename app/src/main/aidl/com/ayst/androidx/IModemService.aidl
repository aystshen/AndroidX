// IModemService.aidl
package com.ayst.androidx;

// Declare any non-default types here with import statements

interface IModemService {
    boolean open4gKeepLive();
    boolean close4gKeepLive();
    boolean keepLiveIsOpen();
}
