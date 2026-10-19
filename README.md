# TTC 真空學習室 iOS

Ứng dụng iPhone dùng SwiftUI và WKWebView để mở trang học cùng ba tài liệu PDF, kể cả khi ngoại tuyến.

## Tạo IPA trên Windows

GitHub Actions chạy Xcode trên máy macOS. Workflow nằm tại `.github/workflows/build-ios-ipa.yml` và có thể chạy trong **Actions → Build iOS IPA → Run workflow** sau khi thiết lập signing.

Trong repository này, ba PDF được lưu thành các phần nhỏ ở `build-assets/pdf-parts/`. Workflow ghép lại các phần trước khi build; IPA cuối cùng chứa các PDF đầy đủ.

Cần tạo repository variable `IOS_BUNDLE_ID` và secrets `IOS_CERTIFICATE_BASE64`, `IOS_CERTIFICATE_PASSWORD`, `IOS_PROVISIONING_PROFILE_BASE64`, `IOS_TEAM_ID`, `KEYCHAIN_PASSWORD`. Certificate, provisioning profile, Bundle ID và thiết bị phải khớp với kiểu phân phối đã chọn. Với `ad-hoc`, provisioning profile phải chứa UDID iPhone cần cài. Không gửi certificate hoặc mật khẩu vào chat; nhập chúng trực tiếp vào GitHub Secrets.

Xem [hướng dẫn đầy đủ](ChanKhongIOS/README.md).