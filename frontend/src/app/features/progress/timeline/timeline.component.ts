import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProgressApiService } from '../data/progress.service';
import { TimelineItemDto } from '../../../core/models/progress.model';

const ICON: Record<TimelineItemDto['type'], string> = {
  weigh: 'ri-scales-3-line',
  workout: 'ri-boxing-fill',
  plan: 'ri-git-commit-line',
};

@Component({
  selector: 'app-timeline',
  standalone: true,
  imports: [DatePipe, RouterLink],
  templateUrl: './timeline.component.html',
  styleUrl: '../progress-shared.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TimelineComponent implements OnInit {
  private readonly api = inject(ProgressApiService);

  protected readonly loading = signal(true);
  protected readonly items = signal<TimelineItemDto[]>([]);

  protected iconFor(type: TimelineItemDto['type']): string {
    return ICON[type];
  }

  ngOnInit(): void {
    this.api.timeline().subscribe({
      next: (items) => {
        this.items.set(items);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
