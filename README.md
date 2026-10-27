# FCM Guard — Push Notification Guardian

> Dự án này là một dự án cá nhân, được build trong AI Studio. Source dự án có tham khảo và lấy ý tưởng từ repo [ReedGAOOO/FCMGuard-HyperOS](https://github.com/ReedGAOOO/FCMGuard-HyperOS).

FCM Guard là ứng dụng Android giúp duy trì kết nối FCM/Google Push trên thiết bị Xiaomi HyperOS/MIUI, với mục tiêu tối ưu hiệu suất pin, không cần Root, và giảm tình trạng mất thông báo đẩy khi ứng dụng chạy nền.

## Tính năng chính

- Quét và phân loại ứng dụng có sử dụng FCM
- Chạy bảo vệ nền bằng foreground service
- Gửi heartbeat MCS khi mạng hoặc màn hình khởi động lại
- Theo dõi lịch sử kết nối và độ trễ socket
- Hỗ trợ chủ đề tối giản, đa ngôn ngữ và tùy chọn tự động bảo vệ

## Cài đặt

1. Mở project trong Android Studio hoặc AI Studio.
2. Đồng bộ Gradle dependencies.
3. Cấu hình keystore/signing nếu cần build release.
4. Chạy ứng dụng trên thiết bị hoặc build APK.

## Build bằng AI Studio

Dự án này đã được phát triển và build chủ yếu trong AI Studio, nên có thể có một số lưu ý đặc thù liên quan đến:

- signingConfig Android
- environment variables / secret injection
- xử lý file keystore và build release

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## MIT License

The MIT License (MIT)

Copyright (c) 2026 NguyenAnh143

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

## Ghi chú

Dự án này là nguồn mở cho mục đích học tập và nghiên cứu kỹ thuật; mọi đóng góp, chỉnh sửa và tận dụng lại mã nguồn cần tuân thủ điều khoản của MIT License và ghi rõ nguồn gốc tham khảo.
