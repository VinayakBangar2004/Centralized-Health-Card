# Centralized Health Card System — Backend

Spring Boot 3 (Java 17) REST API backed by MySQL. JWT auth, role-based access
control for **Doctor**, **Patient**, **Pathologist**, and **Admin**.

## The access model (read this first)

Every patient is issued one `HealthCard` at signup with two values:

| Field              | What it is                                    | Who sees it                          |
|--------------------|------------------------------------------------|---------------------------------------|
| `healthCardNumber` | Public ID printed on the card, e.g. `HC-7F3K-9D21-XQ4P` | Anyone who sees the card / QR code |
| `healthCardId`     | Secret verification key (like a PIN)           | Only the patient, never printed/QR'd  |

A doctor can pull a patient's full medical record (`POST /api/doctors/patients/lookup`)
**only if all of these are true**:

1. The doctor's account has been **verified** by an admin (license check).
2. They supply the **correct pair** of `healthCardNumber` + `healthCardId`.
3. The card is `ACTIVE` (not blocked/expired).

Every attempt — success or failure — is written to `audit_logs`, which the
patient can read back from their own dashboard ("who has looked at my record").
Patients can additionally grant a doctor **standing authorization** so they
don't have to repeat the secret ID on every visit; this is a convenience layer
on top of the hard gate, not a replacement for it. See `PatientAccessService`
for the actual enforcement code — it's the only place in the codebase that
makes this decision.

## Stack

- Spring Boot 3.3, Java 17
- Spring Security 6 + JWT (jjwt)
- Spring Data JPA + MySQL
- ZXing for QR code generation
- Spring Mail for email notifications (SMS is wired through an interface — see below)
- springdoc-openapi for Swagger UI

## Running it locally

### 1. Prerequisites
- Java 17+
- Maven 3.9+
- MySQL 8 running locally (or update the URL to point elsewhere)

### 2. Configure
Either edit `src/main/resources/application.yml` directly, or export env vars:

```bash
export DB_USERNAME=root
export DB_PASSWORD=your_mysql_password
export MAIL_USERNAME=you@gmail.com
export MAIL_PASSWORD=your_gmail_app_password   # Gmail requires an "app password", not your login password
export JWT_SECRET=$(openssl rand -base64 32)
export CORS_ORIGINS=http://localhost:5173
```

The database `health_card_db` is auto-created on first run
(`createDatabaseIfNotExist=true`), and tables are created/updated by Hibernate
(`ddl-auto: update`).

### 3. Run

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080`. Swagger UI:
`http://localhost:8080/swagger-ui.html`.

### 4. Default admin account

A `DataSeeder` creates one admin account on first boot so you can verify
doctors/pathologists:

```
email:    admin@healthcard.system
password: Admin@123
```

**Change this password immediately** — there's no self-service "forgot
password" flow for admin in this build; change it directly in the database or
extend `AdminService` with a change-password endpoint before going live.

## Troubleshooting

### `java.lang.ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag :: UNKNOWN`

This is **not a bug in this project** — it's a known Lombok/javac
compatibility break that happens when the Lombok version gets resolved
against a newer JDK than that Lombok release supports (commonly hit on
JDK 21+, and especially JDK 24+). `pom.xml` already pins an explicit,
current Lombok version (`<lombok.version>`) and wires it into both the
dependency and the `maven-compiler-plugin`'s `annotationProcessorPaths` to
avoid this. If you still hit it:

1. Run `java -version` and `mvn -version` to confirm which JDK Maven is actually using.
2. Bump `<lombok.version>` in `pom.xml` to the latest release from
   [Maven Central](https://central.sonatype.com/artifact/org.projectlombok/lombok)
   — Lombok ships frequent point releases specifically to track new JDK internals.
3. If you're on an IDE (IntelliJ/Eclipse), also update its bundled Lombok
   plugin, since the IDE's own annotation processing is separate from Maven's.
4. As a last resort, compile with JDK 17 or 21 instead of a newer JDK —
   this project only requires Java 17+.

## Notifications

- **Email** is wired with Spring Mail / JavaMailSender and is on by default
  (`app.notifications.email.enabled: true`). Point it at your SMTP provider.
- **SMS** is off by default. `NotificationService.sendSms(...)` is the single
  plug point — implement it against Twilio/AWS SNS/MSG91 and flip
  `app.notifications.sms.enabled: true`. Nothing else in the codebase needs to
  change.

## Payments

`BillingService.payBill(...)` contains a **mock settlement** (it generates a
transaction ID and marks the bill paid immediately) so the full bill →
pay → receipt flow works out of the box without a merchant account. Swap that
block for a real Razorpay/Stripe order-create + webhook-verify flow before
processing real money — the `Bill`/`Payment` entities and the rest of the
flow (notifications, audit log) don't need to change.

## A note on this build environment

This project was generated and code-reviewed in a sandbox without access to
Maven Central, so `mvn compile`/`mvn test` could not be executed here. The
code has been written carefully against Spring Boot 3.3 / Spring Security 6
APIs and reviewed by hand, but **run `mvn clean install` yourself before
deploying** to catch anything a real build would catch (it almost certainly
will compile cleanly, but please verify).

## Folder structure

```
src/main/java/com/healthcard/backend/
  config/        Security, CORS, static file serving, Swagger, admin seeder
  security/      JWT generation/validation, auth filter, UserDetailsService
  entity/        JPA entities + enums/
  repository/    Spring Data JPA repositories
  dto/           request/ and response/ DTOs
  service/       business logic (PatientAccessService is the gatekeeper)
  controller/    REST endpoints, grouped by role
  exception/     custom exceptions + @RestControllerAdvice handler
  util/          health card ID generation, QR code generation
```

## Key endpoints

| Method | Path | Role | Purpose |
|--------|------|------|---------|
| POST | `/api/auth/register/patient` | public | Sign up, auto-issues health card |
| POST | `/api/auth/register/doctor` | public | Sign up, pending verification |
| POST | `/api/auth/register/pathologist` | public | Sign up, pending verification |
| POST | `/api/auth/login` | public | Get JWT |
| GET | `/api/patients/me` | PATIENT | My profile + health card + QR |
| POST | `/api/doctors/patients/lookup` | DOCTOR | **The gated lookup** by card number + ID |
| POST | `/api/doctors/prescriptions` | DOCTOR | Issue prescription (re-verifies card) |
| POST | `/api/doctors/lab-tests` | DOCTOR | Request a lab test (re-verifies card) |
| GET | `/api/pathologists/lab-tests/queue` | PATHOLOGIST | Open/unclaimed test queue |
| POST | `/api/pathologists/lab-tests/{id}/report` | PATHOLOGIST | Upload report (multipart) |
| PATCH | `/api/admin/doctors/verify` | ADMIN | Approve/reject a doctor's license |
| GET | `/api/patients/access-log` | PATIENT | Who has accessed my record |
