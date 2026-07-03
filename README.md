# Healthcare Appointment & Follow-up Manager

A full-stack clinic management platform with separate portals for patients, doctors, and admins. Patients book appointments and share symptoms in advance. An AI generates a pre-visit summary with urgency level for the doctor. After the visit, the doctor submits notes and the AI generates a patient-friendly post-visit summary. Both sides receive email confirmations and Google Calendar events.

---

## Tech Stack

- **Backend**: Java 17, Spring Boot 4.1.0, Spring Security, Spring Data JPA
- **Database**: PostgreSQL 18
- **Authentication**: JWT (jjwt 0.12.6)
- **AI**: Groq API (llama-3.3-70b-versatile)
- **Email**: Brevo (Sendinblue) REST API
- **Calendar**: Google Calendar API v3 with OAuth 2.0
- **Frontend**: React 18, Vite, Axios
- **Build Tool**: Maven

---

## Project Structure

```
care/
├── health/                         # Spring Boot backend
│   └── src/main/java/com/navneet/health/
│       ├── config/                 # JWT, Security, Google Calendar config
│       ├── controller/             # REST API endpoints
│       ├── dto/groq/               # Groq API request/response DTOs
│       ├── entity/                 # JPA entities (User, Doctor, Appointment, Prescription)
│       ├── repository/             # Spring Data JPA interfaces
│       └── service/                # Business logic, AI, Email, Calendar, Reminders
└── frontend/                       # React frontend
    └── src/
        ├── pages/                  # Login, Register, PatientDashboard, DoctorDashboard, AdminDashboard
        └── services/               # Axios API client
```

---

## Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 18
- Node.js 22+
- A Groq API key (free at console.groq.com)
- A Brevo API key (free at brevo.com)
- A Google Cloud project with Calendar API enabled

---

## Setup Guide

### 1. Database

Open pgAdmin and create a database:

```sql
CREATE DATABASE healthdb;
```

After running the app once (Hibernate creates tables automatically), run this index for double-booking prevention:

```sql
CREATE UNIQUE INDEX uq_doctor_slot_active
ON appointment (doctor_id, appointment_date, appointment_time)
WHERE status <> 'CANCELLED';
```

Also alter text columns to avoid 255 character limit:

```sql
ALTER TABLE appointment
ALTER COLUMN pre_visit_summary TYPE TEXT,
ALTER COLUMN post_visit_summary TYPE TEXT,
ALTER COLUMN symptoms TYPE TEXT,
ALTER COLUMN doctor_notes TYPE TEXT;
```

### 2. Backend Configuration

Copy `.env.example` to `application.properties` and fill in your values:

```
src/main/resources/application.properties
```

### 3. Google Calendar Setup

- Go to console.cloud.google.com
- Create a project named "Healthcare System"
- Enable the Google Calendar API
- Go to APIs & Services → Credentials → Create OAuth 2.0 Client ID
- Application type: Web application
- Add authorized redirect URI: `http://localhost:8888/Callback`
- Download the JSON file and rename it `credentials.json`
- Place it in `src/main/resources/credentials.json`
- Go to OAuth consent screen → Test users → add your Gmail account
- On first run, a browser window opens for authorization — log in and allow access
- Tokens are saved to the `tokens/` folder automatically

### 4. Run the Backend

```bash
cd health
./mvnw spring-boot:run
```

App starts on `http://localhost:8081`

On first run: a browser window opens for Google OAuth — authorize it, then the app continues starting.

### 5. Run the Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend starts on `http://localhost:5173`

---

## Environment Variables (.env.example)

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/healthdb
spring.datasource.username=postgres
spring.datasource.password=YOUR_POSTGRES_PASSWORD

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Server
server.port=8081

# Groq AI
groq.api.key=YOUR_GROQ_API_KEY
groq.api.url=https://api.groq.com/openai/v1/chat/completions
groq.model=llama-3.3-70b-versatile

# Brevo Email
brevo.api.key=YOUR_BREVO_API_KEY
brevo.from.email=YOUR_VERIFIED_EMAIL
brevo.from.name=Healthcare App

# Google Calendar
google.calendar.credentials.path=src/main/resources/credentials.json
google.calendar.tokens.path=tokens
```

---

## API Documentation

### Auth

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | /api/auth/register | Register new user | No |
| POST | /api/auth/login | Login and get JWT token | No |

**Register Request:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "secret123",
  "role": "PATIENT"
}
```

**Login Request:**
```json
{
  "email": "john@example.com",
  "password": "secret123"
}
```

**Login Response:**
```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9..."
}
```

All protected endpoints require: `Authorization: Bearer <token>`

---

### Doctors

| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| POST | /api/admin/doctors | Create doctor profile | ADMIN |
| GET | /api/doctors?specialization= | Search doctors | Any |
| GET | /api/doctors/{id}/slots?date= | Get available slots | Any |
| POST | /api/admin/doctors/{id}/leave | Mark doctor leave day | ADMIN |

**Create Doctor Request:**
```json
{
  "user": { "id": 2 },
  "specialization": "Cardiology",
  "workingHoursStart": "09:00",
  "workingHoursEnd": "17:00",
  "slotDurationMinutes": 30,
  "leaveDays": []
}
```

**Get Slots Response:**
```json
["09:00:00", "09:30:00", "10:00:00", "10:30:00"]
```

---

### Appointments

| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| POST | /api/appointments/book | Book appointment | PATIENT |
| GET | /api/appointments/patient/{id} | Get patient appointments | PATIENT |
| GET | /api/appointments/doctor/{id} | Get doctor appointments | DOCTOR |
| PUT | /api/appointments/{id}/cancel | Cancel appointment | Any |
| PUT | /api/appointments/{id}/notes | Submit doctor notes | DOCTOR |
| POST | /api/appointments/{id}/prescription | Add prescription | DOCTOR |

**Book Appointment Request:**
```json
{
  "patient": { "id": 1 },
  "doctor": { "id": 1 },
  "appointmentDate": "2026-07-10",
  "appointmentTime": "09:00",
  "symptoms": "Chest pain and shortness of breath for 3 days"
}
```

**Book Appointment Response:**
```json
{
  "id": 1,
  "appointmentDate": "2026-07-10",
  "appointmentTime": "09:00:00",
  "status": "CONFIRMED",
  "symptoms": "Chest pain and shortness of breath for 3 days",
  "preVisitSummary": "Urgency Level: High\nChief Complaint: Chest pain...",
  "calendarEventId": "abc123xyz"
}
```

---

## Database Schema

```
users
├── id (BIGSERIAL PK)
├── name (VARCHAR)
├── email (VARCHAR UNIQUE NOT NULL)
├── password (VARCHAR - BCrypt hashed)
└── role (VARCHAR - PATIENT/DOCTOR/ADMIN)

doctor
├── id (BIGSERIAL PK)
├── user_id (FK → users.id)
├── specialization (VARCHAR)
├── working_hours_start (VARCHAR)
├── working_hours_end (VARCHAR)
└── slot_duration_minutes (INT)

doctor_leave_days
├── doctor_id (FK → doctor.id)
└── leave_days (DATE)

appointment
├── id (BIGSERIAL PK)
├── patient_id (FK → users.id)
├── doctor_id (FK → doctor.id)
├── appointment_date (DATE)
├── appointment_time (TIME)
├── status (VARCHAR - PENDING/CONFIRMED/CANCELLED)
├── symptoms (TEXT)
├── doctor_notes (TEXT)
├── pre_visit_summary (TEXT)
├── post_visit_summary (TEXT)
└── calendar_event_id (VARCHAR)

prescription
├── id (BIGSERIAL PK)
├── appointment_id (FK → appointment.id)
├── medication_name (VARCHAR)
├── frequency_per_day (INT)
├── duration_days (INT)
└── instructions (VARCHAR)

Unique Index:
  uq_doctor_slot_active ON appointment(doctor_id, appointment_date, appointment_time)
  WHERE status <> 'CANCELLED'
```

---

## LLM Prompts

### Pre-Visit Summary (sent to Groq on booking)

```
You are an experienced physician.

Analyze these symptoms and provide:
1. Urgency Level (Low/Medium/High)
2. Chief Complaint
3. Three questions the doctor should ask.

Symptoms: <patient_symptoms>
```

### Post-Visit Summary (sent to Groq when doctor submits notes)

```
You are a medical assistant.

Convert these doctor's notes into patient-friendly language.

Include:
1. Diagnosis
2. Medication Schedule
3. Lifestyle Advice
4. Follow-up Instructions

Doctor Notes: <doctor_notes>
```

Both prompts are wrapped in try/catch — if Groq is unavailable, the system stores "Pre-visit summary unavailable" or "Post-visit summary unavailable" and continues normally without crashing.

---

## Deployment

- Backend: Render (render.com) — deploy as a Web Service from GitHub
- Frontend: Vercel (vercel.com) — deploy from the frontend folder

Update `frontend/src/services/api.js` baseURL to your Render backend URL before deploying frontend.
