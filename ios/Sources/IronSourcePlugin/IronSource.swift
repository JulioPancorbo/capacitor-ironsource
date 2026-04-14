import Foundation

@objc public class IronSource: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
