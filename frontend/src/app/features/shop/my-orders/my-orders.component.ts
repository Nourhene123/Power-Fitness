import { DatePipe, DecimalPipe, LowerCasePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ShopApiService } from '../data/shop.service';
import { OrderDto } from '../../../core/models/shop.model';

@Component({
  selector: 'app-my-orders',
  standalone: true,
  imports: [RouterLink, DecimalPipe, DatePipe, LowerCasePipe],
  templateUrl: './my-orders.component.html',
  styleUrl: './my-orders.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MyOrdersComponent implements OnInit {
  private readonly api = inject(ShopApiService);

  protected readonly loading = signal(true);
  protected readonly orders = signal<OrderDto[]>([]);

  ngOnInit(): void {
    this.api.myOrders().subscribe({
      next: (orders) => {
        this.orders.set(orders);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  protected statusClass(status: string): string {
    if (status === 'DELIVERED') return 'ok';
    if (status === 'CANCELLED') return 'cancelled';
    if (status === 'PENDING') return 'pending';
    return 'progress';
  }
}
