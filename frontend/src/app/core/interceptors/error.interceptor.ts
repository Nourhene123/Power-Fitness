import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { ApiError } from '../models/api-error.model';

export const errorInterceptor: HttpInterceptorFn = (req, next) =>
  next(req).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse) {
        const body = error.error as Partial<ApiError> | string | null;
        const normalized: ApiError =
          body && typeof body === 'object' && 'code' in body
            ? (body as ApiError)
            : {
                timestamp: new Date().toISOString(),
                status: error.status,
                code: error.status === 0 ? 'NETWORK_ERROR' : 'HTTP_ERROR',
                message:
                  error.status === 0
                    ? 'Could not reach the server. Check your connection.'
                    : error.message,
                path: req.url,
                fieldErrors: [],
              };
        return throwError(() => normalized);
      }
      return throwError(() => error);
    }),
  );
