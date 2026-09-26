/**
 * Fires a wake-up ping to the backend the instant the app loads, instead of waiting for the
 * first real request (e.g. sign-in). Render's free tier puts the backend to sleep after 15
 * minutes idle, and waking it back up takes 1-4 minutes; sending this on page load means the
 * cold start overlaps with the visitor reading the page, rather than starting only once they
 * submit a form.
 *
 * Deliberately raw `fetch`, not Angular's HttpClient: this must never touch the auth/error
 * interceptors, never show a loading state, and never surface a failure — it's a best-effort
 * nudge, not a request the app depends on.
 */
export function wakeBackend(): void {
  fetch('/actuator/health/liveness', { cache: 'no-store' }).catch(() => {
    // Ignored on purpose: the real request (login, register, ...) still works even if this
    // fails, and by then the backend has had a head start waking up regardless.
  });
}
