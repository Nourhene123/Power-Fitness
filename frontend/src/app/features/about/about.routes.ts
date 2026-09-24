import { Routes } from '@angular/router';
import { AboutComponent } from './about.component';

export const ABOUT_ROUTES: Routes = [
  { path: '', component: AboutComponent, title: 'About Coach Ilyesse | Power Fitness', data: { description: 'Meet Coach Ilyesse: his coaching approach, the system behind every roadmap, and what Power Fitness stands for.' } },
];
