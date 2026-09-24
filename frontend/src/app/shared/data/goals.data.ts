/**
 * The four training directions the roadmap generator can take. Shared by the home page and the
 * programs page so both always show the same names and numbers.
 */
export interface TrainingGoal {
  readonly icon: string;
  /** Goal name. */
  readonly h: string;
  /** One-sentence description. */
  readonly p: string;
  /** What changes in the plan: calories, rep ranges, split. */
  readonly chg: string;
}

export const TRAINING_GOALS: readonly TrainingGoal[] = [
  { icon: 'ri-boxing-line', h: 'Build muscle', p: 'Hypertrophy focus — moderate surplus, progressive overload, higher weekly volume per muscle.', chg: 'Calories TDEE +350 · 6–12 rep range · Upper/Lower or PPL' },
  { icon: 'ri-fire-line', h: 'Lose fat', p: 'Sustainable deficit with strength work to keep muscle while you lean out.', chg: 'Calories TDEE −450 · protein 2.2 g/kg · density & supersets' },
  { icon: 'ri-scales-line', h: 'Gain weight / mass', p: 'For hard-gainers — a larger surplus and calorie-dense meal structure.', chg: 'Calories TDEE +500 · compound-led · full-body or U/L' },
  { icon: 'ri-run-line', h: 'Athletic / recomp', p: 'Maintenance calories, power and conditioning alongside strength.', chg: 'Calories ~TDEE · explosive work · mixed rep ranges' },
];
