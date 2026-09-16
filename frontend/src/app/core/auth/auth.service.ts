import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, EMPTY, finalize, map, Observable, of, tap } from 'rxjs';
import { API_BASE_URL } from '../api/api';
import { Role } from '../models/role.enum';
import { AuthResponse, CurrentUser, LoginRequest, RegisterRequest } from './auth.models';
import { AuthStore } from './auth.store';
import { TokenStorageService } from './token-storage.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly base = `${inject(API_BASE_URL)}/auth`;
  private readonly tokens = inject(TokenStorageService);
  private readonly store = inject(AuthStore);
  private readonly router = inject(Router);

  register(request: RegisterRequest): Observable<CurrentUser> {
    return this.http.post<AuthResponse>(`${this.base}/register`, request).pipe(map((r) => this.accept(r)));
  }

  login(request: LoginRequest): Observable<CurrentUser> {
    return this.http.post<AuthResponse>(`${this.base}/login`, request).pipe(map((r) => this.accept(r)));
  }

  /** Exchange the stored refresh token for a new access token. Used on boot and on 401. */
  refresh(): Observable<CurrentUser | null> {
    const refreshToken = this.tokens.getRefreshToken();
    if (!refreshToken) {
      return of(null);
    }
    return this.http.post<AuthResponse>(`${this.base}/refresh`, { refreshToken }).pipe(
      map((r) => this.accept(r)),
      catchError(() => {
        this.clearSession();
        return of(null);
      }),
    );
  }

  /** Called once at startup to restore a session from the persisted refresh token. */
  initialize(): Observable<unknown> {
    return this.refresh().pipe(finalize(() => this.store.markInitialized()));
  }

  logout(): void {
    const refreshToken = this.tokens.getRefreshToken();
    const done = () => {
      this.clearSession();
      void this.router.navigate(['/login']);
    };
    if (refreshToken) {
      this.http.post(`${this.base}/logout`, { refreshToken }).pipe(catchError(() => EMPTY)).subscribe({
        complete: done,
        error: done,
      });
    } else {
      done();
    }
  }

  accessToken(): string | null {
    return this.tokens.getAccessToken();
  }

  /** Where to send a user right after signing in (see login.php redirect logic). */
  landingRoute(user: CurrentUser): string {
    if (user.role === Role.COACH || user.role === Role.ADMIN) {
      return '/coach';
    }
    return user.hasAssessment ? '/dashboard' : '/assessment';
  }

  private accept(response: AuthResponse): CurrentUser {
    this.tokens.setAccessToken(response.accessToken);
    this.tokens.setRefreshToken(response.refreshToken);
    this.store.setUser(response.user);
    return response.user;
  }

  private clearSession(): void {
    this.tokens.clear();
    this.store.setUser(null);
  }
}
