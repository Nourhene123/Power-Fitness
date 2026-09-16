import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ProgramApiService } from '../data/program.service';
import { ChangeRequestDto } from '../../../core/models/program.model';

@Component({
  selector: 'app-answer-coach',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './answer-coach.component.html',
  styleUrl: './answer-coach.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AnswerCoachComponent implements OnInit {
  private readonly api = inject(ProgramApiService);
  private readonly fb = inject(FormBuilder);

  protected readonly loading = signal(true);
  protected readonly notFound = signal(false);
  protected readonly request = signal<ChangeRequestDto | null>(null);
  protected readonly submitting = signal(false);
  protected readonly done = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  // Untyped FormGroup: fields are added dynamically once the coach's field list arrives.
  protected readonly form: FormGroup = this.fb.group({ message: [''] });

  ngOnInit(): void {
    this.api.changeRequest().subscribe({
      next: (dto) => {
        this.request.set(dto);
        for (const f of dto.fields) {
          this.form.addControl(f.field, this.fb.nonNullable.control(dto.prefill[f.field] ?? ''));
        }
        this.loading.set(false);
      },
      error: () => {
        this.notFound.set(true);
        this.loading.set(false);
      },
    });
  }

  protected currentValue(field: string): string {
    return this.request()?.prefill[field] ?? '';
  }

  protected submit(): void {
    const req = this.request();
    if (!req) return;
    const raw = this.form.getRawValue() as Record<string, string>;
    const answers: Record<string, string> = {};
    for (const f of req.fields) {
      answers[f.field] = raw[f.field] ?? '';
    }
    this.submitting.set(true);
    this.errorMessage.set(null);
    this.api.resolveChangeRequest(answers, raw['message'] ?? '').subscribe({
      next: () => {
        this.submitting.set(false);
        this.done.set(true);
      },
      error: () => {
        this.submitting.set(false);
        this.errorMessage.set('Could not send your answers. Please try again.');
      },
    });
  }
}
