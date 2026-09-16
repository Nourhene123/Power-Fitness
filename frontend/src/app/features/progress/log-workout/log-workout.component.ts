import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { DatePipe } from '@angular/common';
import { ProgramApiService } from '../../program/data/program.service';
import { ProgressApiService } from '../data/progress.service';
import { PlanTrainingDay } from '../../../core/models/plan-content.model';
import { WorkoutSessionDto } from '../../../core/models/progress.model';

@Component({
  selector: 'app-log-workout',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, DatePipe],
  templateUrl: './log-workout.component.html',
  styleUrl: '../progress-shared.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LogWorkoutComponent implements OnInit {
  private readonly programApi = inject(ProgramApiService);
  private readonly api = inject(ProgressApiService);
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);

  protected readonly loading = signal(true);
  protected readonly split = signal<PlanTrainingDay[]>([]);
  protected readonly recent = signal<WorkoutSessionDto[]>([]);
  protected readonly saved = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly submitting = signal(false);

  protected readonly form = this.fb.nonNullable.group({
    dayIndex: [0],
    sessionDate: [today(), Validators.required],
    durationMin: this.fb.control<number | null>(null),
    rpe: this.fb.control<number | null>(null),
    note: [''],
  });

  ngOnInit(): void {
    const dayParam = Number(this.route.snapshot.queryParamMap.get('day'));
    if (!Number.isNaN(dayParam) && dayParam >= 0) {
      this.form.controls.dayIndex.setValue(dayParam);
    }

    this.programApi.myProgram().subscribe({
      next: (program) => {
        this.split.set(program.content?.weeklySplit ?? []);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
    this.loadRecent();
  }

  private loadRecent(): void {
    this.api.recentWorkouts().subscribe((sessions) => this.recent.set(sessions));
  }

  protected submit(): void {
    if (this.form.invalid) return;
    const raw = this.form.getRawValue();
    this.submitting.set(true);
    this.errorMessage.set(null);
    this.saved.set(false);
    this.api
      .logWorkout({
        dayIndex: raw.dayIndex,
        sessionDate: raw.sessionDate,
        durationMin: raw.durationMin,
        rpe: raw.rpe,
        note: raw.note?.trim() || null,
      })
      .subscribe({
        next: () => {
          this.submitting.set(false);
          this.saved.set(true);
          this.loadRecent();
        },
        error: (err) => {
          this.submitting.set(false);
          this.errorMessage.set(err?.error?.message ?? 'That date is in the future.');
        },
      });
  }
}

function today(): string {
  return new Date().toISOString().slice(0, 10);
}
