import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { AuthStore } from '../../core/auth/auth.store';
import { DashboardApiService } from './data/dashboard.service';
import { DashboardDto } from '../../core/models/dashboard.model';
import { HabitKey } from '../../core/models/progress.model';
import { ProgressApiService } from '../progress/data/progress.service';
import { WelcomeStateComponent } from './components/welcome-state/welcome-state.component';
import { ChangesRequestedStateComponent } from './components/changes-requested-state/changes-requested-state.component';
import { InReviewStateComponent } from './components/in-review-state/in-review-state.component';
import { ActiveStateComponent } from './components/active-state/active-state.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [WelcomeStateComponent, ChangesRequestedStateComponent, InReviewStateComponent, ActiveStateComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardComponent implements OnInit {
  private readonly api = inject(DashboardApiService);
  private readonly progressApi = inject(ProgressApiService);
  protected readonly authStore = inject(AuthStore);

  protected readonly loading = signal(true);
  protected readonly dashboard = signal<DashboardDto | null>(null);

  protected readonly firstName = () => this.authStore.user()?.name.split(' ')[0] ?? 'there';

  ngOnInit(): void {
    this.api.get().subscribe({
      next: (dto) => {
        this.dashboard.set(dto);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  protected onHabitToggle(key: HabitKey): void {
    const current = this.dashboard();
    if (!current?.today) return;
    const before = current.today.habits[key];
    this.dashboard.set({ ...current, today: { ...current.today, habits: { ...current.today.habits, [key]: !before } } });

    this.progressApi.toggleHabit(key).subscribe({
      next: ({ value }) => {
        const latest = this.dashboard();
        if (!latest?.today) return;
        this.dashboard.set({ ...latest, today: { ...latest.today, habits: { ...latest.today.habits, [key]: value } } });
      },
      error: () => {
        const latest = this.dashboard();
        if (!latest?.today) return;
        this.dashboard.set({ ...latest, today: { ...latest.today, habits: { ...latest.today.habits, [key]: before } } });
      },
    });
  }
}
