import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError, from } from 'rxjs';
import { TokenStorageService } from '../services/token-storage.service';
import { GovosAuthService } from '../services/govos-auth.service';

const AUTH_PATHS = ['/api/v1/auth/login', '/api/v1/auth/refresh', '/api/v1/auth/logout'];

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenStorage = inject(TokenStorageService);
  const authService = inject(GovosAuthService);

  const isAuthEndpoint = AUTH_PATHS.some((path) => req.url.includes(path));
  const accessToken = tokenStorage.getAccessToken();

  const authReq =
    !isAuthEndpoint && accessToken
      ? req.clone({ setHeaders: { Authorization: `Bearer ${accessToken}` } })
      : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status !== 401 || isAuthEndpoint || req.headers.has('X-Retry-After-Refresh')) {
        return throwError(() => error);
      }

      return from(authService.refreshSession()).pipe(
        switchMap((refreshed) => {
          if (!refreshed) {
            return throwError(() => error);
          }

          const retryToken = tokenStorage.getAccessToken();
          const retryReq = req.clone({
            setHeaders: {
              Authorization: `Bearer ${retryToken ?? ''}`,
              'X-Retry-After-Refresh': 'true',
            },
          });
          return next(retryReq);
        }),
      );
    }),
  );
};
