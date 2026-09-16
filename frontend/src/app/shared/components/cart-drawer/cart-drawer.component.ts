import { DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CartService } from '../../../core/cart/cart.service';
import { AuthStore } from '../../../core/auth/auth.store';

const FREE_DELIVERY_THRESHOLD = 150;

@Component({
  selector: 'app-cart-drawer',
  standalone: true,
  imports: [DecimalPipe, RouterLink],
  templateUrl: './cart-drawer.component.html',
  styleUrl: './cart-drawer.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CartDrawerComponent {
  protected readonly cart = inject(CartService);
  private readonly authStore = inject(AuthStore);
  private readonly router = inject(Router);

  protected readonly remainingForFreeDelivery = computed(() =>
    Math.max(0, FREE_DELIVERY_THRESHOLD - this.cart.subtotal()),
  );
  protected readonly deliveryFee = computed(() => (this.remainingForFreeDelivery() > 0 ? 7 : 0));
  protected readonly total = computed(() => this.cart.subtotal() + this.deliveryFee());
  protected readonly progressPct = computed(() =>
    Math.min(100, (this.cart.subtotal() / FREE_DELIVERY_THRESHOLD) * 100),
  );

  protected close(): void {
    this.cart.closeDrawer();
  }

  protected checkout(): void {
    this.cart.closeDrawer();
    if (this.authStore.isAuthenticated()) {
      void this.router.navigateByUrl('/shop/checkout');
    } else {
      void this.router.navigate(['/login'], { queryParams: { returnUrl: '/shop/checkout' } });
    }
  }
}
