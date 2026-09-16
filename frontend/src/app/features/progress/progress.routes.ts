import { Routes } from '@angular/router';
import { LogWorkoutComponent } from './log-workout/log-workout.component';
import { LogWeighInComponent } from './log-weigh-in/log-weigh-in.component';
import { TimelineComponent } from './timeline/timeline.component';

export const PROGRESS_ROUTES: Routes = [
  {
    path: 'log/workout',
    component: LogWorkoutComponent,
    title: 'Log a workout | Power Fitness',
  },
  {
    path: 'log/weigh-in',
    component: LogWeighInComponent,
    title: 'Weigh-in & measurements | Power Fitness',
  },
  {
    path: 'history',
    component: TimelineComponent,
    title: 'History | Power Fitness',
  },
];
