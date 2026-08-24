# VIS-2092

Frontend login screen with protected routing and authentication state management.

## Prerequisites

- Node.js 18+
- Docker and Docker Compose

## Quick Start

### Option 1 — Docker (recommended)

```
git clone <repo-url>
cd VIS-2092
cp .env.example .env
```

Fill in the required values in .env

```
docker-compose up --build
```

Service URLs:
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080 (dependency IA-02/IA-03)

### Option 2 — Manual

Frontend setup:
```
cd frontend
npm install
npm run dev
```

Frontend will be available at http://localhost:3000

## Running Tests

Frontend tests:
```
cd frontend
npm test
```

## Environment Variables

VITE_API_URL - Backend API base URL (default: /api)

## API Endpoints

POST /api/auth/login - Authenticate user with username and password
POST /api/auth/logout - Invalidate current session
GET /api/auth/me - Retrieve current authenticated user profile
# VIS-2093

Facility and User Account Management - Database persistence layer for multi-facility user authentication with facility-scoped username uniqueness.

## Prerequisites

- Java 17+
- Maven 3.8+
- Docker and Docker Compose

## Quick Start

### Option 1 — Docker (recommended)

```
git clone <repo-url>
cd VIS-2093
cp .env.example .env
```

Fill in the required values in .env

```
docker-compose up --build
```

Service URLs:
- Backend: http://localhost:8080
- PostgreSQL: localhost:5432

### Option 2 — Manual

Backend setup:
```
cd backend
mvn clean install
mvn spring-boot:run
```

## Running Tests

Backend tests:
```
cd backend
mvn test
```

## Environment Variables

- `DATABASE_URL` - PostgreSQL connection string (format: jdbc:postgresql://host:port/database)
- `DATABASE_USERNAME` - PostgreSQL username
- `DATABASE_PASSWORD` - PostgreSQL password
- `POSTGRES_USER` - PostgreSQL user for Docker container
- `POSTGRES_PASSWORD` - PostgreSQL password for Docker container
- `POSTGRES_DB` - PostgreSQL database name
- `POSTGRES_PORT` - PostgreSQL port (default: 5432)
- `SERVER_PORT` - Backend server port (default: 8080)

## Database Schema

### facility table
- `facility_id` (BIGSERIAL PRIMARY KEY)
- `timezone` (VARCHAR(100) NOT NULL) - IANA timezone identifier
- `region_code` (VARCHAR(50)) - Optional region code
- `is_active` (BOOLEAN DEFAULT TRUE)
- `created_at`, `created_by`, `updated_at`, `updated_by` - Audit columns

### user_account table
- `user_account_id` (BIGSERIAL PRIMARY KEY)
- `facility_id` (BIGINT NOT NULL FK to facility)
- `username` (VARCHAR(100) NOT NULL) - Facility-scoped unique
- `password_hash` (VARCHAR(255) NOT NULL)
- `role` (VARCHAR(50) NOT NULL) - CHECK constraint: MANAGER, STAFF, SUPERVISOR
- `staff_member_id` (BIGINT) - Optional, maps STAFF role to staff member
- `created_at`, `created_by`, `updated_at`, `updated_by` - Audit columns

Constraints:
- UNIQUE (facility_id, username)
- INDEX on (facility_id, role)
- INDEX on (staff_member_id)