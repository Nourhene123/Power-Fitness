import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-programs',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './programs.component.html',
  styleUrl: './programs.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProgramsComponent {
  protected readonly included = [
    { icon: 'ri-map-pin-time-line', b: '12-week, 4-phase periodization', t: 'adaptation → progressive overload → peak → deload.' },
    { icon: 'ri-fire-line', b: 'Calorie & macro targets', t: 'from your biometrics (Mifflin-St Jeor, TDEE, goal-based surplus/deficit).' },
    { icon: 'ri-calendar-event-line', b: 'Day-by-day split', t: 'matched to your available days, session length and equipment.' },
    { icon: 'ri-restaurant-2-line', b: 'Meal structure', t: 'for your meal count, diet style, allergies and budget.' },
    { icon: 'ri-shield-check-line', b: 'Human coach review', t: 'Coach Ilyesse adjusts and signs off before the plan activates.' },
    { icon: 'ri-line-chart-line', b: 'Progress tracking', t: 'weigh-ins, session logs, habits and weekly adherence on your dashboard.' },
    { icon: 'ri-git-branch-line', b: 'Version updates', t: 'the coach releases a new version as you progress; old versions stay in your history.' },
    { icon: 'ri-alert-line', b: 'Injury-aware', t: 'flagged movement patterns are swapped for pain-free alternatives automatically.' },
  ];

  protected readonly goals = [
    { icon: 'ri-boxing-line', h: 'Build muscle', p: 'Hypertrophy focus — moderate surplus, progressive overload, higher weekly volume per muscle.', chg: 'Calories TDEE +350 · 6–12 rep range · Upper/Lower or PPL' },
    { icon: 'ri-fire-line', h: 'Lose fat', p: 'Sustainable deficit with strength work to keep muscle while you lean out.', chg: 'Calories TDEE −450 · protein 2.2 g/kg · density & supersets' },
    { icon: 'ri-scales-line', h: 'Gain weight / mass', p: 'For hard-gainers — a larger surplus and calorie-dense meal structure.', chg: 'Calories TDEE +500 · compound-led · full-body or U/L' },
    { icon: 'ri-run-line', h: 'Athletic / recomp', p: 'Maintenance calories, power and conditioning alongside strength.', chg: 'Calories ~TDEE · explosive work · mixed rep ranges' },
  ];

  protected readonly levels = [
    { tag: 'Beginner · < 1 yr', h: 'Foundation', level: 'beginner',
      points: ['Machine & dumbbell led, strict form', '~10 weekly sets per muscle', 'Full-body or upper/lower', 'Habit-building over intensity'] },
    { tag: 'Intermediate · 1–3 yrs', h: 'Progressive overload', level: 'intermediate',
      points: ['Barbell compounds + accessories', '~14 weekly sets per muscle', 'Upper/lower or push-pull-legs', 'Weekly load / rep progression'] },
    { tag: 'Advanced · 3+ yrs', h: 'Periodized & autoregulated', level: 'advanced',
      points: ['Intensity techniques, RPE targets', '16–18 weekly sets per muscle', 'PPL ×2 or specialization blocks', 'Planned deloads, peak weeks'] },
  ];

  protected readonly constraints = [
    { icon: 'ri-calendar-line', h: 'Only 3 days?', p: 'The split compresses to full-body sessions so every muscle is trained enough.' },
    { icon: 'ri-home-4-line', h: 'Home / bands only?', p: 'Barbell work is replaced with dumbbell, band, tempo and unilateral variations.' },
    { icon: 'ri-heart-pulse-line', h: 'Injury history?', p: 'Aggravating movement patterns are excluded and swapped; the coach reviews it.' },
    { icon: 'ri-time-line', h: 'Short on time?', p: 'A 30–40 min cap means fewer exercises per session, paired as supersets.' },
  ];

  protected readonly faqs = [
    { q: 'Can I request a specific split or exercise?',
      a: "Yes — you list exercises you love and hate in the assessment, and the coach can build around them. If you want a particular split (e.g. PPL), tell the coach and they'll set it during review." },
    { q: 'What if I only have 3 days a week?',
      a: "The generator uses a full-body ×3 structure so each muscle group still gets 2+ stimulating sessions per week. Nothing is skipped — it's reorganized." },
    { q: 'Do I need a gym?',
      a: 'No. Select "home", "dumbbells only", "bands" or "bodyweight" in the assessment and the plan is built from what you have.' },
    { q: 'How often does the plan change?',
      a: 'The 12 weeks are phased, so intensity and volume shift every 3–4 weeks automatically. The coach can also release a new version any time based on your check-ins.' },
    { q: 'Is the assessment really free?',
      a: 'Yes. Creating an account, completing the assessment and getting your draft roadmap costs nothing and needs no card.' },
  ];
}
