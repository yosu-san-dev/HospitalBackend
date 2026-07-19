# HospitalReservation — Hastane Randevu Sistemi

> A full-stack hospital appointment booking system. Patients register with their Turkish National ID (TC Kimlik No), log in securely, browse departments and doctors, and book time slots — all backed by a Spring Boot REST API and a MongoDB database.

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Vision & Architecture](#vision--architecture)
3. [Tech Stack](#tech-stack)
4. [Project Structure](#project-structure)
5. [Backend — Deep Dive](#backend--deep-dive)
   - [Data Models](#data-models)
   - [REST API Endpoints](#rest-api-endpoints)
   - [Service Layer](#service-layer)
   - [Security Layer](#security-layer)
   - [Database Seeder](#database-seeder)
   - [Docker Support](#docker-support)
6. [Frontend — Deep Dive](#frontend--deep-dive)
   - [Pages & Navigation](#pages--navigation)
   - [Authentication Flow](#authentication-flow)
   - [Booking Flow](#booking-flow)
   - [Calendar & My Appointments](#calendar--my-appointments)
7. [Setup & Running Locally](#setup--running-locally)
   - [Prerequisites](#prerequisites)
   - [1. Configure the Database](#1-configure-the-database)
   - [2. Run the Backend](#2-run-the-backend)
   - [3. Run the Frontend](#3-run-the-frontend)
8. [Deployment](#deployment)
9. [Available Departments & Doctors](#available-departments--doctors)

---

## Project Overview

**HospitalReservation** is a web application that lets patients self-register and book medical appointments online, without any admin involvement. The project was built as a team learning exercise to connect a real Java backend to a browser-based frontend using REST APIs and JWT authentication.

Key highlights:
- Patients authenticate using their **TC Kimlik No** (Turkish National Identity Number) — not a username.
- The TC number is **never stored in plain text**; it is SHA-256 hashed and used directly as the patient's database ID.
- Passwords are hashed with **BCrypt** (random salt per user).
- Sessions are managed via **JWT tokens** (expire after 1 hour), stored in `localStorage`.
- **Appointment conflict detection** is built into the reservation service — two patients cannot book the same doctor at overlapping times.
- The doctor list is seeded automatically into MongoDB on first startup.

---

## Vision & Architecture

The system follows a classic **3-tier architecture**:

```
┌───────────────────────────────────────┐
│           Browser (Frontend)          │
│   HTML + CSS + Vanilla JavaScript     │
│  hospital-frontend/index.html, etc.   │
└───────────────────┬───────────────────┘
                    │  HTTP / REST (JSON)
                    ▼
┌───────────────────────────────────────┐
│       Spring Boot REST API            │
│   Java 21 · Spring Boot 4.0.1         │
│  Controllers → Services → Repos       │
└───────────────────┬───────────────────┘
                    │  MongoDB Driver
                    ▼
┌───────────────────────────────────────┐
│       MongoDB Atlas (Cloud)           │
│   Database: HospitalDB                │
│   Collections: doctors, patients,     │
│                reservations           │
└───────────────────────────────────────┘
```

All API calls are made via `fetch()` from the browser. The backend runs on `http://localhost:8080` by default. CORS is open (`@CrossOrigin(origins = "*")`) to allow the static frontend to communicate with the backend during development.

---

## Tech Stack

| Layer       | Technology                          | Purpose                                  |
|-------------|-------------------------------------|------------------------------------------|
| **Frontend**| HTML5, CSS3, Vanilla JavaScript     | Single-page UI with 3 views              |
| **Backend** | Java 21, Spring Boot 4.0.1          | REST API server                          |
| **Database**| MongoDB Atlas                       | NoSQL cloud database                     |
| **Auth**    | JWT (JJWT 0.11.5) + BCrypt          | Session tokens + password hashing        |
| **Security**| SHA-256 (MessageDigest), BCrypt     | TC hashing & password hashing            |
| **Build**   | Maven (mvnw wrapper)                | Dependency management & build            |
| **Deploy**  | Docker + Vercel                     | Containerized deployment                 |

---

## Project Structure

```
HospitalReservation/
│
├── hospital-backend/               # Spring Boot Maven project
│   ├── src/main/java/com/ryr/hospital/
│   │   ├── Application.java            # Spring Boot entry point
│   │   ├── MongoConfig.java            # MongoDB connection configuration
│   │   ├── controller/
│   │   │   ├── AuthController.java     # POST /auth/register, POST /auth/login
│   │   │   ├── DoctorController.java   # GET /doctors/all
│   │   │   └── ReservationController.java # POST /reservation/create, POST /reservation/my-appointments
│   │   ├── model/
│   │   │   ├── Doctor.java             # Doctor document (id, name, branch, age)
│   │   │   ├── Patient.java            # Patient document (hashedTC as ID, fullName, hashedPassword, role)
│   │   │   └── Reservation.java        # Reservation document (patientId, doctorId, startDT, endDT)
│   │   ├── repository/
│   │   │   ├── DoctorRepo.java         # MongoRepository for Doctor
│   │   │   ├── PatientRepo.java        # MongoRepository for Patient
│   │   │   └── ReservationRepo.java    # MongoRepository for Reservation
│   │   ├── service/
│   │   │   ├── AuthService.java        # Register & Login business logic
│   │   │   └── ReservationService.java # Create reservation + conflict detection
│   │   └── util/
│   │       ├── DatabaseSeeder.java     # Seeds 18 doctors on first startup
│   │       ├── JwtUtil.java            # Generate + validate JWT tokens
│   │       └── SecurityUtil.java       # hashTC (SHA-256) + hashPassword (BCrypt)
│   ├── src/main/resources/
│   │   └── application.properties      # App name + MongoDB URI
│   ├── Dockerfile                      # Multi-stage Docker build
│   └── pom.xml                         # Maven dependencies
│
└── hospital-frontend/              # Static web client
    ├── index.html                      # All 3 pages (auth, booking, calendar)
    ├── script.js                       # All frontend logic (~530 lines)
    └── style.css                       # All styles
```

---

## Backend — Deep Dive

### Data Models

#### `Doctor`
Stored in the `doctors` MongoDB collection.

| Field    | Type   | Description                                    |
|----------|--------|------------------------------------------------|
| `id`     | String | Auto-generated MongoDB ObjectId                |
| `name`   | String | Doctor's full name (e.g., "Ahmet Yilmaz")      |
| `branch` | String | Medical specialty (e.g., "Kardiyoloji")        |
| `age`    | int    | Doctor's age                                   |

#### `Patient`
Stored in the `patients` MongoDB collection.

| Field            | Type   | Description                                      |
|------------------|--------|--------------------------------------------------|
| `id`             | String | **SHA-256 hash of the patient's TC number** — used as the primary key |
| `fullName`       | String | Patient's full name                              |
| `hashedPassword` | String | BCrypt-hashed password                           |
| `role`           | String | Always `"Patient"` (prepared for future roles)   |

> **Why hash the TC as the ID?** Using a deterministic hash (SHA-256) means the system can look up a patient by TC without ever storing the raw TC number. This provides a strong privacy guarantee.

#### `Reservation`
Stored in the `reservations` MongoDB collection.

| Field       | Type          | Description                                      |
|-------------|---------------|--------------------------------------------------|
| `id`        | String        | Auto-generated MongoDB ObjectId                  |
| `patientId` | String        | The hashed TC of the patient who booked          |
| `doctorId`  | String        | The ObjectId of the booked doctor                |
| `startDT`   | LocalDateTime | Start date & time of the appointment             |
| `endDT`     | LocalDateTime | End date & time of the appointment               |

The model enforces two rules in its constructor:
1. `startDT` and `endDT` cannot be `null`.
2. `endDT` must be strictly **after** `startDT`.

A helper method `getDurationMinutes()` calculates the length of the appointment in minutes.

---

### REST API Endpoints

#### Auth — `/auth`
| Method | Path               | Auth Required | Body                                      | Returns              |
|--------|--------------------|---------------|-------------------------------------------|----------------------|
| POST   | `/auth/register`   | No            | `{ fullName, tc, password }`              | Plain text message   |
| POST   | `/auth/login`      | No            | `{ tc, password }`                        | JWT token (string)   |

**Register** hashes the TC and password, checks for duplicate patients, then saves to MongoDB.
**Login** hashes the provided TC, finds the patient by ID, validates the BCrypt password, and returns a signed JWT.

#### Doctors — `/doctors`
| Method | Path             | Auth Required | Returns                          |
|--------|------------------|---------------|----------------------------------|
| GET    | `/doctors/all`   | No            | JSON array of all Doctor objects |

This is a public endpoint — anyone can fetch the list of doctors without logging in.

#### Reservations — `/reservation`
| Method | Path                          | Auth Required | Body                                          | Returns                              |
|--------|-------------------------------|---------------|-----------------------------------------------|--------------------------------------|
| POST   | `/reservation/create`         | Yes (JWT)     | `{ doctorId, startTime, endTime }` (ISO 8601) | Plain text confirmation or error     |
| POST   | `/reservation/my-appointments`| Yes (JWT)     | (empty body)                                  | JSON array of Reservation objects    |

For `/reservation/create`, the JWT is extracted from the `Authorization: Bearer <token>` header. The patient's ID is decoded from the token — the frontend never sends the TC again after login.

---

### Service Layer

#### `AuthService`
Handles all business logic for registration and login.

- **`register(fullName, rawTC, rawPassword)`**
  1. Hashes the TC with SHA-256.
  2. Checks `patientRepo.existsById(hashedTC)` — throws an exception if the patient already exists.
  3. Hashes the password with BCrypt.
  4. Creates a new `Patient` object and saves it to MongoDB.

- **`login(rawTC, rawPassword)`**
  1. Hashes the TC with SHA-256.
  2. Looks up the patient by `hashedTC` ID.
  3. Verifies the password against the stored BCrypt hash.
  4. On success, calls `jwtUtil.generateToken(patient.getId(), patient.getRole())` and returns the JWT string.

#### `ReservationService`
Handles appointment creation and retrieval.

- **`hasOverlap(doctorId, start, end)`**
  Fetches all existing reservations for the given doctor from MongoDB, then checks each one for time overlap using the condition:
  ```
  newStart < existingEnd AND newEnd > existingStart
  ```
  Returns `true` if any overlap is found.

- **`createReser(patientId, doctorId, start, end)`**
  Calls `hasOverlap()` first. If there is a conflict, it throws a `RuntimeException("This time slot is already taken!")`. Otherwise, it creates and saves the new `Reservation`.

- **`getForPatient(patientId)`**
  Returns all reservations where `patientId` matches. Called after extracting the ID from the JWT token.

---

### Security Layer

All security helpers live in `util/SecurityUtil.java` and `util/JwtUtil.java`.

#### `SecurityUtil`
| Method                              | Algorithm     | Purpose                                     |
|-------------------------------------|---------------|---------------------------------------------|
| `hashPassword(rawPassword)`         | BCrypt        | Hashes a password with a random salt        |
| `chackPassword(given, hashed)`      | BCrypt        | Compares a raw password against a BCrypt hash |
| `hashTC(rawTC)`                     | SHA-256       | Produces a deterministic hex hash of the TC |

> Note: `hashTC` uses SHA-256 (from `java.security.MessageDigest`) which is deterministic — the same TC always produces the same hash, allowing database lookups. `hashPassword` uses BCrypt which is non-deterministic (random salt), so you can only verify it by checking, not by re-hashing.

#### `JwtUtil`
Tokens are signed with a **randomly generated `HS256` key** at server startup. This means all tokens are invalidated whenever the server restarts.

| Method                           | Description                                           |
|----------------------------------|-------------------------------------------------------|
| `generateToken(userId, role)`    | Creates a JWT with subject (patient's hashed TC), role claim, issue date, and 1-hour expiry |
| `extractUserId(token)`           | Parses the token and returns the `subject` (patient ID) |
| `validateToken(token)`           | Returns `true` if the token is valid and not expired  |

---

### Database Seeder

`util/DatabaseSeeder.java` implements `CommandLineRunner` and runs automatically on every application startup.

**Behavior:**
- If the `doctors` collection already has documents, it skips seeding (idempotent).
- If the collection is empty (e.g., fresh deployment), it inserts **18 doctors** across 6 specialties.

**Pre-seeded departments and doctors:**

| Department (TR)         | Department (EN)       | Doctors                                          |
|-------------------------|-----------------------|--------------------------------------------------|
| Kardiyoloji             | Cardiology            | Ahmet Yilmaz, Selin Karaca, Murat Aydin          |
| KBB                     | ENT                   | Mehmet Kaya, Hakan Ozturk, Busra Aksoy           |
| Göz Hastalıkları        | Ophthalmology         | Elif Demir, Yusuf Fatthi, Seda Korkmaz           |
| Kadın Doğum             | Gynecology            | Ahmet Akkus, Derya Uslu, Nuran Polat             |
| Diş                     | Dentistry             | Selim Arslan, Nihat Bilgin, Recep Dogan          |
| Çocuk Hastalıkları      | Pediatrics            | Dogukan Sasi, Yusuf Sert, Rabia Emirhan          |

---

### Docker Support

The backend includes a `Dockerfile` for containerized deployment.

```dockerfile
# Stage 1: Build with Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run the lightweight JAR
FROM eclipse-temurin:21-jdk-alpine
VOLUME /tmp
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

**Build and run:**
```bash
cd hospital-backend
docker build -t hospital-backend .
docker run -p 8080:8080 hospital-backend
```

---

## Frontend — Deep Dive

The frontend is a **Single-Page Application (SPA)** built with pure HTML, CSS, and JavaScript. It has no framework dependencies. All 3 "pages" exist in `index.html` at once; navigation is handled by toggling a CSS `active` class.

### Pages & Navigation

The app has 3 sections rendered in the same `index.html`:

| Page ID         | Route (via JS)      | Who Can See It      |
|-----------------|---------------------|---------------------|
| `auth-page`     | Default / logged out | Everyone            |
| `booking-page`  | After login         | Logged-in patients  |
| `calendar-page` | Nav link            | Logged-in patients  |

The `showPage(pageId)` function hides all `.page` sections and then shows the requested one. The navbar links (`Randevu Al`, `Takvimim`, `Çıkış Yap`) are hidden when logged out and shown when logged in — controlled by `updateNav()`.

On page load (`DOMContentLoaded`), the app:
1. Checks if a `jwt_token` exists in `localStorage`.
2. If it exists, decodes the JWT payload to check the `exp` (expiry) timestamp.
3. If expired → calls `logout()` to clear state and show the auth page.
4. If valid → shows the booking page directly (the user stays logged in).

---

### Authentication Flow

#### Registration
```
User fills in: Full Name, TC, Password
        ↓
handleRegister() sends POST /auth/register
        ↓
Backend hashes TC (SHA-256), hashes password (BCrypt), saves Patient to MongoDB
        ↓
Frontend shows success / error message in console
```

#### Login
```
User fills in: TC, Password
        ↓
handleLogin() sends POST /auth/login
        ↓
Backend validates → returns JWT token string
        ↓
Frontend saves token to localStorage ('jwt_token')
Frontend saves TC to localStorage ('user_tc')
        ↓
updateNav() reveals the navbar links
showPage('booking-page') is called
initBookingPage() loads doctors from API
```

#### Logout
```
logout() called (from button or expired token)
        ↓
Removes 'jwt_token' and 'user_tc' from localStorage
Resets currentUser and reservations to null/[]
        ↓
updateNav() hides nav links
showPage('auth-page') is called
```

---

### Booking Flow

After login, the user lands on the **Booking Page**, which is a 4-step flow:

```
Step 1: Select a Department (Poliklinik)
        ↓ Dropdown is populated from unique branches in the doctor list
Step 2: Select a Doctor
        ↓ Dropdown is filtered dynamically based on selected department
Step 3: Select a Date (minimum: today)
        ↓ Native date picker, past dates blocked
Step 4: Select a Time Slot
        ↓ 6 fixed slots: 09:00, 10:00, 11:00, 13:00, 14:00, 15:00
        ↓ Selected slot is highlighted
        ↓
"Randevuyu Onayla" button clicked
        ↓
confirmBooking() sends POST /reservation/create
with Authorization: Bearer <jwt_token>
Body: { doctorId, startTime, endTime } (ISO 8601 format)
        ↓
Backend checks for conflicts in that time slot
        ↓
✅ Success: "Reservation confirmed!" alert
❌ Conflict: "This time slot is already taken!" alert
```

**Time slot mechanics:** Each slot is exactly 1 hour long. The `endTime` is computed by incrementing the start hour by 1 (e.g., `09:00` → `startTime: 2026-01-15T09:00:00`, `endTime: 2026-01-15T10:00:00`).

---

### Calendar & My Appointments

When the user navigates to the **Calendar Page** (`Takvimim`):

1. `renderCalendar()` is called, which first fetches the latest doctor list (if not already loaded).
2. It then calls `fetchUserReservations()` which sends `POST /reservation/my-appointments` with the JWT token. The backend decodes the patient ID from the token and returns their reservations.
3. The calendar is rendered as a CSS grid for the current month. Each day with a reservation gets a **blue highlighted badge** showing the appointment time.
4. Below the calendar, an **"Upcoming Appointments"** list shows each appointment with:
   - Date and time
   - Doctor name and specialty (resolved by matching `doctorId` against the cached `allDoctors` list)

The user can navigate between months using `<` and `>` buttons.

---

## Setup & Running Locally

### Prerequisites

- **Java 21** (JDK)
- **Maven** (or use the included `mvnw` wrapper)
- **A MongoDB Atlas account** (free tier works perfectly)
- A modern web browser (for the frontend)

---

### 1. Configure the Database

The backend connects to MongoDB Atlas. There are two places where the connection string is configured:

**`hospital-backend/src/main/resources/application.properties`:**
```properties
spring.application.name=HospitalReservation
spring.data.mongodb.uri=mongodb+srv://<user>:<password>@<cluster>.mongodb.net/?appName=Cluster0
spring.data.mongodb.database=HospitalDB
```

**`hospital-backend/src/main/java/com/ryr/hospital/MongoConfig.java`** (the active connection used at runtime):
```java
String connectionString = "mongodb+srv://<user>:<password>@<cluster>.mongodb.net/HospitalDB?appName=Cluster0";
```

> Update `MongoConfig.java` with your own MongoDB Atlas credentials before running.

---

### 2. Run the Backend

```bash
cd hospital-backend

# Using the Maven wrapper (no Maven install needed)
./mvnw spring-boot:run

# OR on Windows
mvnw.cmd spring-boot:run
```

The backend will start on `http://localhost:8080`.

On first startup, you will see in the console:
```
🌱 Database seeded with 18 doctors!
```

On subsequent startups:
```
✅ Database already seeded. Skipping...
```

---

### 3. Run the Frontend

The frontend is 100% static — no build step required.

Simply open `hospital-frontend/index.html` directly in your browser, or serve it with any static server:

```bash
# Using Node.js (npx)
cd hospital-frontend
npx serve .
```

Then open `http://localhost:3000` (or the URL shown in your terminal).

> Make sure the backend is running on `http://localhost:8080` before using the frontend, as all API calls point there.

---

## Deployment

The backend is deployed to **Vercel** at:
👉 **https://hospital-backend-lilac.vercel.app**

To deploy your own instance:
1. Build the Docker image using the provided `Dockerfile`.
2. Push the image to a container registry (Docker Hub, GitHub Container Registry, etc.).
3. Deploy to any platform that supports Docker containers (Railway, Render, Fly.io, etc.).

---

## Available Departments & Doctors

The following departments and doctors are available out of the box after the database seeder runs:

| #  | Department (TR)    | Doctor Name     | Age |
|----|--------------------|-----------------|-----|
| 1  | Kardiyoloji        | Ahmet Yilmaz    | 27  |
| 2  | Kardiyoloji        | Selin Karaca    | 45  |
| 3  | Kardiyoloji        | Murat Aydin     | 35  |
| 4  | KBB                | Mehmet Kaya     | 40  |
| 5  | KBB                | Hakan Ozturk    | 50  |
| 6  | KBB                | Busra Aksoy     | 30  |
| 7  | Göz Hastalıkları   | Elif Demir      | 56  |
| 8  | Göz Hastalıkları   | Yusuf Fatthi    | 37  |
| 9  | Göz Hastalıkları   | Seda Korkmaz    | 29  |
| 10 | Kadın Doğum        | Ahmet Akkus     | 42  |
| 11 | Kadın Doğum        | Derya Uslu      | 32  |
| 12 | Kadın Doğum        | Nuran Polat     | 58  |
| 13 | Diş                | Selim Arslan    | 35  |
| 14 | Diş                | Nihat Bilgin    | 48  |
| 15 | Diş                | Recep Dogan     | 44  |
| 16 | Çocuk Hastalıkları | Dogukan Sasi    | 31  |
| 17 | Çocuk Hastalıkları | Yusuf Sert      | 40  |
| 18 | Çocuk Hastalıkları | Rabia Emirhan   | 31  |
