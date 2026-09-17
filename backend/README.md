# Power Fitness — Backend

Spring Boot API for Power Fitness, a personal-coaching platform: a client fills out a 7-step
fitness assessment, a rule-based engine analyzes it and generates a 12-week workout/nutrition
roadmap, a coach reviews and can edit/approve/reject it, and the client then tracks progress
(weigh-ins, workouts, habits) against the approved plan. A small cash-on-delivery shop is bolted
on for supplements/apparel.

This is a rewrite of an older PHP/MySQL app of the same name. Functionality was ported 1:1 except
payments (the original's shop was already cash-on-delivery only — no real payment gateway was
ever wired in, despite leftover "Stripe Secured" marketing copy on the old shop page).

## Stack

- Java 17, Spring Boot 4 (`spring-boot-starter-webmvc`, not `-web` — SB4 renamed it)
- PostgreSQL 16, Flyway migrations, Hibernate with `ddl-auto: validate` (schema changes only
  happen through a new migration file, never through JPA auto-DDL)
- Spring Security with JWT (stateless, no sessions) — `jjwt`
- MapStruct for entity↔DTO mapping, Lombok
- ArchUnit test enforcing the architecture rules below
- Testcontainers (real Postgres in tests, not H2)

## First-time setup

1. JDK 17 installed and on `PATH`.
2. Docker Desktop running (used for Postgres; on Windows it has to be started manually —
   `Docker Desktop.exe` doesn't autostart with the OS by default).
3. Start the database only: `docker compose up -d db` (run from the repo root, not `backend/`).
   - Published on host port **5433**, not 5432 — if you also have a native/local Postgres
     service running on 5432, they won't collide.
4. `./mvnw spring-boot:run` from `backend/`. Runs under the `dev` Spring profile by default,
   which is what points at `jdbc:postgresql://localhost:5433/powerfitness` and enables the dev
   seeder.

API comes up on `http://localhost:8080`.

Seeded accounts (dev profile only, never `prod`/`test` — see `Config/DataSeeder`):

| Role  | Email                       | Password         |
|-------|------------------------------|------------------|
| Coach | coach@powerfitness.com       | PowerCoach123!   |
| Admin | admin@powerfitness.com       | PowerAdmin123!   |

There's no seeded regular user — register one through the app (or via `POST /api/auth/register`).

## Running the whole thing in Docker instead

`docker compose --profile full up --build` builds and runs `backend` + `frontend` (nginx serving
the Angular build, reverse-proxying `/api` and `/uploads` to the backend container) + `db`, all
on the compose network, under the `prod` Spring profile. This exists to prove the app is
deployable as containers, not for day-to-day development — the edit/rebuild loop is much slower
than `spring-boot:run` + a local JAR. Use plain `spring-boot:run` while actively coding, and only
reach for `--profile full` to sanity-check a release build or before deploying somewhere.

## Architecture

Package-by-**layer**, not package-by-feature — every class lives under its architectural role,
not its domain area:

```
com.powerfitness/
  Config/                       security beans, JPA auditing, Jackson config, dev-only DataSeeder
  DTO/                          request/response records — the only shape a controller may touch
  Entity/                       JPA entities + enums
  Exception/                    ApiException hierarchy + GlobalExceptionHandler (-> JSON ApiError)
  Mapper/                       entity <-> DTO (MapStruct)
  Repository/                   Spring Data JPA interfaces
  RestController/                @RestController classes
  Security/                     JwtService, JwtAuthenticationFilter, principal, user-details service
  Services/
    Interface/                  one interface per service
    Implimentation/             impl classes — all entity<->DTO mapping happens here (sic on the name,
                                 kept consistent with an existing sibling project's convention)
    domain/                     framework-free value objects (PlanContent, AnalysisResult, ...)
  Validation/                   custom bean-validation constraints
  common/util/                  JSON helpers etc., no business logic
```

Hard rule, enforced by `src/test/.../ArchitectureTest.java` (ArchUnit): **a `RestController` may
not reference `Entity` or `Repository` at all** — not even as an import. Controllers only see
`DTO` types; all entity access and mapping happens in the service layer. If `mvnw verify` fails
on this rule, the fix is to move the mapping into the service/mapper, not to relax the rule.

The assessment→analysis→roadmap pipeline (`AssessmentAnalyzer`, `RoadmapGenerator` in
`Services/`) is deliberately framework-free — plain Java operating on `Services/domain` value
objects, no Spring annotations, no entity access. It's pure business logic ported from the old
PHP analyzer/generator scripts and is unit-tested in isolation.

Auth: stateless JWT. Access token is short-lived and sent as a bearer header; refresh tokens are
opaque random tokens (SHA-256 hash stored server-side, rotated on every use, revocable). No
server-side session state beyond the refresh-token table.

## What's actually implemented

Auth & users
- Register/login/refresh/logout, roles `USER` / `COACH` / `ADMIN`, `PATCH /users/me`.

Assessment → plan generation
- 7-step intake form → `AssessmentAnalyzer` (goal feasibility, constraints) →
  `RoadmapGenerator` (12-week phased plan: weekly split, exercises, meals, guidelines) →
  program created in `IN_REVIEW` state, coach notified.

Program review state machine
- Coach can approve, request changes (client answers a dynamic set of follow-up questions and it
  goes back to `IN_REVIEW`), or edit and publish a new version. Version history is kept
  (`ProgramVersion`), each with a plain-language changelog against the previous version
  (`ChangelogService`). Locked (approved) versions are read-only until a new draft is opened.

Member dashboard & progress tracking
- Dashboard state depends on where the client is in the pipeline (no assessment yet / awaiting
  review / changes requested / active plan).
- Workout session logging, weigh-ins, daily habit toggles (water/workout/nutrition/sleep),
  merged activity timeline.
- Full program view + plan version history.

Coach tools
- Dashboard (review queue, stats, roster), plan editor (dynamic form over the full plan
  structure), per-client profile and 30-day progress view.
- **At-risk client flagging** (`ClientRiskService`): flags a client if they haven't weighed in
  for 10+ days, haven't logged a workout for 7+ days, or habit completion over the last 7 days
  is under 40% — with the specific reason(s) surfaced, not just a boolean. Shared between the
  roster and the client profile so the same client reads as at-risk consistently in both places.

Shop (cash on delivery only — no payment gateway)
- Public product catalog, cart is client-side (nothing server-side until checkout).
- Order placement, order history, image upload for products (stored on local disk under
  `app.uploads.dir`, served back at `/uploads/**`), coach/admin CRUD on products and order status.

Coach-request intake
- Public "request a coach" form (unauthenticated) feeding an admin/coach-facing request queue.

Notifications
- In-app notifications on the events above (plan submitted/approved/changes requested, order
  status changes, new coach request, etc.) — no email/push, just an in-app list.

See [`../docs/API.md`](../docs/API.md) for the full endpoint reference and
[`../docs/SMOKE_TEST.md`](../docs/SMOKE_TEST.md) for a manual walkthrough of all of the above.

## Testing

```bash
./mvnw verify
```

Runs unit tests, the ArchUnit architecture test, and Testcontainers-backed integration tests
(spins up a real disposable Postgres — Docker must be running). There's no separate Maven
goal for "just the unit tests"; `verify` is what CI and the dev loop both use. For a fast
inner loop while you're only touching business logic, target specific classes instead:

```bash
./mvnw test "-Dtest=AuthServiceImplTest,RefreshTokenServiceImplTest"   # unit only, no Docker
./mvnw test "-Dtest=AuthControllerIntegrationTest"                     # one integration test
```

Naming convention: `*Test` for a plain unit test (no Spring context, collaborators mocked with
Mockito), `*IntegrationTest` for anything extending `support/AbstractIntegrationTest`
(`@SpringBootTest`, real Testcontainers Postgres). `AuthServiceImplTest` /
`RefreshTokenServiceImplTest` / `AuthControllerIntegrationTest` are the reference example of
covering a feature at both levels — unit tests isolate the logic, the integration test proves the
real HTTP → Security → DB stack agrees with it.

## Things that will trip you up

- `ddl-auto: validate` means the app **will not start** if the schema doesn't match the entities.
  Schema changes always go through a new `V{n}__description.sql` file under
  `src/main/resources/db/migration`, never through editing an entity and hoping Hibernate
  figures it out.
- Nullable DTO fields are omitted from JSON entirely (`non_null` inclusion in
  `application.yml`), not sent as `null`. On the frontend this means a genuinely-empty field
  arrives as `undefined`, not `null` — matters if you're adding a new nullable field and writing
  a null-check for it on the Angular side.
- The `Services/Implimentation` package name is intentionally spelled that way (not a typo to
  fix) — it matches the naming convention of a sibling project this codebase was deliberately
  aligned with.
