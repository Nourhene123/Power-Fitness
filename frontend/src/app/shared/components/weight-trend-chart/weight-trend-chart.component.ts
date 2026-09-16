import { DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { WeightPointVm } from '../../../core/models/dashboard.model';

interface Point {
  x: number;
  y: number;
}

@Component({
  selector: 'app-weight-trend-chart',
  standalone: true,
  imports: [DecimalPipe],
  template: `
    @if (series().length < 1) {
      <p class="empty">Log a weigh-in to see your trend.</p>
    } @else {
      <svg viewBox="0 0 300 90" preserveAspectRatio="none" role="img" aria-label="Weight trend">
        <rect x="0" [attr.y]="band().y1" width="300" [attr.height]="band().height" fill="rgba(34,197,94,0.12)" />
        <line
          x1="0"
          [attr.y1]="band().mid"
          x2="300"
          [attr.y2]="band().mid"
          stroke="rgba(34,197,94,0.4)"
          stroke-dasharray="3 3"
          stroke-width="1"
        />
        @if (series().length > 1) {
          <polyline
            [attr.points]="polyline()"
            fill="none"
            stroke="#f9ac54"
            stroke-width="2"
            stroke-linejoin="round"
            stroke-linecap="round"
          />
        }
        <circle [attr.cx]="last().x" [attr.cy]="last().y" r="3.5" fill="#f9ac54" />
        <text [attr.x]="last().x - 4" [attr.y]="Math.max(10, last().y - 7)" fill="#f1f5f9" font-size="10" text-anchor="end">
          {{ lastValue() | number: '1.1-1' }}
        </text>
      </svg>
    }
  `,
  styles: [
    `
      :host {
        display: block;
      }
      svg {
        width: 100%;
        height: auto;
        display: block;
      }
      .empty {
        color: var(--text-muted);
        font-size: 12.5px;
        margin: 6px 0;
      }
    `,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WeightTrendChartComponent {
  readonly series = input.required<WeightPointVm[]>();
  readonly goalBandLow = input.required<number>();
  readonly goalBandHigh = input.required<number>();
  readonly startKg = input.required<number>();

  protected readonly Math = Math;

  private readonly w = 300;
  private readonly h = 90;
  private readonly pad = 8;

  private readonly range = computed(() => {
    const vals = this.series().map((p) => p.value);
    vals.push(this.goalBandLow(), this.goalBandHigh(), this.startKg());
    const min = Math.min(...vals) - 0.5;
    const max = Math.max(...vals) + 0.5;
    return { min, max: Math.max(min + 0.1, max) };
  });

  private x(i: number): number {
    const n = this.series().length;
    return this.pad + (n <= 1 ? (this.w - 2 * this.pad) / 2 : (i * (this.w - 2 * this.pad)) / (n - 1));
  }

  private y(v: number): number {
    const { min, max } = this.range();
    return this.pad + (this.h - 2 * this.pad) * (1 - (v - min) / (max - min));
  }

  protected readonly band = computed(() => {
    const y1 = this.y(this.goalBandHigh());
    const y2 = this.y(this.goalBandLow());
    return { y1, height: Math.max(0, y2 - y1), mid: (y1 + y2) / 2 };
  });

  protected readonly polyline = computed(() =>
    this.series()
      .map((p, i) => `${this.x(i).toFixed(1)},${this.y(p.value).toFixed(1)}`)
      .join(' '),
  );

  protected readonly last = computed((): Point => {
    const series = this.series();
    const i = series.length - 1;
    return { x: this.x(i), y: this.y(series[i]?.value ?? 0) };
  });

  protected readonly lastValue = computed(() => this.series()[this.series().length - 1]?.value ?? 0);
}
