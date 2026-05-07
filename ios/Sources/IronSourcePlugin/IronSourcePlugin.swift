import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(IronSourcePlugin)
public class IronSourcePlugin: CAPPlugin, CAPBridgedPlugin, IronSourceRewardListener, IronSourceBannerListener {
    public let identifier = "IronSourcePlugin"
    public let jsName = "IronSource"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "initialize", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "setMetaData", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "launchTestSuite", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "loadRewardedVideo", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "isRewardedVideoAvailable", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "showRewardedVideo", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "loadBanner", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "updateBannerLayout", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "showBanner", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "hideBanner", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "destroyBanner", returnType: CAPPluginReturnPromise)
    ]
    private static let errorMissingAppKey = "IronSource initialization failed: appKey is required."
    private static let errorNotInitialized = "IronSource SDK is not initialized. Call initialize({ appKey }) first."
    private static let errorRewardedNotReady = "Rewarded video is not ready. Call loadRewardedVideo() and wait until an ad is available."
    private static let errorViewControllerUnavailable = "IronSource operation failed: view controller is unavailable."
    private static let errorBannerAdUnitRequired = "Banner load failed: adUnitId is required."
    private static let errorMetadataKeyRequired = "IronSource setMetaData failed: key is required."
    private static let errorMetadataValueRequired = "IronSource setMetaData failed: provide value or values."

    private var implementation: IronSource?

    public override func load() {
        implementation = IronSource(rewardListener: self, bannerListener: self)
    }

    @objc func initialize(_ call: CAPPluginCall) {
        let appKey = call.getString("appKey")?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        guard !appKey.isEmpty else {
            call.reject(Self.errorMissingAppKey)
            return
        }

        guard let implementation else {
            call.reject(Self.errorViewControllerUnavailable)
            return
        }

        implementation.initialize(appKey: appKey) {
            call.resolve()
        }
    }

    @objc func setMetaData(_ call: CAPPluginCall) {
        let key = call.getString("key")?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        guard !key.isEmpty else {
            call.reject(Self.errorMetadataKeyRequired)
            return
        }

        guard let implementation else {
            call.reject(Self.errorNotInitialized)
            return
        }

        let value = call.getString("value")?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        if !value.isEmpty {
            implementation.setMetaData(key: key, value: value)
            call.resolve()
            return
        }

        let values = (call.getArray("values", ofType: String.self) ?? [])
            .map { $0.trimmingCharacters(in: .whitespacesAndNewlines) }
            .filter { !$0.isEmpty }

        guard !values.isEmpty else {
            call.reject(Self.errorMetadataValueRequired)
            return
        }

        implementation.setMetaData(key: key, values: values)
        call.resolve()
    }

    @objc func launchTestSuite(_ call: CAPPluginCall) {
        guard let implementation, implementation.initialized else {
            call.reject(Self.errorNotInitialized)
            return
        }

        guard let viewController = bridge?.viewController else {
            call.reject(Self.errorViewControllerUnavailable)
            return
        }

        DispatchQueue.main.async {
            implementation.launchTestSuite(from: viewController)
            call.resolve()
        }
    }

    @objc func loadRewardedVideo(_ call: CAPPluginCall) {
        guard let implementation, implementation.initialized else {
            call.reject(Self.errorNotInitialized)
            return
        }

        DispatchQueue.main.async {
            implementation.loadRewardedVideo()
            call.resolve()
        }
    }

    @objc func isRewardedVideoAvailable(_ call: CAPPluginCall) {
        guard let implementation, implementation.initialized else {
            call.reject(Self.errorNotInitialized)
            return
        }

        call.resolve([
            "available": implementation.isRewardedVideoReady()
        ])
    }

    @objc func showRewardedVideo(_ call: CAPPluginCall) {
        guard let implementation, implementation.initialized else {
            call.reject(Self.errorNotInitialized)
            return
        }

        guard let viewController = bridge?.viewController else {
            call.reject(Self.errorViewControllerUnavailable)
            return
        }

        DispatchQueue.main.async {
            guard implementation.isRewardedVideoReady() else {
                call.reject(Self.errorRewardedNotReady)
                return
            }

            implementation.showRewardedVideo(from: viewController)
            call.resolve()
        }
    }

    @objc func loadBanner(_ call: CAPPluginCall) {
        guard let implementation, implementation.initialized else {
            call.reject(Self.errorNotInitialized)
            return
        }

        let adUnitId = call.getString("adUnitId")?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        guard !adUnitId.isEmpty else {
            call.reject(Self.errorBannerAdUnitRequired)
            return
        }

        guard let viewController = bridge?.viewController else {
            call.reject(Self.errorViewControllerUnavailable)
            return
        }

        let x = call.getDouble("x") ?? 0
        let y = call.getDouble("y") ?? 0
        let width = call.getDouble("width") ?? 0
        let height = call.getDouble("height") ?? 0
        let scale = call.getDouble("scale") ?? 1
        let placementName = call.getString("placementName")?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""

        DispatchQueue.main.async {
            implementation.loadBanner(
                adUnitId: adUnitId,
                placementName: placementName,
                in: viewController,
                webView: self.bridge?.webView,
                x: x,
                y: y,
                width: width,
                height: height,
                scale: scale
            )
            call.resolve()
        }
    }

    @objc func updateBannerLayout(_ call: CAPPluginCall) {
        guard let implementation, implementation.initialized else {
            call.reject(Self.errorNotInitialized)
            return
        }

        let x = call.getDouble("x") ?? 0
        let y = call.getDouble("y") ?? 0
        let width = call.getDouble("width") ?? 0
        let height = call.getDouble("height") ?? 0
        let scale = call.getDouble("scale") ?? 1

        DispatchQueue.main.async {
            implementation.updateBannerLayout(
                webView: self.bridge?.webView,
                x: x,
                y: y,
                width: width,
                height: height,
                scale: scale
            )
            call.resolve()
        }
    }

    @objc func showBanner(_ call: CAPPluginCall) {
        guard let implementation, implementation.initialized else {
            call.reject(Self.errorNotInitialized)
            return
        }

        DispatchQueue.main.async {
            implementation.showBanner()
            call.resolve()
        }
    }

    @objc func hideBanner(_ call: CAPPluginCall) {
        guard let implementation, implementation.initialized else {
            call.reject(Self.errorNotInitialized)
            return
        }

        DispatchQueue.main.async {
            implementation.hideBanner()
            call.resolve()
        }
    }

    @objc func destroyBanner(_ call: CAPPluginCall) {
        guard let implementation, implementation.initialized else {
            call.reject(Self.errorNotInitialized)
            return
        }

        DispatchQueue.main.async {
            implementation.destroyBanner()
            call.resolve()
        }
    }

    func onRewardEarned(_ payload: [String: Any]) {
        DispatchQueue.main.async {
            self.notifyListeners("rewardEarned", data: payload)
        }
    }

    func onBannerEvent(_ eventName: String, payload: [String: Any]) {
        DispatchQueue.main.async {
            self.notifyListeners(eventName, data: payload)
        }
    }
}
