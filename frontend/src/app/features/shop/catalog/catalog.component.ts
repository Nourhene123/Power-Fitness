import { DecimalPipe, LowerCasePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ShopApiService } from '../data/shop.service';
import { CartService } from '../../../core/cart/cart.service';
import { ProductCategory, ProductDto } from '../../../core/models/shop.model';

type SortOption = 'featured' | 'price_asc' | 'price_desc' | 'name_asc';

const CATEGORY_TABS: ReadonlyArray<{ value: ProductCategory | 'ALL'; label: string; icon: string }> = [
  { value: 'ALL', label: 'All Products', icon: 'ri-apps-2-line' },
  { value: 'SUPPLEMENTS', label: 'Supplements', icon: 'ri-flask-line' },
  { value: 'APPAREL', label: 'Apparel', icon: 'ri-t-shirt-line' },
  { value: 'ACCESSORIES', label: 'Accessories', icon: 'ri-boxing-line' },
  { value: 'EQUIPMENT', label: 'Equipment', icon: 'ri-tools-fill' },
];

@Component({
  selector: 'app-catalog',
  standalone: true,
  imports: [DecimalPipe, LowerCasePipe],
  templateUrl: './catalog.component.html',
  styleUrl: './catalog.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CatalogComponent implements OnInit {
  private readonly api = inject(ShopApiService);
  protected readonly cart = inject(CartService);
  private readonly route = inject(ActivatedRoute);

  protected readonly loading = signal(true);
  protected readonly products = signal<ProductDto[]>([]);
  protected readonly category = signal<ProductCategory | 'ALL'>('ALL');
  protected readonly search = signal('');
  protected readonly sort = signal<SortOption>('featured');
  protected readonly quantities = signal<Record<number, number>>({});
  protected readonly sizes = signal<Record<number, string>>({});

  protected readonly tabs = CATEGORY_TABS;
  protected readonly sizeOptions = ['XS', 'S', 'M', 'L', 'XL'];

  protected readonly counts = computed(() => {
    const all = this.products();
    const map: Record<string, number> = { ALL: all.length };
    for (const tab of CATEGORY_TABS) {
      if (tab.value !== 'ALL') {
        map[tab.value] = all.filter((p) => p.category === tab.value).length;
      }
    }
    return map;
  });

  protected readonly visible = computed(() => {
    let list = this.products();
    if (this.category() !== 'ALL') {
      list = list.filter((p) => p.category === this.category());
    }
    const term = this.search().trim().toLowerCase();
    if (term) {
      list = list.filter(
        (p) => p.name.toLowerCase().includes(term) || (p.description ?? '').toLowerCase().includes(term),
      );
    }
    const sorted = [...list];
    switch (this.sort()) {
      case 'price_asc':
        sorted.sort((a, b) => a.price - b.price);
        break;
      case 'price_desc':
        sorted.sort((a, b) => b.price - a.price);
        break;
      case 'name_asc':
        sorted.sort((a, b) => a.name.localeCompare(b.name));
        break;
    }
    return sorted;
  });

  ngOnInit(): void {
    const initialCategory = this.route.snapshot.queryParamMap.get('category');
    if (initialCategory) {
      this.category.set(initialCategory.toUpperCase() as ProductCategory);
    }
    this.api.products(null).subscribe({
      next: (products) => {
        this.products.set(products);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  protected selectCategory(value: ProductCategory | 'ALL'): void {
    this.category.set(value);
  }

  protected onSearch(value: string): void {
    this.search.set(value);
  }

  protected onSort(value: string): void {
    this.sort.set(value as SortOption);
  }

  protected quantityFor(id: number): number {
    return this.quantities()[id] ?? 1;
  }

  protected setQuantity(id: number, qty: number): void {
    this.quantities.update((q) => ({ ...q, [id]: Math.max(1, qty) }));
  }

  protected sizeFor(id: number): string {
    return this.sizes()[id] ?? 'M';
  }

  protected setSize(id: number, size: string): void {
    this.sizes.update((s) => ({ ...s, [id]: size }));
  }

  protected addToCart(product: ProductDto): void {
    const qty = this.quantityFor(product.id);
    const size = product.category === 'APPAREL' ? this.sizeFor(product.id) : null;
    this.cart.add(product, qty, size);
    this.cart.openDrawer();
  }

  protected ratingFor(product: ProductDto, index: number): string {
    return (4.8 + (index % 3) * 0.1).toFixed(1);
  }

  protected reviewsFor(product: ProductDto): number {
    return 45 + product.id * 7;
  }

  protected isBestSeller(product: ProductDto, index: number): boolean {
    return index === 0 || product.id % 2 === 0;
  }
}
