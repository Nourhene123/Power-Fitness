import { DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, HostListener, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CoachShopApiService } from '../data/coach-shop.service';
import { ProductCategory, ProductDto } from '../../../../core/models/shop.model';

@Component({
  selector: 'app-coach-products',
  standalone: true,
  imports: [ReactiveFormsModule, DecimalPipe],
  templateUrl: './coach-products.component.html',
  styleUrls: ['../../coach-shared.css', '../coach-shop-shared.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CoachProductsComponent implements OnInit {
  private readonly api = inject(CoachShopApiService);
  private readonly fb = inject(FormBuilder);

  protected readonly loading = signal(true);
  protected readonly products = signal<ProductDto[]>([]);
  protected readonly formOpen = signal(false);
  protected readonly editingId = signal<number | null>(null);
  protected readonly busyId = signal<number | null>(null);
  protected readonly saving = signal(false);
  protected readonly uploading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly categories: ProductCategory[] = ['SUPPLEMENTS', 'APPAREL', 'ACCESSORIES', 'EQUIPMENT'];

  protected readonly form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    category: this.fb.nonNullable.control<ProductCategory>('SUPPLEMENTS'),
    description: [''],
    price: [0, [Validators.required, Validators.min(0.001)]],
    imageUrl: [''],
    stockQty: [0, [Validators.required, Validators.min(0)]],
    active: [true],
  });

  ngOnInit(): void {
    this.reload();
  }

  private reload(): void {
    this.loading.set(true);
    this.api.products().subscribe({
      next: (list) => {
        this.products.set(list);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  protected openCreate(): void {
    this.editingId.set(null);
    this.form.reset({ name: '', category: 'SUPPLEMENTS', description: '', price: 0, imageUrl: '', stockQty: 0, active: true });
    this.errorMessage.set(null);
    this.formOpen.set(true);
  }

  protected openEdit(product: ProductDto): void {
    this.editingId.set(product.id);
    this.form.reset({
      name: product.name,
      category: product.category,
      description: product.description ?? '',
      price: product.price,
      imageUrl: product.imageUrl ?? '',
      stockQty: product.stockQty,
      active: product.active,
    });
    this.errorMessage.set(null);
    this.formOpen.set(true);
  }

  protected cancelForm(): void {
    this.formOpen.set(false);
  }

  @HostListener('document:keydown.escape')
  protected onEscape(): void {
    if (this.formOpen()) this.cancelForm();
  }

  protected onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    this.uploading.set(true);
    this.errorMessage.set(null);
    this.api.uploadProductImage(file).subscribe({
      next: ({ url }) => {
        this.form.controls.imageUrl.setValue(url);
        this.uploading.set(false);
      },
      error: (err) => {
        this.uploading.set(false);
        this.errorMessage.set(err?.error?.message ?? 'Could not upload this image.');
      },
    });
    input.value = '';
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const raw = this.form.getRawValue();
    this.saving.set(true);
    this.errorMessage.set(null);
    const id = this.editingId();

    const body = {
      name: raw.name.trim(),
      category: raw.category,
      description: raw.description?.trim() || null,
      price: raw.price,
      imageUrl: raw.imageUrl?.trim() || null,
      stockQty: raw.stockQty,
    };

    const request = id
      ? this.api.updateProduct(id, { ...body, active: raw.active })
      : this.api.createProduct(body);

    request.subscribe({
      next: () => {
        this.saving.set(false);
        this.formOpen.set(false);
        this.reload();
      },
      error: (err) => {
        this.saving.set(false);
        this.errorMessage.set(err?.error?.message ?? 'Could not save this product.');
      },
    });
  }

  protected toggleActive(product: ProductDto): void {
    this.busyId.set(product.id);
    this.api
      .updateProduct(product.id, {
        name: product.name,
        category: product.category,
        description: product.description,
        price: product.price,
        imageUrl: product.imageUrl,
        stockQty: product.stockQty,
        active: !product.active,
      })
      .subscribe({
        next: () => {
          this.busyId.set(null);
          this.reload();
        },
        error: () => this.busyId.set(null),
      });
  }

  protected remove(product: ProductDto): void {
    if (!confirm(`Delete "${product.name}"? This cannot be undone.`)) return;
    this.busyId.set(product.id);
    this.api.deleteProduct(product.id).subscribe({
      next: () => {
        this.busyId.set(null);
        this.reload();
      },
      error: () => this.busyId.set(null),
    });
  }
}
