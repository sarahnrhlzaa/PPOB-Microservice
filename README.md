# PPOB Microservice System

Sistem microservice PPOB (Payment Point Online Bank) yang mengimplementasikan komunikasi event-driven menggunakan RabbitMQ, dengan Redis untuk session/token storage, dan MySQL sebagai database.

---

## Tech Stack

| Layer | Teknologi |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| Message Broker | RabbitMQ 3 (Spring AMQP) |
| Session Storage | Redis 7 (Spring Data Redis) |
| Database | MySQL 8.0 (Spring Data JPA) |
| Containerization | Docker & Docker Compose |

---

## Arsitektur Sistem

```
Client
  │
  ▼
Service A (port 8080)   ← hanya konek ke RabbitMQ
  │   ▲
  │   │  (RabbitMQ - event-driven, bukan HTTP)
  ▼   │
Service B (port 8081)   ← konek ke RabbitMQ + Redis + MySQL
  ├── Redis  (token/session storage)
  └── MySQL  (users, transactions)
```

Service A dan Service B **tidak pernah berkomunikasi langsung** (tidak ada HTTP/gRPC antar service). Semua komunikasi melewati RabbitMQ menggunakan pola request-reply dengan Correlation ID.

---

## Cara Menjalankan Lokal

### Prasyarat
- Docker & Docker Compose terinstall
- Port yang dibutuhkan bebas: `8080`, `8081`, `5672`, `15672`, `3306`, `6379`

### Langkah

```bash
# 1. Clone / masuk ke folder project
cd project

# 2. Jalankan semua service sekaligus
docker compose up --build

# Atau jalankan di background
docker compose up --build -d
```

Semua service akan otomatis berjalan:

| Service | URL |
|---|---|
| Service A (API Gateway) | http://localhost:8080 |
| Service B (Business Logic) | http://localhost:8081 |
| RabbitMQ Management UI | http://localhost:15672 (user: `ppob_user` / pass: `ppob_pass`) |
| MySQL | localhost:3306 (user: `ppob_user` / pass: `ppob_pass` / db: `ppob_db`) |
| Redis | localhost:6379 |

### Menghentikan Service

```bash
docker compose down

# Hapus volume (reset database)
docker compose down -v
```

---

## Database

### Jenis Database
MySQL 8.0 — berjalan di Docker container `ppob-mysql`.

> **Catatan untuk pengguna phpMyAdmin:** Database MySQL yang dijalankan Docker ini dapat diakses melalui phpMyAdmin dengan menggunakan host `127.0.0.1`, port `3306`, user `ppob_user`, dan password `ppob_pass`. Atau bisa juga menggunakan tool GUI lain seperti DBeaver, TablePlus, atau MySQL Workbench.

### Tabel yang Dibuat

Tabel dibuat otomatis saat pertama kali `docker compose up` dijalankan melalui file `docker/init.sql`.

**Tabel `users`**

| Kolom | Tipe | Keterangan |
|---|---|---|
| id | BIGINT (PK) | Auto increment |
| username | VARCHAR(50) | Unique, tidak boleh null |
| password | VARCHAR(255) | BCrypt hash |
| full_name | VARCHAR(100) | Nama lengkap user |
| balance | DECIMAL(15,2) | Saldo user |
| created_at | DATETIME | Waktu dibuat |

**Tabel `transactions`**

| Kolom | Tipe | Keterangan |
|---|---|---|
| id | BIGINT (PK) | Auto increment |
| transaction_number | VARCHAR(50) | Nomor transaksi unik |
| user_id | BIGINT (FK) | Referensi ke `users.id` |
| product_type | VARCHAR(50) | Jenis produk PPOB |
| customer_number | VARCHAR(50) | Nomor pelanggan |
| amount | DECIMAL(15,2) | Nominal transaksi |
| admin_fee | DECIMAL(15,2) | Biaya admin (Rp 2.500) |
| total_amount | DECIMAL(15,2) | Total yang dibayar |
| status | VARCHAR(20) | Status transaksi |
| created_at | DATETIME | Waktu transaksi |

### Data User Bawaan (untuk testing)

| Username | Password | Saldo |
|---|---|---|
| john_doe | john123 | Rp 500.000 |
| jane_doe | jane123 | Rp 1.000.000 |

---

## Redis — Session/Token Storage

Redis digunakan **hanya oleh Service B** untuk menyimpan token autentikasi.

### Struktur Key

```
token:<uuid-token>  →  <username>
```

### Contoh

```
Key:   token:550e8400-e29b-41d4-a716-446655440000
Value: john_doe
TTL:   86400 detik (24 jam)
```

### Operasi yang Dilakukan

- **Simpan token** saat login berhasil → `SET token:<token> <username> EX 86400`
- **Validasi token** saat ada request bisnis → `GET token:<token>`
- **Hapus token** saat logout → `DEL token:<token>`

---

## Komunikasi RabbitMQ

### Exchange

| Nama | Tipe |
|---|---|
| `ppob.exchange` | Topic Exchange |

### Queue & Routing Key

| Queue | Routing Key | Arah | Fungsi |
|---|---|---|---|
| `auth.request.queue` | `auth.request` | A → B | Request login |
| `auth.reply.queue` | `auth.reply` | B → A | Hasil login |
| `logout.request.queue` | `logout.request` | A → B | Request logout |
| `logout.reply.queue` | `logout.reply` | B → A | Hasil logout |
| `profile.request.queue` | `profile.request` | A → B | Request profil |
| `profile.reply.queue` | `profile.reply` | B → A | Hasil profil |
| `payment.request.queue` | `payment.request` | A → B | Request pembayaran |
| `payment.reply.queue` | `payment.reply` | B → A | Hasil pembayaran |

### Pola Komunikasi

Menggunakan **Request-Reply Pattern** dengan **Correlation ID**:

1. Service A membuat `correlationId` (UUID) unik per request
2. Service A menyimpan `CompletableFuture` di map dengan key `correlationId`
3. Service A publish pesan ke request queue dengan `correlationId` di dalam payload
4. Service B consume pesan, proses, dan publish reply ke reply queue dengan `correlationId` yang sama
5. Service A menerima reply lewat `ReplyListener`, match `correlationId`, complete `CompletableFuture`
6. Service A return response ke client (timeout 10 detik)

---

## Event Contracts

### 1. AUTH_REQUEST (A → B)

**Queue:** `auth.request.queue` | **Routing Key:** `auth.request`

```json
{
  "correlationId": "550e8400-e29b-41d4-a716-446655440000",
  "eventType": "AUTH_REQUEST",
  "username": "john_doe",
  "password": "john123",
  "timestamp": "2025-01-01T10:00:00"
}
```

### 2. AUTH_REPLY (B → A)

**Queue:** `auth.reply.queue` | **Routing Key:** `auth.reply`

**Sukses:**
```json
{
  "correlationId": "550e8400-e29b-41d4-a716-446655440000",
  "eventType": "AUTH_REPLY",
  "success": true,
  "token": "a1b2c3d4-...",
  "username": "john_doe",
  "fullName": "John Doe",
  "message": "Login successful",
  "timestamp": "2025-01-01T10:00:01"
}
```

**Gagal (credentials salah):**
```json
{
  "correlationId": "550e8400-e29b-41d4-a716-446655440000",
  "eventType": "AUTH_REPLY",
  "success": false,
  "message": "Invalid username or password",
  "timestamp": "2025-01-01T10:00:01"
}
```

---

### 3. LOGOUT_REQUEST (A → B)

**Queue:** `logout.request.queue` | **Routing Key:** `logout.request`

```json
{
  "correlationId": "abc12345-...",
  "eventType": "LOGOUT_REQUEST",
  "token": "a1b2c3d4-...",
  "timestamp": "2025-01-01T11:00:00"
}
```

### 4. LOGOUT_REPLY (B → A)

**Sukses:**
```json
{
  "correlationId": "abc12345-...",
  "eventType": "LOGOUT_REPLY",
  "success": true,
  "message": "Logout successful",
  "timestamp": "2025-01-01T11:00:01"
}
```

**Gagal (token tidak valid):**
```json
{
  "correlationId": "abc12345-...",
  "eventType": "LOGOUT_REPLY",
  "success": false,
  "message": "Invalid or expired token",
  "timestamp": "2025-01-01T11:00:01"
}
```

---

### 5. PROFILE_REQUEST (A → B)

**Queue:** `profile.request.queue` | **Routing Key:** `profile.request`

```json
{
  "correlationId": "def67890-...",
  "eventType": "PROFILE_REQUEST",
  "token": "a1b2c3d4-...",
  "timestamp": "2025-01-01T11:05:00"
}
```

### 6. PROFILE_REPLY (B → A)

**Sukses:**
```json
{
  "correlationId": "def67890-...",
  "eventType": "PROFILE_REPLY",
  "success": true,
  "username": "john_doe",
  "fullName": "John Doe",
  "balance": 500000.00,
  "status": "SUCCESS",
  "message": "Profile retrieved successfully",
  "timestamp": "2025-01-01T11:05:01"
}
```

**Gagal (token expired):**
```json
{
  "correlationId": "def67890-...",
  "eventType": "PROFILE_REPLY",
  "success": false,
  "status": "UNAUTHORIZED",
  "message": "Invalid or expired token",
  "timestamp": "2025-01-01T11:05:01"
}
```

---

### 7. PAYMENT_REQUEST (A → B)

**Queue:** `payment.request.queue` | **Routing Key:** `payment.request`

```json
{
  "correlationId": "ghi11223-...",
  "eventType": "PAYMENT_REQUEST",
  "token": "a1b2c3d4-...",
  "productType": "PLN_PREPAID",
  "customerNumber": "123456789012",
  "amount": 50000,
  "timestamp": "2025-01-01T11:10:00"
}
```

### 8. PAYMENT_REPLY (B → A)

**Sukses:**
```json
{
  "correlationId": "ghi11223-...",
  "eventType": "PAYMENT_REPLY",
  "success": true,
  "transactionNumber": "TXN-A1B2C3D4",
  "productType": "PLN_PREPAID",
  "customerNumber": "123456789012",
  "amount": 50000.00,
  "adminFee": 2500.00,
  "totalAmount": 52500.00,
  "status": "SUCCESS",
  "message": "Payment successful",
  "timestamp": "2025-01-01T11:10:02"
}
```

**Gagal (saldo tidak cukup):**
```json
{
  "correlationId": "ghi11223-...",
  "eventType": "PAYMENT_REPLY",
  "success": false,
  "status": "INSUFFICIENT_BALANCE",
  "message": "Insufficient balance",
  "timestamp": "2025-01-01T11:10:02"
}
```

**Gagal (token expired):**
```json
{
  "correlationId": "ghi11223-...",
  "eventType": "PAYMENT_REPLY",
  "success": false,
  "status": "UNAUTHORIZED",
  "message": "Invalid or expired token",
  "timestamp": "2025-01-01T11:10:02"
}
```

---

## API Endpoints

### Base URL
```
http://localhost:8080
```

---

### POST /auth/login

Login dan dapatkan token.

**Request:**
```http
POST /auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "john123"
}
```

**Response Sukses (200):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "username": "john_doe",
    "fullName": "John Doe"
  },
  "timestamp": "2025-01-01T10:00:01"
}
```

**Response Gagal - Credentials Salah (401):**
```json
{
  "success": false,
  "message": "Invalid username or password",
  "timestamp": "2025-01-01T10:00:01"
}
```

**Response Gagal - Validasi Input (400):**
```json
{
  "success": false,
  "message": "Username is required",
  "timestamp": "2025-01-01T10:00:00"
}
```

---

### POST /auth/logout

Logout dan hapus token dari Redis. Membutuhkan token.

**Request:**
```http
POST /auth/logout
Authorization: Bearer a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

**Response Sukses (200):**
```json
{
  "success": true,
  "message": "Logout successful",
  "timestamp": "2025-01-01T11:00:01"
}
```

**Response Gagal - Token Tidak Valid (401):**
```json
{
  "success": false,
  "message": "Invalid or expired token",
  "timestamp": "2025-01-01T11:00:01"
}
```

---

### GET /profile

Ambil data profil user. Membutuhkan token.

**Request:**
```http
GET /profile
Authorization: Bearer a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

**Response Sukses (200):**
```json
{
  "success": true,
  "message": "Profile retrieved",
  "data": {
    "username": "john_doe",
    "fullName": "John Doe",
    "balance": 500000.00,
    "status": "SUCCESS"
  },
  "timestamp": "2025-01-01T11:05:01"
}
```

**Response Gagal - Token Expired (401):**
```json
{
  "success": false,
  "message": "Invalid or expired token",
  "timestamp": "2025-01-01T11:05:01"
}
```

---

### POST /payment/pay

Lakukan pembayaran PPOB. Membutuhkan token.

**Request:**
```http
POST /payment/pay
Authorization: Bearer a1b2c3d4-e5f6-7890-abcd-ef1234567890
Content-Type: application/json

{
  "productType": "PLN_PREPAID",
  "customerNumber": "123456789012",
  "amount": 50000
}
```

**Response Sukses (200):**
```json
{
  "success": true,
  "message": "Payment successful",
  "data": {
    "transactionNumber": "TXN-A1B2C3D4",
    "productType": "PLN_PREPAID",
    "customerNumber": "123456789012",
    "amount": 50000.00,
    "adminFee": 2500.00,
    "totalAmount": 52500.00,
    "status": "SUCCESS",
    "message": "Payment successful"
  },
  "timestamp": "2025-01-01T11:10:02"
}
```

**Response Gagal - Saldo Tidak Cukup (400):**
```json
{
  "success": false,
  "message": "Insufficient balance",
  "timestamp": "2025-01-01T11:10:02"
}
```

**Response Gagal - Token Tidak Valid (401):**
```json
{
  "success": false,
  "message": "Invalid or expired token",
  "timestamp": "2025-01-01T11:10:02"
}
```

---

## Cara Test API (menggunakan curl)

```bash
# 1. Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john_doe","password":"john123"}'

# Simpan token dari response di atas, lalu:
TOKEN="token-dari-response-login"

# 2. Lihat profil
curl -X GET http://localhost:8080/profile \
  -H "Authorization: Bearer $TOKEN"

# 3. Bayar PPOB
curl -X POST http://localhost:8080/payment/pay \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productType":"PLN_PREPAID","customerNumber":"123456789012","amount":50000}'

# 4. Logout
curl -X POST http://localhost:8080/auth/logout \
  -H "Authorization: Bearer $TOKEN"
```

---

## Alur Autentikasi

```
1. Client POST /auth/login → Service A
2. Service A buat correlationId, publish AUTH_REQUEST ke RabbitMQ
3. Service B consume AUTH_REQUEST:
   - Cek username di MySQL
   - Verifikasi password dengan BCrypt
   - Jika valid: generate UUID token, simpan ke Redis (TTL 24 jam)
   - Publish AUTH_REPLY ke RabbitMQ
4. Service A receive AUTH_REPLY, return token ke client

5. Client kirim request + Bearer token → Service A
6. Service A publish request message ke RabbitMQ
7. Service B consume message:
   - Validasi token: GET token:<token> dari Redis
   - Jika token ada: proses request (baca/tulis MySQL)
   - Publish reply ke RabbitMQ
8. Service A receive reply, return ke client
```

---

## Environment Variables

### Service A
| Variable | Default | Keterangan |
|---|---|---|
| RABBITMQ_HOST | localhost | Host RabbitMQ |
| RABBITMQ_PORT | 5672 | Port AMQP |
| RABBITMQ_USER | ppob_user | Username RabbitMQ |
| RABBITMQ_PASS | ppob_pass | Password RabbitMQ |

### Service B
| Variable | Default | Keterangan |
|---|---|---|
| RABBITMQ_HOST | localhost | Host RabbitMQ |
| RABBITMQ_PORT | 5672 | Port AMQP |
| RABBITMQ_USER | ppob_user | Username RabbitMQ |
| RABBITMQ_PASS | ppob_pass | Password RabbitMQ |
| DB_HOST | localhost | Host MySQL |
| DB_PORT | 3306 | Port MySQL |
| DB_NAME | ppob_db | Nama database |
| DB_USER | ppob_user | Username MySQL |
| DB_PASS | ppob_pass | Password MySQL |
| REDIS_HOST | localhost | Host Redis |
| REDIS_PORT | 6379 | Port Redis |

---

## Penanganan Error

| Skenario | Status HTTP | Keterangan |
|---|---|---|
| Username/password salah | 401 | Pesan generik, tidak bisa ditebak field mana yang salah |
| Token tidak ada / expired | 401 | Token tidak ditemukan di Redis |
| Authorization header tidak ada | 401 | Format header salah |
| Saldo tidak cukup | 400 | Total tagihan melebihi saldo user |
| Input tidak valid | 400 | Field wajib kosong atau format salah |
| Service timeout (>10 detik) | 500 | Service B tidak merespons dalam batas waktu |
| Error internal | 500 | Kesalahan tak terduga di service |

