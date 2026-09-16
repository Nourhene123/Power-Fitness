import { Injectable } from '@angular/core';

const REFRESH_KEY = 'pf.refresh';


@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  private accessToken: string | null = null;

  getAccessToken(): string | null {
    return this.accessToken;
  }

  setAccessToken(token: string | null): void {
    this.accessToken = token;
  }

  getRefreshToken(): string | null {
    try {
      return localStorage.getItem(REFRESH_KEY);
    } catch {
      return null;
    }
  }

  setRefreshToken(token: string | null): void {
    try {
      if (token) {
        localStorage.setItem(REFRESH_KEY, token);
      } else {
        localStorage.removeItem(REFRESH_KEY);
      }
    } catch {
    }
  }

  clear(): void {
    this.accessToken = null;
    this.setRefreshToken(null);
  }
}
