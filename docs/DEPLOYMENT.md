# Deployment notes

## Local, full containerised stack

```bash
docker compose --profile full up -d --build
```

Brings up three containers:

- `powerfitness-db` — PostgreSQL 16, Flyway-migrated on backend startup, named volume `pf-db-data`.
- `powerfitness-backend` — Spring Boot, `prod` profile, port `8080`.
- `powerfitness-frontend` — nginx serving the production Angular build on port `4200`, reverse-proxying
  `/api/*` to the backend container (see `frontend/nginx.conf`).

The app is then reachable at `http://localhost:4200` with no other config. `docker compose up -d db`
(no profile) starts only Postgres, for the two dev servers (`./mvnw spring-boot:run` + `npm start`).

## Configuration

Everything the backend needs is passed as environment variables (see `docker-compose.yml`'s `backend`
service and `backend/src/main/resources/application-prod.yml`):

| Variable | Purpose |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` disables the dev-only `DataSeeder` and dev CORS defaults. |
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | Postgres connection. |
| `APP_JWT_SECRET` | HS256 signing key for access/refresh tokens — **must** be overridden for any real deployment (the compose file's value is a placeholder, not a secret). |

For a real deployment (not local docker-compose), also set:

- A JWT secret from a proper secrets manager, not source control.
- `SPRING_DATASOURCE_URL` pointing at a managed Postgres instance, with backups enabled.
- A real `CORS`/allowed-origins value if the frontend is served from a different origin than the API
  (in the containerised setup here they share an origin via the nginx proxy, so none is needed).
- HTTPS termination in front of both containers (a load balancer or an nginx/Caddy TLS layer) — neither
  container terminates TLS itself.

## Database migrations

Flyway runs automatically on backend boot (`ddl-auto: validate` — Hibernate never changes the schema).
Migrations live in `backend/src/main/resources/db/migration/`. Adding a column/table means a new
`V<n>__description.sql` file; never edit an already-shipped migration.

## What is intentionally NOT here

- No CI/CD pipeline is wired up in this repo (out of scope for the migration) — `./mvnw verify` and
  `npm run build` are the gates to run in whatever CI the deploying team uses.
- No payments/orders infrastructure — dropped for this rewrite, see `MIGRATION_PLAN.md`.
- No email/SMS delivery — notifications are in-app only (`notifications` table + bell dropdown), matching
  the original PHP app.

## Rollback

Both containers are stateless (all state is in Postgres). Rolling back is: stop the two app containers,
redeploy the previous image tags, restart. The database schema is additive-only across the shipped
migrations, so a rollback of the app does not require a database rollback unless a specific migration is
known to be backwards-incompatible (none currently are).
