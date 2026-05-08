package com.juliocodex.capacitor.ironsource;

import android.app.Activity;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.util.ArrayList;
import java.util.List;

@CapacitorPlugin(name = "IronSource")
public class IronSourcePlugin extends Plugin {

    private static final String ERROR_MISSING_APP_KEY = "IronSource initialization failed: appKey is required.";
    private static final String ERROR_NOT_INITIALIZED = "IronSource SDK is not initialized. Call initialize({ appKey }) first.";
    private static final String ERROR_REWARDED_NOT_READY = "Rewarded video is not ready. Call loadRewardedVideo({ adUnitId }) and wait until an ad is available.";
    private static final String ERROR_REWARDED_AD_UNIT_REQUIRED = "Rewarded load failed: adUnitId is required.";
    private static final String ERROR_INTERSTITIAL_NOT_READY = "Interstitial is not ready. Call loadInterstitial({ adUnitId }) and wait until an ad is available.";
    private static final String ERROR_INTERSTITIAL_AD_UNIT_REQUIRED = "Interstitial load failed: adUnitId is required.";
    private static final String ERROR_ACTIVITY_UNAVAILABLE = "IronSource operation failed: activity is unavailable.";
    private static final String ERROR_BANNER_AD_UNIT_REQUIRED = "Banner load failed: adUnitId is required.";
    private static final String ERROR_METADATA_KEY_REQUIRED = "IronSource setMetaData failed: key is required.";
    private static final String ERROR_METADATA_VALUE_REQUIRED = "IronSource setMetaData failed: provide value or values.";

    private IronSource implementation;

    @Override
    public void load() {
        implementation = new IronSource(this::notifyRewardEarned, this::notifyRewardedEvent, this::notifyBannerEvent, this::notifyInterstitialEvent);
    }

    @Override
    protected void handleOnResume() {
        super.handleOnResume();

        Activity activity = bridge.getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> com.ironsource.mediationsdk.IronSource.onResume(activity));
        }
    }

    @Override
    protected void handleOnPause() {
        Activity activity = bridge.getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> com.ironsource.mediationsdk.IronSource.onPause(activity));
        }

        super.handleOnPause();
    }

    @PluginMethod
    public void initialize(PluginCall call) {
        String appKey = call.getString("appKey", "").trim();
        if (appKey.isEmpty()) {
            call.reject(ERROR_MISSING_APP_KEY);
            return;
        }

        String userId = call.getString("userId", "").trim();

        Activity activity = bridge.getActivity();
        if (activity == null) {
            call.reject(ERROR_ACTIVITY_UNAVAILABLE);
            return;
        }

        activity.runOnUiThread(() -> implementation.initialize(
            activity,
            appKey,
            userId,
            () -> activity.runOnUiThread(call::resolve),
            errorMessage -> activity.runOnUiThread(() -> call.reject(errorMessage))
        ));
    }

    @PluginMethod
    public void setMetaData(PluginCall call) {
        String key = call.getString("key", "").trim();
        if (key.isEmpty()) {
            call.reject(ERROR_METADATA_KEY_REQUIRED);
            return;
        }

        String value = call.getString("value");
        if (value != null) {
            String trimmedValue = value.trim();
            if (!trimmedValue.isEmpty()) {
                implementation.setMetaData(key, trimmedValue);
                call.resolve();
                return;
            }
        }

        List<String> values = normalizeValues(call.getArray("values"));
        if (values.isEmpty()) {
            call.reject(ERROR_METADATA_VALUE_REQUIRED);
            return;
        }

        implementation.setMetaData(key, values);
        call.resolve();
    }

    @PluginMethod
    public void launchTestSuite(PluginCall call) {
        if (implementation == null || !implementation.isInitialized()) {
            call.reject(ERROR_NOT_INITIALIZED);
            return;
        }

        Activity activity = bridge.getActivity();
        if (activity == null) {
            call.reject(ERROR_ACTIVITY_UNAVAILABLE);
            return;
        }

        activity.runOnUiThread(() -> {
            implementation.launchTestSuite(activity);
            call.resolve();
        });
    }

    @PluginMethod
    public void loadRewardedVideo(PluginCall call) {
        if (implementation == null || !implementation.isInitialized()) {
            call.reject(ERROR_NOT_INITIALIZED);
            return;
        }

        String adUnitId = call.getString("adUnitId", "").trim();
        if (adUnitId.isEmpty()) {
            call.reject(ERROR_REWARDED_AD_UNIT_REQUIRED);
            return;
        }

        Activity activity = bridge.getActivity();
        if (activity == null) {
            call.reject(ERROR_ACTIVITY_UNAVAILABLE);
            return;
        }

        activity.runOnUiThread(() -> {
            implementation.loadRewardedVideo(adUnitId);
            call.resolve();
        });
    }

    @PluginMethod
    public void isRewardedVideoAvailable(PluginCall call) {
        if (implementation == null || !implementation.isInitialized()) {
            call.reject(ERROR_NOT_INITIALIZED);
            return;
        }

        JSObject result = new JSObject();
        result.put("available", implementation.isRewardedVideoReady());
        call.resolve(result);
    }

    @PluginMethod
    public void showRewardedVideo(PluginCall call) {
        if (implementation == null || !implementation.isInitialized()) {
            call.reject(ERROR_NOT_INITIALIZED);
            return;
        }

        Activity activity = bridge.getActivity();
        if (activity == null) {
            call.reject(ERROR_ACTIVITY_UNAVAILABLE);
            return;
        }

        activity.runOnUiThread(() -> {
            if (!implementation.isRewardedVideoReady()) {
                call.reject(ERROR_REWARDED_NOT_READY);
                return;
            }

            implementation.showRewardedVideo(activity);
            call.resolve();
        });
    }

    @PluginMethod
    public void loadInterstitial(PluginCall call) {
        if (implementation == null || !implementation.isInitialized()) {
            call.reject(ERROR_NOT_INITIALIZED);
            return;
        }

        String adUnitId = call.getString("adUnitId", "").trim();
        if (adUnitId.isEmpty()) {
            call.reject(ERROR_INTERSTITIAL_AD_UNIT_REQUIRED);
            return;
        }

        Activity activity = bridge.getActivity();
        if (activity == null) {
            call.reject(ERROR_ACTIVITY_UNAVAILABLE);
            return;
        }

        String placementName = call.getString("placementName", "").trim();

        activity.runOnUiThread(() -> {
            implementation.loadInterstitial(adUnitId, placementName);
            call.resolve();
        });
    }

    @PluginMethod
    public void isInterstitialAvailable(PluginCall call) {
        if (implementation == null || !implementation.isInitialized()) {
            call.reject(ERROR_NOT_INITIALIZED);
            return;
        }

        JSObject result = new JSObject();
        result.put("available", implementation.isInterstitialReady());
        call.resolve(result);
    }

    @PluginMethod
    public void showInterstitial(PluginCall call) {
        if (implementation == null || !implementation.isInitialized()) {
            call.reject(ERROR_NOT_INITIALIZED);
            return;
        }

        Activity activity = bridge.getActivity();
        if (activity == null) {
            call.reject(ERROR_ACTIVITY_UNAVAILABLE);
            return;
        }

        activity.runOnUiThread(() -> {
            if (!implementation.isInterstitialReady()) {
                call.reject(ERROR_INTERSTITIAL_NOT_READY);
                return;
            }

            implementation.showInterstitial(activity);
            call.resolve();
        });
    }

    @PluginMethod
    public void destroyInterstitial(PluginCall call) {
        if (implementation == null || !implementation.isInitialized()) {
            call.reject(ERROR_NOT_INITIALIZED);
            return;
        }

        Activity activity = bridge.getActivity();
        if (activity == null) {
            call.reject(ERROR_ACTIVITY_UNAVAILABLE);
            return;
        }

        activity.runOnUiThread(() -> {
            implementation.destroyInterstitial();
            call.resolve();
        });
    }

    @PluginMethod
    public void loadBanner(PluginCall call) {
        if (implementation == null || !implementation.isInitialized()) {
            call.reject(ERROR_NOT_INITIALIZED);
            return;
        }

        String adUnitId = call.getString("adUnitId", "").trim();
        if (adUnitId.isEmpty()) {
            call.reject(ERROR_BANNER_AD_UNIT_REQUIRED);
            return;
        }

        Activity activity = bridge.getActivity();
        if (activity == null) {
            call.reject(ERROR_ACTIVITY_UNAVAILABLE);
            return;
        }

        double x = call.getDouble("x", 0d);
        double y = call.getDouble("y", 0d);
        double width = call.getDouble("width", 0d);
        double height = call.getDouble("height", 0d);
        double scale = call.getDouble("scale", 1d);
        String placementName = call.getString("placementName", "").trim();

        activity.runOnUiThread(() -> {
            implementation.loadBanner(activity, bridge.getWebView(), adUnitId, placementName, x, y, width, height, scale);
            call.resolve();
        });
    }

    @PluginMethod
    public void updateBannerLayout(PluginCall call) {
        if (implementation == null || !implementation.isInitialized()) {
            call.reject(ERROR_NOT_INITIALIZED);
            return;
        }

        Activity activity = bridge.getActivity();
        if (activity == null) {
            call.reject(ERROR_ACTIVITY_UNAVAILABLE);
            return;
        }

        double x = call.getDouble("x", 0d);
        double y = call.getDouble("y", 0d);
        double width = call.getDouble("width", 0d);
        double height = call.getDouble("height", 0d);
        double scale = call.getDouble("scale", 1d);

        activity.runOnUiThread(() -> {
            implementation.updateBannerLayout(bridge.getWebView(), x, y, width, height, scale);
            call.resolve();
        });
    }

    @PluginMethod
    public void showBanner(PluginCall call) {
        if (implementation == null || !implementation.isInitialized()) {
            call.reject(ERROR_NOT_INITIALIZED);
            return;
        }

        Activity activity = bridge.getActivity();
        if (activity == null) {
            call.reject(ERROR_ACTIVITY_UNAVAILABLE);
            return;
        }

        activity.runOnUiThread(() -> {
            implementation.showBanner(activity);
            call.resolve();
        });
    }

    @PluginMethod
    public void hideBanner(PluginCall call) {
        if (implementation == null || !implementation.isInitialized()) {
            call.reject(ERROR_NOT_INITIALIZED);
            return;
        }

        Activity activity = bridge.getActivity();
        if (activity == null) {
            call.reject(ERROR_ACTIVITY_UNAVAILABLE);
            return;
        }

        activity.runOnUiThread(() -> {
            implementation.hideBanner();
            call.resolve();
        });
    }

    @PluginMethod
    public void destroyBanner(PluginCall call) {
        if (implementation == null || !implementation.isInitialized()) {
            call.reject(ERROR_NOT_INITIALIZED);
            return;
        }

        Activity activity = bridge.getActivity();
        if (activity == null) {
            call.reject(ERROR_ACTIVITY_UNAVAILABLE);
            return;
        }

        activity.runOnUiThread(() -> {
            implementation.destroyBanner();
            call.resolve();
        });
    }

    private void notifyRewardEarned(JSObject payload) {
        Activity activity = bridge.getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> notifyListeners("rewardEarned", payload));
            return;
        }

        notifyListeners("rewardEarned", payload);
    }

    private void notifyRewardedEvent(String eventName, JSObject payload) {
        Activity activity = bridge.getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> notifyListeners(eventName, payload));
            return;
        }

        notifyListeners(eventName, payload);
    }

    private void notifyBannerEvent(String eventName, JSObject payload) {
        Activity activity = bridge.getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> notifyListeners(eventName, payload));
            return;
        }

        notifyListeners(eventName, payload);
    }

    private void notifyInterstitialEvent(String eventName, JSObject payload) {
        Activity activity = bridge.getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> notifyListeners(eventName, payload));
            return;
        }

        notifyListeners(eventName, payload);
    }

    private List<String> normalizeValues(JSArray values) {
        List<String> normalizedValues = new ArrayList<>();
        if (values == null) {
            return normalizedValues;
        }

        for (int index = 0; index < values.length(); index += 1) {
            String item = values.optString(index, "").trim();
            if (!item.isEmpty()) {
                normalizedValues.add(item);
            }
        }

        return normalizedValues;
    }
}
