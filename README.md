# AITASKER Backend

AITASKER Backend là service Spring Boot 21 cho nền tảng kết nối Business, Expert, Staff và Admin. Dự án cung cấp các API cho auth, profile/KYC, marketplace, contract, payment, dispute, notification, AI SOW và expert recommendation.

## Tổng quan

- Framework: Spring Boot
- Ngôn ngữ: Java 21
- Database: PostgreSQL + Flyway
- Cache/OTP: Redis
- Tài liệu API: Swagger/OpenAPI
- Tích hợp: JWT, Firebase Storage, Google Sign-In, PayOS, OpenAI

## Yêu cầu môi trường

- Java 21
- Docker Desktop
- Maven Wrapper (`./mvnw`)

## Chạy local

1. Khởi động database và Redis:

```bash
docker compose up -d
```

2. Cấu hình biến môi trường:

   - Dự án đọc từ file `.env` nếu có.
   - Có thể dùng các giá trị mặc định trong `src/main/resources/application.properties` khi chạy local.

3. Chạy backend:

```bash
./mvnw spring-boot:run
```

4. Kiểm tra sức khỏe:

- `GET http://localhost:8080/api/health`

## Tài liệu API

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Nếu API cần xác thực, bấm `Authorize` và nhập JWT access token, không thêm tiền tố `Bearer`.

## Biến môi trường cần thiết

Tối thiểu cho local:

```env
DB_HOST=127.0.0.1
DB_PORT=5433
DB_NAME=aitasker_db
DB_USER=aitasker
DB_PASSWORD=aitasker123
APP_JWT_SECRET=<base64-secret>
```

Thường dùng thêm:

```env
REDIS_HOST=localhost
REDIS_PORT=6379
OPENAI_API_KEY=<openai-api-key>
MAIL_USERNAME=<smtp-username>
MAIL_PASSWORD=<smtp-app-password>
PAYOS_CLIENT_ID=<payos-client-id>
PAYOS_API_KEY=<payos-api-key>
PAYOS_CHECKSUM_KEY=<payos-checksum-key>
FIREBASE_STORAGE_BUCKET=<firebase-storage-bucket>
FIREBASE_SERVICE_ACCOUNT_JSON=<firebase-service-account-json>
GOOGLE_CLIENT_ID=<google-client-id>
APP_FRONTEND_URL=http://localhost:5173
SWAGGER_ENABLED=true
```

Nếu deploy cloud:

- Đặt `DB_SSLMODE=require`
- Tắt Swagger nếu không cần public: `SWAGGER_ENABLED=false`
- Nếu dùng file service account local, có thể khai báo `FIREBASE_SERVICE_ACCOUNT_PATH`

## Test

```bash
docker compose up -d
./mvnw test
```

## Build production

```bash
./mvnw -DskipTests package
java -jar target/aitasker-0.0.1-SNAPSHOT.jar
```

## Tài liệu tham khảo

- `BACKEND_TEST_GUIDE.md`
- `POSTMAN_MANUAL_CHECKLIST.md`
- `docs/README.md`
- `docs/swagger-api-overview.md`
- `docs/postman-api-test-guide.md`

## Ghi chú

- Flyway chịu trách nhiệm schema và seed data.
- `GET /api/users/me/quota` là source of truth cho quota, active package và Premium permission.
- Backend dùng role-based check trong service để chặn gọi API sai thẩm quyền.
