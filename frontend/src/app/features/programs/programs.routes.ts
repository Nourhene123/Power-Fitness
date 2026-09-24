import { Routes } from '@angular/router';
import { ProgramsComponent } from './programs.component';

export const PROGRAMS_ROUTES: Routes = [
  { path: '', component: ProgramsComponent, title: 'Coaching Programs | Power Fitness', data: { description: 'How Power Fitness programs work: a 12-week, 4-phase plan built from your assessment, with calorie targets and coach approval.' } },
];
