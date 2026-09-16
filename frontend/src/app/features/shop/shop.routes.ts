import { Routes } from '@angular/router';
import { CatalogComponent } from './catalog/catalog.component';
import { CheckoutComponent } from './checkout/checkout.component';
import { OrderConfirmationComponent } from './order-confirmation/order-confirmation.component';
import { authGuard } from '../../core/guards/auth.guard';

export const SHOP_ROUTES: Routes = [
  {
    path: '',
    component: CatalogComponent,
    title: 'Pro Shop | Power Fitness',
  },
  {
    path: 'checkout',
    component: CheckoutComponent,
    canActivate: [authGuard],
    title: 'Checkout | Power Fitness',
  },
  {
    path: 'order-confirmation',
    component: OrderConfirmationComponent,
    canActivate: [authGuard],
    title: 'Order Confirmed | Power Fitness',
  },
];
