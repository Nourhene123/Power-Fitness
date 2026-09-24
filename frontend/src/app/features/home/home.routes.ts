import { Routes } from '@angular/router';
import { HomeComponent } from './home.component';

export const HOME_ROUTES: Routes = [
  { path: '', component: HomeComponent, title: 'Power Fitness | Personalized Online Coaching', data: { description: 'Science-based online coaching: take a free 10-minute assessment and get a coach-reviewed 12-week workout and nutrition roadmap.' } },
];
