import { AnalysisResult } from './analysis-result.model';
import { PlanContent } from '../../../core/models/plan-content.model';



export interface CoachStats {
  pending: number;
  approved: number;
  totalClients: number;
  atRisk: number;
}

export interface PendingReview {
  programId: number;
  versionId: number;
  userId: number;
  userName: string;
  userEmail: string;
  primaryGoal: string;
  experienceLevel: string | null;
  workoutFrequency: number;
  submittedAt: string;
  userJoinedAt: string;
}

export interface ApprovedRoadmap {
  programId: number;
  userId: number;
  userName: string;
  userEmail: string;
  primaryGoal: string;
  approvedAt: string | null;
}

export interface RosterEntry {
  userId: number;
  userName: string;
  userEmail: string;
  primaryGoal: string;
  atRisk: boolean;
  riskReasons: string[];
}

export interface CoachDashboardDto {
  stats: CoachStats;
  pending: PendingReview[];
  recentlyApproved: ApprovedRoadmap[];
  roster: RosterEntry[];
}


export interface InjurySummary {
  bodyPart: string;
  stillPainful: boolean;
  avoid: string | null;
}

export interface AssessmentSummary {
  age: number | null;
  gender: string | null;
  sexAtBirth: string | null;
  heightCm: number | null;
  weightKg: number | null;
  targetWeightKg: number | null;
  primaryGoal: string | null;
  experienceLevel: string | null;
  workoutFrequency: number | null;
  sessionDurationMin: number | null;
  workoutLocation: string | null;
  occupationType: string | null;
  sleepHours: number | null;
  sleepQuality: number | null;
  stressLevel: number | null;
  energyLevel: number | null;
  equipment: string[];
  dietPreference: string | null;
  allergies: string | null;
  dislikedFoods: string | null;
  lovedExercises: string | null;
  hatedExercises: string | null;
  coachingTonePref: string | null;
  injuries: InjurySummary[];
  medicalConditions: string | null;
}

export interface ClientProfileDto {
  userId: number;
  name: string;
  email: string;
  joinedAt: string;
  assessment: AssessmentSummary | null;
  analysis: AnalysisResult | null;
  versionId: number | null;
  versionNo: number | null;
  versionStatus: string | null;
  content: PlanContent | null;
  atRisk: boolean;
  riskReasons: string[];
}

export interface ProgressStats {
  workoutPct: number;
  nutritionPct: number;
  waterPct: number;
  avgSleepHours: number;
  weightChangeKg: number;
  workoutsCompleted: number;
  totalDays: number;
}

export interface WeightLogRow {
  date: string;
  weightKg: number | null;
  waistCm: number | null;
  hipCm: number | null;
  chestCm: number | null;
  energy: number | null;
  note: string | null;
}

export interface WorkoutLogRow {
  date: string;
  dayLabel: string | null;
  durationMin: number | null;
  rpe: number | null;
  note: string | null;
}

export interface HabitDay {
  date: string;
  water: boolean;
  workout: boolean;
  nutrition: boolean;
  sleep: boolean;
}

export interface ClientProgressDto {
  userId: number;
  name: string;
  email: string;
  stats: ProgressStats;
  weightLogs: WeightLogRow[];
  workoutLogs: WorkoutLogRow[];
  habitMatrix: HabitDay[];
  atRisk: boolean;
  riskReasons: string[];
  daysSinceLastWeighIn: number | null;
  daysSinceLastWorkout: number | null;
  currentStreak: number;
}


export interface ExerciseSummary {
  name: string;
  category: string;
  primaryMuscle: string | null;
  equipment: string;
}

export interface PlanEditorDto {
  versionId: number;
  programId: number;
  versionNo: number;
  status: string;
  locked: boolean;
  coachNote: string | null;
  content: PlanContent;
  liveChangelog: string[];
  client: ClientProfileDto;
  safeExercises: ExerciseSummary[];
}

export interface CoachVersionDto {
  versionId: number;
  programId: number;
  clientUserId: number;
  versionNo: number;
  status: string;
  locked: boolean;
  coachNote: string | null;
  content: PlanContent;
  liveChangelog: string[];
}
