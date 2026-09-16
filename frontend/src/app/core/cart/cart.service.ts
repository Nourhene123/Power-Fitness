import { computed, Injectable, signal } from '@angular/core';
import { CartLine } from './cart.model';
import { ProductDto } from '../models/shop.model';

const STORAGE_KEY = 'pf_cart';

@Injectable({ providedIn: 'root' })
export class CartService {
  private readonly _lines = signal<CartLine[]>(load());
  private readonly _drawerOpen = signal(false);

  readonly lines = this._lines.asReadonly();
  readonly drawerOpen = this._drawerOpen.asReadonly();
  readonly count = computed(() => this._lines().reduce((n, l) => n + l.quantity, 0));
  readonly subtotal = computed(() => this._lines().reduce((s, l) => s + l.price * l.quantity, 0));

  openDrawer(): void {
    this._drawerOpen.set(true);
  }

  closeDrawer(): void {
    this._drawerOpen.set(false);
  }

  add(product: ProductDto, quantity: number, size: string | null): void {
    const key = lineKey(product.id, size);
    this._lines.update((lines) => {
      const existing = lines.find((l) => l.key === key);
      if (existing) {
        return lines.map((l) =>
          l.key === key ? { ...l, quantity: Math.min(l.quantity + quantity, l.stockQty) } : l,
        );
      }
      return [
        ...lines,
        {
          key,
          productId: product.id,
          name: product.name,
          price: product.price,
          imageUrl: product.imageUrl,
          quantity: Math.min(quantity, product.stockQty),
          size,
          stockQty: product.stockQty,
        },
      ];
    });
    this.persist();
  }

  updateQuantity(key: string, quantity: number): void {
    if (quantity <= 0) {
      this.remove(key);
      return;
    }
    this._lines.update((lines) =>
      lines.map((l) => (l.key === key ? { ...l, quantity: Math.min(quantity, l.stockQty) } : l)),
    );
    this.persist();
  }

  remove(key: string): void {
    this._lines.update((lines) => lines.filter((l) => l.key !== key));
    this.persist();
  }

  clear(): void {
    this._lines.set([]);
    this.persist();
  }

  private persist(): void {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(this._lines()));
    } catch {
    }
  }
}

function lineKey(productId: number, size: string | null): string {
  return size ? `${productId}_${size}` : `${productId}`;
}

function load(): CartLine[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? (JSON.parse(raw) as CartLine[]) : [];
  } catch {
    return [];
  }
}
