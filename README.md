# Inventory Management System (InventoryApp)

A multi-platform inventory management system designed for real-time stock tracking, featuring an offline-first Android client, a secure Spring Boot backend, and a web administrative dashboard[cite: 4]. Developed as a Diploma Project (2026).

---

## 🚀 Key Features

- **Offline-First Architecture:** Built with Room and WorkManager, allowing warehouse employees to scan items and record transactions without network connectivity; changes auto-sync upon reconnection[cite: 4].
- **Real-Time Barcode Scanning:** Integrates CameraX and Google ML Kit on Android to instantly decode EAN-13, EAN-8, UPC-A, and QR codes[cite: 4].
- **Role-Based Access Control (RBAC):** Secure multi-tier permission hierarchy (`ADMIN`, `MANAGER`, `EMPLOYEE`) enforced via server-side Spring Security annotations (`@PreAuthorize`) and JWT authentication[cite: 4].
- **Web Dashboard:** A responsive Thymeleaf & Bootstrap 5 dashboard providing full inventory management, live transaction history with audit trails, and CSV export capabilities[cite: 4].

---

## 🛠️ Technology Stack

### Backend (`/backend`)
- **Framework:** Spring Boot 3.2, Java 17[cite: 4]
- **Security:** Spring Security, JSON Web Tokens (JWT) via `jjwt`[cite: 4]
- **Persistence:** Spring Data JPA, H2 Database (easily configurable to PostgreSQL)[cite: 4]

### Android Client (`/android-app`)
- **Language & UI:** Kotlin, Jetpack Compose (Single Activity Architecture)[cite: 4]
- **Scanning:** CameraX 1.3, Google ML Kit 17.2[cite: 4]
- **Local Storage & Sync:** Room Database, WorkManager[cite: 4]
- **Networking:** Retrofit 2, OkHttp (with AuthInterceptor for Bearer tokens)[cite: 4]

### Web Dashboard (`/web-dashboard`)
- **Templates & Styling:** Thymeleaf, Bootstrap 5.3[cite: 4]
- **Client Logic:** JavaScript Fetch API, Session Storage for JWT[cite: 4]

---

## 📊 System Architecture

- **Client-Server Architecture:** Decoupled backend REST API communicating via JSON with both the native Android client and the web dashboard[cite: 4].
- **Implicit Item Creation Pattern:** Automatically registers new products into the database during a barcode scanning transaction if the item does not already exist, optimizing warehouse workflows[cite: 4].

---

## 📄 Documentation
For detailed architecture, UML diagrams, and testing results, refer to the documentation included in the project or check the repository releases.

---
*Developed by Mircea-Andrei Rață (2026)*