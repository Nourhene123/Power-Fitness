import { DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, input, signal } from '@angular/core';
import { PlanContent } from '../../../core/models/plan-content.model';

@Component({
  selector: 'app-plan-view',
  standalone: true,
  imports: [DecimalPipe],
  templateUrl: './plan-view.component.html',
  styleUrl: './plan-view.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PlanViewComponent {
  readonly content = input.required<PlanContent>();

  protected readonly activeDay = signal(0);

  protected showDay(i: number): void {
    this.activeDay.set(i);
  }
}
