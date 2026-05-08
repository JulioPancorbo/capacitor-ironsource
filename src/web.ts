import { WebPlugin } from '@capacitor/core';
import type {
  BannerLayoutOptions,
  BannerOptions,
  InterstitialAvailabilityResult,
  InterstitialOptions,
  IronSourcePlugin,
  RewardedVideoOptions,
  SetMetaDataOptions,
} from './definitions';

export class IronSourceWeb extends WebPlugin implements IronSourcePlugin {
  async initialize(options: { appKey: string }): Promise<void> {
    console.log('ironSource initialize called with appKey:', options.appKey);
  }

  async setMetaData(options: SetMetaDataOptions): Promise<void> {
    console.log('ironSource setMetaData called with options:', options);
  }

  async launchTestSuite(): Promise<void> {
    console.log('ironSource launchTestSuite called');
  }

  async loadRewardedVideo(options: RewardedVideoOptions): Promise<void> {
    console.log('ironSource loadRewardedVideo called with options:', options);
  }

  async isRewardedVideoAvailable(): Promise<{ available: boolean }> {
    return { available: false };
  }

  async showRewardedVideo(): Promise<void> {
    console.log('ironSource showRewardedVideo called');
  }

  async loadInterstitial(options: InterstitialOptions): Promise<void> {
    console.log('ironSource loadInterstitial called with options:', options);
  }

  async isInterstitialAvailable(): Promise<InterstitialAvailabilityResult> {
    return { available: false };
  }

  async showInterstitial(): Promise<void> {
    console.log('ironSource showInterstitial called');
  }

  async destroyInterstitial(): Promise<void> {
    console.log('ironSource destroyInterstitial called');
  }

  async loadBanner(options: BannerOptions): Promise<void> {
    console.log('ironSource loadBanner called with options:', options);
  }

  async updateBannerLayout(options: BannerLayoutOptions): Promise<void> {
    console.log('ironSource updateBannerLayout called with options:', options);
  }

  async showBanner(): Promise<void> {
    console.log('ironSource showBanner called');
  }

  async hideBanner(): Promise<void> {
    console.log('ironSource hideBanner called');
  }

  async destroyBanner(): Promise<void> {
    console.log('ironSource destroyBanner called');
  }
}