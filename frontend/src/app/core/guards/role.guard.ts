import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthStore } from '../auth/auth.store';
import { Role } from '../models/role.enum';

export const roleGuard: CanActivateFn = (route, state) => {
  const store = inject(AuthStore);
  const router = inject(Router);
  const allowed = (route.data['roles'] as Role[] | undefined) ?? [];

  if (!store.isAuthenticated()) {
    return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
  }
  const role = store.role();
  return role && allowed.includes(role) ? true : router.createUrlTree(['/']);
};
