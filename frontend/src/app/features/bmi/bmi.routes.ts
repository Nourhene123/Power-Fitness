import { Routes } from '@angular/router';
import { BmiComponent } from './bmi.component';

export const BMI_ROUTES: Routes = [
  { path: '', component: BmiComponent, title: 'Free BMI Calculator | Power Fitness', data: { description: 'Free BMI calculator: enter your height and weight to get your body mass index and what it means for your training.' } },
];
