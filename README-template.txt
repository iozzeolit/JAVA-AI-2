Ứng dụng Java-AI
===================

Gói này chứa tất cả các thành phần cần thiết để chạy ứng dụng Java-AI:

- project.jar: Tệp JAR chính của ứng dụng
- mssql-jdbc_auth-12.10.0.x64.dll: DLL xác thực SQL Server
- lib/: Thư mục chứa tất cả các thư viện cần thiết

Để chạy ứng dụng, chỉ cần nhấp đúp vào tập lệnh khởi động phù hợp.

Nếu gặp sự cố kết nối cơ sở dữ liệu:
1. Đảm bảo SQL Server đang chạy và có thể truy cập
2. Kiểm tra rằng config.properties có cài đặt cơ sở dữ liệu chính xác
3. Xác minh rằng tệp DLL nằm trong cùng thư mục với JAR và trong thư mục lib/
4. Có thể cần sao chép tệp mssql-jdbc_auth-12.10.0.x64.dll vào thư mục bin của Java

Khắc phục sự cố kết nối cơ sở dữ liệu:
-----------------------------------
Nếu thấy "No suitable driver found for jdbc:sqlserver:", hãy thử các bước sau:
1. Đảm bảo tệp mssql-jdbc-12.10.0.jre11.jar nằm trong thư mục lib
2. Chuỗi kết nối trong config.properties phải khớp với thiết lập SQL Server của bạn
3. Đối với bảo mật tích hợp, đảm bảo tệp mssql-jdbc_auth-12.10.0.x64.dll có thể truy cập
4. Thử thêm trình điều khiển JDBC vào classpath của hệ thống

Nếu gặp lỗi về mô hình Vosk hoặc nhận dạng giọng nói:
1. Đảm bảo thư mục vosk-models có đầy đủ các mô hình ngôn ngữ (en, vi, ja)
2. Đường dẫn vosk-models phải nằm trong thư mục dist
3. Nếu cần, tải mô hình từ trang chủ Vosk và giải nén vào dist/vosk-models

Nếu gặp lỗi về FFmpeg:
1. Đảm bảo thư mục ffmpeg có đầy đủ các tệp thực thi (ffmpeg.exe, ffplay.exe, ffprobe.exe)
2. Đường dẫn ffmpeg phải nằm trong thư mục dist
3. Nếu cần, chạy install_ffmpeg.bat để tự động tải và cài đặt FFmpeg

Mọi thắc mắc hoặc góp ý, vui lòng liên hệ nhóm phát triển.
