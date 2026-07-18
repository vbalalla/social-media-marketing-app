# Serendia Social Dashboard

> **Unified Social Media Marketing & Ad Management Dashboard**
> Built by Serendia Solutions LLC · Stack: Spring Boot 3 / Java 21 · React 18 / Vite · PostgreSQL · Redis · Docker

---

## Repository Structure

```
social-media-marketing-app/
├── apps/
│   └── web/                    # React 18 + Vite SPA (TypeScript)
├── services/
│   ├── auth-service/           # JWT, OAuth, RBAC (Spring Boot)
│   ├── core-service/           # Workspaces, Social Accounts, Scheduler (Spring Boot)
│   ├── ai-service/             # Content Slicer, Sentiment, Fatigue (Spring Boot)
│   ├── ad-service/             # Meta/TikTok Ads, Reallocation Engine (Spring Boot)
│   └── inbox-service/          # Message Aggregation, WebSocket (Spring Boot)
├── infra/
│   ├── db/init/                # PostgreSQL initialization scripts
│   ├── nginx/                  # Local API gateway config
│   └── helm/serendia-platform/ # Kubernetes Helm chart
├── shared/
│   └── api-contracts/          # OpenAPI specifications
├── docs/                       # Architecture & planning documents
├── .github/workflows/          # CI/CD pipelines
├── docker-compose.yml          # Full local development stack
└── .env.example                # Environment variable template
```

---

## Quick Start (Local Development)

### Prerequisites
- Docker Desktop 4.x+
- Java 21 (for running services locally without Docker)
- Node.js 22+ and pnpm (for frontend)

### 1. Configure Environment

```bash
cp .env.example .env
# Edit .env and fill in required values (DB password, JWT keys, etc.)
```

### 2. Start the Full Stack

```bash
docker compose up --build
```

This starts:
| Service | URL |
|---|---|
| API Gateway (Nginx) | http://localhost:8080 |
| Auth Service | http://localhost:8081 |
| Core Service | http://localhost:8082 |
| AI Service | http://localhost:8083 |
| Ad Service | http://localhost:8084 |
| Inbox Service | http://localhost:8085 |
| React Web App | http://localhost:3000 |
| PostgreSQL | localhost:5432 |
| Redis | localhost:6379 |

### 3. Stop Everything

```bash
docker compose down          # stop, keep volumes
docker compose down -v       # stop + delete all data volumes
```

---

## Development Workflow

### Running a Single Service Locally

```bash
cd services/auth-service
mvn spring-boot:run
```

### Running Frontend

```bash
cd apps/web
pnpm install
pnpm dev         # starts Vite dev server at http://localhost:5173
```

### Running Tests

```bash
# Backend (per service)
cd services/auth-service
mvn verify

# Frontend
cd apps/web
pnpm test
pnpm test:coverage
```

---

## Architecture

See [`docs/phase1_architecture_design.md`](docs/phase1_architecture_design.md) for:
- C4 Container Diagram
- Entity-Relationship Diagram (ERD)
- OAuth 2.0 Sequence Diagram
- UI Wireframes

See [`docs/phase2_implementation_plan.md`](docs/phase2_implementation_plan.md) for the full 6-milestone implementation checklist.

---

## CI/CD

| Workflow | Trigger | What it does |
|---|---|---|
| `ci-frontend.yml` | PR / push to `apps/web` | Lint, type-check, Vitest, Vite build |
| `ci-backend.yml` | PR / push to `services/` | Maven verify (matrix across 5 services) |
| `docker-build.yml` | Push to `main` | Build + push all 6 images to GHCR |

---

## Documentation

| Document | Description |
|---|---|
| [`docs/Software_Requirements_Specification.md`](docs/Software_Requirements_Specification.md) | Full SRS |
| [`docs/phase1_architecture_design.md`](docs/phase1_architecture_design.md) | Architecture & UI design |
| [`docs/phase2_implementation_plan.md`](docs/phase2_implementation_plan.md) | Implementation checklist |

---

*Serendia Solutions LLC · July 2026*
