import { ChangeFieldVm } from './program.model';

export type DashboardState = 'NONE' | 'CHANGES_REQUESTED' | 'IN_REVIEW' | 'ACTIVE';

export interface ChangeRequestVm {
  programId: number;
  coachNote: string | null;
  fields: ChangeFieldVm[];
}

export interface PreviewVm {
  targetCalories: number | null;
  splitDays: number | null;
}

export interface GoalVm {
  line: string;
  onTrack: boolean | null;
  startKg: number;
  currentKg: number;
  targetKg: number;
  toGoKg: number;
  progressPct: number;
  direction: 'lose' | 'gain' | 'maintain';
}

export interface PhaseVm {
  name: string;
  version: number;
  weekNo: number;
  totalWeeks: number;
  phaseLabel: string;
  phaseShort: string | null;
}

export interface TodaySessionVm {
  title: string;
  exercises: number;
  duration: string | null;
}

export interface TodayHabitsVm {
  water: boolean;
  workout: boolean;
  nutrition: boolean;
  sleep: boolean;
}

export interface TodayVm {
  isRest: boolean;
  sessionIdx: number | null;
  session: TodaySessionVm | null;
  doneToday: boolean;
  calories: number;
  proteinG: number;
  carbsG: number;
  fatsG: number;
  waterL: number;
  habits: TodayHabitsVm;
}

export interface WeightPointVm {
  date: string;
  value: number;
}

export interface WeekAdherenceVm {
  label: string;
  workout: number;
  adherencePct: number;
}

export interface ProgressVm {
  weightSeries: WeightPointVm[];
  weeks: WeekAdherenceVm[];
  streak: number;
  goalBandLow: number;
  goalBandHigh: number;
}

export interface CoachVm {
  note: string;
  focus: string;
  focusSource: 'coach' | 'system';
  unread: number;
}

export interface MilestoneVm {
  label: string | null;
  date: string | null;
  inDays: number | null;
}

/** `GET /me/dashboard` — the client dashboard's "7 questions" view-model. */
export interface DashboardDto {
  state: DashboardState;
  hasAssessment: boolean;
  changeRequest: ChangeRequestVm | null;
  preview: PreviewVm | null;
  goal: GoalVm | null;
  phase: PhaseVm | null;
  today: TodayVm | null;
  progress: ProgressVm | null;
  coach: CoachVm | null;
  milestone: MilestoneVm | null;
}
