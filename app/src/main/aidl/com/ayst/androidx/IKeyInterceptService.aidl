// IKeyInterceptService.aidl
package com.ayst.androidx;

// Declare any non-default types here with import statements

interface IKeyInterceptService {
    void openKeyIntercept();
    void closeKeyIntercept();
    boolean isOpen();
}
