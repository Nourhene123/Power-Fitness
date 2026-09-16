import { DatePipe, TitleCasePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CoachService } from '../data/coach.service';
import { CoachDashboardDto } from '../data/coach.model';
import { AuthStore } from '../../../core/auth/auth.store';

@Component({
  selector: 'app-coach-dashboard',
  standalone: true,
  imports: [RouterLink, DatePipe, TitleCasePipe],
  templateUrl: './coach-dashboard.component.html',
  styleUrls: ['../coach-shared.css', './coach-dashboard.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CoachDashboardComponent implements OnInit {
  private readonly coachService = inject(CoachService);
  protected readonly authStore = inject(AuthStore);

  protected readonly loading = signal(true);
  protected readonly dashboard = signal<CoachDashboardDto | null>(null);

  protected readonly firstName = () => this.authStore.user()?.name.split(' ')[0] ?? 'Coach';

  ngOnInit(): void {
    this.coachService.dashboard().subscribe({
      next: (d) => {
        this.dashboard.set(d);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  protected goalLabel(goal: string): string {
    return goal.replace(/_/g, ' ');
  }
}
