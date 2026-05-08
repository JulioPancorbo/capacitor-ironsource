package com.juliocodex.capacitor.ironsource;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.getcapacitor.JSObject;
import com.unity3d.mediation.LevelPlay;
import com.unity3d.mediation.LevelPlayAdInfo;
import com.unity3d.mediation.LevelPlayAdError;
import com.unity3d.mediation.LevelPlayAdSize;
import com.unity3d.mediation.LevelPlayConfiguration;
import com.unity3d.mediation.LevelPlayInitError;
import com.unity3d.mediation.LevelPlayInitListener;
import com.unity3d.mediation.LevelPlayInitRequest;
import com.unity3d.mediation.banner.LevelPlayBannerAdView;
import com.unity3d.mediation.banner.LevelPlayBannerAdViewListener;
import com.unity3d.mediation.interstitial.LevelPlayInterstitialAd;
import com.unity3d.mediation.interstitial.LevelPlayInterstitialAdListener;
import com.unity3d.mediation.rewarded.LevelPlayReward;
import com.unity3d.mediation.rewarded.LevelPlayRewardedAd;
import com.unity3d.mediation.rewarded.LevelPlayRewardedAdListener;

import java.util.List;

public class IronSource implements LevelPlayRewardedAdListener {

    public interface InitializationErrorListener {
        void onInitializationFailed(String errorMessage);
    }

    public interface RewardEventListener {
        void onRewardEarned(JSObject payload);
    }

    public interface RewardedStatusListener {
        void onRewardedEvent(String eventName, JSObject payload);
    }

    public interface BannerEventListener {
        void onBannerEvent(String eventName, JSObject payload);
    }

    public interface InterstitialEventListener {
        void onInterstitialEvent(String eventName, JSObject payload);
    }

    private final RewardEventListener rewardEventListener;
    private final RewardedStatusListener rewardedStatusListener;
    private final BannerEventListener bannerEventListener;
    private final InterstitialEventListener interstitialEventListener;
    private volatile boolean initialized;
    private volatile boolean rewardedVideoAvailable;
    private volatile boolean interstitialAvailable;
    private LevelPlayRewardedAd rewardedAd;
    private String rewardedAdUnitId;
    private LevelPlayInterstitialAd interstitialAd;
    private String interstitialAdUnitId;
    private String interstitialPlacementName;
    private LevelPlayBannerAdView bannerAdView;
    private ViewGroup bannerContainer;
    private String bannerAdUnitId;
    private String bannerPlacementName;
    private final LevelPlayBannerAdViewListener bannerAdListener = new LevelPlayBannerAdViewListener() {
        @Override
        public void onAdLoaded(LevelPlayAdInfo adInfo) {
            bannerEventListener.onBannerEvent("bannerLoaded", buildBannerPayload(adInfo));
        }

        @Override
        public void onAdLoadFailed(LevelPlayAdError error) {
            bannerEventListener.onBannerEvent("bannerLoadFailed", buildBannerErrorPayload(null, error));
        }

        @Override
        public void onAdDisplayed(LevelPlayAdInfo adInfo) {
            bannerEventListener.onBannerEvent("bannerDisplayed", buildBannerPayload(adInfo));
        }

        @Override
        public void onAdDisplayFailed(LevelPlayAdInfo adInfo, LevelPlayAdError error) {
            bannerEventListener.onBannerEvent("bannerLoadFailed", buildBannerErrorPayload(adInfo, error));
        }

        @Override
        public void onAdClicked(LevelPlayAdInfo adInfo) {
            bannerEventListener.onBannerEvent("bannerClicked", buildBannerPayload(adInfo));
        }
    };
    private final LevelPlayInterstitialAdListener interstitialAdListener = new LevelPlayInterstitialAdListener() {
        @Override
        public void onAdLoaded(LevelPlayAdInfo adInfo) {
            interstitialAvailable = true;
            interstitialEventListener.onInterstitialEvent("interstitialLoaded", buildInterstitialPayload(adInfo));
        }

        @Override
        public void onAdLoadFailed(LevelPlayAdError error) {
            interstitialAvailable = false;
            interstitialEventListener.onInterstitialEvent("interstitialLoadFailed", buildInterstitialErrorPayload(error, null));
        }

        @Override
        public void onAdDisplayed(LevelPlayAdInfo adInfo) {
            interstitialAvailable = false;
            interstitialEventListener.onInterstitialEvent("interstitialDisplayed", buildInterstitialPayload(adInfo));
        }

        @Override
        public void onAdDisplayFailed(LevelPlayAdError error, LevelPlayAdInfo adInfo) {
            interstitialAvailable = interstitialAd != null && interstitialAd.isAdReady();
            interstitialEventListener.onInterstitialEvent("interstitialDisplayFailed", buildInterstitialErrorPayload(error, adInfo));
        }

        @Override
        public void onAdClicked(LevelPlayAdInfo adInfo) {
            interstitialEventListener.onInterstitialEvent("interstitialClicked", buildInterstitialPayload(adInfo));
        }

        @Override
        public void onAdClosed(LevelPlayAdInfo adInfo) {
            interstitialAvailable = interstitialAd != null && interstitialAd.isAdReady();
            interstitialEventListener.onInterstitialEvent("interstitialClosed", buildInterstitialPayload(adInfo));
        }

        @Override
        public void onAdInfoChanged(LevelPlayAdInfo adInfo) {
        }
    };

    public IronSource(
        RewardEventListener rewardEventListener,
        RewardedStatusListener rewardedStatusListener,
        BannerEventListener bannerEventListener,
        InterstitialEventListener interstitialEventListener
    ) {
        this.rewardEventListener = rewardEventListener;
        this.rewardedStatusListener = rewardedStatusListener;
        this.bannerEventListener = bannerEventListener;
        this.interstitialEventListener = interstitialEventListener;
    }

    public void initialize(
        Activity activity,
        String appKey,
        String userId,
        Runnable onInitializationComplete,
        InitializationErrorListener onInitializationFailed
    ) {
        initialized = false;
        rewardedVideoAvailable = false;
        rewardedAd = null;
        rewardedAdUnitId = null;
        interstitialAvailable = false;
        interstitialAd = null;
        interstitialAdUnitId = null;
        interstitialPlacementName = null;

        LevelPlayInitRequest.Builder requestBuilder = new LevelPlayInitRequest.Builder(appKey);

        if (userId != null && !userId.isEmpty()) {
            requestBuilder.withUserId(userId);
        }

        LevelPlay.init(activity, requestBuilder.build(), new LevelPlayInitListener() {
            @Override
            public void onInitSuccess(LevelPlayConfiguration configuration) {
                initialized = true;
                if (onInitializationComplete != null) {
                    onInitializationComplete.run();
                }
            }

            @Override
            public void onInitFailed(LevelPlayInitError error) {
                initialized = false;
                if (onInitializationFailed != null) {
                    String errorMessage = error != null ? error.getErrorMessage() : "LevelPlay initialization failed.";
                    onInitializationFailed.onInitializationFailed(errorMessage);
                }
            }
        });
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setMetaData(String key, String value) {
        LevelPlay.setMetaData(key, value);
    }

    public void setMetaData(String key, List<String> values) {
        LevelPlay.setMetaData(key, values);
    }

    public void launchTestSuite(Activity activity) {
        LevelPlay.launchTestSuite(activity);
    }

    public void loadRewardedVideo(String adUnitId) {
        if (rewardedAd == null || !adUnitId.equals(rewardedAdUnitId)) {
            rewardedAd = new LevelPlayRewardedAd(adUnitId);
            rewardedAd.setListener(this);
            rewardedAdUnitId = adUnitId;
        }

        rewardedVideoAvailable = rewardedAd.isAdReady();
        rewardedAd.loadAd();
    }

    public boolean isRewardedVideoReady() {
        return rewardedAd != null && rewardedAd.isAdReady();
    }

    public void showRewardedVideo(Activity activity) {
        if (rewardedAd == null) {
            return;
        }

        rewardedAd.showAd(activity);
    }

    public void loadInterstitial(String adUnitId, String placementName) {
        if (interstitialAd == null || !adUnitId.equals(interstitialAdUnitId)) {
            interstitialAd = new LevelPlayInterstitialAd(adUnitId);
            interstitialAd.setListener(interstitialAdListener);
            interstitialAdUnitId = adUnitId;
        }

        interstitialPlacementName = placementName;
        interstitialAvailable = interstitialAd.isAdReady();
        interstitialAd.loadAd();
    }

    public boolean isInterstitialReady() {
        return interstitialAd != null && interstitialAd.isAdReady();
    }

    public void showInterstitial(Activity activity) {
        if (interstitialAd == null) {
            return;
        }

        interstitialAd.showAd(activity);
    }

    public void destroyInterstitial() {
        if (interstitialAd == null) {
            return;
        }

        interstitialAd.setListener(null);
        interstitialAd = null;
        interstitialAvailable = false;
        interstitialAdUnitId = null;
        interstitialPlacementName = null;
    }

    public void loadBanner(Activity activity, View webView, String adUnitId, String placementName, double x, double y, double width, double height, double scale) {
        destroyBanner();

        bannerAdUnitId = adUnitId;
        bannerPlacementName = placementName;

        LevelPlayBannerAdView.Config.Builder configBuilder = new LevelPlayBannerAdView.Config.Builder()
            .setAdSize(LevelPlayAdSize.createAdaptiveAdSize(activity));
        if (!placementName.isEmpty()) {
            configBuilder.setPlacementName(placementName);
        }

        bannerAdView = new LevelPlayBannerAdView(activity, adUnitId, configBuilder.build());
        bannerAdView.setBannerListener(bannerAdListener);
        bannerAdView.setVisibility(View.GONE);

        bannerContainer = activity.findViewById(android.R.id.content);
        attachBannerView();

        updateBannerLayout(webView, x, y, width, height, scale);
        bannerAdView.loadAd();
    }

    public void updateBannerLayout(View webView, double x, double y, double width, double height, double scale) {
        if (bannerAdView == null || webView == null) {
            return;
        }

        float safeScale = scale > 0 ? (float) scale : 1f;
        int left = Math.round((float) x * safeScale) + webView.getLeft();
        int top = Math.round((float) y * safeScale) + webView.getTop();
        int layoutWidth = Math.max(Math.round((float) width * safeScale), 1);
        int layoutHeight = Math.max(Math.round((float) height * safeScale), 1);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(layoutWidth, layoutHeight);
        layoutParams.leftMargin = left;
        layoutParams.topMargin = top;
        bannerAdView.setLayoutParams(layoutParams);
        bannerAdView.requestLayout();
    }

    public void showBanner(Activity activity) {
        if (bannerAdView == null) {
            return;
        }

        if (bannerContainer == null && activity != null) {
            bannerContainer = activity.findViewById(android.R.id.content);
        }

        attachBannerView();
        bannerAdView.setVisibility(View.VISIBLE);
        bannerAdView.bringToFront();
        bannerAdView.requestLayout();
        bannerAdView.invalidate();
    }

    public void hideBanner() {
        if (bannerAdView == null) {
            return;
        }

        bannerAdView.setVisibility(View.GONE);
        detachBannerView();
    }

    public void destroyBanner() {
        if (bannerAdView == null) {
            return;
        }

        detachBannerView();

        bannerAdView.setBannerListener(null);
        bannerAdView.destroy();
        bannerAdView = null;
        bannerContainer = null;
        bannerAdUnitId = null;
        bannerPlacementName = null;
    }

    private void attachBannerView() {
        if (bannerAdView == null || bannerContainer == null) {
            return;
        }

        ViewParent parent = bannerAdView.getParent();
        if (parent == bannerContainer) {
            return;
        }

        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(bannerAdView);
        }

        bannerContainer.addView(bannerAdView);
    }

    private void detachBannerView() {
        if (bannerAdView == null) {
            return;
        }

        ViewParent parent = bannerAdView.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(bannerAdView);
        }
    }

    private JSObject buildBannerPayload(LevelPlayAdInfo adInfo) {
        JSObject payload = new JSObject();

        if (bannerAdView != null) {
            payload.put("width", bannerAdView.getWidth());
            payload.put("height", bannerAdView.getHeight());
        }

        if (adInfo == null) {
            if (bannerAdUnitId != null) {
                payload.put("adUnitId", bannerAdUnitId);
            }
            if (bannerPlacementName != null && !bannerPlacementName.isEmpty()) {
                payload.put("placement", bannerPlacementName);
            }
            return payload;
        }

        if (bannerAdUnitId != null) {
            payload.put("adUnitId", bannerAdUnitId);
        }
        if (bannerPlacementName != null && !bannerPlacementName.isEmpty()) {
            payload.put("placement", bannerPlacementName);
        }
        if (adInfo.getPlacementName() != null && !adInfo.getPlacementName().isEmpty()) {
            payload.put("placement", adInfo.getPlacementName());
        }
        payload.put("adUnit", adInfo.getAdUnitId());
        payload.put("adFormat", "BANNER");
        payload.put("adNetwork", adInfo.getAdNetwork());
        payload.put("instanceId", adInfo.getInstanceId());
        payload.put("instanceName", adInfo.getInstanceName());
        return payload;
    }

    private JSObject buildBannerErrorPayload(LevelPlayAdInfo adInfo, LevelPlayAdError error) {
        JSObject payload = buildBannerPayload(adInfo);
        if (error != null) {
            payload.put("errorCode", error.getErrorCode());
            payload.put("errorMessage", error.getErrorMessage());
        }
        return payload;
    }

    private JSObject buildRewardPayload(LevelPlayReward reward, LevelPlayAdInfo adInfo) {
        JSObject payload = new JSObject();
        payload.put("rewardName", reward.getName());
        payload.put("amount", reward.getAmount());

        if (adInfo != null) {
            payload.put("placement", adInfo.getPlacementName());
            payload.put("adUnit", adInfo.getAdUnitId());
            payload.put("adNetwork", adInfo.getAdNetwork());
            payload.put("instanceId", adInfo.getInstanceId());
            payload.put("instanceName", adInfo.getInstanceName());
        }

        return payload;
    }

    private JSObject buildRewardedPayload(LevelPlayAdInfo adInfo) {
        JSObject payload = new JSObject();

        if (rewardedAdUnitId != null) {
            payload.put("adUnitId", rewardedAdUnitId);
        }

        if (adInfo == null) {
            return payload;
        }

        payload.put("placement", adInfo.getPlacementName());
        payload.put("adUnit", adInfo.getAdUnitId());
        payload.put("adNetwork", adInfo.getAdNetwork());
        payload.put("instanceId", adInfo.getInstanceId());
        payload.put("instanceName", adInfo.getInstanceName());
        return payload;
    }

    private JSObject buildRewardedErrorPayload(LevelPlayAdError error, LevelPlayAdInfo adInfo) {
        JSObject payload = buildRewardedPayload(adInfo);
        if (error != null) {
            payload.put("errorCode", error.getErrorCode());
            payload.put("errorMessage", error.getErrorMessage());
        }
        return payload;
    }

    private JSObject buildInterstitialPayload(LevelPlayAdInfo adInfo) {
        JSObject payload = new JSObject();

        if (interstitialAdUnitId != null) {
            payload.put("adUnitId", interstitialAdUnitId);
        }

        if (interstitialPlacementName != null && !interstitialPlacementName.isEmpty()) {
            payload.put("placement", interstitialPlacementName);
        }

        if (adInfo == null) {
            return payload;
        }

        payload.put("placement", adInfo.getPlacementName());
        payload.put("adUnit", adInfo.getAdUnitId());
        payload.put("adNetwork", adInfo.getAdNetwork());
        payload.put("instanceId", adInfo.getInstanceId());
        payload.put("instanceName", adInfo.getInstanceName());
        return payload;
    }

    private JSObject buildInterstitialErrorPayload(LevelPlayAdError error, LevelPlayAdInfo adInfo) {
        JSObject payload = buildInterstitialPayload(adInfo);
        if (error != null) {
            payload.put("errorCode", error.getErrorCode());
            payload.put("errorMessage", error.getErrorMessage());
        }
        return payload;
    }

    @Override
    public void onAdLoaded(LevelPlayAdInfo adInfo) {
        rewardedVideoAvailable = true;
        rewardedStatusListener.onRewardedEvent("rewardedLoaded", buildRewardedPayload(adInfo));
    }

    @Override
    public void onAdLoadFailed(LevelPlayAdError error) {
        rewardedVideoAvailable = false;
        rewardedStatusListener.onRewardedEvent("rewardedLoadFailed", buildRewardedErrorPayload(error, null));
    }

    @Override
    public void onAdDisplayed(LevelPlayAdInfo adInfo) {
        rewardedVideoAvailable = false;
    }

    @Override
    public void onAdRewarded(LevelPlayReward reward, LevelPlayAdInfo adInfo) {
        rewardEventListener.onRewardEarned(buildRewardPayload(reward, adInfo));
    }

    @Override
    public void onAdDisplayFailed(LevelPlayAdError error, LevelPlayAdInfo adInfo) {
        rewardedVideoAvailable = rewardedAd != null && rewardedAd.isAdReady();
    }

    @Override
    public void onAdClicked(LevelPlayAdInfo adInfo) {
    }

    @Override
    public void onAdClosed(LevelPlayAdInfo adInfo) {
        rewardedVideoAvailable = rewardedAd != null && rewardedAd.isAdReady();
    }

    @Override
    public void onAdInfoChanged(LevelPlayAdInfo adInfo) {
    }

}
