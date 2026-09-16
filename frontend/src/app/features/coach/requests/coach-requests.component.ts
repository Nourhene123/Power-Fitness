import { DatePipe, TitleCasePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { CoachRequestsAdminService, CoachRequestStats } from './data/coach-requests-admin.service';
import { CoachRequest } from '../../contact/data/coach-request.service';

@Component({
  selector: 'app-coach-requests',
  standalone: true,
  imports: [DatePipe, TitleCasePipe],
  templateUrl: './coach-requests.component.html',
  styleUrls: ['../coach-shared.css', './coach-requests.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CoachRequestsComponent implements OnInit {
  private readonly api = inject(CoachRequestsAdminService);

  protected readonly loading = signal(true);
  protected readonly requests = signal<CoachRequest[]>([]);
  protected readonly stats = signal<CoachRequestStats | null>(null);
  protected readonly busyId = signal<number | null>(null);

  ngOnInit(): void {
    this.reload();
  }

  private reload(): void {
    this.loading.set(true);
    this.api.list().subscribe({
      next: (list) => {
        this.requests.set(list);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
    this.api.stats().subscribe((s) => this.stats.set(s));
  }

  protected updateStatus(request: CoachRequest, status: string): void {
    this.busyId.set(request.id);
    this.api.updateStatus(request.id, status as CoachRequest['status']).subscribe({
      next: () => {
        this.busyId.set(null);
        this.reload();
      },
      error: () => this.busyId.set(null),
    });
  }

  protected remove(request: CoachRequest): void {
    if (!confirm(`Delete the request from ${request.name}?`)) return;
    this.busyId.set(request.id);
    this.api.delete(request.id).subscribe({
      next: () => {
        this.busyId.set(null);
        this.reload();
      },
      error: () => this.busyId.set(null),
    });
  }
}
