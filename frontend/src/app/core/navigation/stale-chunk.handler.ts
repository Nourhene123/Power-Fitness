import { NavigationError } from '@angular/router';

const RELOAD_KEY = 'pf-stale-chunk-reload';

/**
 * After a redeploy, a tab still running the previous build asks for lazy chunks whose hashed
 * filenames no longer exist, and navigation fails. Reloading picks up the new build. The
 * sessionStorage flag limits this to one reload a minute, so a real outage can't cause a loop.
 */
export function reloadOnStaleChunk(error: NavigationError): void {
  const message = String((error.error as Error | undefined)?.message ?? error.error);
  const isStaleChunk = /Failed to fetch dynamically imported module|Importing a module script failed|error loading dynamically imported module/i
    .test(message);
  if (!isStaleChunk) {
    return;
  }

  const lastReload = Number(sessionStorage.getItem(RELOAD_KEY) ?? 0);
  if (Date.now() - lastReload < 60_000) {
    return;
  }
  sessionStorage.setItem(RELOAD_KEY, String(Date.now()));
  // Go straight to the page the user was navigating to.
  window.location.assign(error.url);
}
