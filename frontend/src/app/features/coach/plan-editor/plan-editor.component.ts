import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CoachService } from '../data/coach.service';
import { PlanEditorDto } from '../data/coach.model';
import { PlanContent } from '../../../core/models/plan-content.model';

interface RequestFieldOption {
  field: string;
  label: string;
}

interface ExerciseFormValue {
  name: string;
  sets: string;
  reps: string;
  rest: string;
  notes: string;
}
interface DayFormValue {
  day: string;
  duration: string;
  title: string;
  exercises: ExerciseFormValue[];
}
interface MealFormValue {
  number: number;
  name: string;
  calories: number;
  proteinG: number;
  carbsG: number;
  fatsG: number;
  suggestion: string;
}
interface PhaseFormValue {
  phase: number;
  weeks: string;
  title: string;
  focus: string;
  intensity: string;
  targets: string[];
}

const REQUEST_FIELD_OPTIONS: RequestFieldOption[] = [
  { field: 'weightKg', label: 'Weight' },
  { field: 'targetWeightKg', label: 'Target weight' },
  { field: 'targetDate', label: 'Target date' },
  { field: 'workoutFrequency', label: 'Workout frequency' },
  { field: 'sessionDurationMin', label: 'Session duration' },
  { field: 'equipment', label: 'Equipment' },
  { field: 'injuries', label: 'Injuries' },
  { field: 'allergies', label: 'Allergies' },
  { field: 'mealsPerDay', label: 'Meals per day' },
];

@Component({
  selector: 'app-plan-editor',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './plan-editor.component.html',
  styleUrls: ['../coach-shared.css', './plan-editor.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PlanEditorComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly coachService = inject(CoachService);
  private readonly fb = inject(FormBuilder);

  protected readonly requestFieldOptions = REQUEST_FIELD_OPTIONS;

  protected readonly loading = signal(true);
  protected readonly editor = signal<PlanEditorDto | null>(null);
  protected readonly saving = signal(false);
  protected readonly approving = signal(false);
  protected readonly requestingChanges = signal(false);
  protected readonly creatingVersion = signal(false);
  protected readonly flash = signal<{ ok: boolean; text: string } | null>(null);
  protected readonly showRequestFields = signal(false);
  protected readonly checkedFields = signal<Set<string>>(new Set());

  protected readonly form = this.fb.nonNullable.group({
    coachNote: [''],
    roadmapTitle: [''],
    targetCalories: [0],
    proteinG: [0],
    carbsG: [0],
    fatsG: [0],
    waterLiters: [0],
    hydration: [''],
    sleep: [''],
    consistencyRule: [''],
    supplementsText: [''],
  });

  protected readonly splitDays = this.fb.array<FormGroup>([]);
  protected readonly meals = this.fb.array<FormGroup>([]);
  protected readonly phaseGroups = this.fb.array<FormGroup>([]);

  private versionId = 0;

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      this.versionId = Number(params.get('id'));
      this.load();
    });
  }

  private load(): void {
    this.loading.set(true);
    this.coachService.planEditor(this.versionId).subscribe({
      next: (e) => {
        this.editor.set(e);
        this.populateForm(e);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  private populateForm(e: PlanEditorDto): void {
    const c = e.content;
    this.form.reset({
      coachNote: e.coachNote ?? '',
      roadmapTitle: c.metrics.roadmapTitle,
      targetCalories: c.metrics.targetCalories,
      proteinG: c.metrics.proteinG,
      carbsG: c.metrics.carbsG,
      fatsG: c.metrics.fatsG,
      waterLiters: c.metrics.waterLiters,
      hydration: c.guidelines.hydration,
      sleep: c.guidelines.sleep,
      consistencyRule: c.guidelines.consistencyRule,
      supplementsText: (c.guidelines.supplements ?? []).join('\n'),
    });

    this.splitDays.clear();
    for (const day of c.weeklySplit) {
      const exercisesArray = this.fb.array<FormGroup>([]);
      for (const ex of day.exercises) {
        exercisesArray.push(
          this.fb.nonNullable.group({
            name: [ex.name],
            sets: [ex.sets],
            reps: [ex.reps],
            rest: [ex.rest],
            notes: [ex.notes],
          }),
        );
      }
      this.splitDays.push(
        this.fb.nonNullable.group({
          day: [day.day],
          duration: [day.duration],
          title: [day.title],
          exercises: exercisesArray,
        }),
      );
    }

    this.meals.clear();
    for (const meal of c.mealPlan) {
      this.meals.push(
        this.fb.nonNullable.group({
          number: [meal.number],
          name: [meal.name],
          calories: [meal.calories],
          proteinG: [meal.proteinG],
          carbsG: [meal.carbsG],
          fatsG: [meal.fatsG],
          suggestion: [meal.suggestion],
        }),
      );
    }

    this.phaseGroups.clear();
    for (const p of c.phases) {
      this.phaseGroups.push(
        this.fb.nonNullable.group({
          phase: [p.phase],
          weeks: [p.weeks],
          title: [p.title],
          focus: [p.focus],
          intensity: [p.intensity],
          targets: [p.targets],
        }),
      );
    }

    this.checkedFields.set(new Set());
    this.showRequestFields.set(false);
    this.flash.set(null);
  }

  protected dayExercises(dayIndex: number): FormArray<FormGroup> {
    return this.splitDays.at(dayIndex).get('exercises') as FormArray<FormGroup>;
  }

  protected addExercise(dayIndex: number): void {
    this.dayExercises(dayIndex).push(
      this.fb.nonNullable.group({ name: [''], sets: ['3 sets'], reps: ['8-12 reps'], rest: ['60s'], notes: [''] }),
    );
  }

  protected removeExercise(dayIndex: number, exIndex: number): void {
    this.dayExercises(dayIndex).removeAt(exIndex);
  }

  protected toggleField(field: string): void {
    const set = new Set(this.checkedFields());
    if (set.has(field)) set.delete(field);
    else set.add(field);
    this.checkedFields.set(set);
  }

  private buildContent(): PlanContent {
    const base = this.editor()!.content;
    const raw = this.form.getRawValue();
    const supplements = raw.supplementsText
      .split('\n')
      .map((s) => s.trim())
      .filter((s) => s.length > 0);

    return {
      ...base,
      metrics: {
        ...base.metrics,
        roadmapTitle: raw.roadmapTitle,
        targetCalories: Number(raw.targetCalories),
        proteinG: Number(raw.proteinG),
        carbsG: Number(raw.carbsG),
        fatsG: Number(raw.fatsG),
        waterLiters: Number(raw.waterLiters),
      },
      weeklySplit: (this.splitDays.getRawValue() as DayFormValue[]).map((d) => ({
        day: d.day,
        duration: d.duration,
        title: d.title,
        exercises: d.exercises.filter((ex) => ex.name.trim() !== ''),
      })),
      mealPlan: (this.meals.getRawValue() as MealFormValue[]).map((m) => ({
        number: m.number,
        name: m.name,
        calories: Number(m.calories),
        proteinG: Number(m.proteinG),
        carbsG: Number(m.carbsG),
        fatsG: Number(m.fatsG),
        suggestion: m.suggestion,
      })),
      guidelines: {
        ...base.guidelines,
        hydration: raw.hydration,
        sleep: raw.sleep,
        consistencyRule: raw.consistencyRule,
        supplements,
      },
      phases: this.phaseGroups.getRawValue() as PhaseFormValue[],
    };
  }

  protected saveDraft(): void {
    this.saving.set(true);
    this.flash.set(null);
    this.coachService.saveDraft(this.versionId, this.buildContent(), this.form.getRawValue().coachNote).subscribe({
      next: (e) => {
        this.editor.set(e);
        this.populateForm(e);
        this.saving.set(false);
        this.flash.set({ ok: true, text: 'Draft saved.' });
      },
      error: (err) => {
        this.saving.set(false);
        this.flash.set({ ok: false, text: err?.error?.message ?? 'Could not save the draft.' });
      },
    });
  }

  protected approve(): void {
    if (!confirm('Approve and activate this plan for the client?')) return;
    this.approving.set(true);
    this.flash.set(null);
    const note = this.form.getRawValue().coachNote;
    this.coachService.saveDraft(this.versionId, this.buildContent(), note).subscribe({
      next: () => {
        this.coachService.approve(this.versionId, note).subscribe({
          next: () => void this.router.navigateByUrl('/coach'),
          error: (err) => {
            this.approving.set(false);
            this.flash.set({ ok: false, text: err?.error?.message ?? 'Could not approve.' });
          },
        });
      },
      error: (err) => {
        this.approving.set(false);
        this.flash.set({ ok: false, text: err?.error?.message ?? 'Could not save the draft.' });
      },
    });
  }

  protected submitRequestChanges(): void {
    const note = this.form.getRawValue().coachNote;
    if (!note.trim()) {
      this.flash.set({ ok: false, text: 'Explain to the client what needs to be clarified.' });
      return;
    }
    if (!confirm('Send back to the client for changes?')) return;

    const fields = this.requestFieldOptions.filter((o) => this.checkedFields().has(o.field));
    this.requestingChanges.set(true);
    this.flash.set(null);
    this.coachService.saveDraft(this.versionId, this.buildContent(), note).subscribe({
      next: () => {
        this.coachService.requestChanges(this.versionId, note, fields).subscribe({
          next: () => void this.router.navigateByUrl('/coach'),
          error: (err) => {
            this.requestingChanges.set(false);
            this.flash.set({ ok: false, text: err?.error?.message ?? 'Could not request changes.' });
          },
        });
      },
      error: (err) => {
        this.requestingChanges.set(false);
        this.flash.set({ ok: false, text: err?.error?.message ?? 'Could not save the draft.' });
      },
    });
  }

  protected createNewVersion(): void {
    const programId = this.editor()!.programId;
    this.creatingVersion.set(true);
    this.coachService.newVersionFromActive(programId).subscribe({
      next: (v) => void this.router.navigate(['/coach/plan', v.versionId]),
      error: () => this.creatingVersion.set(false),
    });
  }
}
