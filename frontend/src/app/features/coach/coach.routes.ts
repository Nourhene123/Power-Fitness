import { Routes } from '@angular/router';
import { CoachDashboardComponent } from './dashboard/coach-dashboard.component';
import { CoachRequestsComponent } from './requests/coach-requests.component';
import { ClientProfileComponent } from './client-profile/client-profile.component';
import { ClientProgressComponent } from './client-progress/client-progress.component';
import { PlanEditorComponent } from './plan-editor/plan-editor.component';
import { CoachProductsComponent } from './shop/products/coach-products.component';
import { CoachOrdersComponent } from './shop/orders/coach-orders.component';

export const COACH_ROUTES: Routes = [
  {
    path: '',
    component: CoachDashboardComponent,
    title: 'Coach Dashboard | Power Fitness',
  },
  {
    path: 'requests',
    component: CoachRequestsComponent,
    title: 'Client Requests | Power Fitness',
  },
  {
    path: 'shop/products',
    component: CoachProductsComponent,
    title: 'Shop Products | Power Fitness',
  },
  {
    path: 'shop/orders',
    component: CoachOrdersComponent,
    title: 'Shop Orders | Power Fitness',
  },
  {
    path: 'plan/:id',
    component: PlanEditorComponent,
    title: 'Plan Editor | Power Fitness',
  },
  {
    path: 'clients/:id/progress',
    component: ClientProgressComponent,
    title: 'Client Progress | Power Fitness',
  },
  {
    path: 'clients/:id',
    component: ClientProfileComponent,
    title: 'Client Profile | Power Fitness',
  },
];
