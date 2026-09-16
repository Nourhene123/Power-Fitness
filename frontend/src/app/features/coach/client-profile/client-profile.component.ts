import { DatePipe, DecimalPipe, TitleCasePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CoachService } from '../data/coach.service';
import { ClientProfileDto } from '../data/coach.model';

const FEASIBILITY_COLOR: Record<string, string> = {
  realistic: '#34d399',
  maintenance: '#38bdf8',
  aggressive: '#fbbf24',
  unrealistic: '#f87171',
};
const SEVERITY_COLOR: Record<string, string> = { high: '#f87171', medium: '#fbbf24', low: '#94a3b8' };


@Component({
  selector: 'app-client-profile',
  standalone: true,
  imports: [RouterLink, TitleCasePipe, DecimalPipe, DatePipe],
  templateUrl: './client-profile.component.html',
  styleUrls: ['../coach-shared.css', './client-profile.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ClientProfileComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly coachService = inject(CoachService);

  protected readonly loading = signal(true);
  protected readonly profile = signal<ClientProfileDto | null>(null);

  ngOnInit(): void {
    // Reactive rather than a one-time snapshot, so navigating between two clients' profiles
    // (same route, different :id) reloads correctly even if Angular reuses the component instance.
    this.route.paramMap.subscribe((params) => {
      const userId = Number(params.get('id'));
      this.loading.set(true);
      this.coachService.clientProfile(userId).subscribe({
        next: (p) => {
          this.profile.set(p);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
    });
  }

  protected feasColor(verdict: string): string {
    return FEASIBILITY_COLOR[verdict] ?? '#94a3b8';
  }

  protected sevColor(sev: string): string {
    return SEVERITY_COLOR[sev] ?? '#94a3b8';
  }

  protected label(s: string | null | undefined): string {
    return s ? s.replace(/_/g, ' ') : '—';
  }
}
