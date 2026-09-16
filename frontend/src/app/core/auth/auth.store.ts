import { computed, Injectable, signal } from '@angular/core';
import { Role } from '../models/role.enum';
import { CurrentUser } from './auth.models';

@Injectable({ providedIn: 'root' })
export class AuthStore {
  private readonly _user = signal<CurrentUser | null>(null);
  private readonly _initializing = signal(true);

  readonly user = this._user.asReadonly();
  readonly initializing = this._initializing.asReadonly();
  readonly isAuthenticated = computed(() => this._user() !== null);
  readonly role = computed<Role | null>(() => this._user()?.role ?? null);
  readonly isCoach = computed(() => {
    const r = this._user()?.role;
    return r === Role.COACH || r === Role.ADMIN;
  });

  setUser(user: CurrentUser | null): void {
    this._user.set(user);
  }

  patchUser(patch: Partial<CurrentUser>): void {
    const current = this._user();
    if (current) {
      this._user.set({ ...current, ...patch });
    }
  }

  markInitialized(): void {
    this._initializing.set(false);
  }
}
