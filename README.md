# 📦 InventoryApp — Inventory Management System
---

## 📋 Table of Contents

* [🎯 Project Overview & Motivation](#-project-overview--motivation)
* [🏗️ System Architecture & Topology](#-system-architecture--topology)
* [🛠️ Core Technical Modules](#-core-technical-modules)

  * [Backend Service](#1-backend-service-spring-boot)
  * [Android Client](#2-android-client-kotlin--jetpack-compose)
  * [Web Dashboard](#3-web-dashboard-thymeleaf--bootstrap)
* [💡 Key Architectural Innovations](#-key-architectural-innovations)

  * [Offline-First Synchronization](#-offline-first-synchronization-room--workmanager)
  * [Implicit Item Creation](#-implicit-item-creation-pattern)
* [🔒 Security & Role-Based Access Control](#-security--role-based-access-control)
* [🚀 Getting Started](#-getting-started)
* [📱 Android Configuration](#-android-configuration)
* [📊 Technical Stack](#-technical-stack)
* [👨‍💻 Author](#-author)

---

## 🎯 Project Overview & Motivation

Warehouse operations frequently suffer from the **Inventory Record Inaccuracy Problem**, where data-entry errors, delayed updates, and manual processes can lead to significant discrepancies between physical and recorded stock.

Traditional inventory-management solutions often rely on expensive proprietary handheld scanners or complex Enterprise Resource Planning (ERP) systems. These solutions may introduce high hardware costs, vendor lock-in, and unnecessary complexity for smaller organizations.

**InventoryApp** is a custom, multi-platform inventory management ecosystem designed to:

* 📦 simplify warehouse inventory operations;
* 📱 replace dedicated scanning hardware with Android devices;
* 🔄 maintain inventory visibility across multiple platforms;
* 📴 continue working when the warehouse has limited or no network connectivity;
* 🔐 provide secure role-based access to inventory operations;
* ⚡ reduce manual data entry and workflow interruptions;
* 📊 provide managers with a centralized web dashboard.

The system combines an **Android warehouse client**, a **Spring Boot REST backend**, and a **server-rendered web dashboard** into a single inventory-management ecosystem.

---

## 🏗️ System Architecture & Topology

InventoryApp follows a decoupled **three-tier client-server architecture**, based on the principles of layered architecture.

The system consists of three main components:

```text
┌───────────────────────────────┐
│       Android Client         │
│     Kotlin + Jetpack Compose │
│                               │
│  • Barcode Scanner            │
│  • Offline Storage            │
│  • Transaction Management     │
└───────────────┬───────────────┘
                │
                │ REST API / JSON
                ▼
┌───────────────────────────────┐
│       Spring Boot Backend     │
│          Java 17              │
│                               │
│  • REST API                   │
│  • Business Logic             │
│  • Authentication             │
│  • Authorization              │
│  • Persistence                │
└───────────────┬───────────────┘
                │
                │ JPA / Hibernate
                ▼
┌───────────────────────────────┐
│       H2 Database             │
│      File-based Storage       │
└───────────────────────────────┘


┌───────────────────────────────┐
│        Web Dashboard          │
│   Thymeleaf + Bootstrap 5.3   │
│                               │
│  • Inventory Management       │
│  • Transaction Logs           │
│  • User Management            │
│  • Reporting                  │
└───────────────┬───────────────┘
                │
                │ REST API / Fetch
                ▼
        Spring Boot Backend
```

### Architecture Components

| Component        | Technology               | Main Responsibility                       |
| ---------------- | ------------------------ | ----------------------------------------- |
| Android Client   | Kotlin, Jetpack Compose  | Warehouse operations and barcode scanning |
| Backend          | Spring Boot, Java 17     | Business logic, REST API, authentication  |
| Database         | H2, JPA, Hibernate       | Persistent inventory data                 |
| Web Dashboard    | Thymeleaf, Bootstrap 5.3 | Management and administration             |
| Local Android DB | Room                     | Offline transaction storage               |
| Background Sync  | WorkManager              | Synchronization after network recovery    |

### Architecture Diagram

The project architecture is illustrated below:

![System Architecture](images/architecture.png)

*Reference: System Architecture Diagram, Diploma Thesis, Chapter 2.*

---

# 🛠️ Core Technical Modules

## 1. Backend Service — Spring Boot

The backend represents the central component of the InventoryApp ecosystem. It exposes a RESTful API consumed by both the Android application and the web dashboard.

### Technology Stack

* **Java 17**
* **Spring Boot 3.2**
* **Spring Data JPA**
* **Hibernate**
* **Spring Security**
* **JWT**
* **BCrypt**
* **H2 Database**
* **Maven**

### REST API

The backend exposes multiple REST controllers responsible for different parts of the system, including:

* `Item`
* `Category`
* `Transaction`
* `User`
* `StockSettings`
* Authentication and authorization

The API follows a REST-oriented design and uses JSON for communication between clients and the backend.

### Persistence

The persistence layer is implemented using:

* Spring Data JPA
* Hibernate ORM
* H2 file-based relational database

The database configuration can be changed to another relational database, such as PostgreSQL, through application configuration.

### Example Backend Structure

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ...
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── pom.xml
└── README.md
```

---

## 2. Android Client — Kotlin & Jetpack Compose

The Android application is designed primarily for warehouse employees working directly on the warehouse floor.

The application is an **offline-capable fat client**, meaning that important operations can continue even when the network connection is temporarily unavailable.

### Technology Stack

* **Kotlin**
* **Jetpack Compose**
* **CameraX 1.3**
* **Google ML Kit 17.2**
* **Retrofit 2.9**
* **OkHttp**
* **Room**
* **WorkManager**
* **Android API 26+**

### User Interface

The application is built entirely using **Jetpack Compose**.

A **Single Activity architecture** is used through `MainActivity`, while application navigation is handled through a sealed `Screen` hierarchy rather than XML-based Navigation Graphs.

Example conceptual structure:

```text
MainActivity
     │
     ├── Login
     │
     ├── Dashboard
     │
     ├── Scanner
     │
     ├── Inventory
     │
     └── Transactions
```

### Barcode Scanning

The application integrates:

* **CameraX**
* **Google ML Kit**

The scanner supports common barcode formats including:

* EAN-13
* EAN-8
* UPC-A
* QR Codes

Barcode recognition is performed locally on the Android device, allowing scanning to continue without an active Internet connection.

### Networking

Communication with the backend is implemented using **Retrofit** and **OkHttp**.

An `AuthInterceptor` automatically attaches the JWT access token to outgoing authenticated requests:

```text
Authorization: Bearer <JWT>
```

This allows authenticated API requests to be handled transparently by the application.

---

## 3. Web Dashboard — Thymeleaf & Bootstrap

The web dashboard provides management and administrative functionality for managers and administrators.

Unlike the Android client, the dashboard is designed as a thin client that relies on the backend for business logic and data.

### Technology Stack

* **Spring Boot**
* **Thymeleaf**
* **Bootstrap 5.3**
* **JavaScript**
* **Fetch API**
* **Browser Session Storage**
* **Browser Local Storage**

The dashboard runs independently on:

```text
http://localhost:8081
```

and communicates with the backend API running on port:

```text
8082
```

### Main Features

The dashboard provides:

* 📦 Inventory management
* 🔎 Client-side table filtering
* 🧾 Transaction log visualization
* 📊 Inventory reporting
* 📥 CSV export
* 🌙 Dark mode
* 👤 User management
* ⚠️ Duplicate barcode detection
* 🔐 Authenticated API communication

### Dark Mode

The dashboard includes an instant dark-mode toggle.

The selected theme is persisted using browser `localStorage`, allowing the preference to survive page reloads and browser sessions.

### CSV Export

Managers can export filtered inventory datasets directly from the browser.

The export is implemented using JavaScript `Blob` generation, avoiding the need for an additional backend reporting endpoint.

---

# 💡 Key Architectural Innovations

## 🔄 Offline-First Synchronization — Room + WorkManager

One of the main architectural features of InventoryApp is its **offline-first design**.

The system follows the principle:

> **"The Network is a Bonus, not a Requirement."**

Warehouse environments can experience:

* weak Wi-Fi coverage;
* temporary network outages;
* unstable mobile connections;
* network congestion;
* connectivity loss inside storage areas.

Instead of blocking warehouse operations when connectivity is unavailable, transactions are stored locally and synchronized later.

### Local Persistence

Unsubmitted transactions are stored inside a local **Room database**.

Each transaction contains a synchronization state:

```text
synced = false
```

indicating that the transaction is pending synchronization.

After successful submission to the backend:

```text
synced = true
```

### Synchronization Workflow

```text
┌─────────────────────┐
│   User scans item   │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Create transaction  │
│ in Room database    │
└──────────┬──────────┘
           │
           ▼
     synced = false
           │
           │
     Network unavailable
           │
           ▼
┌─────────────────────┐
│ Transaction remains │
│ in local database   │
└──────────┬──────────┘
           │
           │ Network restored
           ▼
┌─────────────────────┐
│    WorkManager      │
│    SyncWorker       │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Send transaction    │
│ to backend API      │
└──────────┬──────────┘
           │
           ▼
      synced = true
```

### WorkManager

A background `WorkManager` task called `SyncWorker` is responsible for processing pending transactions.

Synchronization is configured with a network constraint:

```text
NetworkType.CONNECTED
```

This prevents the worker from attempting network synchronization when the device has no available connection.

### Synchronization State Machine

The application uses a simple two-state model:

```text
PENDING
  │
  │ successful synchronization
  ▼
SYNCED
```

Internally:

```text
synced = false
       ↓
synced = true
```

Transactions are processed sequentially to reduce the risk of race conditions and duplicate submissions.

### Offline Synchronization Diagram

![Offline Synchronization](images/offline-sync.png)

*Reference: Transaction Synchronization State Machine & Room Inspector, Diploma Thesis, Chapter 4.*

---

# 📦 Implicit Item Creation Pattern

A second important architectural feature is the **Implicit Item Creation Pattern**.

In many warehouse systems, scanning an unknown barcode interrupts the workflow. The employee must first leave the scanning screen, create the product manually, and then return to the transaction.

InventoryApp eliminates this unnecessary workflow interruption.

### Workflow

When an employee scans an unknown barcode:

```text
Scan Barcode
     │
     ▼
Does item exist?
   /       \
 YES        NO
 │           │
 ▼           ▼
Process    Create Item
Transaction     │
 │              ▼
 │         Process Transaction
 │              │
 └──────┬───────┘
        ▼
   Transaction
    completed
```

The functionality is implemented through:

```text
POST /api/transactions/scan
```

If the scanned barcode does not already exist, the backend creates the corresponding item and records the transaction as part of the same service operation.

This approach provides:

* fewer workflow interruptions;
* faster warehouse operations;
* automatic registration of previously unknown products;
* consistent referential integrity;
* simplified employee interaction.

The employee therefore does not need to manually register an item before recording its stock movement.

---

# 🔒 Security & Role-Based Access Control

Security is enforced primarily on the backend to ensure that authorization cannot be bypassed by manipulating the Android application or web interface.

The system implements **Role-Based Access Control (RBAC)** using Spring Security.

Authorization is enforced at the service/controller level through method-level security annotations such as:

```java
@PreAuthorize(...)
```

### Authentication

The system uses stateless authentication based on **JSON Web Tokens (JWT)**.

The JWT implementation follows:

* RFC 7519
* HMAC-SHA256 signing
* server-side token validation

A custom `OncePerRequestFilter` is responsible for validating incoming JWT tokens.

### Password Security

User passwords are never stored in plaintext.

Passwords are hashed using:

```text
BCrypt
```

with a strength factor of:

```text
10
```

### Role Hierarchy

InventoryApp defines three primary roles:

```text
ADMIN
  │
  ▼
MANAGER
  │
  ▼
EMPLOYEE
```

Each role has a different level of access.

### EMPLOYEE

Employees can:

* scan barcodes;
* view inventory items;
* record incoming stock;
* record outgoing stock;
* trigger implicit item creation through scanning.

### MANAGER

Managers have access to:

* employee functionality;
* item CRUD operations;
* category management;
* transaction management;
* transaction log filtering;
* inventory reporting;
* CSV exports.

### ADMIN

Administrators have full system access, including:

* user management;
* password resets;
* system configuration;
* low-stock threshold configuration;
* all manager and employee functionality.

### Authorization Overview

| Operation                  | Employee | Manager | Admin |
| -------------------------- | :------: | :-----: | :---: |
| Scan barcode               |     ✅    |    ✅    |   ✅   |
| View items                 |     ✅    |    ✅    |   ✅   |
| Record transactions        |     ✅    |    ✅    |   ✅   |
| Create/Edit items          |     ❌    |    ✅    |   ✅   |
| Manage categories          |     ❌    |    ✅    |   ✅   |
| View transaction logs      |  Limited |    ✅    |   ✅   |
| Export CSV reports         |     ❌    |    ✅    |   ✅   |
| Manage users               |     ❌    |    ❌    |   ✅   |
| Reset passwords            |     ❌    |    ❌    |   ✅   |
| Configure stock thresholds |     ❌    |    ❌    |   ✅   |

---

# 🚀 Getting Started

## Prerequisites

Before running InventoryApp, make sure the following software is installed:

* **JDK 17 or newer**
* **Maven**
* **Android Studio**
* Android SDK supporting **API Level 26+**
* Git

---

## 1. Backend Setup

Navigate to the backend directory:

```bash
cd backend
```

Configure the application properties:

```text
backend/src/main/resources/application.properties
```

Make sure the JWT secret is configured:

```properties
jwt.secret=your-secure-random-string-here
```

> ⚠️ For production environments, the JWT secret should not be committed to source control. It should be externalized using environment variables or another secure secret-management mechanism.

Verify the H2 file-based database configuration in the same configuration file.

Start the backend:

```bash
mvn spring-boot:run
```

The backend API will be available on:

```text
http://localhost:8082
```

---

## 2. Web Dashboard Setup

Navigate to the dashboard:

```bash
cd web-dashboard
```

Make sure the backend is already running.

Start the dashboard:

```bash
mvn spring-boot:run
```

The web application will be available at:

```text
http://localhost:8081
```

Open the address in a web browser.

---

## 3. Android App Setup

Open the Android project:

```text
android-app/
```

using **Android Studio**.

Locate:

```text
ApiClient.kt
```

and configure the backend URL according to the environment in which the application is running.

### Android Emulator

When using the standard Android Emulator, the host machine can be accessed through:

```text
http://10.0.2.2:8082/
```

Example:

```kotlin
const val BASE_URL = "http://10.0.2.2:8082/"
```

### Physical Android Device

When using a physical Android device, replace `10.0.2.2` with the local IP address of the machine running the backend.

For example:

```kotlin
const val BASE_URL = "http://192.168.1.100:8082/"
```

The Android device and backend machine must be connected to the same network.

After configuring the URL:

1. Open the project in Android Studio.
2. Allow Gradle dependencies to synchronize.
3. Connect an Android device or start an emulator.
4. Build the project.
5. Run the application.

---

# 📱 Android Configuration

The Android application targets devices starting from:

```text
API Level 26
```

The application requires camera access for barcode scanning.

Make sure the appropriate camera permission is granted when prompted by Android.

### Network Configuration

For local HTTP development, the Android application must be configured to allow communication with the local backend where required by the Android project configuration.

For emulator development:

```text
Android Device
      │
      │ HTTP
      ▼
10.0.2.2:8082
      │
      ▼
Local Machine
      │
      ▼
Spring Boot Backend
```

---

# 🗂️ Project Structure

The repository is organized into three primary applications:

```text
InventoryApp/
│
├── backend/
│   ├── src/
│   ├── pom.xml
│   └── ...
│
├── android-app/
│   ├── app/
│   ├── gradle/
│   └── ...
│
├── web-dashboard/
│   ├── src/
│   ├── pom.xml
│   └── ...
│
├── images/
│   ├── architecture.png
│   └── offline-sync.png
│
└── README.md
```

---

# 📊 Technical Stack

| Layer                  | Technology                      |
| ---------------------- | ------------------------------- |
| Backend Language       | Java 17                         |
| Backend Framework      | Spring Boot 3.2                 |
| API                    | REST / JSON                     |
| Security               | Spring Security                 |
| Authentication         | JWT                             |
| Password Hashing       | BCrypt                          |
| ORM                    | Hibernate                       |
| Persistence            | Spring Data JPA                 |
| Development Database   | H2                              |
| Android Language       | Kotlin                          |
| Android UI             | Jetpack Compose                 |
| Camera                 | CameraX 1.3                     |
| Barcode Recognition    | Google ML Kit 17.2              |
| Android Networking     | Retrofit 2.9 + OkHttp           |
| Local Android Database | Room                            |
| Background Processing  | WorkManager                     |
| Web UI                 | Thymeleaf                       |
| CSS Framework          | Bootstrap 5.3                   |
| Web Communication      | JavaScript Fetch API            |
| Web Storage            | Session Storage + Local Storage |
| Build Tool — Backend   | Maven                           |
| Build Tool — Android   | Gradle                          |
| Minimum Android API    | API 26                          |

---

# ✨ Main Features

### 📦 Inventory Management

* Create and manage inventory items.
* Categorize products.
* Track stock quantities.
* Detect duplicate barcodes.
* Configure stock thresholds.

### 📷 Barcode Scanning

* EAN-13 support.
* EAN-8 support.
* UPC-A support.
* QR Code support.
* On-device barcode recognition.

### 🔄 Offline Operation

* Local transaction persistence.
* Automatic background synchronization.
* Network-aware synchronization.
* Pending transaction queue.
* Sequential transaction processing.

### 👥 User Management

* Employee role.
* Manager role.
* Administrator role.
* JWT authentication.
* BCrypt password hashing.
* Method-level authorization.

### 📊 Reporting

* Transaction history.
* Inventory filtering.
* Client-side table filtering.
* CSV export.
* Low-stock monitoring.

### 🌙 User Experience

* Responsive web interface.
* Bootstrap-based UI.
* Dark mode.
* Persistent theme preference.
* Android Jetpack Compose interface.

---

# 🔐 Security Considerations

InventoryApp is designed around server-side security enforcement.

Important security principles include:

* Authentication is handled through JWT.
* Passwords are stored using BCrypt hashes.
* Authorization is enforced on the backend.
* Client-side restrictions are not considered sufficient security controls.
* API endpoints are protected according to user roles.
* JWT secrets should be externalized in production environments.

The Android and web clients are therefore treated as untrusted clients from an authorization perspective, while the backend remains the final authority for access control.

---

# 🧪 Development Notes

The H2 database is used for development because it provides a lightweight relational database requiring minimal setup.

For a production deployment, the application can be migrated to a database such as PostgreSQL by changing the datasource and JPA configuration.

The architecture keeps the persistence layer sufficiently decoupled from the clients, allowing the database implementation to evolve without requiring major changes to the Android or web applications.

---

# 📌 Architectural Highlights

The main architectural contributions of InventoryApp are:

1. **Multi-platform inventory ecosystem**
   Android, backend, and web dashboard applications work together through a centralized REST API.

2. **Offline-first warehouse operations**
   Warehouse employees can continue recording transactions despite temporary network failures.

3. **Automatic background synchronization**
   WorkManager synchronizes locally stored transactions after network connectivity is restored.

4. **Implicit item creation**
   Unknown barcodes can automatically result in item creation without interrupting the employee's workflow.

5. **Server-side RBAC**
   Authorization is enforced by the backend rather than relying on client-side UI restrictions.

6. **Hardware-independent barcode scanning**
   Standard Android devices can be used instead of proprietary warehouse scanning hardware.

7. **Separation of concerns**
   Business logic, persistence, Android UI, and web presentation remain separated into dedicated application layers.

---

# 🎓 Diploma Project

This project was developed as part of the **Diploma Project — 2026** at:

**National University of Science and Technology POLITEHNICA Bucharest**
**Faculty of Engineering in Foreign Languages (FILS)**

### Author

**Mircea-Andrei Rață**

### Coordinator

**Sl. dr. ing. Mitrea Dan Alexandru**

---

# 👨‍💻 Author

**Mircea-Andrei Rață**

Diploma Project — 2026

---

## 📄 License

This project was developed as an academic diploma project.

Unless otherwise specified, the source code and documentation are intended primarily for educational and academic purposes.
