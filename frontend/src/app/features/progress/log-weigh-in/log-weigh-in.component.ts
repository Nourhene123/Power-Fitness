import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DatePipe, DecimalPipe } from '@angular/common';
import { ProgressApiService } from '../data/progress.service';
import { ProgressLogDto } from '../../../core/models/progress.model';

@Component({
  selector: 'app-log-weigh-in',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, DatePipe, DecimalPipe],
  templateUrl: './log-weigh-in.component.html',
  styleUrl: '../progress-shared.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LogWeighInComponent implements OnInit {
  private readonly api = inject(ProgressApiService);
  private readonly fb = inject(FormBuilder);

  protected readonly history = signal<ProgressLogDto[]>([]);
  protected readonly saved = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly submitting = signal(false);

  protected readonly form = this.fb.group({
    loggedOn: this.fb.nonNullable.control(today(), Validators.required),
    weightKg: this.fb.control<number | null>(null),
    waistCm: this.fb.control<number | null>(null),
    hipCm: this.fb.control<number | null>(null),
    chestCm: this.fb.control<number | null>(null),
    armCm: this.fb.control<number | null>(null),
    thighCm: this.fb.control<number | null>(null),
    neckCm: this.fb.control<number | null>(null),
    energy: this.fb.control<number | null>(null),
    note: this.fb.nonNullable.control(''),
  });

  ngOnInit(): void {
    this.loadHistory();
  }

  private loadHistory(): void {
    this.api.recentWeighIns().subscribe((rows) => {
      this.history.set(rows);
      const last = rows[0];
      if (last?.weightKg != null) {
        this.form.controls.weightKg.setValue(last.weightKg);
      }
    });
  }

  protected submit(): void {
    if (this.form.invalid) return;
    const raw = this.form.getRawValue();
    this.submitting.set(true);
    this.errorMessage.set(null);
    this.saved.set(false);
    this.api
      .logWeighIn({
        loggedOn: raw.loggedOn,
        weightKg: raw.weightKg,
        waistCm: raw.waistCm,
        hipCm: raw.hipCm,
        chestCm: raw.chestCm,
        armCm: raw.armCm,
        thighCm: raw.thighCm,
        neckCm: raw.neckCm,
        energy: raw.energy,
        note: raw.note?.trim() || null,
      })
      .subscribe({
        next: () => {
          this.submitting.set(false);
          this.saved.set(true);
          this.loadHistory();
        },
        error: (err) => {
          this.submitting.set(false);
          this.errorMessage.set(err?.error?.message ?? 'Enter a realistic weight (30-300 kg).');
        },
      });
  }
}

function today(): string {
  return new Date().toISOString().slice(0, 10);
}
