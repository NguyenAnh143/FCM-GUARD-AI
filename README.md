# 🛡️ FCM Guard — Push Notification Guardian (v1.2.0)

> Dự án này là một dự án cá nhân, được build trong AI Studio. Source dự án có tham khảo và lấy ý tưởng từ repo [ReedGAOOO/FCMGuard-HyperOS](https://github.com/ReedGAOOO/FCMGuard-HyperOS).

Giải pháp tối ưu hóa và duy trì thông báo đẩy Firebase Cloud Messaging (FCM) dành riêng cho Xiaomi HyperOS / MIUI và Android — Tiết kiệm pin, không cần Root, thân thiện 100% với ứng dụng ngân hàng.

---

## 📦 File APK Đã Được Build Thành Công (Phiên bản 1.2.0)

File APK cài đặt đã được build thành công và được đặt ngay tại thư mục gốc của dự án:

- Tên file: `FCM_Guard.apk`
- Vị trí: Ngay tại thư mục gốc của dự án (`/FCM_Guard.apk`)
- Phiên bản: `1.2.0 (Build 3)`

### Cách tải

1. Mở thanh danh sách file (**File Explorer**) ở cột bên trái của AI Studio.
2. Tìm file `FCM_Guard.apk` ngay tại thư mục gốc.
3. Nhấn chuột phải hoặc biểu tượng dấu 3 chấm `⋮` bên cạnh `FCM_Guard.apk` và chọn **Download** (Tải về).
4. Hoặc nhấn nút **Export** ở góc trên bên phải → chọn **Download as .zip file**.

---

## 🆕 Điểm mới trong phiên bản 1.2.0 (Build 3)

### 1. 3 Mục nhỏ chọn nhanh loại ứng dụng dưới thanh tìm kiếm (3 Category Selector Tabs)

Thêm 3 ô thẻ chọn nhanh ngay dưới thanh tìm kiếm ứng dụng:

- 🔴 **Đã tắt** (kèm số lượng ứng dụng): Nhấn vào để chỉ hiển thị các ứng dụng chưa bật tự khởi chạy.
- 🟢 **Đã bật** (kèm số lượng ứng dụng): Nhấn vào để chỉ hiển thị các ứng dụng đã cấp quyền tự khởi chạy.
- ⚪ **Không rõ** (kèm số lượng ứng dụng): Nhấn vào để lọc các ứng dụng bảo mật của ROM.

Nhấn chọn ô nào sẽ lọc ngay ứng dụng thuộc loại đó, kèm thanh thông báo trạng thái lọc và nút bấm **Hiện tất cả** tiện lợi.

### 2. Mở rộng chiều cao và độ rộng các nút hành động (No Text Clipping)

- Nút **"Mở trang Thông tin ứng dụng để Xóa dữ liệu"** (Pin & Hiệu suất - `com.miui.powerkeeper`): Nâng chiều cao tối thiểu lên `58.dp`, khoảng cách lề trên dưới `14.dp`, chữ và phụ đề hiển thị hoàn toàn đầy đủ, không bị che chữ xuống dòng.
- Nút **"Chẩn đoán Dịch vụ Google Play (`*#*#426#*#*`)"**: Tăng chiều cao lên `58.dp` với padding `14.dp`, phân chia tiêu đề và mã quay số thành 2 dòng riêng biệt rõ ràng.

### 3. Giao diện Đen Trắng Monochrome Minimalist

Tối ưu cho cả chế độ Sáng và Tối (AMOLED True Black), dịu mắt và trực quan.

---

## 🌟 Giới thiệu tổng quan (Overview)

Trên các thiết bị Xiaomi chạy **HyperOS** hoặc **MIUI**, cơ chế quản lý pin và đa nhiệm nghiêm ngặt thường xuyên tạm ngắt socket kết nối ngầm giữa thiết bị và máy chủ Google (`mtalk.google.com:5228`), dẫn đến hiện tượng:

- Thông báo Zalo, Telegram, WhatsApp, Gmail, Facebook... bị trễ hàng chục phút cho đến khi người dùng mở khóa màn hình hoặc mở ứng dụng.
- Nhiều giải pháp khác đòi hỏi quyền nguy hiểm như **Root**, **Shizuku/ADB qua máy tính**, **Dịch vụ trợ năng (Accessibility)** hoặc tạo kết nối **VPN ảo**, khiến các ứng dụng tài chính và ngân hàng (VCB, Techcombank, MB, Momo, VNPay...) nghi ngờ rủi ro bảo mật và từ chối hoạt động.

**FCM Guard** giải quyết triệt để vấn đề này với triết lý: **Đặc quyền thấp nhất – Độ tin cậy cao nhất – Tuyệt đối an toàn cho tài chính.**

---

## 🚀 Các tính năng chính (Key Features)

### 1. Phân loại 3 nhóm ứng dụng FCM trực quan (Trợ lý ứng dụng)

- Tự động quét toàn bộ ứng dụng cài trong máy có sử dụng FCM push (`com.google.firebase.MESSAGING_EVENT`, `com.google.android.c2dm.intent.RECEIVE`).
- Chia thành 3 mục rõ ràng: **Đã tắt**, **Đã bật**, **Không rõ**.
- Cung cấp nút **Cấu hình tất cả trong HyperOS** và lối tắt đến cài đặt chi tiết của từng ứng dụng.

### 2. Xóa dữ liệu Pin & Hiệu suất (Battery & Performance Reset)

- Tích hợp trực tiếp tại **Bảng điều khiển (Dashboard)**.
- Mở thẳng màn hình **Thông tin ứng dụng** của dịch vụ `com.miui.powerkeeper` để người dùng bấm **Xóa dữ liệu (Clear all data)**.

### 3. Thân thiện tuyệt đối với ứng dụng ngân hàng (Finance-App Friendly)

- Sử dụng quyền chuẩn người dùng có thể cấp trực tiếp: **Sửa đổi cài đặt hệ thống (Modify system settings)**.
- **KHÔNG** yêu cầu Root hay Shizuku.
- **KHÔNG** dùng Dịch vụ trợ năng (Accessibility Service) — không lo bị app ngân hàng cảnh báo hoặc chặn.
- **KHÔNG** tạo VPN ngầm, không dùng quyền **Quản trị viên thiết bị (Device Admin)**, không dùng **Lớp phủ màn hình (Overlay)**.

### 4. Giám sát thông minh theo sự kiện & Siêu tiết kiệm pin (Low Background Power)

- **Cơ chế kích hoạt chính xác (Event-driven)**: Tự động phát hiện khi thiết bị kết nối lại Wi‑Fi / 4G / 5G hoặc khi màn hình bật sáng (`ACTION_SCREEN_ON`).
- **Tùy chọn chu kỳ dự phòng linh hoạt (10, 15, hoặc 30 phút)**: Kiểm tra ngầm dự phòng chạy hoàn toàn nội bộ trong tiến trình (`in-process`), không ép đánh thức CPU (không `WakeLock`), giúp máy duy trì giấc ngủ sâu (Doze Mode) trọn vẹn.
- **Tái kết nối chỉ khi cần**: Gửi broadcast làm mới socket push (`com.google.android.intent.action.MCS_HEARTBEAT` tới GMS) khi mạng vừa phục hồi hoặc khi bạn chủ động bấm sửa lỗi.

### 5. Thông báo chạy ngầm tùy chọn (Optional Persistent Notification)

- Cho phép bật kênh thông báo im lặng (`IMPORTANCE_LOW`, không chuông, không rung) để hệ điều hành HyperOS nhận diện tiến trình ưu tiên, tránh bị hệ thống vô tình thu hồi RAM.

### 6. Chẩn đoán kết nối Socket thời gian thực (Push Diagnostics)

- Kiểm tra trực tiếp cổng TCP socket `mtalk.google.com:5228` (máy chủ Google Push):
  - Thời gian phân giải DNS (DNS latency)
  - Địa chỉ IP máy chủ kết nối
  - Độ trễ bắt tay mạng (TCP handshake latency)
- Phím tắt mở màn hình **Chẩn đoán Dịch vụ Google Play** (`GcmDiagnostics / *#*#426#*#*`).

### 7. Nhật ký theo dõi độ trễ & Mất kết nối (Push Latency & Disconnection History Log)

Tab riêng biệt ghi lại toàn bộ lịch sử:

- Thời điểm socket `mtalk:5228` bị đứt hoặc mất kết nối.
- Thời điểm ứng dụng tự động thực hiện ép gửi broadcast `MCS_HEARTBEAT` thành công.
- Thống kê tổng quan: Tổng sự kiện, số lần mất kết nối, số lần gửi Heartbeat thành công, độ trễ trung bình.

### 8. Giao diện Đa ngôn ngữ (11 ngôn ngữ) & Đen Trắng tối giản

Hỗ trợ đầy đủ:

- Tiếng Việt
- English
- 简体中文
- 繁體中文
- Français
- 日本語
- 한국어
- Español
- Português
- Deutsch
- Русский

3 chế độ:

- **Theo hệ thống (System)**
- **Sáng (Light)**
- **Tối (Dark)**

---

## 🛠️ Hướng dẫn cài đặt nhanh (4 bước đơn giản)

1. **Cài đặt & Cấp quyền**: Mở **FCM Guard**, tại thẻ *Sửa đổi cài đặt hệ thống* nhấn **Cấp quyền** và gạt bật cho ứng dụng.
2. **Sửa kết nối lần đầu**: Nhấn nút **Sửa lỗi ngay (Repair now)** 1 lần để làm mới kênh socket kết nối với Google.
3. **Bật Tự động bảo vệ**: Đảm bảo công tắc **Tự động bảo vệ** đang được bật.
4. **Cấu hình HyperOS**:
   - Nhấn **Cấu hình tự khởi chạy** → Bật tự khởi chạy cho FCM Guard.
   - Nhấn **Cấu hình pin** → Chọn **Không hạn chế (No restrictions)**.
   - (Tùy chọn) Nhấn **Xóa dữ liệu Pin & Hiệu suất** → Bấm **Xóa dữ liệu** để reset giới hạn ngắt tiến trình ngầm của HyperOS.

---

## 🏗️ Công nghệ & Kiến trúc (Tech Stack)

- **Ngôn ngữ**: 100% Kotlin
- **Giao diện**: Jetpack Compose, Material Design 3 (M3) Monochrome
- **Kiến trúc**: MVVM (Model‑View‑ViewModel), Clean Architecture
- **Xử lý bất đồng bộ**: Kotlin Coroutines & StateFlow
- **Lưu trữ dữ liệu**: Room Database & SharedPreferences
- **Tiến trình ngầm**: Foreground Service với `specialUse` type (tương thích Android 14+)
- **Kiểm thử**: Robolectric JVM Testing & Android Unit Tests

---

## 📄 License

Dự án này được phát hành theo giấy phép **MIT License**.

### MIT License

MIT License

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

---

*Phát triển với mục tiêu đem lại trải nghiệm thông báo tức thì, tin cậy và an toàn nhất cho người dùng Android & Xiaomi HyperOS.*
