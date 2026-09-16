export type LearnLevel = 'beginner' | 'intermediate' | 'advanced';

export interface LearnContent {
  level: LearnLevel;
  label: string;
  title: string;
  intro: string;
  workoutHeaders: [string, string, string];
  workout: [string, string, string][];
  meals: [string, string, string][];
}

const MEAL_HEADERS = ['Breakfast', 'Lunch', 'Snack', 'Dinner', 'Post-Workout'];

export const LEARN: Record<LearnLevel, LearnContent> = {
  beginner: {
    level: 'beginner',
    label: 'Beginner',
    title: 'Beginner — Routine & Nutrition',
    intro:
      'The foundational workout schedule and meal recommendations for building the habit. Machine and dumbbell led, strict form, habit-building over intensity.',
    workoutHeaders: ['Day', 'Workout', 'Duration'],
    workout: [
      ['Monday', 'Cardio & Movement Prep', '30 minutes'],
      ['Tuesday', 'Strength Training (Upper Body)', '45 minutes'],
      ['Wednesday', 'Rest or Active Recovery Walk', '—'],
      ['Thursday', 'Strength Training (Lower Body)', '45 minutes'],
      ['Friday', 'Cardio + Core Activation', '30 minutes'],
      ['Saturday', 'Flexibility & Mobility Stretching', '30 minutes'],
      ['Sunday', 'Full Rest & Regeneration', '—'],
    ],
    meals: [
      [MEAL_HEADERS[0], '7:00 – 8:00 AM', 'Oatmeal with fruit and nuts, Greek yogurt with honey'],
      [MEAL_HEADERS[1], '12:00 – 1:00 PM', 'Grilled chicken salad with mixed greens, olive oil and quinoa'],
      [MEAL_HEADERS[2], '3:00 – 4:00 PM', 'Protein smoothie or a handful of almonds & apple'],
      [MEAL_HEADERS[3], '6:00 – 7:00 PM', 'Baked white fish with steamed vegetables and brown rice'],
      [MEAL_HEADERS[4], 'Immediately after', 'Whey protein shake or a banana with natural peanut butter'],
    ],
  },
  intermediate: {
    level: 'intermediate',
    label: 'Intermediate',
    title: 'Intermediate — Routine & Nutrition',
    intro:
      'Progressive overload with barbell compounds and accessories. Weekly load and rep progression on an upper/lower or push-pull-legs structure.',
    workoutHeaders: ['Day', 'Workout Focus', 'Duration'],
    workout: [
      ['Monday', 'Advanced Cardio & Aerobic Conditioning', '40 minutes'],
      ['Tuesday', 'Strength Training (Full Body Power)', '60 minutes'],
      ['Wednesday', 'Rest or Light Mobility Work', '—'],
      ['Thursday', 'High-Intensity Interval Training (HIIT)', '45 minutes'],
      ['Friday', 'Strength Training (Weak Area Hypertrophy)', '60 minutes'],
      ['Saturday', 'Core & Flexibility Decompression', '30 minutes'],
      ['Sunday', 'Rest & Recovery', '—'],
    ],
    meals: [
      [MEAL_HEADERS[0], '7:00 – 8:00 AM', 'Scrambled eggs with spinach, whole-grain toast, and avocado'],
      [MEAL_HEADERS[1], '12:00 – 1:00 PM', 'Grilled salmon with brown rice and roasted asparagus'],
      [MEAL_HEADERS[2], '3:00 – 4:00 PM', 'Greek yogurt with honey, chia seeds and blueberries'],
      [MEAL_HEADERS[3], '6:00 – 7:00 PM', 'Lean beef sirloin / turkey bowl with sweet potatoes and broccoli'],
      [MEAL_HEADERS[4], 'Immediately after', 'Protein isolate shake with creatine and rice cakes'],
    ],
  },
  advanced: {
    level: 'advanced',
    label: 'Advanced',
    title: 'Advanced — Routine & Nutrition',
    intro:
      'Periodized and autoregulated: intensity techniques, RPE targets, planned deloads and peak weeks. High-yield nutrition around training windows.',
    workoutHeaders: ['Day', 'Workout Focus', 'Duration'],
    workout: [
      ['Monday', 'Heavy Compound Strength (Squat & Press Bias)', '60 minutes'],
      ['Tuesday', 'High-Intensity Interval Training (HIIT Conditioning)', '45 minutes'],
      ['Wednesday', 'Active Recovery & Myofascial Release', '30 minutes'],
      ['Thursday', 'Advanced Compound Pull & Posterior Chain Density', '60 minutes'],
      ['Friday', 'Athletic Power & Hypertrophy Overload', '60 minutes'],
      ['Saturday', 'Functional Mobility & Joint Decompression', '30 minutes'],
      ['Sunday', 'CNS Rest & Macro Re-feed', '—'],
    ],
    meals: [
      [MEAL_HEADERS[0], '7:00 – 8:00 AM', '4-egg omelette with spinach, whole-grain sourdough, and a protein shake'],
      [MEAL_HEADERS[1], '12:00 – 1:00 PM', 'Grilled chicken breast (200 g) with baked sweet potatoes and green beans'],
      [MEAL_HEADERS[2], '3:00 – 4:00 PM', 'Protein bar, cottage cheese with mixed berries & walnuts'],
      [MEAL_HEADERS[3], '6:00 – 7:00 PM', 'Baked Atlantic salmon with organic quinoa and roasted asparagus'],
      [MEAL_HEADERS[4], 'Immediately after', 'Rapid-absorption whey isolate with creatine monohydrate & cyclic dextrin'],
    ],
  },
};
