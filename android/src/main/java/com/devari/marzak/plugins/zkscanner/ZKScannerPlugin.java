package com.devari.marzak.plugins.zkscanner;

import com.devari.marzak.plugins.zkscanner.ZKUSBManager.CaptureListener;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "ZKScanner")
public class ZKScannerPlugin extends Plugin {

    private ZKScanner implementation = new ZKScanner();


    @Override
    public void load() {
        super.load();
        implementation.init(this.getActivity());

    }


    @PluginMethod
    public void openDevice(PluginCall call) {
        implementation.setOnCaptureListener(new CaptureListener()
        {
            @Override
            public void onCapture(String base64)
            {
                notifyListeners("CaptureEvent",new JSObject().put("data", "data:image/png;base64," +base64) );
            }
        });

        implementation.openDevice(this.getActivity(), call);
    }


    @PluginMethod
    public void closeDevice(PluginCall call) {
        implementation.uninit(call);
    }



}
