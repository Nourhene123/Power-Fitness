import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TRAINING_GOALS } from '../../shared/data/goals.data';
import { GuestCtaDirective } from '../../shared/directives/guest-cta.directive';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, GuestCtaDirective],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HomeComponent {
  protected readonly steps = [
    { n: '01', icon: 'ri-survey-line', title: 'Take Assessment',
      text: 'Input height, weight, activity frequency, nutrition schedule, and goals in about 10 minutes.' },
    { n: '02', icon: 'ri-cpu-line', title: 'Instant Smart Split',
      text: 'Your BMR, TDEE, calorie target, macros, and a 4-phase 12-week workout split are generated.' },
    { n: '03', icon: 'ri-shield-check-line', title: 'Coach Review',
      text: 'Coach Ilyesse personally audits your routine, fine-tunes exercises, and unlocks your active roadmap.' },
    { n: '04', icon: 'ri-line-chart-line', title: 'Track & Evolve',
      text: 'Access your dashboard, log weights and workouts, and receive adaptive adjustments every phase.' },
  ];

  protected readonly goals = TRAINING_GOALS;

  protected readonly faqs = [
    { q: 'How does the 12-week roadmap work?',
      a: 'You complete a short biometric assessment (height, weight, training frequency, meals, goal). The system calculates your BMR, TDEE, calorie target and macros, then builds a 4-phase 12-week workout split. Coach Ilyesse reviews and approves it before it becomes active on your personal dashboard.' },
    { q: 'How long before I receive my approved plan?',
      a: 'Your draft plan is generated instantly. Coach Ilyesse personally audits and fine-tunes your plan within 24 to 48 hours — you will get a notification on your dashboard the moment it is ready.' },
    { q: 'Is the initial assessment really free?',
      a: 'Yes. Creating an account and completing the 10-minute fitness assessment is 100% free and does not require any payment information.' },
    { q: 'Can the coach customize my exercise selection?',
      a: 'Absolutely. Coach Ilyesse adjusts sets, reps, exercise variations, and macro distribution according to any injuries or equipment access you have.' },
    { q: 'What fitness goals are supported?',
      a: 'Four: build muscle, lose fat, gain weight / mass, and athletic / recomp. Your assessment decides which one applies, and the plan adjusts for home workouts, calisthenics, or a full gym.' },
  ];
}
