export interface CartLine {
  key: string;
  productId: number;
  name: string;
  price: number;
  imageUrl: string | null;
  quantity: number;
  size: string | null;
  stockQty: number;
}
