import type { PluginListenerHandle } from '@capacitor/core';

export interface InitializeOptions {
  appKey: string;
  userId?: string;
}

export interface RewardedVideoOptions {
  adUnitId: string;
}

export interface BannerOptions {
  adUnitId: string;
  x: number;
  y: number;
  width: number;
  height: number;
  scale?: number;
  placementName?: string;
}

export interface BannerLayoutOptions {
  x: number;
  y: number;
  width: number;
  height: number;
  scale?: number;
}

export interface InterstitialOptions {
  adUnitId: string;
  placementName?: string;
}

export interface RewardedAvailabilityResult {
  available: boolean;
}

export interface InterstitialAvailabilityResult {
  available: boolean;
}

export interface SetMetaDataOptions {
  key: string;
  value?: string;
  values?: string[];
}

export interface BannerEvent {
  adUnitId?: string;
  adUnitName?: string;
  adFormat?: string;
  adNetwork?: string;
  placementName?: string;
  instanceId?: string;
  instanceName?: string;
  width?: number;
  height?: number;
  errorCode?: number;
  errorMessage?: string;
}

export interface RewardEarnedEvent {
  placement?: string;
  rewardName?: string;
  amount?: number;
  adUnit?: string;
  adNetwork?: string;
  instanceId?: string;
  instanceName?: string;
}

export interface RewardedStatusEvent {
  placement?: string;
  adUnit?: string;
  adUnitId?: string;
  adNetwork?: string;
  instanceId?: string;
  instanceName?: string;
  errorCode?: number;
  errorMessage?: string;
}

export interface InterstitialEvent {
  placement?: string;
  adUnit?: string;
  adUnitId?: string;
  adNetwork?: string;
  instanceId?: string;
  instanceName?: string;
  errorCode?: number;
  errorMessage?: string;
}

export interface IronSourcePlugin {
  initialize(options: InitializeOptions): Promise<void>;
  setMetaData(options: SetMetaDataOptions): Promise<void>;
  launchTestSuite(): Promise<void>;
  loadRewardedVideo(options: RewardedVideoOptions): Promise<void>;
  isRewardedVideoAvailable(): Promise<RewardedAvailabilityResult>;
  showRewardedVideo(): Promise<void>;
  loadInterstitial(options: InterstitialOptions): Promise<void>;
  isInterstitialAvailable(): Promise<InterstitialAvailabilityResult>;
  showInterstitial(): Promise<void>;
  destroyInterstitial(): Promise<void>;
  loadBanner(options: BannerOptions): Promise<void>;
  updateBannerLayout(options: BannerLayoutOptions): Promise<void>;
  showBanner(): Promise<void>;
  hideBanner(): Promise<void>;
  destroyBanner(): Promise<void>;
  addListener(
    eventName: 'rewardEarned',
    listenerFunc: (event: RewardEarnedEvent) => void,
  ): Promise<PluginListenerHandle>;
  addListener(
    eventName: 'rewardedLoaded' | 'rewardedLoadFailed',
    listenerFunc: (event: RewardedStatusEvent) => void,
  ): Promise<PluginListenerHandle>;
  addListener(
    eventName:
      | 'interstitialLoaded'
      | 'interstitialLoadFailed'
      | 'interstitialDisplayed'
      | 'interstitialDisplayFailed'
      | 'interstitialClicked'
      | 'interstitialClosed',
    listenerFunc: (event: InterstitialEvent) => void,
  ): Promise<PluginListenerHandle>;
  addListener(
    eventName: 'bannerLoaded' | 'bannerDisplayed' | 'bannerClicked' | 'bannerLoadFailed',
    listenerFunc: (event: BannerEvent) => void,
  ): Promise<PluginListenerHandle>;
  removeAllListeners(): Promise<void>;
}