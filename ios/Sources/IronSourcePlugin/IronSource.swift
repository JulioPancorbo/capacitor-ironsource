import Foundation
import UIKit
import IronSource

private protocol IronSourceRewardListener: AnyObject {
    func onRewardEarned(_ payload: [String: Any])
}

private protocol IronSourceBannerListener: AnyObject {
    func onBannerEvent(_ eventName: String, payload: [String: Any])
}

private protocol IronSourceInterstitialListener: AnyObject {
    func onInterstitialEvent(_ eventName: String, payload: [String: Any])
}

private typealias IronSourceSDK = IronSource.IronSource
private typealias IronSourceInitializationDelegate = IronSource.ISInitializationDelegate
private typealias IronSourceRewardedVideoDelegate = IronSource.LevelPlayRewardedVideoDelegate
private typealias IronSourcePlacementInfo = IronSource.ISPlacementInfo
private typealias IronSourceAdInfo = IronSource.ISAdInfo
private typealias IronSourceBannerAdView = IronSource.LPMBannerAdView
private typealias IronSourceBannerAdViewConfigBuilder = IronSource.LPMBannerAdViewConfigBuilder
private typealias IronSourceBannerAdViewDelegate = IronSource.LPMBannerAdViewDelegate
private typealias IronSourceAdSize = IronSource.LPMAdSize
private typealias IronSourceInterstitialAd = IronSource.LPMInterstitialAd
private typealias IronSourceInterstitialAdDelegate = IronSource.LPMInterstitialAdDelegate
private typealias IronSourceInterstitialAdInfo = IronSource.LPMAdInfo

private final class InterstitialDelegateProxy: NSObject, IronSourceInterstitialAdDelegate {
    var onLoaded: ((IronSourceInterstitialAdInfo) -> Void)?
    var onLoadFailed: ((String, NSError) -> Void)?
    var onDisplayed: ((IronSourceInterstitialAdInfo) -> Void)?
    var onDisplayFailed: ((IronSourceInterstitialAdInfo?, NSError) -> Void)?
    var onClicked: ((IronSourceInterstitialAdInfo) -> Void)?
    var onClosed: ((IronSourceInterstitialAdInfo) -> Void)?

    func didLoadAd(withAdInfo adInfo: IronSourceInterstitialAdInfo!) {
        guard let adInfo else {
            return
        }

        onLoaded?(adInfo)
    }

    func didFailToLoadAd(withAdUnitId adUnitId: String!, error: Error!) {
        let nsError = (error as NSError?) ?? NSError(domain: "IronSource", code: -1)
        onLoadFailed?(adUnitId ?? "", nsError)
    }

    func didDisplayAd(withAdInfo adInfo: IronSourceInterstitialAdInfo!) {
        guard let adInfo else {
            return
        }

        onDisplayed?(adInfo)
    }

    func didFailToDisplayAd(withAdInfo adInfo: IronSourceInterstitialAdInfo!, error: Error!) {
        let nsError = (error as NSError?) ?? NSError(domain: "IronSource", code: -1)
        onDisplayFailed?(adInfo, nsError)
    }

    func didClickAd(withAdInfo adInfo: IronSourceInterstitialAdInfo!) {
        guard let adInfo else {
            return
        }

        onClicked?(adInfo)
    }

    func didCloseAd(withAdInfo adInfo: IronSourceInterstitialAdInfo!) {
        guard let adInfo else {
            return
        }

        onClosed?(adInfo)
    }
}

@objc public class IronSource: NSObject, IronSourceInitializationDelegate, IronSourceRewardedVideoDelegate, IronSourceBannerAdViewDelegate {
    private weak var rewardListener: IronSourceRewardListener?
    private weak var bannerListener: IronSourceBannerListener?
    private weak var interstitialListener: IronSourceInterstitialListener?
    private var initializeCompletion: (() -> Void)?
    private var bannerAdView: IronSourceBannerAdView?
    private var bannerAdUnitId: String?
    private var interstitialAd: IronSourceInterstitialAd?
    private var interstitialAdUnitId: String?
    private var interstitialPlacementName: String?
    private let interstitialDelegateProxy = InterstitialDelegateProxy()

    private(set) var initialized = false
    private(set) var rewardedVideoAvailable = false
    private(set) var interstitialAvailable = false

    init(
        rewardListener: IronSourceRewardListener,
        bannerListener: IronSourceBannerListener,
        interstitialListener: IronSourceInterstitialListener
    ) {
        self.rewardListener = rewardListener
        self.bannerListener = bannerListener
        self.interstitialListener = interstitialListener
        super.init()

        interstitialDelegateProxy.onLoaded = { [weak self] adInfo in
            self?.handleInterstitialLoaded(adInfo)
        }
        interstitialDelegateProxy.onLoadFailed = { [weak self] adUnitId, error in
            self?.handleInterstitialLoadFailed(adUnitId: adUnitId, error: error)
        }
        interstitialDelegateProxy.onDisplayed = { [weak self] adInfo in
            self?.handleInterstitialDisplayed(adInfo)
        }
        interstitialDelegateProxy.onDisplayFailed = { [weak self] adInfo, error in
            self?.handleInterstitialDisplayFailed(adInfo: adInfo, error: error)
        }
        interstitialDelegateProxy.onClicked = { [weak self] adInfo in
            self?.handleInterstitialClicked(adInfo)
        }
        interstitialDelegateProxy.onClosed = { [weak self] adInfo in
            self?.handleInterstitialClosed(adInfo)
        }
    }

    func initialize(appKey: String, completion: @escaping () -> Void) {
        initializeCompletion = completion
        initialized = false
        rewardedVideoAvailable = false
        interstitialAvailable = false
        interstitialAd = nil
        interstitialAdUnitId = nil
        interstitialPlacementName = nil

        DispatchQueue.main.async { [weak self] in
            guard let self else {
                return
            }

            IronSourceSDK.setLevelPlayRewardedVideoDelegate(self)
            IronSourceSDK.init(withAppKey: appKey, adUnits: [IS_REWARDED_VIDEO, IS_BANNER], delegate: self)
        }
    }

    func setMetaData(key: String, value: String) {
        DispatchQueue.main.async {
            LevelPlay.setMetaDataWithKey(key, value: value)
        }
    }

    func setMetaData(key: String, values: [String]) {
        DispatchQueue.main.async {
            LevelPlay.setMetaDataWithKey(key, values: NSMutableArray(array: values))
        }
    }

    func launchTestSuite(from viewController: UIViewController) {
        DispatchQueue.main.async {
            LevelPlay.launchTestSuite(viewController)
        }
    }

    func loadRewardedVideo() {
        DispatchQueue.main.async {
            IronSourceSDK.loadRewardedVideo()
        }
    }

    func isRewardedVideoReady() -> Bool {
        rewardedVideoAvailable && IronSourceSDK.hasRewardedVideo()
    }

    func showRewardedVideo(from viewController: UIViewController) {
        DispatchQueue.main.async {
            IronSourceSDK.showRewardedVideo(with: viewController)
        }
    }

    func loadInterstitial(adUnitId: String, placementName: String) {
        destroyInterstitial()

        let interstitialAd = IronSourceInterstitialAd(adUnitId: adUnitId)
        interstitialAd.setDelegate(interstitialDelegateProxy)
        self.interstitialAd = interstitialAd
        interstitialAdUnitId = adUnitId
        interstitialPlacementName = placementName
        interstitialAvailable = interstitialAd.isAdReady()

        DispatchQueue.main.async {
            interstitialAd.loadAd()
        }
    }

    func isInterstitialReady() -> Bool {
        interstitialAvailable && (interstitialAd?.isAdReady() ?? false)
    }

    func showInterstitial(from viewController: UIViewController) {
        guard let interstitialAd else {
            return
        }

        let placementName = interstitialPlacementName?.isEmpty == false ? interstitialPlacementName : nil
        DispatchQueue.main.async {
            interstitialAd.showAd(viewController: viewController, placementName: placementName)
        }
    }

    func destroyInterstitial() {
        interstitialAd?.setDelegate(nil)
        interstitialAd = nil
        interstitialAvailable = false
        interstitialAdUnitId = nil
        interstitialPlacementName = nil
    }

    func loadBanner(
        adUnitId: String,
        placementName: String,
        in viewController: UIViewController,
        webView: UIView?,
        x: Double,
        y: Double,
        width: Double,
        height: Double,
        scale: Double
    ) {
        destroyBanner()

        let adSize = IronSourceAdSize.createAdaptiveAdSize()
        let configBuilder = IronSourceBannerAdViewConfigBuilder()
        configBuilder.setWithAdSize(adSize)
        if !placementName.isEmpty {
            configBuilder.setWithPlacementName(placementName)
        }

        let bannerView = IronSourceBannerAdView(adUnitId: adUnitId, config: configBuilder.build())
        bannerView.delegate = self
        bannerView.isHidden = true
        bannerView.translatesAutoresizingMaskIntoConstraints = true

        viewController.view.addSubview(bannerView)
        bannerAdView = bannerView
        bannerAdUnitId = adUnitId

        updateBannerLayout(webView: webView, x: x, y: y, width: width, height: height, scale: scale)
        bannerView.loadAd(with: viewController)
    }

    func updateBannerLayout(webView: UIView?, x: Double, y: Double, width: Double, height: Double, scale: Double) {
        guard let bannerAdView else {
            return
        }

        let safeScale = scale > 0 ? scale : 1
        let webViewOrigin = webView?.frame.origin ?? .zero
        let frame = CGRect(
            x: webViewOrigin.x + CGFloat(x * safeScale),
            y: webViewOrigin.y + CGFloat(y * safeScale),
            width: max(CGFloat(width * safeScale), 1),
            height: max(CGFloat(height * safeScale), 1)
        )

        bannerAdView.frame = frame
    }

    func showBanner() {
        bannerAdView?.isHidden = false
    }

    func hideBanner() {
        bannerAdView?.isHidden = true
    }

    func destroyBanner() {
        guard let bannerAdView else {
            return
        }

        bannerAdView.removeFromSuperview()
        bannerAdView.destroy()
        self.bannerAdView = nil
        bannerAdUnitId = nil
    }

    private func buildBannerPayload(adInfo: IronSourceAdInfo?) -> [String: Any] {
        var payload: [String: Any] = [:]

        if let bannerAdUnitId {
            payload["adUnitId"] = bannerAdUnitId
        }

        if let frame = bannerAdView?.frame {
            payload["width"] = Int(frame.width)
            payload["height"] = Int(frame.height)
        }

        if let adInfo {
            payload["adUnitId"] = adInfo.adUnit
            payload["adNetwork"] = adInfo.adNetwork
            payload["instanceId"] = adInfo.instanceId
            payload["instanceName"] = adInfo.instanceName
        }

        return payload
    }

    private func buildInterstitialPayload(adInfo: IronSourceInterstitialAdInfo?) -> [String: Any] {
        var payload: [String: Any] = [:]

        if let interstitialAdUnitId {
            payload["adUnitId"] = interstitialAdUnitId
        }

        if let interstitialPlacementName, !interstitialPlacementName.isEmpty {
            payload["placement"] = interstitialPlacementName
        }

        if let adInfo {
            payload["placement"] = adInfo.placementName ?? payload["placement"]
            payload["adUnit"] = adInfo.adUnitId
            payload["adNetwork"] = adInfo.adNetwork
            payload["instanceId"] = adInfo.instanceId
            payload["instanceName"] = adInfo.instanceName
        }

        return payload
    }

    private func buildInterstitialErrorPayload(adInfo: IronSourceInterstitialAdInfo?, error: NSError) -> [String: Any] {
        var payload = buildInterstitialPayload(adInfo: adInfo)
        payload["errorCode"] = error.code
        payload["errorMessage"] = error.localizedDescription
        return payload
    }

    private func handleInterstitialLoaded(_ adInfo: IronSourceInterstitialAdInfo) {
        interstitialAvailable = true
        interstitialListener?.onInterstitialEvent("interstitialLoaded", payload: buildInterstitialPayload(adInfo: adInfo))
    }

    private func handleInterstitialLoadFailed(adUnitId: String, error: NSError) {
        interstitialAvailable = false
        if !adUnitId.isEmpty {
            interstitialAdUnitId = adUnitId
        }
        interstitialListener?.onInterstitialEvent(
            "interstitialLoadFailed",
            payload: buildInterstitialErrorPayload(adInfo: nil, error: error)
        )
    }

    private func handleInterstitialDisplayed(_ adInfo: IronSourceInterstitialAdInfo) {
        interstitialAvailable = false
        interstitialListener?.onInterstitialEvent("interstitialDisplayed", payload: buildInterstitialPayload(adInfo: adInfo))
    }

    private func handleInterstitialDisplayFailed(adInfo: IronSourceInterstitialAdInfo?, error: NSError) {
        interstitialAvailable = interstitialAd?.isAdReady() ?? false
        interstitialListener?.onInterstitialEvent(
            "interstitialDisplayFailed",
            payload: buildInterstitialErrorPayload(adInfo: adInfo, error: error)
        )
    }

    private func handleInterstitialClicked(_ adInfo: IronSourceInterstitialAdInfo) {
        interstitialListener?.onInterstitialEvent("interstitialClicked", payload: buildInterstitialPayload(adInfo: adInfo))
    }

    private func handleInterstitialClosed(_ adInfo: IronSourceInterstitialAdInfo) {
        interstitialAvailable = interstitialAd?.isAdReady() ?? false
        interstitialListener?.onInterstitialEvent("interstitialClosed", payload: buildInterstitialPayload(adInfo: adInfo))
    }

    public func initializationDidComplete() {
        initialized = true

        let completion = initializeCompletion
        initializeCompletion = nil

        DispatchQueue.main.async {
            completion?()
        }
    }

    public func hasAvailableAdWithAdInfo(_ adInfo: IronSourceAdInfo!) {
        rewardedVideoAvailable = true
    }

    public func hasNoAvailableAd() {
        rewardedVideoAvailable = false
    }

    public func didReceiveRewardForPlacement(_ placementInfo: IronSourcePlacementInfo!, withAdInfo adInfo: IronSourceAdInfo!) {
        var payload: [String: Any] = [
            "placement": placementInfo?.placementName ?? "",
            "rewardName": placementInfo?.rewardName ?? "",
            "amount": placementInfo?.rewardAmount?.intValue ?? 0,
        ]

        if let adInfo {
            payload["adUnit"] = adInfo.adUnit
            payload["adNetwork"] = adInfo.adNetwork
            payload["instanceId"] = adInfo.instanceId
            payload["instanceName"] = adInfo.instanceName
        }

        DispatchQueue.main.async { [weak self] in
            self?.rewardListener?.onRewardEarned(payload)
        }
    }

    public func didFailToShowWithError(_ error: Error!, andAdInfo adInfo: IronSourceAdInfo!) {
        rewardedVideoAvailable = IronSourceSDK.hasRewardedVideo()
    }

    public func didOpenWithAdInfo(_ adInfo: IronSourceAdInfo!) {
        rewardedVideoAvailable = false
    }

    public func didClick(_ placementInfo: IronSourcePlacementInfo!, withAdInfo adInfo: IronSourceAdInfo!) {
    }

    public func didCloseWithAdInfo(_ adInfo: IronSourceAdInfo!) {
        rewardedVideoAvailable = IronSourceSDK.hasRewardedVideo()
    }

    public func didLoadAd(withAdInfo adInfo: IronSourceAdInfo!) {
        bannerListener?.onBannerEvent("bannerLoaded", payload: buildBannerPayload(adInfo: adInfo))
    }

    public func didFailToLoadAd(withAdUnitId adUnitId: String!, error: Error!) {
        var payload = buildBannerPayload(adInfo: nil)
        payload["adUnitId"] = adUnitId ?? bannerAdUnitId ?? ""
        payload["errorMessage"] = error.localizedDescription
        bannerListener?.onBannerEvent("bannerLoadFailed", payload: payload)
    }

    public func didClickAd(withAdInfo adInfo: IronSourceAdInfo!) {
        bannerListener?.onBannerEvent("bannerClicked", payload: buildBannerPayload(adInfo: adInfo))
    }

    public func didDisplayAd(withAdInfo adInfo: IronSourceAdInfo!) {
        bannerListener?.onBannerEvent("bannerDisplayed", payload: buildBannerPayload(adInfo: adInfo))
    }

    public func didFailToDisplayAd(withAdInfo adInfo: IronSourceAdInfo!, error: Error!) {
        var payload = buildBannerPayload(adInfo: adInfo)
        payload["errorMessage"] = error.localizedDescription
        bannerListener?.onBannerEvent("bannerLoadFailed", payload: payload)
    }
}
