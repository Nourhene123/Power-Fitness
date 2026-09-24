import { ChangeDetectionStrategy, Component, OnDestroy, computed, effect, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { AuthStore } from '../../core/auth/auth.store';
import { CartService } from '../../core/cart/cart.service';
import { NotifBellComponent } from '../../shared/components/notif-bell/notif-bell.component';
import { ThemeService } from '../../core/theme/theme.service';

interface SidebarLink {
  readonly path: string;
  readonly label: string;
  readonly icon: string;
  readonly exact?: boolean;
}

@Component({
  selector: 'app-member-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, NotifBellComponent],
  templateUrl: './member-layout.component.html',
  styleUrl: './member-layout.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { '(document:keydown.escape)': 'closeSidebar()' },
})
export class MemberLayoutComponent implements OnDestroy {
  private readonly auth = inject(AuthService);
  protected readonly store = inject(AuthStore);
  protected readonly cart = inject(CartService);
  protected readonly theme = inject(ThemeService);

  protected readonly sidebarOpen = signal(false);
  protected readonly collapsed = signal(false);

  constructor() {
    effect(() => {
      document.body.setAttribute('data-theme', this.theme.theme());
    });
  }

  ngOnDestroy(): void {
    document.body.removeAttribute('data-theme');
  }

  protected readonly links: readonly SidebarLink[] = [
    { path: '/dashboard', label: 'Today', icon: 'ri-home-smile-2-line', exact: true },
    { path: '/program', label: 'Full program', icon: 'ri-map-2-line', exact: true },
    { path: '/program/history', label: 'Plan history', icon: 'ri-history-line' },
    { path: '/progress/log/workout', label: 'Log workout', icon: 'ri-boxing-fill' },
    { path: '/progress/log/weigh-in', label: 'Weigh-in', icon: 'ri-scales-3-line' },
    { path: '/progress/history', label: 'Activity', icon: 'ri-timeline-view' },
  ];

  protected readonly storeLinks: readonly SidebarLink[] = [
    { path: '/shop', label: 'Fitness Shop', icon: 'ri-shopping-bag-3-line' },
    { path: '/orders', label: 'My Orders', icon: 'ri-file-list-3-line' },
    { path: '/account', label: 'Account', icon: 'ri-user-settings-line' },
  ];

  protected readonly name = computed(() => this.store.user()?.name ?? 'Member');
  protected readonly initial = computed(() => this.name().trim().charAt(0).toUpperCase() || 'M');

  protected toggleSidebar(): void {
    this.sidebarOpen.update((v) => !v);
  }

  protected toggleCollapse(): void {
    this.collapsed.update((v) => !v);
  }

  protected closeSidebar(): void {
    this.sidebarOpen.set(false);
  }

  protected logout(): void {
    this.auth.logout();
  }
}
