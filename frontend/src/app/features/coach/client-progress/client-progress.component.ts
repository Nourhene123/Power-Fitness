import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CoachService } from '../data/coach.service';
import { ClientProgressDto, RosterEntry } from '../data/coach.model';

@Component({
  selector: 'app-client-progress',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './client-progress.component.html',
  styleUrls: ['../coach-shared.css', './client-progress.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ClientProgressComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly coachService = inject(CoachService);

  protected readonly loading = signal(true);
  protected readonly progress = signal<ClientProgressDto | null>(null);
  protected readonly roster = signal<RosterEntry[]>([]);
  protected readonly clientId = signal(0);

  ngOnInit(): void {
    this.coachService.dashboard().subscribe((d) => this.roster.set(d.roster));

    // Reactive, not a one-time snapshot: the client picker below navigates from this same route
    // to itself with a new :id, and Angular reuses the component instance rather than re-running
    // ngOnInit, so a snapshot read here would go stale on that specific transition.
    this.route.paramMap.subscribe((params) => {
      const id = Number(params.get('id'));
      this.clientId.set(id);
      this.loading.set(true);
      this.coachService.clientProgress(id).subscribe({
        next: (p) => {
          this.progress.set(p);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
    });
  }

  protected onSelect(event: Event): void {
    const id = (event.target as HTMLSelectElement).value;
    if (id) {
      void this.router.navigate(['/coach/clients', id, 'progress']);
    }
  }

  protected weightDelta(i: number): number {
    const rows = this.progress()?.weightLogs ?? [];
    if (i === 0 || !rows[i].weightKg || !rows[i - 1].weightKg) return 0;
    return Math.round((rows[i].weightKg! - rows[i - 1].weightKg!) * 10) / 10;
  }

  protected daysAgo(days: number | null | undefined): string {
    // Jackson's global non_null inclusion drops null fields from the JSON entirely, so a missing
    // "never happened" value arrives as `undefined`, not `null` — check both with `==`.
    if (days == null) return 'Never';
    if (days === 0) return 'Today';
    if (days === 1) return 'Yesterday';
    return `${days} days ago`;
  }
}
