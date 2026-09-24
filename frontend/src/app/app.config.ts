import { APP_INITIALIZER, ApplicationConfig, ENVIRONMENT_INITIALIZER, inject, provideZoneChangeDetection } from '@angular/core';
import {
  provideRouter,
  withComponentInputBinding,
  withInMemoryScrolling,
  withNavigationErrorHandler,
} from '@angular/router';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';

import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';
import { AuthService } from './core/auth/auth.service';
import { SeoService } from './core/seo/seo.service';
import { reloadOnStaleChunk } from './core/navigation/stale-chunk.handler';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(
      routes,
      withComponentInputBinding(),
      withInMemoryScrolling({ scrollPositionRestoration: 'top', anchorScrolling: 'enabled' }),
      withNavigationErrorHandler(reloadOnStaleChunk),
    ),
    provideHttpClient(withFetch(), withInterceptors([authInterceptor, errorInterceptor])),
    provideAnimationsAsync(),
    {
      // Restore a session from the persisted refresh token before the app renders.
      provide: APP_INITIALIZER,
      multi: true,
      useFactory: () => {
        const auth = inject(AuthService);
        return () => auth.initialize();
      },
    },
    {
      // Per-page meta description, canonical URL and link-preview tags.
      provide: ENVIRONMENT_INITIALIZER,
      multi: true,
      useValue: () => inject(SeoService).init(),
    },
  ],
};
