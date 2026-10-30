# 🛡️ FCM Guard — Push Notification Guardian

Dự án này là một dự án cá nhân, được build trong AI Studio. Source dự án có tham khảo và lấy ý tưởng từ repo ReedGAOOO/FCMGuard-HyperOS.

Giải pháp tối ưu hóa và duy trì thông báo đẩy Firebase Cloud Messaging (FCM) dành riêng cho Xiaomi HyperOS / MIUI (kể cả bản nội địa Trung Quốc) và Android — Tiết kiệm pin, không cần Root, thân thiện 100% với ứng dụng ngân hàng.

---

## 🌟 Thông tin phiên bản (Version Info)

- **Phiên bản ứng dụng**: 1.6.2 (Mã phiên bản: Build 9)
- **Tương thích**: Xiaomi HyperOS 1.0/2.0 (China & Global ROM), MIUI 12/13/14, Android 5.0 (minSdk 21) đến Android 15/16.
- **Tập tin cài đặt**: `FCM_Guard.apk` (nằm ngay tại thư mục gốc của dự án `/`).
- **targetSdk**: 22 (Bắt buộc để ghi được key vendor-private `MILLET_NO_RESTRICT_APP` trên HyperOS qua quyền Sửa đổi cài đặt hệ thống).

> ⚠️ **Lưu ý quan trọng khi cài đặt v1.6.2**: Nếu thiết bị của bạn đang cài bản 1.6.1 (targetSdk 35), Android sẽ chặn cập nhật đè do cơ chế hạ cấp quyền (`INSTALL_FAILED_PERMISSION_MODEL_DOWNGRADE -26`). Bạn vui lòng **gỡ bỏ bản cũ (1.6.1)** trước, sau đó cài đặt file `FCM_Guard.apk` của bản 1.6.2.

---

## ⚡ Chi tiết cập nhật v1.6.2: Khôi phục targetSdk 22 & Sửa triệt để lỗi ghi Whitelist

### 1. Quay lại targetSdk 22 để ghi thành công `MILLET_NO_RESTRICT_APP`
- **Nguyên nhân cốt lõi**: Trên targetSdk 35, lời gọi `Settings.System.putString("MILLET_NO_RESTRICT_APP", ...)` bị hệ điều hành Android chặn vì đây là key vendor-private không thuộc danh mục công khai của AOSP, dẫn đến app báo lỗi `KEY_UNSUPPORTED` hoặc `WRITE_FAILED` và GMS không thể thêm vào whitelist.
- **Giải pháp chuẩn từ repo gốc ReedGAOOO/FCMGuard-HyperOS**: Sử dụng `targetSdk = 22` kết hợp `minSdk = 21` để đi qua đường tương thích cũ (legacy target permission model), cho phép quyền "Sửa đổi cài đặt hệ thống" (`WRITE_SETTINGS`) ghi trực tiếp vào `Settings.System.MILLET_NO_RESTRICT_APP`.

### 2. Khắc phục triệt để lỗi dải đen (Letterbox) cuối màn hình của targetSdk 22
- Với targetSdk cũ (22), Android thường áp đặt giới hạn tỷ lệ khung hình màn hình 16:9 hoặc ép hiển thị thanh đen giả lập ở đáy.
- FCM Guard 1.6.2 đã khắc phục hoàn toàn bằng cách cấu hình trực tiếp trong `AndroidManifest.xml`:
  - `android:resizeableActivity="true"` (hỗ trợ hiển thị tỷ lệ tự do không giới hạn).
  - `<meta-data android:name="android.max_aspect_ratio" android:value="3.0" />` (cho phép kéo dài toàn bộ màn hình đến tỷ lệ 3:1 tràn viền).
  - Khôi phục `.windowInsetsPadding(WindowInsets.navigationBars)` tại `NavigationBar` trong `FcmGuardApp.kt` để thanh điều hướng ôm sát cạnh dưới màn hình chuẩn mực.

### 3. Tương thích thư viện với `<uses-sdk tools:overrideLibrary="...">`
- Khôi phục thẻ overrideLibrary trong Manifest cho `androidx.core.ktx` và `androidx.core` để dự án tương thích hoàn hảo giữa `minSdk = 21`, `targetSdk = 22` và bộ công cụ Jetpack Compose hiện đại.

### 4. Thông báo chẩn đoán chính xác hơn
- Khi gặp lỗi `WRITE_FAILED` hoặc `KEY_UNSUPPORTED`, app không còn đưa ra nhận định chung chung "thiết bị không hỗ trợ", mà hướng dẫn rõ: *"Ghi bị từ chối, kiểm tra targetSdk phải là 22 và quyền Sửa đổi cài đặt hệ thống!"*.

### 5. Giám sát thời gian thực (ContentObserver Watchdog)
- HyperOS thường xuyên tự động dựng lại cài đặt `MILLET_NO_RESTRICT_APP` trong nền và xóa GMS ra ngoài.
- `FcmGuardService` đăng ký một `ContentObserver` lắng nghe URI `Settings.System.getUriFor("MILLET_NO_RESTRICT_APP")` với cơ chế debounce ~1s.
- Bất cứ khi nào HyperOS xóa `com.google.android.gms`, FCM Guard sẽ phát hiện ngay lập tức và ghi trả lại GMS vào Whitelist.

### 6. Tối ưu hóa chu kỳ báo thức & Không hao pin khi đang sử dụng máy
- **Hủy báo thức khi mở máy**: Đăng ký `BroadcastReceiver` lắng nghe `ACTION_SCREEN_ON` và `ACTION_USER_PRESENT`. Ngay khi màn hình bật hoặc người dùng mở khóa, toàn bộ chu kỳ báo thức CPU (`AlarmManager`) lập tức bị hủy để tiết kiệm pin tối đa.
- **Chỉ kích hoạt báo thức khi màn hình tắt**: Khi màn hình tắt (`ACTION_SCREEN_OFF`), ứng dụng khởi động chu kỳ báo thức ngắn để đánh thức CPU, kiểm tra whitelist và duy trì socket.
- **Kiểm tra trạng thái thiết bị trong `AlarmReceiver`**: Nếu màn hình đã bật lại trước khi báo thức chạy (`PowerManager.isInteractive == true`), báo thức tự động dừng và không lên lịch lại.

### 7. Giao diện chẩn đoán trung thực (Honest Diagnostics)
- Tách bạch rõ ràng giữa:
  - **Trạng thái Whitelist HyperOS**: Báo cáo chính xác GMS đã có trong `MILLET_NO_RESTRICT_APP` hay chưa (Green/Red), kèm số lượng gói và giá trị chuỗi thực tế.
  - **Kiểm tra mạng tới Google (`mtalk.google.com:5228`)**: Dán nhãn trung thực "Kiểm tra mạng tới Google" — chỉ đo độ trễ DNS và TCP Handshake từ ứng dụng tới server Google Push, không gây hiểu lầm là đã "sửa" FCM thật của GMS.

---

## 🚀 Các tính năng chính (Key Features)

### 1. Thân thiện tuyệt đối với ứng dụng ngân hàng (Finance-App Friendly)
- Sử dụng quyền chuẩn người dùng có thể cấp trực tiếp: Sửa đổi cài đặt hệ thống (Modify system settings).
- **KHÔNG** yêu cầu Root hay Shizuku.
- **KHÔNG** dùng Dịch vụ trợ năng (Accessibility Service) — không lo bị app ngân hàng cảnh báo hoặc chặn.
- **KHÔNG** tạo VPN ngầm, không dùng quyền Quản trị viên thiết bị (Device Admin), không dùng Lớp phủ màn hình (Overlay).

### 2. Giám sát thông minh theo sự kiện & Cơ chế AlarmManager đánh thức Doze
- Tự động phát hiện khi thiết bị kết nối lại Wi‑Fi / 4G / 5G hoặc khi màn hình bật sáng (`ACTION_SCREEN_ON`).
- Khắc phục lỗi tắt màn hình lâu: Tích hợp `AlarmManager` chính xác kết hợp `WakeLock` ngắn định kỳ duy trì kết nối khi màn hình tắt.
- Tùy chọn chu kỳ dự phòng linh hoạt (5, 10, 15 phút khi màn hình tắt; 15, 30, 60 phút khi màn hình sáng).

### 3. Thông báo chạy ngầm tùy chọn (Optional Persistent Notification)
- Cho phép bật/tắt kênh thông báo im lặng (`IMPORTANCE_LOW`, không chuông, không rung) để hệ điều hành HyperOS nhận diện tiến trình ưu tiên.

### 4. Trợ lý ứng dụng FCM & Đọc trạng thái Autostart HyperOS
- Quét danh sách các ứng dụng cài trong máy đang sử dụng FCM/GCM.
- Phân loại rõ ràng thành 3 mục: **Đã bật** (Enabled), **Đã tắt** (Disabled), **Không rõ** (Unknown) với các nút lọc nhanh ngay dưới thanh tìm kiếm.
- Cung cấp nút Cấu hình tất cả trong HyperOS và lối tắt đến cài đặt từng ứng dụng.

### 5. Chẩn đoán kết nối Socket thời gian thực (Push Diagnostics)
- Kiểm tra cổng TCP socket `mtalk.google.com:5228` (thời gian phân giải DNS, IP máy chủ, độ trễ TCP).
- Phím tắt mở màn hình Chẩn đoán Dịch vụ Google Play (`GcmDiagnostics` / `*#*#426#*#*`).

### 6. Nhật ký theo dõi (History Log)
- Tab riêng biệt ghi lại lịch sử kiểm tra whitelist, socket, độ trễ mạng và nhật ký tự động phục hồi.

### 7. Tính năng Xóa dữ liệu Pin & Hiệu suất (`com.miui.powerkeeper`)
- Lối tắt trực tiếp từ Bảng điều khiển đến màn hình Thông tin ứng dụng của Pin & Hiệu suất HyperOS để người dùng bấm "Xóa tất cả dữ liệu" (Clear all data) khi hệ thống tích tụ các giới hạn ngắt tiến trình bất thường.

### 8. Giao diện Đen Trắng tối giản & 11 Ngôn ngữ
- Giao diện Đen Trắng (Monochrome) tối giản, dịu mắt ở cả chế độ Tối (Dark Mode) và chế độ Sáng (Light Mode).
- Hỗ trợ đầy đủ 11 ngôn ngữ: Tiếng Việt, English, 简体中文, 繁體中文, Français, 日本語, 한국어, Español, Português, Deutsch, Русский.

---

## 💡 Hướng dẫn khắc phục khi Facebook Messenger bị trễ thông báo khi tắt màn hình

Trên HyperOS, khi màn hình tắt lâu, hệ thống đưa máy vào chế độ Ngủ sâu và chặn broadcast đánh thức ứng dụng nếu chưa cấu hình đúng. Để Messenger luôn nhận thông báo tức thì:

1. **Cấu hình Tự khởi chạy**: Vào Thông tin ứng dụng Messenger → Bật Tự khởi chạy.
2. **Tiết kiệm pin**: Vào Thông tin ứng dụng Messenger → Tiết kiệm pin → Chọn **Không hạn chế** (No restrictions).
3. **Cài đặt Pin HyperOS**: Mở Bảo mật → Pin → Biểu tượng bánh răng (Cài đặt) → Đặt **Tắt dữ liệu di động khi thiết bị bị khóa** là **Không bao giờ**.
4. **Khóa trong Đa nhiệm**: Mở màn hình Đa nhiệm (Recent apps) → Nhấn giữ ô Messenger → Nhấn vào biểu tượng **Ổ khóa**.
5. **Duy trì trong FCM Guard**: Đảm bảo công tắc **Tự động bảo vệ thông minh** đang **BẬT** và **Whitelist HyperOS (GMS)** hiển thị màu xanh lá **GMS: ĐÃ THÊM**.

---

## 🛠️ Hướng dẫn cài đặt nhanh (4 bước đơn giản)

1. **Cài đặt & Cấp quyền**: Mở FCM Guard, cấp quyền **Sửa đổi cài đặt hệ thống** (WRITE_SETTINGS).
2. **Sửa kết nối & Ghi Whitelist**: Nhấn nút **Sửa lỗi ngay: Thêm GMS vào Whitelist** tại Bảng điều khiển.
3. **Bật Tự động bảo vệ**: Đảm bảo công tắc **Tự động bảo vệ thông minh** đang được bật.
4. **Cấu hình HyperOS**:
   - Nhấn **Cấu hình tự khởi chạy** → Bật tự khởi chạy cho FCM Guard.
   - Nhấn **Cấu hình pin** → Chọn **Không hạn chế** (No restrictions).

---

## 📦 Tải file APK cài đặt

File APK đã được biên dịch sẵn ngay tại thư mục gốc của dự án:

- **Tên file**: `FCM_Guard.apk` (nằm ở thư mục gốc dự án `/`)
- **Tải trực tiếp**: Mở bảng File explorer bên trái → Click chuột phải hoặc nhấn vào dấu 3 chấm ⋮ cạnh file `FCM_Guard.apk` → Chọn **Download**.
- **Hoặc xuất file zip**: Nhấn nút Export (góc trên bên phải) → Chọn **Download as .zip file** để tải về gói chứa sẵn file APK.
