# 📦 InventoryApp
---

## 🎯 About

**InventoryApp** is a multi-platform inventory management system designed to simplify warehouse operations through barcode scanning, offline-first functionality, automatic synchronization, and role-based access control.

The system consists of:

* 📱 **Android App** — warehouse operations and barcode scanning
* ⚙️ **Spring Boot Backend** — REST API, business logic, security
* 🌐 **Web Dashboard** — inventory and administration

---

## 🏗️ Architecture

```text id="r9xw7a"
┌──────────────────┐
│   Android App    │
│ Kotlin + Compose │
└────────┬─────────┘
         │ REST API
         ▼
┌──────────────────┐
│ Spring Boot API  │
│ Java 17          │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│   H2 Database    │
└──────────────────┘
         ▲
         │ REST API
┌────────┴─────────┐
│  Web Dashboard   │
│ Thymeleaf + BS5  │
└──────────────────┘
```

![System Architecture](images/architecture.png)

---

## ✨ Features

### 📷 Barcode Scanning

* EAN-13
* EAN-8
* UPC-A
* QR Codes
* On-device scanning using CameraX + ML Kit

### 📴 Offline-First

Transactions are stored locally using **Room** when the device is offline and automatically synchronized using **WorkManager** when connectivity returns.

```text id="6bh8km"
Scan → Room → Pending → WorkManager → Backend → Synced
```

### 📦 Implicit Item Creation

Scanning an unknown barcode can automatically create the corresponding item and record the transaction through:

```text id="t0j4cb"
POST /api/transactions/scan
```

This avoids interrupting the warehouse workflow.

### 🔐 Security

* JWT authentication
* Spring Security
* BCrypt password hashing
* Method-level authorization
* Role-based access control

| Role     | Access                             |
| -------- | ---------------------------------- |
| EMPLOYEE | Scanning & stock transactions      |
| MANAGER  | Inventory & transaction management |
| ADMIN    | Full system administration         |

### 🌐 Web Dashboard

* Inventory management
* Transaction history
* Filtering
* CSV export
* Dark mode
* Duplicate barcode detection
* User administration

---

## 🛠️ Tech Stack

**Backend**

`Java 17` · `Spring Boot 3.2` · `Spring Security` · `JWT` · `JPA` · `Hibernate` · `H2` · `Maven`

**Android**

`Kotlin` · `Jetpack Compose` · `CameraX` · `Google ML Kit` · `Retrofit` · `OkHttp` · `Room` · `WorkManager`

**Web**

`Thymeleaf` · `Bootstrap 5.3` · `JavaScript` · `Fetch API`

---

## 🚀 Quick Start

### Backend

```bash id="m1v6m4"
cd backend
mvn spring-boot:run
```

Runs on:

```text id="h5t4di"
http://localhost:8082
```

Configure the JWT secret in:

```text id="8xq5jz"
src/main/resources/application.properties
```

---

### Web Dashboard

```bash id="8kq3a4"
cd web-dashboard
mvn spring-boot:run
```

Open:

```text id="d8v9m2"
http://localhost:8081
```

---

### Android

Open `android-app` in Android Studio.

For the Android Emulator:

```kotlin id="1m7q8d"
const val BASE_URL = "http://10.0.2.2:8082/"
```

For a physical device, replace the address with the local IP of the computer running the backend.

---

## 📁 Repository Structure

```text id="8g1v6a"
InventoryApp/
├── backend/
├── android-app/
├── web-dashboard/
├── images/
│   ├── architecture.png
│   └── offline-sync.png
└── README.md
```

---

## 🎓 Diploma Project

**2026 — National University of Science and Technology POLITEHNICA Bucharest**
**Faculty of Engineering in Foreign Languages (FILS)**

**Author:** Mircea-Andrei Rață


---

## 📄 License

Academic diploma project developed for educational purposes.
