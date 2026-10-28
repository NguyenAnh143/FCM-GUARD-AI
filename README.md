# 🛡️ FCM Guard — Push Notification Guardian

Dự án này là một dự án cá nhân, được build trong AI Studio. Source dự án có tham khảo và lấy ý tưởng từ repo ReedGAOOO/FCMGuard-HyperOS.

Giải pháp tối ưu hóa và duy trì thông báo đẩy Firebase Cloud Messaging (FCM) dành riêng cho Xiaomi HyperOS / MIUI và Android — Tiết kiệm pin, không cần Root, thân thiện 100% với ứng dụng ngân hàng.

## 🌟 Thông tin phiên bản (Version Info)

- Phiên bản ứng dụng: 1.3.0 (Mã phiên bản: Build 4)
- Tương thích: Xiaomi HyperOS 1.0/2.0, MIUI 12/13/14, Android 7.0 đến Android 15/16.
- Tập tin cài đặt: FCM_Guard.apk (nằm ngay tại thư mục gốc của dự án /).

## 🌟 Giới thiệu tổng quan (Overview)

Trên các thiết bị Xiaomi chạy HyperOS hoặc MIUI, cơ chế quản lý pin và đa nhiệm nghiêm ngặt thường xuyên tạm ngắt socket kết nối ngầm giữa thiết bị và máy chủ Google (mtalk.google.com:5228), dẫn đến hiện tượng:

- Thông báo Facebook Messenger, Zalo, Telegram, WhatsApp, Gmail... bị trễ hàng chục phút khi tắt màn hình lâu, hoặc chỉ nhận được khi mở sáng màn hình hoặc vào ứng dụng.
- Nhiều giải pháp khác đòi hỏi quyền nguy hiểm như Root, Shizuku/ADB qua máy tính, Dịch vụ trợ năng (Accessibility) hoặc tạo kết nối VPN ảo, khiến các ứng dụng tài chính và ngân hàng (VCB, Techcombank, MB, Momo, VNPay...) nghi ngờ rủi ro bảo mật và từ chối hoạt động.

FCM Guard giải quyết triệt để vấn đề này với triết lý: Đặc quyền thấp nhất – Độ tin cậy cao nhất – Tuyệt đối an toàn cho tài chính.

## 🚀 Các tính năng chính (Key Features)

### 1. Thân thiện tuyệt đối với ứng dụng ngân hàng (Finance-App Friendly)
- Sử dụng quyền chuẩn người dùng có thể cấp trực tiếp: Sửa đổi cài đặt hệ thống (Modify system settings).
- KHÔNG yêu cầu Root hay Shizuku.
- KHÔNG dùng Dịch vụ trợ năng (Accessibility Service) — không lo bị app ngân hàng cảnh báo hoặc chặn.
- KHÔNG tạo VPN ngầm, không dùng quyền Quản trị viên thiết bị (Device Admin), không dùng Lớp phủ màn hình (Overlay).

### 2. Giám sát thông minh theo sự kiện & Cơ chế AlarmManager đánh thức Doze
- Cơ chế kích hoạt chính xác (Event-driven): Tự động phát hiện khi thiết bị kết nối lại Wi‑Fi / 4G / 5G hoặc khi màn hình bật sáng (ACTION_SCREEN_ON).
- Khắc phục lỗi tắt màn hình lâu (Screen-Off Doze Wakeup): Tích hợp AlarmManager chính xác kết hợp WakeLock ngắn (vài giây) định kỳ đánh thức socket Google Play Services và làm mới kết nối mtalk.google.com:5228 ngay cả khi thiết bị ngủ sâu hàng giờ liền.
- Tùy chọn chu kỳ dự phòng linh hoạt (10, 15, hoặc 30 phút): Người dùng có thể chủ động chọn chu kỳ kiểm tra ngầm dự phòng (10 phút, 15 phút, hoặc 30 phút) ngay tại thẻ Bảng điều khiển.

### 3. Thông báo chạy ngầm tùy chọn (Optional Persistent Notification)
- Cho phép bật/tắt kênh thông báo im lặng (IMPORTANCE_LOW, không chuông, không rung) để hệ điều hành HyperOS nhận diện tiến trình ưu tiên, tránh bị hệ thống thu hồi RAM.

### 4. Trợ lý ứng dụng FCM & Đọc trạng thái Autostart HyperOS
- Quét danh sách các ứng dụng cài trong máy đang sử dụng FCM/GCM (`com.google.firebase.MESSAGING_EVENT`, `com.google.android.c2dm.intent.RECEIVE`).
- Phân loại rõ ràng thành 3 mục: Đã bật (Enabled), Đã tắt (Disabled), Không rõ (Unknown) với các nút lọc nhanh ngay dưới thanh tìm kiếm.
- Cung cấp nút Cấu hình tất cả trong HyperOS và lối tắt đến cài đặt từng ứng dụng.

### 5. Chẩn đoán kết nối Socket thời gian thực (Push Diagnostics)
- Kiểm tra trực tiếp cổng TCP socket `mtalk.google.com:5228` (máy chủ Google Push):
  - Thời gian phân giải DNS (DNS latency).
  - Địa chỉ IP máy chủ kết nối.
  - Độ trễ bắt tay mạng (TCP handshake latency).
- Phím tắt mở màn hình Chẩn đoán Dịch vụ Google Play (`GcmDiagnostics` / `*#*#426#*#*`) để kiểm tra trạng thái sống của kết nối MCS.

### 6. Nhật ký theo dõi độ trễ & Mất kết nối (Push History Log)
- Tab riêng biệt ghi lại toàn bộ lịch sử:
  - Thời điểm socket `mtalk:5228` bị đứt hoặc mất kết nối.
  - Thời điểm ứng dụng tự động thực hiện ép gửi broadcast `MCS_HEARTBEAT` thành công.
  - Bộ lọc xem linh hoạt và nút xóa sạch nhật ký.

### 7. Tính năng Xóa dữ liệu Pin & Hiệu suất (com.miui.powerkeeper)
- Lối tắt trực tiếp từ Bảng điều khiển đến màn hình Thông tin ứng dụng của Pin & Hiệu suất HyperOS (`com.miui.powerkeeper`) để người dùng bấm "Xóa tất cả dữ liệu" (Clear all data) khi hệ thống tích tụ các giới hạn ngắt tiến trình bất thường.

### 8. Giao diện Đen Trắng tối giản & 11 Ngôn ngữ
- Giao diện Đen Trắng (Monochrome) tối giản, dịu mắt ở cả chế độ Tối (Dark Mode) và chế độ Sáng (Light Mode).
- Hỗ trợ đầy đủ 11 ngôn ngữ: Tiếng Việt, English, 简体中文, 繁體中文, Français, 日本語, 한국어, Español, Português, Deutsch, Русский.

## 💡 Hướng dẫn khắc phục khi Facebook Messenger bị trễ thông báo khi tắt màn hình

Trên HyperOS, khi màn hình tắt lâu, hệ thống thường đưa máy vào chế độ Ngủ sâu và chặn broadcast đánh thức ứng dụng nếu chưa cấu hình đúng. Để Messenger luôn nhận thông báo tức thì:

1. Cấu hình Tự khởi chạy: Vào Thông tin ứng dụng Messenger → Bật Tự khởi chạy (Bắt buộc để HyperOS không chặn Google Play Services đánh thức Messenger).
2. Tiết kiệm pin: Vào Thông tin ứng dụng Messenger → Tiết kiệm pin → Chọn Không hạn chế (No restrictions).
3. Cài đặt Pin HyperOS: Mở Bảo mật → Pin → Biểu tượng bánh răng (Cài đặt) → Đặt Tắt dữ liệu di động khi thiết bị bị khóa là Không bao giờ.
4. Khóa trong Đa nhiệm: Mở màn hình Đa nhiệm (Recent apps) → Nhấn giữ ô Messenger → Nhấn vào biểu tượng Ổ khóa.
5. Duy trì trong FCM Guard: Đảm bảo công tắc Tự động bảo vệ thông minh đang BẬT.

## 🛠️ Hướng dẫn cài đặt nhanh (4 bước đơn giản)

1. Cài đặt & Cấp quyền: Mở FCM Guard, tại thẻ Sửa đổi cài đặt hệ thống nhấn Cấp quyền và gạt bật cho ứng dụng.
2. Sửa kết nối lần đầu: Nhấn nút Sửa lỗi ngay (Repair now) 1 lần để làm mới kênh socket kết nối với Google.
3. Bật Tự động bảo vệ: Đảm bảo công tắc Tự động bảo vệ thông minh đang được bật.
4. Cấu hình HyperOS:
   - Nhấn Cấu hình tự khởi chạy → Bật tự khởi chạy cho FCM Guard.
   - Nhấn Cấu hình pin → Chọn Không hạn chế (No restrictions).

## 📦 Tải file APK cài đặt

File APK đã được biên dịch sẵn ngay tại thư mục gốc của dự án:

- Tên file: FCM_Guard.apk (nằm ở thư mục gốc dự án)
- Tải trực tiếp: Mở bảng File explorer bên trái → Click chuột phải hoặc nhấn vào dấu 3 chấm ⋮ cạnh file FCM_Guard.apk → Chọn Download.
- Hoặc xuất file zip: Nhấn nút Export (góc trên bên phải) → Chọn Download as .zip file để tải về gói chứa sẵn file APK.

## 🛠️ Đã sửa lỗi và cập nhật phiên bản 1.3.0 (Build 4)

### 1. Nguyên nhân & Giải pháp khắc phục lỗi không nhận thông báo Messenger khi tắt màn hình lâu

| Nguyên nhân gây trễ thông báo khi tắt màn hình | Giải pháp đã khắc phục trong FCM Guard v1.3.0 |
|---|---|
| CPU ngủ sâu (Deep Doze Mode): Khi màn hình tắt từ 10–15 phút trở lên, Android và HyperOS đưa CPU vào chế độ ngủ sâu, làm đóng băng các bộ đếm Coroutine (delay). Khi đó, socket kết nối tới máy chủ Google (mtalk.google.com:5228) bị hệ thống ngắt. | Tích hợp FcmAlarmScheduler & AlarmReceiver: Sử dụng `AlarmManager.setExactAndAllowWhileIdle()` kết hợp giữ tạm thời `WakeLock` trong vài giây để đánh thức CPU, làm mới socket và gửi broadcast `MCS_HEARTBEAT` với cờ ưu tiên cao (`FLAG_RECEIVER_FOREGROUND`) ngay cả khi thiết bị tắt màn hình nhiều giờ. |
| HyperOS chặn Google Play Services đánh thức Messenger: Khi có tin nhắn đến máy chủ, Google Play Services gửi broadcast `com.google.android.c2dm.intent.RECEIVE` đến Facebook Messenger (`com.facebook.orca`). Nếu Messenger chưa được bật Tự khởi chạy, hệ thống bảo mật Xiaomi sẽ chặn và hủy broadcast này. | Thêm thẻ chuyên biệt trên Bảng điều khiển: nút bấm 1 chạm mở trực tiếp trang cài đặt của Facebook Messenger để bật Tự khởi chạy và chọn Tiết kiệm pin là Không hạn chế. |
| Tính năng tự ngắt 4G/Wi‑Fi khi khóa máy của HyperOS: Trong ứng dụng Bảo mật của Xiaomi có cài đặt "Tắt dữ liệu di động khi thiết bị bị khóa" (mặc định một số máy để 5 hoặc 10 phút). | Bổ sung lối tắt Cài đặt Pin HyperOS: Thêm nút bấm mở trực tiếp cài đặt Pin hệ thống để người dùng kiểm tra và đảm bảo cài đặt này luôn là "Không bao giờ". |

### 2. Đã nhúng toàn bộ nội dung file README.md vào ứng dụng

Trong mục Cài đặt & Hướng dẫn → nút Xem tài liệu README & hướng dẫn: Toàn bộ nội dung của file README.md đã được đưa vào màn hình xem chi tiết:

- Thông tin phiên bản & file APK cài đặt
- Giới thiệu tổng quan & triết lý bảo mật không Root / không VPN (an toàn 100% với app ngân hàng)
- Chi tiết toàn bộ 8 tính năng chính
- Mẹo khắc phục chuyên sâu cho Messenger, Zalo, Telegram khi tắt màn hình
- Hướng dẫn cài đặt nhanh 4 bước
- Công nghệ & kiến trúc hệ thống

### 3. Cập nhật phiên bản & File APK tại thư mục gốc

- Phiên bản mới: 1.3.0 (Mã phiên bản: Build 4) đã được đồng bộ trong build.gradle.kts, README.md, và giao diện ứng dụng.
- File APK cài đặt: Đã biên dịch thành công và được đặt trực tiếp tại thư mục gốc: `/FCM_Guard.apk`
- Cách tải: Click chuột phải vào file FCM_Guard.apk tại bảng duyệt file bên trái → Chọn Download (hoặc nhấn nút Export ở góc trên bên phải → chọn Download as .zip file).

## 🏗️ Công nghệ & Kiến trúc (Tech Stack)

- Ngôn ngữ: 100% Kotlin
- Giao diện: Jetpack Compose, Material Design 3 (M3) Monochrome
- Kiến trúc: MVVM (Model‑View‑ViewModel), Clean Architecture
- Xử lý bất đồng bộ: Kotlin Coroutines & StateFlow
- Lưu trữ dữ liệu: Room Database & SharedPreferences
- Tiến trình ngầm: Foreground Service với specialUse type (tương thích Android 14+)
- Kiểm thử: Robolectric JVM Testing & Android Unit Tests

## 📄 License

Dự án này được phát hành theo giấy phép MIT License.

MIT License
This project is licensed under the MIT License. See the LICENSE file for details.

MIT License
The MIT License (MIT)

Copyright (c) 2026 NguyenAnh143

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

## Ghi chú

Dự án này là nguồn mở cho mục đích học tập và nghiên cứu kỹ thuật; mọi đóng góp, chỉnh sửa và tận dụng lại mã nguồn cần tuân thủ điều khoản của MIT License và ghi rõ nguồn gốc tham khảo.

Phát triển với mục tiêu đem lại trải nghiệm thông báo tức thì, tin cậy và an toàn nhất cho người dùng Android & Xiaomi HyperOS.
