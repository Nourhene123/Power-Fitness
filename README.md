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

## Features

- **Auth** — register / login / refresh / logout, stateless JWT, roles `USER` / `COACH` / `ADMIN`
- **Assessment → roadmap generation** — 7-step intake wizard → rule-based feasibility/constraint
  analysis → an auto-generated 12-week workout + nutrition plan (no AI/LLM — deterministic,
  unit-tested business logic)
- **Coach review workflow** — approve, request changes, or edit-and-republish a plan; full
  version history with a plain-language changelog between versions
- **Member dashboard & progress tracking** — workout logging, weigh-ins, daily habit tracking,
  a merged activity timeline, full plan + version history
- **Coach tools** — review queue, dynamic plan editor, per-client profile & 30-day history, and
  automatic **at-risk client flagging** (stale weigh-in / no recent workout / low habit adherence)
  with the specific reason(s) surfaced, not just a red flag
- **Shop** — cash-on-delivery product catalog, cart, checkout, order history, coach/admin
  product & order management (image upload included)
- **Coach-request intake** — public "request a coach" form feeding an admin/coach queue
- **In-app notifications** — plan/order/request status changes surfaced without email or push

Full detail: [backend/README.md § "What's actually implemented"](./backend/README.md#whats-actually-implemented),
[frontend/README.md § "Feature map"](./frontend/README.md#feature-map).

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

Three layers, each proving something different — a lower layer failing tells you *what's wrong*
precisely; a higher layer failing tells you *that* something's wrong across a real user flow:

| Layer | Tool | What it proves | Needs |
|---|---|---|---|
| Unit | JUnit 5 + Mockito | The business logic is correct in isolation (every collaborator mocked) | nothing |
| Integration | JUnit 5 + Testcontainers + MockMvc | Real HTTP → Spring Security → service → **real Postgres** all work together | Docker |
| E2E | Playwright | A real browser drives the real Angular app against the real backend, like an actual user | Docker + backend + frontend running |

### Backend (unit + integration)

```bash
cd backend
./mvnw verify                                                          # everything
./mvnw test "-Dtest=AuthServiceImplTest,RefreshTokenServiceImplTest"    # just the fast unit tests
./mvnw test "-Dtest=AuthControllerIntegrationTest"                     # just one integration test
```

Real output from a unit-only run (`AuthServiceImplTest` + `RefreshTokenServiceImplTest`):

```
Running com.powerfitness.Services.AuthServiceImplTest
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.053 s
Running com.powerfitness.Services.RefreshTokenServiceImplTest
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.200 s

Results:
Tests run: 16, Failures: 0, Errors: 0, Skipped: 0

BUILD SUCCESS
```

The auth module (`register`/`login`/`refresh`/`logout`) is the current reference example for how
a feature is meant to be covered end to end:
- [`AuthServiceImplTest`](backend/src/test/java/com/powerfitness/Services/AuthServiceImplTest.java) —
  unit: email normalization, wrong-credentials handling, token issuance logic
- [`RefreshTokenServiceImplTest`](backend/src/test/java/com/powerfitness/Services/RefreshTokenServiceImplTest.java) —
  unit: token hashing, rotation, revocation
- [`AuthControllerIntegrationTest`](backend/src/test/java/com/powerfitness/RestController/AuthControllerIntegrationTest.java) —
  integration: the real `/api/auth/**` endpoints against a real disposable Postgres (Testcontainers)

Also covered at unit/integration level: `AssessmentAnalyzer`, `RoadmapGenerator`, `ClientRiskService`,
`ProgramService` — see [`backend/src/test/java/com/powerfitness/Services/`](backend/src/test/java/com/powerfitness/Services/).

### Frontend — unit

```bash
cd frontend
npm test          # Karma/Jasmine
```

### Frontend — E2E (Playwright)

Drives a real browser against the real Angular dev server and real backend — see
[`frontend/e2e/README.md`](frontend/e2e/README.md) for the two things to have running first
(`docker compose up -d db` and the backend on the `dev` profile).

```bash
cd frontend
npx playwright install chrome     # first time only — uses the Chrome already on your machine
npm run test:e2e                  # headless
npm run test:e2e:ui               # interactive, step-through UI with time-travel debugging
```

Current coverage — login and role-based landing routes
([`frontend/e2e/auth.spec.ts`](frontend/e2e/auth.spec.ts)):

- Coach signs in → lands on `/coach` dashboard
- New member registers → lands on `/assessment` (no plan generated yet)
- Wrong credentials → inline error shown, stays on `/login`
- Unauthenticated visit to `/dashboard` → redirected to `/login?returnUrl=...`

## Default accounts (dev seed)

Seeded by `DataSeeder` (only under the `dev` profile — never `prod`/`test`). Register your own `USER`
account through the app; there's no seeded client account.

| Role | Email | Password |
|---|---|---|
| Coach | `coach@powerfitness.com` | `PowerCoach123!` |
| Admin | `admin@powerfitness.com` | `PowerAdmin123!` |

## Project structure

```
powerfitness/
├── backend/                    Spring Boot API — package-by-LAYER, not by feature: every class's
│   ├── src/main/java/          architectural role is obvious from its package alone, and an
│   │   └── com/powerfitness/   ArchUnit test fails the build if a controller reaches into the
│   │       ├── RestController/   data layer directly. Full breakdown: backend/README.md.
│   │       ├── Services/         (Interface/ + Implimentation/ + framework-free domain/ engines)
│   │       ├── Repository/       Spring Data JPA interfaces
│   │       ├── Entity/           JPA entities
│   │       ├── DTO/              request/response records — the only shape a controller may touch
│   │       ├── Mapper/           entity <-> DTO (MapStruct)
│   │       ├── Security/         JWT filter/service, principal
│   │       ├── Exception/        ApiException hierarchy -> uniform JSON error body
│   │       └── Config/           security beans, dev-only DataSeeder, app properties
│   ├── src/main/resources/
│   │   └── db/migration/       Flyway SQL migrations — the only way the schema ever changes
│   └── src/test/java/          unit tests, ArchitectureTest (ArchUnit), Testcontainers integration
│                                 tests (support/AbstractIntegrationTest is the shared base)
│
├── frontend/                   Angular 17 SPA — feature-based (the opposite split from the
│   ├── src/app/                 backend, deliberately: a screen's code lives together here)
│   │   ├── core/                singletons loaded once: auth (guards/interceptors live with it),
│   │   │                        api base URL, cart, notifications
│   │   ├── shared/              reusable presentational components used across features
│   │   ├── layout/              route-shell components: public/auth/member/coach layouts
│   │   └── features/<name>/     one folder per screen area — routes, service, models, components,
│   │                            lazy-loaded so nothing eager-bundles into the main chunk
│   └── e2e/                    Playwright end-to-end tests — real browser, real backend
│
├── docs/                       API.md (endpoint reference), SMOKE_TEST.md (manual acceptance
│                                checklist), DEPLOYMENT.md (what production still needs)
│
└── docker-compose.yml          db (always), pgweb (--profile tools), full containerised
                                 stack (--profile full) — see "Run it locally" above
```

The *why* behind each layer/folder is documented where the code lives, not duplicated here — see
the **Architecture** sections of [backend/README.md](./backend/README.md#architecture) and
[frontend/README.md](./frontend/README.md#architecture) for the reasoning; this section is just
the map.
