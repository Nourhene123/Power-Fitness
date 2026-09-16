import { DatePipe, DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { OrderDto } from '../../../core/models/shop.model';

@Component({
  selector: 'app-order-confirmation',
  standalone: true,
  imports: [RouterLink, DecimalPipe, DatePipe],
  templateUrl: './order-confirmation.component.html',
  styleUrl: './order-confirmation.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OrderConfirmationComponent implements OnInit {
  private readonly router = inject(Router);

  protected readonly order = signal<OrderDto | null>(null);

  ngOnInit(): void {
    const order = (history.state as { order?: OrderDto } | undefined)?.order;
    if (!order) {
      void this.router.navigateByUrl('/shop/orders');
      return;
    }
    this.order.set(order);
  }
}
