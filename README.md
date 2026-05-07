
# capacitor-ironsource

Capacitor plugin for native ironSource ad monetization.

## Install

To use npm

```bash
npm install capacitor-ironsource
```

To use yarn

```bash
yarn add capacitor-ironsource
```

Sync native files

```bash
npx cap sync
```

## API

<docgen-index>

* [`initialize(...)`](#initialize)
* [`setMetaData(...)`](#setmetadata)
* [`launchTestSuite()`](#launchtestsuite)
* [`loadRewardedVideo(...)`](#loadrewardedvideo)
* [`isRewardedVideoAvailable()`](#isrewardedvideoavailable)
* [`showRewardedVideo()`](#showrewardedvideo)
* [`loadBanner(...)`](#loadbanner)
* [`updateBannerLayout(...)`](#updatebannerlayout)
* [`showBanner()`](#showbanner)
* [`hideBanner()`](#hidebanner)
* [`destroyBanner()`](#destroybanner)
* [`addListener('rewardEarned', ...)`](#addlistenerrewardearned-)
* [`addListener('rewardedLoaded' | 'rewardedLoadFailed', ...)`](#addlistenerrewardedloaded--rewardedloadfailed-)
* [`addListener('bannerLoaded' | 'bannerDisplayed' | 'bannerClicked' | 'bannerLoadFailed', ...)`](#addlistenerbannerloaded--bannerdisplayed--bannerclicked--bannerloadfailed-)
* [`removeAllListeners()`](#removealllisteners)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### initialize(...)

```typescript
initialize(options: InitializeOptions) => any
```

| Param         | Type                                                            |
| ------------- | --------------------------------------------------------------- |
| **`options`** | <code><a href="#initializeoptions">InitializeOptions</a></code> |

**Returns:** <code>any</code>

--------------------


### setMetaData(...)

```typescript
setMetaData(options: SetMetaDataOptions) => any
```

| Param         | Type                                                              |
| ------------- | ----------------------------------------------------------------- |
| **`options`** | <code><a href="#setmetadataoptions">SetMetaDataOptions</a></code> |

**Returns:** <code>any</code>

--------------------


### launchTestSuite()

```typescript
launchTestSuite() => any
```

**Returns:** <code>any</code>

--------------------


### loadRewardedVideo(...)

```typescript
loadRewardedVideo(options: RewardedVideoOptions) => any
```

| Param         | Type                                                                  |
| ------------- | --------------------------------------------------------------------- |
| **`options`** | <code><a href="#rewardedvideooptions">RewardedVideoOptions</a></code> |

**Returns:** <code>any</code>

--------------------


### isRewardedVideoAvailable()

```typescript
isRewardedVideoAvailable() => any
```

**Returns:** <code>any</code>

--------------------


### showRewardedVideo()

```typescript
showRewardedVideo() => any
```

**Returns:** <code>any</code>

--------------------


### loadBanner(...)

```typescript
loadBanner(options: BannerOptions) => any
```

| Param         | Type                                                    |
| ------------- | ------------------------------------------------------- |
| **`options`** | <code><a href="#banneroptions">BannerOptions</a></code> |

**Returns:** <code>any</code>

--------------------


### updateBannerLayout(...)

```typescript
updateBannerLayout(options: BannerLayoutOptions) => any
```

| Param         | Type                                                                |
| ------------- | ------------------------------------------------------------------- |
| **`options`** | <code><a href="#bannerlayoutoptions">BannerLayoutOptions</a></code> |

**Returns:** <code>any</code>

--------------------


### showBanner()

```typescript
showBanner() => any
```

**Returns:** <code>any</code>

--------------------


### hideBanner()

```typescript
hideBanner() => any
```

**Returns:** <code>any</code>

--------------------


### destroyBanner()

```typescript
destroyBanner() => any
```

**Returns:** <code>any</code>

--------------------


### addListener('rewardEarned', ...)

```typescript
addListener(eventName: 'rewardEarned', listenerFunc: (event: RewardEarnedEvent) => void) => any
```

| Param              | Type                                                                                |
| ------------------ | ----------------------------------------------------------------------------------- |
| **`eventName`**    | <code>'rewardEarned'</code>                                                         |
| **`listenerFunc`** | <code>(event: <a href="#rewardearnedevent">RewardEarnedEvent</a>) =&gt; void</code> |

**Returns:** <code>any</code>

--------------------


### addListener('rewardedLoaded' | 'rewardedLoadFailed', ...)

```typescript
addListener(eventName: 'rewardedLoaded' | 'rewardedLoadFailed', listenerFunc: (event: RewardedStatusEvent) => void) => any
```

| Param              | Type                                                                                    |
| ------------------ | --------------------------------------------------------------------------------------- |
| **`eventName`**    | <code>'rewardedLoaded' \| 'rewardedLoadFailed'</code>                                   |
| **`listenerFunc`** | <code>(event: <a href="#rewardedstatusevent">RewardedStatusEvent</a>) =&gt; void</code> |

**Returns:** <code>any</code>

--------------------


### addListener('bannerLoaded' | 'bannerDisplayed' | 'bannerClicked' | 'bannerLoadFailed', ...)

```typescript
addListener(eventName: 'bannerLoaded' | 'bannerDisplayed' | 'bannerClicked' | 'bannerLoadFailed', listenerFunc: (event: BannerEvent) => void) => any
```

| Param              | Type                                                                                      |
| ------------------ | ----------------------------------------------------------------------------------------- |
| **`eventName`**    | <code>'bannerLoaded' \| 'bannerDisplayed' \| 'bannerClicked' \| 'bannerLoadFailed'</code> |
| **`listenerFunc`** | <code>(event: <a href="#bannerevent">BannerEvent</a>) =&gt; void</code>                   |

**Returns:** <code>any</code>

--------------------


### removeAllListeners()

```typescript
removeAllListeners() => any
```

**Returns:** <code>any</code>

--------------------


### Interfaces


#### InitializeOptions

| Prop         | Type                |
| ------------ | ------------------- |
| **`appKey`** | <code>string</code> |
| **`userId`** | <code>string</code> |


#### SetMetaDataOptions

| Prop         | Type                |
| ------------ | ------------------- |
| **`key`**    | <code>string</code> |
| **`value`**  | <code>string</code> |
| **`values`** | <code>{}</code>     |


#### RewardedVideoOptions

| Prop           | Type                |
| -------------- | ------------------- |
| **`adUnitId`** | <code>string</code> |


#### RewardedAvailabilityResult

| Prop            | Type                 |
| --------------- | -------------------- |
| **`available`** | <code>boolean</code> |


#### BannerOptions

| Prop                | Type                |
| ------------------- | ------------------- |
| **`adUnitId`**      | <code>string</code> |
| **`x`**             | <code>number</code> |
| **`y`**             | <code>number</code> |
| **`width`**         | <code>number</code> |
| **`height`**        | <code>number</code> |
| **`scale`**         | <code>number</code> |
| **`placementName`** | <code>string</code> |


#### BannerLayoutOptions

| Prop         | Type                |
| ------------ | ------------------- |
| **`x`**      | <code>number</code> |
| **`y`**      | <code>number</code> |
| **`width`**  | <code>number</code> |
| **`height`** | <code>number</code> |
| **`scale`**  | <code>number</code> |


#### RewardEarnedEvent

| Prop               | Type                |
| ------------------ | ------------------- |
| **`placement`**    | <code>string</code> |
| **`rewardName`**   | <code>string</code> |
| **`amount`**       | <code>number</code> |
| **`adUnit`**       | <code>string</code> |
| **`adNetwork`**    | <code>string</code> |
| **`instanceId`**   | <code>string</code> |
| **`instanceName`** | <code>string</code> |


#### PluginListenerHandle

| Prop         | Type                      |
| ------------ | ------------------------- |
| **`remove`** | <code>() =&gt; any</code> |


#### RewardedStatusEvent

| Prop               | Type                |
| ------------------ | ------------------- |
| **`placement`**    | <code>string</code> |
| **`adUnit`**       | <code>string</code> |
| **`adUnitId`**     | <code>string</code> |
| **`adNetwork`**    | <code>string</code> |
| **`instanceId`**   | <code>string</code> |
| **`instanceName`** | <code>string</code> |
| **`errorCode`**    | <code>number</code> |
| **`errorMessage`** | <code>string</code> |


#### BannerEvent

| Prop                | Type                |
| ------------------- | ------------------- |
| **`adUnitId`**      | <code>string</code> |
| **`adUnitName`**    | <code>string</code> |
| **`adFormat`**      | <code>string</code> |
| **`adNetwork`**     | <code>string</code> |
| **`placementName`** | <code>string</code> |
| **`instanceId`**    | <code>string</code> |
| **`instanceName`**  | <code>string</code> |
| **`width`**         | <code>number</code> |
| **`height`**        | <code>number</code> |
| **`errorCode`**     | <code>number</code> |
| **`errorMessage`**  | <code>string</code> |

</docgen-api>
