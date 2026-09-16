import { TitleCasePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import {
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AssessmentService } from './data/assessment.service';
import { ApiError } from '../../core/models/api-error.model';

type InjuryGroup = FormGroup<{
  bodyPart: FormControl<string>;
  description: FormControl<string>;
  stillPainful: FormControl<boolean>;
  avoid: FormControl<string>;
}>;

const STEP_LABELS = [
  'Identity & context',
  'Body & biometrics',
  'Training history',
  'Goals & focus',
  'Nutrition & lifestyle',
  'Recovery & health',
  'Preferences',
];
@Component({
  selector: 'app-assessment',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, TitleCasePipe],
  templateUrl: './assessment.component.html',
  styleUrl: './assessment.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AssessmentComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(AssessmentService);
  private readonly router = inject(Router);

  protected readonly stepLabels = STEP_LABELS;
  protected readonly totalSteps = STEP_LABELS.length;
  protected readonly step = signal(1);
  protected readonly submitting = signal(false);
  protected readonly submitted = signal<{ roadmapTitle: string } | null>(null);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly progressPct = computed(() => Math.round((this.step() / this.totalSteps) * 100));

  protected readonly daysList = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
  protected readonly muscleList = ['chest', 'back', 'shoulders', 'arms', 'legs', 'glutes', 'core'];
  protected readonly equipmentList: [string, string][] = [
    ['full_gym', 'Full gym'], ['dumbbells', 'Dumbbells'], ['barbell', 'Barbell'],
    ['bands', 'Resistance bands'], ['machines', 'Machines'], ['bodyweight', 'Bodyweight only'],
  ];
  protected readonly injuryParts = ['neck', 'shoulder', 'upper_back', 'lower_back', 'elbow', 'wrist', 'hip', 'knee', 'ankle', 'other'];

  protected readonly form = this.fb.nonNullable.group({
   
    fullName: [''],
    dateOfBirth: ['', Validators.required],
    sexAtBirth: ['', Validators.required],
    gender: ['male'],
    country: [''],
    occupationType: ['desk'],
    motivation: [''],
   
    heightCm: [null as number | null, [Validators.required, Validators.min(100), Validators.max(250)]],
    weightKg: [null as number | null, [Validators.required, Validators.min(30), Validators.max(300)]],
    targetWeightKg: [null as number | null, [Validators.required, Validators.min(30), Validators.max(300)]],
    waistCm: [null as number | null],
    hipCm: [null as number | null],
    chestCm: [null as number | null],
    armCm: [null as number | null],
    thighCm: [null as number | null],
    neckCm: [null as number | null],
    experienceLevel: ['beginner'],
    yearsTraining: [null as number | null],
    currentProgram: [''],
    strengthSquat: [null as number | null],
    strengthBench: [null as number | null],
    strengthDeadlift: [null as number | null],
    workoutFrequency: [3, Validators.required],
    sessionDurationMin: [60, Validators.required],
    trainingDays: this.fb.nonNullable.array<string>([]),
    equipment: this.fb.nonNullable.array<string>([]),
    dailySteps: [null as number | null],
    activityLevel: ['moderate'],
    primaryGoal: ['muscle_gain', Validators.required],
    secondaryGoal: [''],
    targetDate: [''],
    priorityMuscles: this.fb.nonNullable.array<string>([]),
    priorityLifts: [''],
    successDefinition: [''],
    mealsPerDay: [3],
    eatingOutPerWeek: [null as number | null],
    dietPreference: ['high_protein'],
    cookingAbility: ['basic'],
    budgetLevel: ['moderate'],
    allergies: [''],
    dislikedFoods: [''],
    waterIntakeLiters: [2.5],
    alcoholUnitsWeek: [0],
    caffeineMg: [null as number | null],
    supplements: [''],
    sleepHours: [7],
    sleepQuality: [3],
    wakeFeeling: ['ok'],
    stressLevel: [5],
    energyLevel: [6],
    hasInjuries: ['no'],
    injuries: this.fb.array<InjuryGroup>([]),
    medicalConditions: [''],
    medications: [''],
    isPregnant: [false],
    medicalAck: [false, Validators.requiredTrue],
    lovedExercises: [''],
    hatedExercises: [''],
    trainingStylePref: ['bodybuilding'],
    coachingTonePref: ['direct'],
    checkinFrequency: ['weekly'],
    contactChannel: ['in_app'],
  });

  protected get injuries(): FormArray<InjuryGroup> {
    return this.form.controls.injuries;
  }

  private newInjury(): InjuryGroup {
    return this.fb.nonNullable.group({
      bodyPart: '',
      description: '',
      stillPainful: this.fb.nonNullable.control<boolean>(false),
      avoid: '',
    });
  }

  protected addInjury(): void {
    this.injuries.push(this.newInjury());
  }

  protected removeInjury(i: number): void {
    this.injuries.removeAt(i);
  }

  protected toggleArray(name: 'trainingDays' | 'equipment' | 'priorityMuscles', value: string): void {
    const arr = this.form.get(name) as FormArray;
    const idx = arr.value.indexOf(value);
    if (idx === -1) arr.push(this.fb.nonNullable.control(value));
    else arr.removeAt(idx);
  }

  protected inArray(name: 'trainingDays' | 'equipment' | 'priorityMuscles', value: string): boolean {
    return (this.form.get(name)!.value as string[]).includes(value);
  }

  protected next(): void {
    if (!this.validateStep()) return;
    if (this.step() < this.totalSteps) {
      this.step.update((s) => s + 1);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  protected back(): void {
    if (this.step() > 1) {
      this.step.update((s) => s - 1);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  private validateStep(): boolean {
    const groups: Record<number, string[]> = {
      1: ['dateOfBirth', 'sexAtBirth'],
      2: ['heightCm', 'weightKg', 'targetWeightKg'],
      3: ['workoutFrequency', 'sessionDurationMin'],
      4: ['primaryGoal'],
      6: ['medicalAck'],
    };
    const controls = groups[this.step()] ?? [];
    let ok = true;
    for (const c of controls) {
      const ctrl = this.form.get(c)!;
      ctrl.markAsTouched();
      if (ctrl.invalid) ok = false;
    }
    if (this.step() === 6 && this.form.get('medicalAck')!.invalid) {
      this.errorMessage.set('You must acknowledge the medical disclaimer to continue.');
    } else {
      this.errorMessage.set(null);
    }
    return ok;
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMessage.set('Some required fields are missing — check the earlier steps.');
      return;
    }
    this.submitting.set(true);
    this.errorMessage.set(null);

    const v = this.form.getRawValue();
    const dateOrNull = (s: string) => (s && s.trim() ? s : null);
    const payload: Record<string, unknown> = {
      ...v,
      dateOfBirth: dateOrNull(v.dateOfBirth),
      targetDate: dateOrNull(v.targetDate),
      timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
      injuries: v.hasInjuries === 'yes' ? v.injuries : [],
      hasInjuries: v.hasInjuries === 'yes',
      yearsTraining: v.yearsTraining ?? null,
    };

    this.service.submit(payload).subscribe({
      next: (res) => {
        this.submitting.set(false);
        this.submitted.set({ roadmapTitle: res.roadmapTitle });
      },
      error: (err: ApiError) => {
        this.submitting.set(false);
        this.errorMessage.set(err?.message ?? 'Could not submit your assessment. Please try again.');
      },
    });
  }

  protected goToDashboard(): void {
    void this.router.navigateByUrl('/dashboard');
  }
}
