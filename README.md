# Power Fitness

Personal-coaching platform: a member completes a science-based fitness assessment, gets a
coach-reviewed 12-week workout & nutrition roadmap, and tracks progress against it week over
week. Coaches run their client roster through a full review/edit workflow and get flagged when a
client is falling behind. A small cash-on-delivery shop sells supplements and apparel on the
side.

## About this project

Power Fitness runs the online coaching business of my brother, a personal trainer. It started as
a PHP/MySQL site he had been running for a while. As the business grew, the original app hit the
ceiling that any monolithic PHP app eventually hits: business logic mixed directly into view
scripts, no automated tests, no enforced boundary between the data layer and everything else, and
every new feature risking a regression somewhere unrelated.

This repository is a ground-up rewrite as a proper **Spring Boot + Angular monorepo** — same
functionality end to end (assessment → analysis → roadmap generation → coach review → member
dashboard → progress tracking → shop), rebuilt on an architecture meant to actually scale with
the business instead of fighting it: a layered backend with a boundary the build enforces (an
ArchUnit test fails CI if a controller reaches into the data layer), stateless JWT auth, a
migration-tracked schema instead of ad-hoc SQL, and a feature-based Angular front end instead of a
page-per-PHP-script structure.

| Part | Stack |
|---|---|
| `backend/` | Spring Boot 4 · Java 17 · PostgreSQL 16 · Flyway · Spring Security (JWT) · clean layered architecture (entity / repository / dto / mapper / service+impl / controller / exception per feature) |
| `frontend/` | Angular 17 · standalone components · feature-based routing · SCSS design system |

Full architecture, implemented features, and known gotchas for each side live in
[`backend/README.md`](./backend/README.md) and [`frontend/README.md`](./frontend/README.md).
See [`docs/API.md`](./docs/API.md) for the endpoint reference,
[`docs/SMOKE_TEST.md`](./docs/SMOKE_TEST.md) for the manual acceptance checklist, and
[`docs/DEPLOYMENT.md`](./docs/DEPLOYMENT.md) for what a real deployment still needs.

## Prerequisites

- JDK 17
- Node 20+ / npm
- Docker (for PostgreSQL)

## Run it locally

```bash
# 1. database
docker compose up -d db

# 2. backend  ->  http://localhost:8080
cd backend
./mvnw spring-boot:run            # uses the 'dev' profile by default

# 3. frontend ->  http://localhost:4200
cd frontend
npm install
npm start
```

The Docker Postgres is published on **host port 5433** (to sidestep a native Postgres on 5432).

Optional DB UI: `docker compose --profile tools up -d pgweb` → http://localhost:8081
Full containerised stack: `docker compose --profile full up --build`

## Tests

```bash
cd backend  && ./mvnw verify           # unit + slice + Testcontainers integration
cd frontend && npm test                # Karma/Jasmine
cd frontend && npm run test:e2e        # Playwright, needs db + backend running — see frontend/e2e/README.md
```

## Default accounts (dev seed)

Seeded by `DataSeeder` (only under the `dev` profile — never `prod`/`test`). Register your own `USER`
account through the app; there's no seeded client account.

| Role | Email | Password |
|---|---|---|
| Coach | `coach@powerfitness.com` | `PowerCoach123!` |
| Admin | `admin@powerfitness.com` | `PowerAdmin123!` |

## Repository layout

```
backend/     Spring Boot API — package-by-layer, ArchUnit-enforced boundaries. See backend/README.md.
frontend/    Angular SPA — feature-based, standalone components. See frontend/README.md.
docs/        API reference, manual smoke-test checklist, deployment notes.
```
