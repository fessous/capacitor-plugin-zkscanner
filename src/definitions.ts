import type { PluginListenerHandle } from '@capacitor/core';

export interface ZKScannerPlugin {
  openDevice(): Promise<OpenDeviceResponse>;
  closeDevice(): Promise<CloseDeviceResponse>;

  addListener(
    eventName: 'CaptureEvent',
    listenerFunc: (data: { data: any }) => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
}


export interface OpenDeviceResponse {
  success: boolean;
  error?: string;
}

export interface CloseDeviceResponse {
  success: boolean;
  error?: string;
}

