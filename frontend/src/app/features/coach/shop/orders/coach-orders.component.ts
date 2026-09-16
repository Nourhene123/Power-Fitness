import { DatePipe, DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { CoachShopApiService } from '../data/coach-shop.service';
import { AdminOrderDto } from '../../../../core/models/shop.model';

@Component({
  selector: 'app-coach-orders',
  standalone: true,
  imports: [DatePipe, DecimalPipe],
  templateUrl: './coach-orders.component.html',
  styleUrls: ['../../coach-shared.css', '../coach-shop-shared.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CoachOrdersComponent implements OnInit {
  private readonly api = inject(CoachShopApiService);

  protected readonly loading = signal(true);
  protected readonly orders = signal<AdminOrderDto[]>([]);
  protected readonly statusFilter = signal<string>('');
  protected readonly busyId = signal<number | null>(null);

  ngOnInit(): void {
    this.reload();
  }

  protected onFilterChange(status: string): void {
    this.statusFilter.set(status);
    this.reload();
  }

  private reload(): void {
    this.loading.set(true);
    this.api.orders(this.statusFilter() || null).subscribe({
      next: (list) => {
        this.orders.set(list);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  protected updateStatus(order: AdminOrderDto, status: string): void {
    this.busyId.set(order.id);
    this.api.updateOrderStatus(order.id, status).subscribe({
      next: () => {
        this.busyId.set(null);
        this.reload();
      },
      error: () => this.busyId.set(null),
    });
  }
}
