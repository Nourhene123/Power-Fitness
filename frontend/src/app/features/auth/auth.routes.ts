import { Routes } from '@angular/router';
import { AuthLayoutComponent } from '../../layout/auth-layout/auth-layout.component';
import { guestGuard } from '../../core/guards/guest.guard';

export const AUTH_ROUTES: Routes = [
  {
    path: 'login',
    component: AuthLayoutComponent,
    canActivate: [guestGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./login/login.component').then((m) => m.LoginComponent),
        title: 'Sign in | Power Fitness',
      },
    ],
  },
  {
    path: 'register',
    component: AuthLayoutComponent,
    canActivate: [guestGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./register/register.component').then((m) => m.RegisterComponent),
        title: 'Create account | Power Fitness',
      },
    ],
  },
];
