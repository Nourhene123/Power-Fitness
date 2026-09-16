export interface AnalysisMetabolic {
  bmi: number;
  bmiCategory: string;
  bmr: number;
  tdee: number;
  activityMultiplier: number;
  targetCalories: number;
  proteinG: number;
  carbsG: number;
  fatsG: number;
  bodyfatBand: string;
  roadmapTitle: string;
  waterLiters: number;
}

export interface AnalysisFeasibility {
  verdict: 'realistic' | 'aggressive' | 'unrealistic' | 'maintenance';
  direction: 'loss' | 'gain' | 'maintain';
  deltaKg: number;
  safeWeeklyPctRange: number[];
  weeklyRatePct: number | null;
  weeksAvailable: number | null;
  projectedWeeks: number;
  message: string;
}

export interface AnalysisStrength {
  label: string;
  detail: string;
}

export interface AnalysisLimiter {
  severity: 'high' | 'medium' | 'low';
  label: string;
  action: string;
}

export interface AnalysisContraindications {
  excludedPatterns: string[];
  equipmentMode: string;
  maxExercisesPerSession: number;
  noHighImpact: boolean;
  notes: string[];
}

export interface AnalysisSuggestedStart {
  split: string;
  weeklySetsPerMuscle: number;
  calorieTarget: number;
  proteinG: number;
  sessionCap: number;
  focusHabits: string[];
}

export interface AnalysisDataQualityFlag {
  type: string;
  message: string;
}

export interface AnalysisResult {
  generatedAt: string;
  metabolic: AnalysisMetabolic;
  feasibility: AnalysisFeasibility;
  strengths: AnalysisStrength[];
  limiters: AnalysisLimiter[];
  contraindications: AnalysisContraindications;
  suggestedStart: AnalysisSuggestedStart;
  dataQualityFlags: AnalysisDataQualityFlag[];
}
