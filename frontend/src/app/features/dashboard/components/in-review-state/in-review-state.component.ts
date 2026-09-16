import { DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { PreviewVm } from '../../../../core/models/dashboard.model';

@Component({
  selector: 'app-in-review-state',
  standalone: true,
  imports: [DecimalPipe],
  template: `
    <div class="member-hero in-review">
      <i class="ri-time-fill hero-icon"></i>
      <h1>Your plan is being reviewed by <span>Coach Ilyesse</span></h1>
      <p class="lead">
        Your assessment is in. Your coach is checking your biometrics, goals and injury notes, and
        will fine-tune the draft roadmap before it goes live here.
      </p>
      <div class="eta-pill">
        <i class="ri-calendar-check-line"></i> Expected by {{ eta() }} · you'll get a notification
      </div>
      @if (preview() && preview()!.targetCalories) {
        <p class="preview-text">
          Preview: ~{{ preview()!.targetCalories | number: '1.0-0' }} kcal/day target
          @if (preview()!.splitDays) {
            · {{ preview()!.splitDays }}-day split
          }
        </p>
      }
    </div>
  `,
  styleUrl: '../../dashboard-hero.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InReviewStateComponent {
  readonly preview = input<PreviewVm | null>(null);

  protected readonly eta = computed(() => {
    const d = new Date();
    d.setDate(d.getDate() + 2);
    return d.toLocaleDateString('en-US', { day: 'numeric', month: 'short' });
  });
}
