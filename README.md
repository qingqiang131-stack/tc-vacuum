Warning: fs was declared with const; use let for reassignable variables.
# TTC 真空學習室 iOS

Đây là dự án Xcode cho iPhone, đóng gói file HTML được cung cấp cùng ba PDF. Trang học và tài liệu được lưu trong ứng dụng nên có thể mở ngoại tuyến.

## Xuất IPA từ Windows bằng GitHub Actions

Windows không biên dịch iOS tại chỗ; GitHub Actions sẽ chạy Xcode trên một máy macOS được cấp cho workflow. Trước khi chạy, cần một GitHub repo và signing assets hợp lệ từ tài khoản Apple Developer.

1. Đưa toàn bộ repo này lên GitHub. Workflow nằm ở `.github/workflows/build-ios-ipa.yml`.
2. Trong **Settings → Secrets and variables → Actions**, tạo repository variable `IOS_BUNDLE_ID` với mã duy nhất (ví dụ tên miền bạn sở hữu theo dạng `com.tenban.ttcvacuumstudy`).
3. Tạo các repository secrets sau: `IOS_CERTIFICATE_BASE64` (file `.p12` đã mã hóa Base64), `IOS_CERTIFICATE_PASSWORD`, `IOS_PROVISIONING_PROFILE_BASE64` (file `.mobileprovision` đã mã hóa Base64), `IOS_TEAM_ID`, và `KEYCHAIN_PASSWORD` (mật khẩu bất kỳ mới dùng riêng cho keychain tạm trong CI).
4. Vào **Actions → Build iOS IPA → Run workflow**. Chọn `development` hoặc `ad-hoc` và chạy.
5. Khi workflow hoàn tất, tải artifact `ChanKhong-IPA` từ trang run. Artifact được lưu trong 7 ngày.

Profile và certificate phải khớp với loại phân phối đã chọn, Bundle ID và iPhone đích. Với `ad-hoc`, provisioning profile phải bao gồm UDID của iPhone cần cài. Không gửi certificate/password vào chat; chỉ nhập chúng vào GitHub Secrets.

Nếu có máy Mac, có thể mở `ChanKhong.xcodeproj` trong Xcode, chọn Apple Development Team, rồi chạy trực tiếp lên iPhone hoặc dùng **Product → Archive** để xuất IPA.

## Thành phần

- `ChanKhong/ChanKhongApp.swift`: ứng dụng SwiftUI mở nội dung bằng WKWebView.
- `ChanKhong/WebContent/`: trang HTML gốc và ba PDF.
- `ChanKhong/Assets.xcassets/`: biểu tượng ứng dụng.
