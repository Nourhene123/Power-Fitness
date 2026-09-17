import { defineConfig, devices } from '@playwright/test';

/**
 * E2E tests drive the real Angular dev server against the real backend (see e2e/README.md for
 * the two things you need running first: `docker compose up -d db` and the Spring Boot app on
 * the `dev` profile — the same prerequisites as the project's normal "Run it locally" flow).
 * Only the Angular dev server is started automatically here; the backend isn't, because its
 * seeded coach/admin accounts and Postgres data need to persist across runs.
 */
const isCI = !!process.env['CI'];

export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  forbidOnly: isCI,
  retries: isCI ? 2 : 0,
  workers: isCI ? 2 : undefined,
  reporter: 'html',
  timeout: 30_000,
  use: {
    baseURL: process.env['E2E_BASE_URL'] ?? 'http://localhost:4200',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },
  projects: [
    // Uses the Google Chrome already installed on this machine instead of Playwright's own
    // downloaded Chromium — `npx playwright install` needs to reach cdn.playwright.dev, which
    // times out on networks that block or throttle it. Swap to `chromium` (and run the install
    // command) once that download works, for closer parity with CI.
    { name: 'chrome', use: { ...devices['Desktop Chrome'], channel: 'chrome' } },
  ],
  webServer: {
    command: 'npm start',
    url: 'http://localhost:4200',
    reuseExistingServer: !isCI,
    timeout: 120_000,
  },
});
