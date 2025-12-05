# GreenMap Mobile App

Ứng dụng bản đồ xanh giúp người dùng theo dõi môi trường đô thị, khám phá không gian xanh và nhận gợi ý thông minh cho lối sống bền vững. Dự án đang trong quá trình hoàn thiện, nhưng README mô tả đầy đủ tầm nhìn và tính năng dự kiến.

## Tính năng nổi bật
- Bản đồ xanh: lớp phủ MapLibre/MapTiler hiển thị công viên, mặt nước, cây xanh, khu tập luyện ngoài trời, đường đi bộ và các “điểm xanh” gợi ý tập thể dục.
- Thời tiết & lượng mưa: thông tin hiện tại và dự báo (nhiệt độ, UV, mưa, cảm giác thực), cảnh báo mưa lớn hoặc nắng gắt.
- Chất lượng không khí: chỉ số AQI, bụi mịn, khuyến nghị sức khỏe theo từng mức cảnh báo.
- Giao thông & di chuyển xanh: lớp tắc nghẽn giao thông, gợi ý cung đường mát/rợp cây, gợi ý “điểm dừng xanh” trên lộ trình.
- Điểm sạc xe điện & thuê xe đạp: tìm trạm sạc EV, bãi đỗ, trạm thuê xe đạp/xe điện, kiểm tra tình trạng còn trống.
- Địa điểm du lịch & trải nghiệm địa phương: gợi ý không gian xanh, bãi biển, khu sinh thái, hoạt động ngoài trời thân thiện môi trường.
- Tin tức môi trường: bản tin nhanh về thời tiết cực đoan, chất lượng không khí, chiến dịch xanh của địa phương.
- Hồ sơ cá nhân xanh: bảng thành tích đi bộ/đạp xe, huy hiệu thử thách xanh, lưu điểm yêu thích.

### Tính năng AI (định hướng)
- Dự báo thời tiết/ô nhiễm nâng cao theo từng quận/huyện, khung giờ cao điểm.
- Gợi ý lịch tập luyện ngoài trời “an toàn” (nhiệt, mưa, AQI) và nhắc nhở push notification.
- Chấm điểm “độ xanh” cho tuyến đường, đề xuất phương án di chuyển tối ưu (đi bộ/xe đạp/điện).
- Phân tích thói quen di chuyển để gợi ý thử thách xanh hàng tuần.

## Dữ liệu & tích hợp hệ thống
- Hệ sinh thái: Mobile App (ứng dụng người dân) + Backend FastAPI/Orion-LD (dữ liệu NGSI-LD) + Admin Portal (React/Vite) để quản trị và đồng bộ dữ liệu.
- Nguồn dữ liệu backend (tham khảo GreenMap-Backend):
  - GeoJSON: công viên, trạm sạc EV, bãi thuê xe đạp, điểm du lịch, mô phỏng giao thông.
  - Context Broker Orion-LD: `AirQualityObserved`, `WeatherObserved`, `PUBLIC_PARK`… (yêu cầu header `Accept: application/ld+json` và `Link` context).
  - API REST: đăng nhập (`/api/auth/login`), địa điểm (`/api/locations` CRUD), báo cáo cộng đồng (`/api/reports`), tin tức (`/api/news/hanoimoi?limit=20`), traffic (`/api/traffic`), hệ thống (`/api/system/health`).
- Admin Portal (GreenMap-Frontend) giúp: duyệt báo cáo, CRUD hạ tầng xanh (công viên/EV/xe đạp/du lịch), giám sát bản đồ đa lớp AQI/Traffic/Weather, dashboard KPI thời gian thực.

## Công nghệ chính
- Kotlin + Jetpack Compose (Material 3) cho giao diện.
- MapLibre SDK tích hợp MapTiler (cần `MAPTILER_API_KEY`) cho bản đồ.
- Navigation 3 (Compose), Hilt DI, MVI cơ bản cho luồng màn hình & state.
- Retrofit/OkHttp + Gson + kotlinx.serialization cho kết nối mạng; Room + DataStore cho lưu trữ cục bộ.
- Coil (tải ảnh), Lottie (animation), Timber (logging).

## Kiến trúc & cấu trúc thư mục
- Phân tầng data/domain/ui, áp dụng MVI qua `BaseMviViewModel`.
- Di (Hilt) ở `app/src/main/java/com/houhackathon/greenmap_app/di`.
- Điều hướng: `MainActivity` + `navigation/*` sử dụng Navigation 3 Compose.
- Bản đồ: `ui/map/MapScreen.kt` dùng MapLibre, camera giới hạn trong lãnh thổ VN, hỗ trợ theo dõi vị trí người dùng.
- Ví dụ layout:
```
app/
 ├─ src/main/java/com/houhackathon/greenmap_app/
 │   ├─ core/         # network config, datastore, MVI base
 │   ├─ data/         # repository, remote API
 │   ├─ domain/       # model, repository interface, use case
 │   ├─ ui/           # màn hình Compose: home, map, auth, profile, components
 │   ├─ navigation/   # cấu hình Nav3
 │   └─ di/           # Hilt modules
 └─ build.gradle.kts  # khai báo Compose, Hilt, MapLibre, Retrofit...
```

## Thiết lập nhanh
1) Cài đặt:
   - Android Studio Hedgehog+ (AGP 8.9.1), JDK 17, Android SDK 24+.
   - Thiết bị/giả lập có dịch vụ định vị để trải nghiệm bản đồ.
2) Sao chép repo và tạo `local.properties` ở thư mục gốc:
```properties
sdk.dir=/path/to/Android/sdk
MAPTILER_API_KEY=your_maptiler_key
API_BASE_URL=https://api.your-backend.com/
```
3) Build & chạy:
   - Android Studio: mở dự án, sync Gradle, chọn build variant `debug`, Run.
   - CLI: `./gradlew assembleDebug` hoặc `./gradlew installDebug`.

## Cấu hình dữ liệu & API
- `MAPTILER_API_KEY`: khóa MapTiler để tải style bản đồ; nếu bỏ trống sẽ dùng demo style MapLibre.
- `API_BASE_URL`: endpoint backend cho health-check, login, dữ liệu thời tiết/AQI/trạm sạc...
- Quyền cần thiết: `ACCESS_FINE_LOCATION`/`ACCESS_COARSE_LOCATION` để hiển thị vị trí người dùng và gợi ý tuyến đường xanh.
- Tương thích backend mẫu: FastAPI tại `http://127.0.0.1:8000` (có các route `/api/auth/login`, `/api/locations`, `/api/reports`, `/api/news/hanoimoi`, `/ngsi-ld/v1/entities?...`).
- Khi gọi Orion-LD: thêm header `Link: <https://raw.githubusercontent.com/smart-data-models/dataModel.Environment/master/context.jsonld>; rel="http://www.w3.org/ns/ldp#context"; type="application/ld+json"`.

## Kiểm thử & chất lượng
- Unit test: `./gradlew test`.
- Kiến nghị thêm: mock API cho dữ liệu thời tiết/AQI, kiểm thử permission cho MapLibre, và screenshot test cho Compose.

## Lộ trình đề xuất
- Tích hợp nguồn dữ liệu thực cho thời tiết, lượng mưa, AQI, tắc nghẽn, trạm sạc/xe đạp.
- Hoàn thiện AI gợi ý tuyến đường xanh và cảnh báo cá nhân hóa.
- Thêm bộ lọc bản đồ (điểm xanh, công viên, du lịch, sạc/thuê xe).
- Offline-first: cache bản đồ và dữ liệu thời tiết cơ bản.
- Hệ thống huy hiệu/thử thách xanh, chia sẻ cộng đồng.

## Giấy phép
Dự án tuân theo giấy phép trong file `LICENSE`.
