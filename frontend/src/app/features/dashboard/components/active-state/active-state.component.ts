import { DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import {
  CoachVm,
  GoalVm,
  MilestoneVm,
  PhaseVm,
  ProgressVm,
  TodayVm,
} from '../../../../core/models/dashboard.model';
import { HabitKey } from '../../../../core/models/progress.model';
import { WeightTrendChartComponent } from '../../../../shared/components/weight-trend-chart/weight-trend-chart.component';

@Component({
  selector: 'app-active-state',
  standalone: true,
  imports: [RouterLink, DecimalPipe, WeightTrendChartComponent],
  templateUrl: './active-state.component.html',
  styleUrl: './active-state.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ActiveStateComponent {
  readonly goal = input.required<GoalVm>();
  readonly phase = input.required<PhaseVm>();
  readonly today = input.required<TodayVm>();
  readonly progress = input.required<ProgressVm>();
  readonly coach = input.required<CoachVm>();
  readonly milestone = input.required<MilestoneVm>();

  readonly habitToggle = output<HabitKey>();

  protected readonly habitMeta: ReadonlyArray<{ key: HabitKey; icon: string; label: string }> = [
    { key: 'workout', icon: 'ri-boxing-fill', label: 'Trained' },
    { key: 'nutrition', icon: 'ri-restaurant-fill', label: 'Ate on target' },
    { key: 'water', icon: 'ri-drop-fill', label: 'Hydrated' },
    { key: 'sleep', icon: 'ri-moon-fill', label: 'Slept 7h+' },
  ];

  protected readonly weeks = computed(() => Array.from({ length: this.phase().totalWeeks }, (_, i) => i + 1));

  protected readonly todayLabel = new Date().toLocaleDateString('en-US', {
    weekday: 'long',
    day: 'numeric',
    month: 'short',
  });

  protected habitOn(key: HabitKey): boolean {
    return this.today().habits[key];
  }

  protected onHabitClick(key: HabitKey): void {
    this.habitToggle.emit(key);
  }
}
