import { WebPlugin } from '@capacitor/core';
import type { IronSourcePlugin } from './definitions';

export class IronSourceWeb extends WebPlugin implements IronSourcePlugin {
  async initialize(options: { appKey: string }): Promise<void> {
    console.log('ironSource initialize called with appKey:', options.appKey);
  }

  async showRewardedVideo(): Promise<void> {
    console.log('ironSource showRewardedVideo called');
  }

  async isRewardedVideoAvailable(): Promise<{ available: boolean }> {
    console.log('ironSource isRewardedVideoAvailable called');
    return { available: false };
  }
}