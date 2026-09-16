export type ProductCategory = 'SUPPLEMENTS' | 'APPAREL' | 'ACCESSORIES' | 'EQUIPMENT';

export interface ProductDto {
  id: number;
  name: string;
  category: ProductCategory;
  description: string | null;
  price: number;
  imageUrl: string | null;
  stockQty: number;
  active: boolean;
}

export interface CreateProductRequest {
  name: string;
  category: ProductCategory;
  description: string | null;
  price: number;
  imageUrl: string | null;
  stockQty: number;
}

export interface UpdateProductRequest extends CreateProductRequest {
  active: boolean;
}

export interface OrderItemLineRequest {
  productId: number;
  quantity: number;
  size: string | null;
}

export interface PlaceOrderRequest {
  items: OrderItemLineRequest[];
  recipientName: string;
  phone: string;
  addressLine: string;
  city: string;
  note: string | null;
}

export interface OrderItemDto {
  productId: number | null;
  productName: string;
  unitPrice: number;
  quantity: number;
  size: string | null;
  lineTotal: number;
}

export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';

export interface OrderDto {
  id: number;
  status: OrderStatus;
  subtotal: number;
  deliveryFee: number;
  total: number;
  recipientName: string;
  phone: string;
  addressLine: string;
  city: string;
  note: string | null;
  createdAt: string;
  items: OrderItemDto[];
}

export interface AdminOrderDto extends OrderDto {
  buyerName: string;
  buyerEmail: string;
}
