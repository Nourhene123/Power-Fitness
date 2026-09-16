export type HabitKey = 'water' | 'workout' | 'nutrition' | 'sleep';

export interface LogWorkoutRequest {
  dayIndex: number | null;
  sessionDate: string | null;
  durationMin: number | null;
  rpe: number | null;
  note: string | null;
}

export interface WorkoutSessionDto {
  id: number;
  dayIndex: number | null;
  dayLabel: string;
  sessionDate: string;
  durationMin: number | null;
  rpe: number | null;
  note: string | null;
}

export interface LogWeighInRequest {
  loggedOn: string | null;
  weightKg: number | null;
  waistCm: number | null;
  hipCm: number | null;
  chestCm: number | null;
  armCm: number | null;
  thighCm: number | null;
  neckCm: number | null;
  energy: number | null;
  note: string | null;
}

export interface ProgressLogDto {
  id: number;
  loggedOn: string;
  weightKg: number | null;
  waistCm: number | null;
  hipCm: number | null;
  chestCm: number | null;
  armCm: number | null;
  thighCm: number | null;
  neckCm: number | null;
  energy: number | null;
  note: string | null;
}

export interface HabitToggleResult {
  value: boolean;
}

export interface TimelineItemDto {
  date: string;
  type: 'weigh' | 'workout' | 'plan';
  text: string;
}
