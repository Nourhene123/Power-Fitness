import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink],
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

  protected readonly programs = [
    { icon: 'ri-boxing-fill', title: 'Strength & Power',
      text: 'Build maximal strength with progressive overload on core compound lifts, periodized for optimal joint recovery.' },
    { icon: 'ri-heart-pulse-fill', title: 'Athletic Fitness',
      text: 'Improve cardiovascular stamina, functional mobility, and lean conditioning with a balanced strength-endurance mix.' },
    { icon: 'ri-run-line', title: 'Fat Loss & Shred',
      text: 'Targeted caloric deficit protocols combined with high-protein nutrition and resistance splits that preserve lean muscle mass.' },
    { icon: 'ri-shopping-basket-fill', title: 'Hypertrophy & Bulk',
      text: 'Gain dense muscle tissue with calculated caloric surplus, targeted volume schemes, and smart recovery strategies.' },
  ];

  protected readonly plans = [
    { name: 'Basic Plan', price: '40', popular: false, features: [
      'Smart 12-Week Workout Plan', 'Home & Gym Alternatives', 'Calorie & Macro Targets', 'Dashboard Progress Tracking' ] },
    { name: 'Weekly Plan', price: '80', popular: true, features: [
      'Everything in Basic Plan', 'PRO Gyms Access', 'Bi-Weekly Coach Plan Adjustments', 'Priority Support & Form Check' ] },
    { name: 'Elite VIP Plan', price: '160', popular: false, features: [
      'ELITE Gyms & Masterclasses', '1-on-1 Personal Video Consultation', 'Fully Custom Meal-by-Meal Schedule', '24/7 Direct WhatsApp Coach Access' ] },
  ];

  protected readonly faqs = [
    { q: 'How does the 12-week roadmap work?',
      a: 'You complete a short biometric assessment (height, weight, training frequency, meals, goal). The system calculates your BMR, TDEE, calorie target and macros, then builds a 4-phase 12-week workout split. Coach Ilyesse reviews and approves it before it becomes active on your personal dashboard.' },
    { q: 'How long before I receive my approved plan?',
      a: 'Your draft plan is generated instantly. Coach Ilyesse personally audits and fine-tunes your plan within 24 to 48 hours — you will receive an email and dashboard notification the moment it is ready.' },
    { q: 'Is the initial assessment really free?',
      a: 'Yes. Creating an account and completing the 10-minute fitness assessment is 100% free and does not require any payment information.' },
    { q: 'Can the coach customize my exercise selection?',
      a: 'Absolutely. Coach Ilyesse adjusts sets, reps, exercise variations, and macro distribution according to any injuries or equipment access you have.' },
    { q: 'What fitness goals are supported?',
      a: 'Muscle Hypertrophy, Aggressive Fat Loss & Shredding, Clean Bulking & Weight Gain, and General Athletic Performance. The system adjusts for home workouts, calisthenics, or full commercial gym setups.' },
  ];
}
