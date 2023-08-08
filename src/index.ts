import { registerPlugin } from '@capacitor/core';

import type { ZKScannerPlugin } from './definitions';

const ZKScanner = registerPlugin<ZKScannerPlugin>('ZKScanner', {
  web: () => import('./web').then(m => new m.ZKScannerWeb()),
});

export * from './definitions';
export { ZKScanner };
