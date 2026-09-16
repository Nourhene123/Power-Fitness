import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../../core/api/api';
import {
  AdminOrderDto,
  CreateProductRequest,
  ProductDto,
  UpdateProductRequest,
} from '../../../../core/models/shop.model';

@Injectable({ providedIn: 'root' })
export class CoachShopApiService {
  private readonly http = inject(HttpClient);
  private readonly base = inject(API_BASE_URL);

  products(): Observable<ProductDto[]> {
    return this.http.get<ProductDto[]>(`${this.base}/coach/shop/products`);
  }

  createProduct(body: CreateProductRequest): Observable<ProductDto> {
    return this.http.post<ProductDto>(`${this.base}/coach/shop/products`, body);
  }

  uploadProductImage(file: File): Observable<{ url: string }> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<{ url: string }>(`${this.base}/coach/shop/products/image`, form);
  }

  updateProduct(id: number, body: UpdateProductRequest): Observable<ProductDto> {
    return this.http.put<ProductDto>(`${this.base}/coach/shop/products/${id}`, body);
  }

  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/coach/shop/products/${id}`);
  }

  orders(status: string | null): Observable<AdminOrderDto[]> {
    const params: Record<string, string> = {};
    if (status) params['status'] = status;
    return this.http.get<AdminOrderDto[]>(`${this.base}/coach/shop/orders`, { params });
  }

  updateOrderStatus(id: number, status: string): Observable<AdminOrderDto> {
    return this.http.patch<AdminOrderDto>(`${this.base}/coach/shop/orders/${id}/status`, { status });
  }
}
