# Employee Management App🛡️ 
> An enterprise-grade, offline-first Android platform for managing security personnel, site deployments, and attendance.

## 📱 Project Overview
This is a modern B2B Native Android application built to solve the logistical challenges of managing a distributed security workforce. It features a robust role-based architecture (Admin vs. Employee), real-time cloud synchronization, and a complete media pipeline for employee KYC/Bio-Data. 

Whether online or offline, Neutron ensures that administrators can seamlessly deploy guards to sites, track monthly attendance, and calculate salaries dynamically.

---

## ✨ Key Features

### 👨‍💼 For Administrators (Command Center)
* **Real-Time Dashboard:** View active personnel count, total payroll, and site deployment stats at a glance.
* **Employee Management:** Register new guards with secure credential generation and automatic profile picture compression/cloud upload.
* **Site Deployment:** Dynamically assign available guards to specific sites using reactive UI lists.
* **Push Notifications:** Receive instant Firebase Cloud Messaging (FCM) alerts when guards request leave.
* **PDF Generation:** Instantly generate KYC & Bio-Data PDF forms for compliance.

### 👮 For Guards (Staff Portal)
* **Role-Based Routing:** Secure login that automatically routes to the restricted employee dashboard.
* **Deployment Tracking:** View currently assigned sites and active status.
* **Attendance & Payroll:** Track monthly present/absent days and view calculated net salary based on base pay and advances.
* **Leave Requests:** Submit leave requests directly to the Admin for approval.

---

## 🛠️ Tech Stack & Libraries
This project was built entirely using modern Android development standards:

* **Language:** Kotlin
* **UI Toolkit:** Jetpack Compose (Material 3 with custom Poppins typography and enterprise color palette)
* **Architecture:** MVVM (Model-View-ViewModel) + Clean Architecture principles
* **Dependency Injection:** Dagger-Hilt
* **Asynchronous Programming:** Kotlin Coroutines & StateFlow
* **Local Database:** Room Database (Offline-first caching)
* **Backend as a Service (BaaS):** * Firebase Authentication (Secure Login)
  * Firebase Cloud Firestore (NoSQL Real-time Database)
  * Firebase Cloud Storage (Media Pipeline & Image Hosting)
  * Firebase Cloud Messaging (Push Notifications)
* **Image Loading:** Coil (Async Image Loading)

---

## 🏗️ Architecture Highlight
Neutron utilizes a **Single Source of Truth** pattern. 
When data is fetched or modified, it is first written to the local Room database to ensure the app works flawlessly without an internet connection. Kotlin `Flow` observes these local tables and reactively updates the Jetpack Compose UI.
In the background, Repository classes sync this local data securely to Firebase Firestore, ensuring data is never lost and is updated in real-time across all devices.
