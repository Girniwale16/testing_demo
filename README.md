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