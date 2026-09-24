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
        data: { description: 'Sign in to your Power Fitness dashboard to see your roadmap, log workouts and track progress.' },
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
        data: { description: 'Create a free Power Fitness account and start your assessment to get a personalized 12-week roadmap.' },
      },
    ],
  },
];
