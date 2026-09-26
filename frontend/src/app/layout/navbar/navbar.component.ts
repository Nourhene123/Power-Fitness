import { ChangeDetectionStrategy, Component, computed, HostListener, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { AuthStore } from '../../core/auth/auth.store';
import { CartService } from '../../core/cart/cart.service';
import { GuestCtaDirective } from '../../shared/directives/guest-cta.directive';

interface NavLink {
  readonly path: string;
  readonly label: string;
  readonly icon: string;
}

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, GuestCtaDirective],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NavbarComponent {
  private readonly auth = inject(AuthService);
  protected readonly store = inject(AuthStore);
  protected readonly cart = inject(CartService);

  protected readonly scrolled = signal(false);
  protected readonly menuOpen = signal(false);

  protected readonly links: readonly NavLink[] = [
    { path: '/', label: 'Home', icon: 'ri-home-5-line' },
    { path: '/about', label: 'About', icon: 'ri-user-heart-line' },
    { path: '/programs', label: 'Programs', icon: 'ri-boxing-line' },
    { path: '/bmi', label: 'BMI', icon: 'ri-heart-pulse-line' },
    { path: '/shop', label: 'Shop', icon: 'ri-shopping-bag-3-line' },
  ];

  protected readonly appLink = computed(() =>
    this.store.isCoach()
      ? { path: '/coach', label: 'Coach Panel', icon: 'ri-shield-star-line' }
      : { path: '/dashboard', label: 'My Roadmap', icon: 'ri-fire-fill' },
  );

  protected readonly firstName = computed(() => this.store.user()?.name.split(' ')[0] ?? 'Member');

  @HostListener('window:scroll')
  protected onScroll(): void {
    this.scrolled.set(window.scrollY > 30);
  }

  protected toggleMenu(): void {
    this.menuOpen.update((v) => !v);
  }

  protected closeMenu(): void {
    this.menuOpen.set(false);
  }

  protected logout(): void {
    this.closeMenu();
    this.auth.logout();
  }
}
