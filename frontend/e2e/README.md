# E2E tests (Playwright)

Browser-driven tests for real user journeys (login, registration, route guards) against the real
app — no mocked HTTP layer. Playwright starts the Angular dev server for you, but **not** the
backend, because its seeded coach/admin accounts and Postgres data need to survive across runs.

## Prerequisites

Same as the project's normal local run (see the root `README.md`), started once beforehand:

```bash
docker compose up -d db

cd backend && ./mvnw spring-boot:run   # dev profile, http://localhost:8080
```

## Install (first time only)

```bash
npm install
npx playwright install chromium
```

## Run

```bash
npm run test:e2e        # headless
npm run test:e2e:ui     # interactive UI mode
```

`playwright.config.ts` starts `npm start` (the Angular dev server on port 4200) automatically and
reuses it if it's already running.
