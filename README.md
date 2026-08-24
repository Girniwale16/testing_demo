# VIS-2094 Authentication System

User authentication system with facility-scoped login, session management, and security logging for healthcare facility roster management.

## Prerequisites

- Java 17+
- Maven 3.8+
- Node.js 18+
- Docker and Docker Compose

## Quick Start

### Option 1 - Docker (recommended)

```bash
git clone <repo-url>
cd VIS-2094
cp .env.example .env
```

Fill in the required values in .env

```bash
docker-compose up --build
```

Services will be available at:
- Backend API: http://localhost:8080
- Frontend: http://localhost:3000
- PostgreSQL: localhost:5432

### Option 2 - Manual

#### Backend Setup

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend will start at http://localhost:8080

#### Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

Frontend will start at http://localhost:3000

## Running Tests

### Backend Tests

```bash
cd backend
mvn test
```

### Frontend Tests

```bash
cd frontend
npm test
```

## Environment Variables

See `.env.example` for all required environment variables:

- `SPRING_DATASOURCE_URL` - PostgreSQL connection URL
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password
- `POSTGRES_USER` - PostgreSQL user for Docker
- `POSTGRES_PASSWORD` - PostgreSQL password for Docker
- `POSTGRES_DB` - PostgreSQL database name
- `POSTGRES_PORT` - PostgreSQL port (default 5432)
- `VITE_API_URL` - Backend API URL for frontend

## API Endpoints

### POST /api/v1/auth/login
Authenticate user with username, password, and facility scope.

**Request:**
```json
{
  "username": "string",
  "password": "string",
  "facilityId": "integer"
}
```

**Response (200):**
```json
{
  "userId": "integer",
  "username": "string",
  "role": "string",
  "facilityId": "integer",
  "facilityName": "string",
  "message": "string"
}
```

### POST /api/v1/auth/logout
Invalidate current session.

**Response (200):**
```json
{
  "message": "string"
}
```

### GET /api/v1/auth/session
Get current authenticated user session information.

**Response (200):**
```json
{
  "userId": "integer",
  "username": "string",
  "role": "string",
  "facilityId": "integer",
  "facilityName": "string",
  "isActive": "boolean"
}
```