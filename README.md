Ứng dụng nhận diện giọng nói bằng Java
=================

Một ứng dụng desktop nhận diện giọng nói và xử lý ngôn ngữ sử dụng SQL Server, VOSK và FFmpeg.

---

## Yêu cầu hệ thống

- **Cơ sở dữ liệu:** Microsoft SQL Server
- **Java:** Java 11 trở lên
- **FFmpeg:** ffmpeg.exe, ffplay.exe, ffprobe.exe (xem hướng dẫn bên dưới)
- **Mô hình VOSK:** Tiếng Anh, tiếng Nhật, tiếng Việt

---

## Hướng dẫn cài đặt

### 1. Cấu hình SQL Server
- Đảm bảo SQL Server đã được cài đặt và đang chạy.
- Mở `config.properties` và thiết lập `serverName` (lấy từ giao diện đăng nhập SSMS) và `databaseName` (chưa tồn tại trên server).
- Mở **SQL Server Configuration Manager**:
  - Vào `SQL Server Network Configuration > Protocols for ... > TCP/IP`.
  - Bật TCP/IP, sau đó ở mục `IPAll`, đặt `TCP Port` thành `1433`.
  - Nhấn OK, sau đó khởi động lại dịch vụ SQL Server.

### 2. Chuẩn bị FFmpeg
- Tải gói zip FFmpeg.
- Giải nén và sao chép `ffmpeg.exe`, `ffplay.exe`, `ffprobe.exe` vào thư mục `ffmpeg/` của dự án.

### 3. Chuẩn bị mô hình VOSK
- Tải các mô hình VOSK cho tiếng Anh, Nhật, Việt.
- Giải nén từng mô hình vào các thư mục con tương ứng trong `vosk-models/` (`vosk-models/en/`, `vosk-models/ja/`, `vosk-models/vi/`).

### 4. Chạy ứng dụng
- Nhấp đúp `RunMe.jar` hoặc sử dụng script khởi động (ví dụ: `run.bat`).

---

## Thành phần gói

- `RunMe.jar`: Tệp JAR chính của ứng dụng
- `mssql-jdbc_auth-12.10.0.x64.dll`: Thư viện xác thực SQL Server
- `lib/`: Thư viện cần thiết (bao gồm JDBC và VOSK)

---

## Khắc phục sự cố

### Lỗi kết nối cơ sở dữ liệu
- Đảm bảo SQL Server đang chạy và có thể truy cập.
- Kiểm tra `config.properties` đã đúng thông tin.
- Đảm bảo `mssql-jdbc_auth-12.10.0.x64.dll` nằm cùng thư mục với JAR và trong `lib/`.
- Nếu cần, sao chép DLL vào thư mục `bin` của Java.
- Nếu gặp lỗi `No suitable driver found for jdbc:sqlserver:`:
  - Đảm bảo `mssql-jdbc-12.10.0.jre11.jar` nằm trong `lib/`.
  - Chuỗi kết nối trong `config.properties` phải đúng với cấu hình SQL Server.
  - Nếu dùng bảo mật tích hợp, đảm bảo DLL có thể truy cập.
  - Thử thêm JDBC driver vào classpath hệ thống.

### Lỗi mô hình VOSK hoặc nhận diện giọng nói
- Đảm bảo `vosk-models/` có đủ các mô hình (`en`, `vi`, `ja`).
- Thư mục `vosk-models` phải nằm trong thư mục `dist` nếu chạy từ bản phân phối.
- Nếu cần, tải mô hình từ trang chủ VOSK và giải nén vào `dist/vosk-models`.

### Lỗi FFmpeg
- Đảm bảo `ffmpeg/` có đủ `ffmpeg.exe`, `ffplay.exe`, `ffprobe.exe`.
- Thư mục `ffmpeg` phải nằm trong `dist` nếu chạy từ bản phân phối.
- Nếu cần, chạy `install_ffmpeg.bat` để tự động tải và cài đặt FFmpeg.

---

## Hỗ trợ

Mọi thắc mắc hoặc góp ý, vui lòng liên hệ nhóm phát triển.
