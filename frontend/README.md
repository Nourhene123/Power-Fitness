# Power Fitness — Frontend

Angular client for Power Fitness: public marketing pages (home/about/programs/bmi calculator/
learn/contact), a 7-step assessment wizard, a member area (dashboard, plan, progress logging,
account), a coach area (review queue, plan editor, client tracking, shop management), and a
small cash-on-delivery shop. Talks to the [Spring Boot backend](../backend/README.md) over a
JSON API.

Rewrite of an older PHP app's front end — pages were ported class-for-class from the original
CSS where a design existed, then re-themed (dark/gold) where the app added new screens the old
one didn't have (member dashboard, coach plan editor, etc.).

## Stack

- Angular 17.3, **standalone components only** (no NgModules)
- Signals for local/component state, not RxJS state stores — no NgRx/Akita, and no global store
  at all beyond a couple of small singleton services with a signal on them
  (`AuthStore`, `CartService`)
- `OnPush` change detection everywhere
- Plain SCSS design system (`styles/_tokens.scss` for shared tokens), no UI kit — CDK Overlay is
  used directly for the one thing that needed it (`ModalService`)

## First-time setup

1. Node 20+, npm.
2. Backend running first (see [`../backend/README.md`](../backend/README.md)) — this app has no
   mock/offline mode, every screen past the public pages needs a real API.
3. `npm install`
4. `npm start` (this is `ng serve` with `proxy.conf.json`, not a bare `ng serve` — see below)

Runs on `http://localhost:4200`.

### The dev proxy

`proxy.conf.json` forwards `/api`, `/actuator`, and `/uploads` to `http://localhost:8080` so the
app can call relative URLs in dev instead of hardcoding a backend origin. **`ng serve` only reads
this file at startup** — if you add a new proxied path (e.g. a new top-level route the backend
serves directly), you have to restart the dev server, saving the file alone won't pick it up.
This has caused real "why is this 404ing" confusion before.

## Architecture

Feature-based, standard Angular-docs layout:

```
src/app/
  core/              singletons, loaded once: auth/ (token storage, auth store, guards live
                      alongside), api/ (base URL + generic helpers), cart/ (localStorage-backed
                      cart service), interceptors/ (auth bearer + 401 silent-refresh-replay,
                      error normalization), notifications/, models/
  shared/             reusable, dumb-ish UI used across features: components/ (notif-bell,
                      plan-view, weight-trend-chart, cart-drawer, not-found), directives/
  layout/             route-wrapper shells, one per area: public-layout, auth-layout,
                      member-layout, coach-layout, plus navbar/footer used by public-layout
  features/<name>/
    <name>.routes.ts   lazy-loaded route definitions for this feature
    data/<name>.service.ts   the only thing in the feature allowed to call HttpClient
    models/
    *.component.{ts,html,scss}   standalone, OnPush
```

Every feature is lazy-loaded via its own `*.routes.ts`; nothing is eagerly bundled into the main
chunk except `core`/`shared`/`layout`.

Auth: access token kept in memory (never localStorage — deliberate, to limit XSS blast radius),
refresh token in localStorage, an `APP_INITIALIZER` does a silent refresh on boot, and an HTTP
interceptor attaches the bearer token and replays a request once after a silent refresh on 401.
Route guards (`authGuard`/`guestGuard`/`roleGuard`, all functional guards) gate everything past
the public pages; `authGuard` supports `returnUrl` so an anonymous shopper can be bounced to
`/login` mid-checkout and land back exactly where they were, cart intact.

## Feature map

Public
- `home`, `about`, `contact` (submits to the backend's coach-request intake), `programs`, `bmi`
  calculator, `learn`.

Auth
- `login`, `register`, silent refresh, guest/auth/role guards.

Assessment
- `assessment` — the 7-step intake wizard behind `authGuard`, submits to the backend engine that
  generates the initial plan.

Member area (behind `member-layout`)
- `dashboard` — 4 states depending on pipeline stage (no assessment / in review / changes
  requested / active plan).
- `program` — full current plan, version history with an inline diff/changelog viewer,
  answer-coach form (dynamic, built from whatever fields the coach asked about) when changes were
  requested.
- `progress` — log a workout, log a weigh-in, activity timeline merging both plus plan review
  events.
- `account` — profile edit, re-do-assessment entry point, link to plan history.
- `shop` — catalog (client-side filter/sort/search over one fetched list, no per-keystroke API
  calls), checkout (auth-gated, cart survives the login round-trip), my-orders,
  order-confirmation.

Coach area (behind `coach-layout`, role-guarded)
- `dashboard` — review queue, stats (including at-risk client count), roster.
- `plan-editor` — dynamic reactive form over the entire plan structure (metrics, weekly split +
  exercises, meals, guidelines, phases); locked/approved versions render via a disabled
  `<fieldset>` instead of a separate read-only view.
- `client-profile` / `client-progress` — per-client assessment + plan + 30-day habit/weight/
  workout history, with at-risk badge and reasons surfaced (not just a red flag — actual "no
  weigh-in in 12 days" style reasons).
- `requests` — coach-request queue (from the public contact/request form).
- `shop` — product CRUD (with a real file-upload-from-disk flow into a modal form, not a URL
  text field) and the order fulfillment queue.

## Testing

```bash
npm test
```

Karma/Jasmine, `ng test`, unit-level component/service tests.

### E2E (Playwright)

```bash
npx playwright install chrome     # first time only — uses the Chrome already on your machine
npm run test:e2e                  # headless
npm run test:e2e:ui               # interactive, step-through UI with time-travel debugging
```

Drives a real browser against the real Angular dev server (`npm start`, started automatically by
`playwright.config.ts`) and the real backend (**not** started automatically — see
[`e2e/README.md`](./e2e/README.md) for the two things to run first). Current coverage is login and
role-based landing routes (`e2e/auth.spec.ts`): coach → `/coach`, fresh member → `/assessment`,
wrong credentials → inline error, unauthenticated visit to a member route → redirected to
`/login` with `returnUrl` preserved.

`playwright.config.ts` launches the browser via its **`channel`** (`chrome`, i.e. the Chrome
already installed on the machine) rather than Playwright's own downloaded Chromium — useful on
networks that block `cdn.playwright.dev`. Swap to a plain `chromium` project (and run
`npx playwright install chromium`) once that download works, for closer parity with CI.

Manual verification beyond what's automated currently follows
[`../docs/SMOKE_TEST.md`](../docs/SMOKE_TEST.md).

## Things worth knowing before you touch this

- Nullable fields coming back from the API can arrive as `undefined`, not `null` (Jackson's
  `non_null` inclusion on the backend just omits the key). Null-checks should use `== null`
  unless the TypeScript model already types the field as `| undefined`.
- `@else if (x(); as y)` is not valid Angular 17 control-flow syntax — the `as` alias only works
  on the primary `@if`. Restructure as `@else { @if (x(); as y) { ... } }`.
- `@for` with `track $index` combined with `let i = $index` doesn't resolve correctly — track the
  object itself instead of the index when you also need the index as a template variable.
- A route that reuses the same path with a different `:id` param (e.g. redirecting from one
  plan-editor URL to another after cloning a version) does **not** re-run `ngOnInit()` — Angular's
  default `RouteReuseStrategy` reuses the component instance. Subscribe to `route.paramMap`
  (it's an `Observable`) instead of reading `route.snapshot.paramMap` once, on any component whose
  `:id` can change without a full route class change.
- `styleUrl` in a component decorator resolves relative to that `.ts` file's own folder, not the
  feature root.
