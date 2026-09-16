import { DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ShopApiService } from '../data/shop.service';
import { CartService } from '../../../core/cart/cart.service';
import { AuthStore } from '../../../core/auth/auth.store';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, DecimalPipe],
  templateUrl: './checkout.component.html',
  styleUrl: './checkout.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CheckoutComponent implements OnInit {
  private readonly api = inject(ShopApiService);
  protected readonly cart = inject(CartService);
  private readonly authStore = inject(AuthStore);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    recipientName: [this.authStore.user()?.name ?? '', Validators.required],
    phone: ['', Validators.required],
    addressLine: ['', Validators.required],
    city: ['', Validators.required],
    note: [''],
  });

  protected readonly deliveryFee = () => (this.cart.subtotal() < 150 ? 7 : 0);
  protected readonly total = () => this.cart.subtotal() + this.deliveryFee();

  ngOnInit(): void {
    if (!this.cart.lines().length) {
      void this.router.navigateByUrl('/shop');
    }
  }

  protected submit(): void {
    if (this.form.invalid || !this.cart.lines().length) {
      this.form.markAllAsTouched();
      return;
    }
    const raw = this.form.getRawValue();
    this.submitting.set(true);
    this.errorMessage.set(null);

    this.api
      .placeOrder({
        items: this.cart.lines().map((l) => ({ productId: l.productId, quantity: l.quantity, size: l.size })),
        recipientName: raw.recipientName.trim(),
        phone: raw.phone.trim(),
        addressLine: raw.addressLine.trim(),
        city: raw.city.trim(),
        note: raw.note?.trim() || null,
      })
      .subscribe({
        next: (order) => {
          this.cart.clear();
          this.submitting.set(false);
          void this.router.navigate(['/shop/order-confirmation'], { state: { order } });
        },
        error: (err) => {
          this.submitting.set(false);
          this.errorMessage.set(err?.error?.message ?? 'Could not place your order. Please try again.');
        },
      });
  }
}
