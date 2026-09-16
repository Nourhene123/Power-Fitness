import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/api/api';
import { OrderDto, PlaceOrderRequest, ProductDto } from '../../../core/models/shop.model';

@Injectable({ providedIn: 'root' })
export class ShopApiService {
  private readonly http = inject(HttpClient);
  private readonly base = inject(API_BASE_URL);

  products(category: string | null): Observable<ProductDto[]> {
    const params: Record<string, string> = {};
    if (category) params['category'] = category;
    return this.http.get<ProductDto[]>(`${this.base}/shop/products`, { params });
  }

  product(id: number): Observable<ProductDto> {
    return this.http.get<ProductDto>(`${this.base}/shop/products/${id}`);
  }

  placeOrder(body: PlaceOrderRequest): Observable<OrderDto> {
    return this.http.post<OrderDto>(`${this.base}/me/orders`, body);
  }

  myOrders(): Observable<OrderDto[]> {
    return this.http.get<OrderDto[]>(`${this.base}/me/orders`);
  }
}
