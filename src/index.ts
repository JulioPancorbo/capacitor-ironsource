import { registerPlugin } from '@capacitor/core';

import type { IronSourcePlugin } from './definitions';

const IronSource = registerPlugin<IronSourcePlugin>('IronSource', {
  web: () => import('./web').then((m) => new m.IronSourceWeb()),
});

export * from './definitions';
export { IronSource };
