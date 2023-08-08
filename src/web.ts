import { WebPlugin } from '@capacitor/core';

import type { ZKScannerPlugin,OpenDeviceResponse,CloseDeviceResponse } from './definitions';

export class ZKScannerWeb extends WebPlugin implements ZKScannerPlugin {

  
  async openDevice(): Promise<OpenDeviceResponse> {
    return { success: true, error: 'Not Implemented in web' };
  }

  async closeDevice(): Promise<CloseDeviceResponse> {
    return {
      success: true,
      error: 'not Implemented in web',
    };
  }

}
