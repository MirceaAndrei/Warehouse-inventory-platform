# Inventory Management System (InventoryApp)

A full-stack, offline-first inventory management system developed as a Diploma Project (2026). It provides real-time stock visibility and secure access across Android devices and web platforms.

---

## 🏗️ System Architecture
The system uses a three-tier architecture with a RESTful Spring Boot backend, a native Android client (offline-capable), and a web dashboard.

![System Architecture](images/architecture.png)

## 🚀 Key Features
- **Offline-First:** Built with **Room & WorkManager**. Transactions are queued locally and auto-sync when network is restored—zero data loss.
- **Barcode Scanning:** Native Android integration with **CameraX & Google ML Kit** for real-time decoding.
- **Role-Based Access Control (RBAC):** Hierarchical access (`ADMIN`, `MANAGER`, `EMPLOYEE`) enforced via Spring Security (`@PreAuthorize`) and JWTs.
- **Implicit Item Creation:** A custom design pattern allowing staff to create new product records instantly during a scan transaction if the item is missing from the database.

## 🛠️ Technology Stack
![Tech Stack](images/tech-stack.png)

## 📱 Offline-First Logic
The sync engine ensures reliability even during intermittent connectivity.

![Offline Synchronization](images/offline-sync.png)

## 🌐 Web Dashboard
Managers can monitor inventory, view audit trails, and export data via the responsive dashboard.

![Dashboard Preview](images/dashboard.png)

## 📝 About the Project
This system was designed to address the "Inventory Record Inaccuracy Problem," typical in warehouse environments. By moving from paper-based or desktop-dependent systems to a mobile-native solution, we significantly reduced data entry errors and latency.

---
*Developed by Mircea-Andrei Rață (2026)*