import { Routes } from '@angular/router';
import { LearnComponent } from './learn.component';

export const LEARN_ROUTES: Routes = [
  { path: ':level', component: LearnComponent, title: 'Sample Week | Power Fitness', data: { description: 'See a sample week of training and nutrition from a Power Fitness roadmap, by experience level.' } },
  { path: '', redirectTo: 'beginner', pathMatch: 'full' },
];
