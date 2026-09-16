import { DatePipe, DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ProgramApiService } from '../data/program.service';
import { ProgramVersionDetail, ProgramVersionSummary } from '../../../core/models/program.model';

const VIEWABLE = new Set(['ACTIVE', 'SUPERSEDED', 'COMPLETED']);

/** `/program/history` — the version timeline + changelog. Port of `program_history.php`. */
@Component({
  selector: 'app-program-history',
  standalone: true,
  imports: [RouterLink, DatePipe, DecimalPipe],
  templateUrl: './program-history.component.html',
  styleUrl: './program-history.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProgramHistoryComponent implements OnInit {
  private readonly api = inject(ProgramApiService);

  protected readonly loading = signal(true);
  protected readonly versions = signal<ProgramVersionSummary[]>([]);
  protected readonly viewingId = signal<number | null>(null);
  protected readonly viewing = signal<ProgramVersionDetail | null>(null);
  protected readonly viewLoading = signal(false);

  protected readonly canView = (status: string) => VIEWABLE.has(status);

  ngOnInit(): void {
    this.api.versions().subscribe({
      next: (list) => {
        this.versions.set(list);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  protected toggleView(id: number): void {
    if (this.viewingId() === id) {
      this.viewingId.set(null);
      this.viewing.set(null);
      return;
    }
    this.viewingId.set(id);
    this.viewing.set(null);
    this.viewLoading.set(true);
    this.api.version(id).subscribe({
      next: (detail) => {
        this.viewing.set(detail);
        this.viewLoading.set(false);
      },
      error: () => this.viewLoading.set(false),
    });
  }

  protected statusClass(status: string): string {
    if (status === 'ACTIVE') return 'active';
    if (status === 'SUPERSEDED') return 'superseded';
    return 'other';
  }

  protected statusLabel(status: string): string {
    return status
      .toLowerCase()
      .split('_')
      .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
      .join(' ');
  }
}
