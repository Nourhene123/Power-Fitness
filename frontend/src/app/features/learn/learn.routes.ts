import { Routes } from '@angular/router';
import { LearnComponent } from './learn.component';

export const LEARN_ROUTES: Routes = [
  { path: ':level', component: LearnComponent, title: 'Sample Week | Power Fitness' },
  { path: '', redirectTo: 'beginner', pathMatch: 'full' },
];
