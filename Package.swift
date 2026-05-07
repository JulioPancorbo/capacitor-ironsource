// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CapacitorIronsource",
    platforms: [.iOS(.v15)],
    products: [
        .library(
            name: "CapacitorIronsource",
            targets: ["IronSourcePlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "8.0.0")
    ],
    targets: [
        .target(
            name: "IronSourcePlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm"),
                "IronSourceSDK"
            ],
            path: "ios/Sources/IronSourcePlugin",
            linkerSettings: [
                .linkedFramework("AdSupport"),
                .linkedFramework("AudioToolbox"),
                .linkedFramework("AVFoundation"),
                .linkedFramework("CFNetwork"),
                .linkedFramework("CoreGraphics"),
                .linkedFramework("CoreMedia"),
                .linkedFramework("CoreTelephony"),
                .linkedFramework("CoreVideo"),
                .linkedFramework("Foundation"),
                .linkedFramework("MobileCoreServices"),
                .linkedFramework("QuartzCore"),
                .linkedFramework("Security"),
                .linkedFramework("StoreKit"),
                .linkedFramework("SystemConfiguration"),
                .linkedLibrary("z")
            ]),
        .binaryTarget(
            name: "IronSourceSDK",
            url: "https://raw.githubusercontent.com/ironsource-mobile/iOS-sdk/master/8.12.0/IronSource8.12.0.zip",
            checksum: "4f6f0d5b90823c2ca9e845d7241840665a725bdf2e300006879354c67886cfc2"
        ),
        .testTarget(
            name: "IronSourcePluginTests",
            dependencies: ["IronSourcePlugin"],
            path: "ios/Tests/IronSourcePluginTests")
    ]
)