# Smoke-test checklist

Manual acceptance pass for a fresh stack (`docker compose --profile full up --build`, or the two dev
servers + `docker compose up -d db`). Mirrors the PHP app's flows end to end. Check each box against a
clean database (or a fresh test user) before calling a release good.

## Marketing / anonymous

- [ ] `/` loads (hero, programs teaser, testimonials).
- [ ] `/about`, `/programs`, `/bmi`, `/learn`, `/contact` all render.
- [ ] `/contact` form submit creates a `coach_requests` row (check via `/admin` after logging in as coach).
- [ ] `/bmi` calculator computes without a network call.
- [ ] An unknown path (e.g. `/no-such-page`) shows the 404 page, not a blank screen.

## Auth

- [ ] Register a new account → auto-logged-in → redirected to `/assessment` (no assessment yet).
- [ ] Log out, log back in with the same credentials → redirected to `/dashboard` (assessment on file) or
      `/assessment` (none).
- [ ] Refresh the page while logged in — session survives (silent refresh on boot).
- [ ] Visiting a guarded route while logged out bounces to `/login`.
- [ ] An expired/invalid access token triggers a silent refresh-and-replay, not a logout, unless the
      refresh token is also invalid.

## Client journey (`USER` role)

- [ ] Complete the 7-step assessment (all steps gate correctly, injury repeater works) → submission
      creates a program in `IN_REVIEW` and a coach notification.
- [ ] Dashboard shows the **in-review** state with a metrics preview.
- [ ] As coach, approve the version (see below) → client dashboard flips to **active**: goal line,
      phase/week, today's session or rest day, macros, habit chips, weight trend, adherence bars, streak,
      coach note/focus, next milestone.
- [ ] Click a habit chip → toggles instantly (optimistic) and persists (reload confirms).
- [ ] "Start workout" from the dashboard opens `/progress/log/workout` prefilled to today's session day.
- [ ] Log a workout session → appears in "Recent sessions" and marks today's workout habit.
- [ ] Log a weigh-in → appears in history; a second weigh-in the same day **updates** the same row
      (upsert), doesn't duplicate.
- [ ] Out-of-range weight (e.g. 5 kg) and a future-dated entry are both rejected with a clear message.
- [ ] `/progress/history` shows a merged, newest-first timeline of weigh-ins, workouts, and plan events.
- [ ] `/program` (full plan) renders phases, weekly split, meal plan, guidelines.
- [ ] `/program/history` lists versions with changelog; opening one shows its full content (locked/read-only).
- [ ] When the coach requests changes, `/program/answer-coach` prefills current values and resubmitting
      flips the version back to `IN_REVIEW` and notifies the coach.
- [ ] `/account` — edit name (persists, sidebar updates), "re-do assessment" shows the correct count.
- [ ] Bell icon shows the correct unread count; opening it and marking read updates the badge.

## Coach journey (`COACH`/`ADMIN` role)

- [ ] Coach dashboard shows stats, the pending-review queue, recently-approved list, and active roster.
- [ ] Opening a pending version loads the plan editor with the client's profile alongside it.
- [ ] Edit a field, save draft → client does **not** yet see it (still `IN_REVIEW`, no notification of a
      content change).
- [ ] Approve → version goes `ACTIVE`, any prior active version is superseded, client is notified and
      their dashboard reflects it live on next load.
- [ ] Request changes with specific fields + a note → client sees the exact fields/note on `answer-coach`.
- [ ] On an already-`ACTIVE` version, "new version" clones it into an editable `DRAFT` instead of erroring;
      calling it twice returns the same draft (no duplicates).
- [ ] Client profile page shows assessment + analysis + current plan; client progress page shows the
      30-day habit matrix and weight log.
- [ ] `/coach/requests` (admin) lists contact-form submissions with status filters; status update and
      delete both work.
- [ ] Navigating between two different client plans (`/coach/plan/:id` → a different `:id`) shows the new
      client's data, not the previous one (route-reuse regression guard).

## Cross-cutting

- [ ] No console errors on any of the above pages (check devtools / `read_console_messages`).
- [ ] Every list/detail page has a real loading state (not a flash of empty content) and a real empty
      state (not a silently blank card) — dashboard, program history, coach dashboard, coach requests,
      progress timeline all covered.
- [ ] Resizing to a mobile width keeps the member/coach sidebars usable (hamburger toggle) and no
      horizontal scroll on any page.
- [ ] `prefers-reduced-motion` is respected (no forced animation) — spot check the auth pages and 404.

## Backend

- [ ] `cd backend && ./mvnw verify` green — ArchUnit layering rules + Testcontainers integration tests.
- [ ] `docker compose --profile full up --build` — all three containers healthy, app reachable at
      `http://localhost:4200` with API calls proxied through nginx to the backend container.

## Frontend

- [ ] `cd frontend && npm run build` — clean production build, no budget warnings beyond the configured
      threshold.
