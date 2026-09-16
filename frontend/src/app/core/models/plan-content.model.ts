export interface PlanMetrics {
  bmi: number;
  bmiCategory: string;
  bmr: number;
  tdee: number;
  targetCalories: number;
  proteinG: number;
  carbsG: number;
  fatsG: number;
  waterLiters: number;
  roadmapTitle: string;
}

export interface PlanPhase {
  phase: number;
  title: string;
  weeks: string;
  focus: string;
  intensity: string;
  targets: string[];
}

export interface PlannedExercise {
  name: string;
  sets: string;
  reps: string;
  rest: string;
  notes: string;
}

export interface PlanTrainingDay {
  day: string;
  title: string;
  duration: string;
  exercises: PlannedExercise[];
}

export interface PlanMeal {
  number: number;
  name: string;
  calories: number;
  proteinG: number;
  carbsG: number;
  fatsG: number;
  suggestion: string;
}

export interface PlanGuidelines {
  hydration: string;
  sleep: string;
  supplements: string[];
  consistencyRule: string;
}

export interface PlanAnalysisSummary {
  feasibility: string;
  topLimiters: string[];
  focusHabits: string[];
}

export interface PlanContent {
  metrics: PlanMetrics;
  phases: PlanPhase[];
  weeklySplit: PlanTrainingDay[];
  mealPlan: PlanMeal[];
  guidelines: PlanGuidelines;
  coachConstraints: string[];
  analysisSummary: PlanAnalysisSummary | null;
  changelog: string[];
}
