package com.devari.marzak.plugins.zkscanner;

import android.app.Activity;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;



import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.getcapacitor.Plugin;
import com.getcapacitor.JSObject;
import com.getcapacitor.Logger;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;


import com.devari.marzak.plugins.zkscanner.ZKUSBManager.ZKUSBManager;
import com.devari.marzak.plugins.zkscanner.ZKUSBManager.CaptureListener;
import com.devari.marzak.plugins.zkscanner.ZKUSBManager.ZKUSBManagerListener;
import com.devari.marzak.plugins.zkscanner.util.PermissionUtils;
import com.zkteco.android.biometric.FingerprintExceptionListener;
import com.zkteco.android.biometric.core.device.ParameterHelper;
import com.zkteco.android.biometric.core.device.TransportType;
import com.zkteco.android.biometric.core.utils.LogHelper;
import com.zkteco.android.biometric.core.utils.ToolUtils;
import com.zkteco.android.biometric.module.fingerprintreader.FingerprintCaptureListener;
import com.zkteco.android.biometric.module.fingerprintreader.FingerprintSensor;
import com.zkteco.android.biometric.module.fingerprintreader.FingprintFactory;
import com.zkteco.android.biometric.module.fingerprintreader.ZKFingerService;
import com.zkteco.android.biometric.module.fingerprintreader.exception.FingerprintException;


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ZKScanner {

    private CaptureListener listener;
    private static final int ZKTECO_VID =   0x1b55;

    private static final int LIVE20R_PID =   0x0120;
    private static final int LIVE10R_PID =   0x0124;
    private static final String TAG = "MainActivity";
    private final int REQUEST_PERMISSION_CODE = 9;
    private ZKUSBManager zkusbManager = null;
    private FingerprintSensor fingerprintSensor = null;
    private int usb_vid = ZKTECO_VID;
    private int usb_pid = LIVE10R_PID;
    private boolean bStarted = false;
    private int deviceIndex = 0;
    private boolean isReseted = false;
    private String strUid = null;
    private final static int ENROLL_COUNT   =   3;
    private int enroll_index = 0;
    private byte[][] regtemparray = new byte[3][2048];  //register template buffer array
    private boolean bRegister = false;
    private String dbFileName;


    public void setOnCaptureListener(CaptureListener listener)
    {
        this.listener = listener;
    }

    protected FingerprintCaptureListener fingerprintCaptureListener = new FingerprintCaptureListener() {
        @Override
        public void captureOK(byte[] fpImage) {
            final Bitmap bitmap = ToolUtils.renderCroppedGreyScaleBitmap(fpImage, fingerprintSensor.getImageWidth(), fingerprintSensor.getImageHeight());

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream .toByteArray();
            String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

            if(listener != null)
            {
                listener.onCapture(encoded);
            }
        }

        @Override
        public void captureError(FingerprintException e) {
            Logger.debug("captureError");
        }
        @Override
        public void extractOK(byte[] fpTemplate) {
            Logger.debug("extractOK");
        }

        @Override
        public void extractError(int i) {
            Logger.debug("extractError");
        }

    };

    private FingerprintExceptionListener fingerprintExceptionListener = new FingerprintExceptionListener() {
        @Override
        public void onDeviceException() {
            LogHelper.e("usb exception!!!");
            Logger.error("USB exception");
            if (!isReseted) {
                try {
                    fingerprintSensor.openAndReboot(deviceIndex);
                } catch (FingerprintException e) {
                    e.printStackTrace();
                }
                isReseted = true;
            }
        }
    };

    private ZKUSBManagerListener zkusbManagerListener = new ZKUSBManagerListener() {
        @Override
        public void onCheckPermission(int result) {
            afterGetUsbPermission();
        }

        @Override
        public void onUSBArrived(UsbDevice device) {
            if (bStarted)
            {
                closeDevice();
                tryGetUSBPermission();
            }
        }

        @Override
        public void onUSBRemoved(UsbDevice device) {
            LogHelper.d("usb removed!");
            Logger.debug("USB removed");
        }
    };


    // private void checkStoragePermission() {
        //     String[] permission = new String[]{
        //             Manifest.permission.READ_EXTERNAL_STORAGE,
        //             Manifest.permission.WRITE_EXTERNAL_STORAGE
        //     };
        //     ArrayList<String> deniedPermissions = PermissionUtils.checkPermissions(this, permission);
        //     if (deniedPermissions.isEmpty()) {
            //permission all granted
            //         Log.i(TAG, "[checkStoragePermission]: all granted");
            //         Logger.debug("All granted");
            //    } else {
            //        int size = deniedPermissions.size();
            //        String[] deniedPermissionArray = deniedPermissions.toArray(new String[size]);
            //        PermissionUtils.requestPermission(this, deniedPermissionArray, REQUEST_PERMISSION_CODE);
            //    }
        //}


    public void init(Activity activity) {
        //checkStoragePermission();
        Logger.debug("init");
        Log.i(TAG, "init");
        zkusbManager = new ZKUSBManager(activity.getApplicationContext(), zkusbManagerListener);
        zkusbManager.registerUSBPermissionReceiver();
    }

    public void createFingerprintSensor(Activity activity) {
       if (null != fingerprintSensor)
        {
            FingprintFactory.destroy(fingerprintSensor);
            fingerprintSensor = null;
        }
        // Define output log level
        LogHelper.setLevel(Log.VERBOSE);
        LogHelper.setNDKLogLevel(Log.ASSERT);
        // Start fingerprint sensor
        Map deviceParams = new HashMap();
        //set vid
        deviceParams.put(ParameterHelper.PARAM_KEY_VID, usb_vid);
        //set pid
        deviceParams.put(ParameterHelper.PARAM_KEY_PID, usb_pid);
        fingerprintSensor = FingprintFactory.createFingerprintSensor(activity.getApplicationContext(), TransportType.USB, deviceParams); 
    }


    
    protected void captureFingerprint(Activity activity, PluginCall call){
        JSObject object = new JSObject();
        object.put("success", true);
        object.put("error", "captured");
        call.resolve(object);
    }

    protected void openDevice(Activity activity, PluginCall call)
    {
        tryGetUSBPermission();

        zkusbManager = new ZKUSBManager(activity.getApplicationContext(), zkusbManagerListener);
        zkusbManager.registerUSBPermissionReceiver();
        Log.i(TAG, "endregister");
        JSObject object = new JSObject();
        createFingerprintSensor(activity);
        bRegister = false;
        enroll_index = 0;
        isReseted = false;
        try {
            //fingerprintSensor.setCaptureMode(1);
            fingerprintSensor.open(deviceIndex);
            //load all templates form db
            if (1 != 1)
            {
                
            }
            {
                // device parameter
                LogHelper.d("sdk version" + fingerprintSensor.getSDK_Version());
                LogHelper.d("firmware version" + fingerprintSensor.getFirmwareVersion());
                LogHelper.d("serial:" + fingerprintSensor.getStrSerialNumber());
                LogHelper.d("width=" + fingerprintSensor.getImageWidth() + ", height=" + fingerprintSensor.getImageHeight());
            }
            fingerprintSensor.setFingerprintCaptureListener(deviceIndex, fingerprintCaptureListener);
            fingerprintSensor.SetFingerprintExceptionListener(fingerprintExceptionListener);
            fingerprintSensor.startCapture(deviceIndex);
            bStarted = true;

            object.put("success", true);
            object.put("error", "connect success!");
            call.resolve(object);
        } catch (FingerprintException e) {
            e.printStackTrace();
            try {
                fingerprintSensor.openAndReboot(deviceIndex);
            } catch (FingerprintException ex) {
                ex.printStackTrace();
            }

            call.reject("connect failed!");
        }
    }


    private void tryGetUSBPermission() {
        zkusbManager.initUSBPermission(usb_vid, usb_pid);
    }

    private void afterGetUsbPermission()
    {

        //openDevice();
    }

    private void closeDevice()
    {
        if (bStarted)
        {
            try {
                fingerprintSensor.stopCapture(deviceIndex);
                fingerprintSensor.close(deviceIndex);
            } catch (FingerprintException e) {
                e.printStackTrace();
            }
            bStarted = false;
        }
    }

    protected void uninit(PluginCall call) {
        if (bStarted)
        {
            closeDevice();
        }
        zkusbManager.unRegisterUSBPermissionReceiver();
    }


}
