// ILog2fileService.aidl
package com.ayst.androidx;

// Declare any non-default types here with import statements

interface ILog2fileService {
    void openLog2file();
    void closeLog2file();
    boolean isOpen();
}
