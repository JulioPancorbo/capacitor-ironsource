import { WebPlugin } from '@capacitor/core';

import type { IronSourcePlugin } from './definitions';

export class IronSourceWeb extends WebPlugin implements IronSourcePlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
