package com.juliocodex.capacitor.ironsource;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.getcapacitor.JSObject;
import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.IronSourceBannerLayout;
import com.ironsource.mediationsdk.adunit.adapter.utility.AdInfo;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.LevelPlayBannerListener;
import com.unity3d.mediation.LevelPlay;
import com.unity3d.mediation.LevelPlayAdInfo;
import com.unity3d.mediation.LevelPlayAdError;
import com.unity3d.mediation.LevelPlayConfiguration;
import com.unity3d.mediation.LevelPlayInitError;
import com.unity3d.mediation.LevelPlayInitListener;
import com.unity3d.mediation.LevelPlayInitRequest;
import com.unity3d.mediation.rewarded.LevelPlayReward;
import com.unity3d.mediation.rewarded.LevelPlayRewardedAd;
import com.unity3d.mediation.rewarded.LevelPlayRewardedAdListener;

import java.util.Collections;
import java.util.List;

public class IronSource implements LevelPlayRewardedAdListener, LevelPlayBannerListener {

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

    private final RewardEventListener rewardEventListener;
    private final RewardedStatusListener rewardedStatusListener;
    private final BannerEventListener bannerEventListener;
    private volatile boolean initialized;
    private volatile boolean rewardedVideoAvailable;
    private LevelPlayRewardedAd rewardedAd;
    private String rewardedAdUnitId;
    private IronSourceBannerLayout bannerAdView;
    private ViewGroup bannerContainer;
    private String bannerAdUnitId;

    public IronSource(
        RewardEventListener rewardEventListener,
        RewardedStatusListener rewardedStatusListener,
        BannerEventListener bannerEventListener
    ) {
        this.rewardEventListener = rewardEventListener;
        this.rewardedStatusListener = rewardedStatusListener;
        this.bannerEventListener = bannerEventListener;
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

        LevelPlayInitRequest.Builder requestBuilder = new LevelPlayInitRequest.Builder(appKey)
            .withLegacyAdFormats(Collections.singletonList(LevelPlay.AdFormat.BANNER));

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

    public void loadBanner(Activity activity, View webView, String adUnitId, String placementName, double x, double y, double width, double height, double scale) {
        destroyBanner();

        bannerAdUnitId = adUnitId;
        bannerAdView = com.ironsource.mediationsdk.IronSource.createBanner(activity, ISBannerSize.BANNER);
        bannerAdView.setLevelPlayBannerListener(this);
        bannerAdView.setVisibility(View.GONE);

        if (!placementName.isEmpty()) {
            bannerAdView.setPlacementName(placementName);
        }

        bannerContainer = activity.findViewById(android.R.id.content);
        attachBannerView();

        updateBannerLayout(webView, x, y, width, height, scale);
        if (!placementName.isEmpty()) {
            com.ironsource.mediationsdk.IronSource.loadBanner(bannerAdView, placementName);
            return;
        }

        com.ironsource.mediationsdk.IronSource.loadBanner(bannerAdView);
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

        com.ironsource.mediationsdk.IronSource.destroyBanner(bannerAdView);
        bannerAdView = null;
        bannerContainer = null;
        bannerAdUnitId = null;
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

    private JSObject buildBannerPayload(AdInfo adInfo) {
        JSObject payload = new JSObject();

        if (bannerAdView != null) {
            payload.put("width", bannerAdView.getWidth());
            payload.put("height", bannerAdView.getHeight());
        }

        if (adInfo == null) {
            if (bannerAdUnitId != null) {
                payload.put("adUnitId", bannerAdUnitId);
            }
            return payload;
        }

        if (bannerAdUnitId != null) {
            payload.put("adUnitId", bannerAdUnitId);
        }
        payload.put("adFormat", adInfo.getAdUnit());
        payload.put("adNetwork", adInfo.getAdNetwork());
        payload.put("instanceId", adInfo.getInstanceId());
        payload.put("instanceName", adInfo.getInstanceName());
        return payload;
    }

    private JSObject buildBannerErrorPayload(AdInfo adInfo, IronSourceError error) {
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

    @Override
    public void onAdLoaded(AdInfo adInfo) {
        bannerEventListener.onBannerEvent("bannerLoaded", buildBannerPayload(adInfo));
    }

    @Override
    public void onAdLoadFailed(IronSourceError error) {
        bannerEventListener.onBannerEvent("bannerLoadFailed", buildBannerErrorPayload(null, error));
    }

    @Override
    public void onAdClicked(AdInfo adInfo) {
        bannerEventListener.onBannerEvent("bannerClicked", buildBannerPayload(adInfo));
    }

    @Override
    public void onAdLeftApplication(AdInfo adInfo) {
    }

    @Override
    public void onAdScreenPresented(AdInfo adInfo) {
    }

    @Override
    public void onAdScreenDismissed(AdInfo adInfo) {
    }
}
