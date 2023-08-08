package com.devari.marzak.plugins.zkscanner.ZKUSBManager;

import android.hardware.usb.UsbDevice;

public interface ZKUSBManagerListener
{
    void onCheckPermission(int result);

    void onUSBArrived(UsbDevice device);

    void onUSBRemoved(UsbDevice device);
}
