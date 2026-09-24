import { Routes } from '@angular/router';
import { ContactComponent } from './contact.component';

export const CONTACT_ROUTES: Routes = [
  { path: '', component: ContactComponent, title: 'Request a Coach | Power Fitness', data: { description: 'Request a coach: tell Coach Ilyesse about your goals and get a personal coaching plan.' } },
];
