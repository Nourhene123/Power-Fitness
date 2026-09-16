import { Routes } from '@angular/router';
import { FullProgramComponent } from './full-program/full-program.component';
import { ProgramHistoryComponent } from './program-history/program-history.component';
import { AnswerCoachComponent } from './answer-coach/answer-coach.component';

export const PROGRAM_ROUTES: Routes = [
  {
    path: '',
    component: FullProgramComponent,
    title: 'My Program | Power Fitness',
  },
  {
    path: 'history',
    component: ProgramHistoryComponent,
    title: 'Plan History | Power Fitness',
  },
  {
    path: 'answer-coach',
    component: AnswerCoachComponent,
    title: 'Answer Your Coach | Power Fitness',
  },
];
