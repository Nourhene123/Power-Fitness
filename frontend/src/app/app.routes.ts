import { Routes } from '@angular/router';
import { PublicLayoutComponent } from './layout/public-layout/public-layout.component';
import { MemberLayoutComponent } from './layout/member-layout/member-layout.component';
import { CoachLayoutComponent } from './layout/coach-layout/coach-layout.component';
import { AccountComponent } from './features/account/account.component';
import { MyOrdersComponent } from './features/shop/my-orders/my-orders.component';
import { NotFoundComponent } from './shared/components/not-found/not-found.component';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { Role } from './core/models/role.enum';

export const routes: Routes = [
  {
    
    path: '',
    component: PublicLayoutComponent,
    children: [
      {
        path: '',
        loadChildren: () => import('./features/home/home.routes').then((m) => m.HOME_ROUTES),
      },
      {
        path: 'about',
        loadChildren: () => import('./features/about/about.routes').then((m) => m.ABOUT_ROUTES),
      },
      {
        path: 'contact',
        loadChildren: () => import('./features/contact/contact.routes').then((m) => m.CONTACT_ROUTES),
      },
      {
        path: 'programs',
        loadChildren: () => import('./features/programs/programs.routes').then((m) => m.PROGRAMS_ROUTES),
      },
      {
        path: 'bmi',
        loadChildren: () => import('./features/bmi/bmi.routes').then((m) => m.BMI_ROUTES),
      },
      {
        path: 'learn',
        loadChildren: () => import('./features/learn/learn.routes').then((m) => m.LEARN_ROUTES),
      },
      {
        path: 'shop',
        loadChildren: () => import('./features/shop/shop.routes').then((m) => m.SHOP_ROUTES),
      },

      // --- authed areas ---
      {
        path: 'assessment',
        loadChildren: () =>
          import('./features/assessment/assessment.routes').then((m) => m.ASSESSMENT_ROUTES),
        canActivate: [authGuard],
      },
    ],
  },
  {
    // Auth pages have their own split-screen shell (no navbar/footer).
    path: '',
    loadChildren: () => import('./features/auth/auth.routes').then((m) => m.AUTH_ROUTES),
  },
  {
    // The coach/admin app shell: sidebar + header, guarded to COACH/ADMIN only.
    path: 'coach',
    component: CoachLayoutComponent,
    canActivate: [roleGuard],
    data: { roles: [Role.COACH, Role.ADMIN] },
    loadChildren: () => import('./features/coach/coach.routes').then((m) => m.COACH_ROUTES),
  },
  {
    // The member app shell: sidebar + header, wrapping the client-facing dashboard/program pages.
    path: '',
    component: MemberLayoutComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadChildren: () => import('./features/dashboard/dashboard.routes').then((m) => m.DASHBOARD_ROUTES),
      },
      {
        path: 'program',
        loadChildren: () => import('./features/program/program.routes').then((m) => m.PROGRAM_ROUTES),
      },
      {
        path: 'progress',
        loadChildren: () => import('./features/progress/progress.routes').then((m) => m.PROGRESS_ROUTES),
      },
      {
        path: 'orders',
        component: MyOrdersComponent,
        title: 'My Orders | Power Fitness',
      },
      {
        path: 'account',
        component: AccountComponent,
        title: 'Account | Power Fitness',
      },
    ],
  },
  { path: '**', component: NotFoundComponent, title: 'Page Not Found | Power Fitness' },
];
