import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { TokenStorageService } from '../auth/token-storage.service';

const AUTH_SKIP = ['/auth/login', '/auth/register', '/auth/refresh'];

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const tokens = inject(TokenStorageService);
  const auth = inject(AuthService);

  const isAuthCall = AUTH_SKIP.some((p) => req.url.includes(p));
  const token = tokens.getAccessToken();
  const authed = token && !isAuthCall ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(authed).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === 401 && !isAuthCall && token) {
        return auth.refresh().pipe(
          switchMap((user) => {
            const fresh = tokens.getAccessToken();
            if (!user || !fresh) {
              return throwError(() => error);
            }
            return next(req.clone({ setHeaders: { Authorization: `Bearer ${fresh}` } }));
          }),
        );
      }
      return throwError(() => error);
    }),
  );
};
