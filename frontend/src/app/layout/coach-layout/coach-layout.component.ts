import { ChangeDetectionStrategy, Component, OnDestroy, computed, effect, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { AuthStore } from '../../core/auth/auth.store';
import { Role } from '../../core/models/role.enum';
import { NotifBellComponent } from '../../shared/components/notif-bell/notif-bell.component';
import { ThemeService } from '../../core/theme/theme.service';

interface SidebarLink {
  readonly path: string;
  readonly label: string;
  readonly icon: string;
  readonly exact?: boolean;
}

@Component({
  selector: 'app-coach-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, NotifBellComponent],
  templateUrl: './coach-layout.component.html',
  styleUrl: './coach-layout.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { '(document:keydown.escape)': 'closeSidebar()' },
})
export class CoachLayoutComponent implements OnDestroy {
  private readonly auth = inject(AuthService);
  protected readonly store = inject(AuthStore);
  protected readonly theme = inject(ThemeService);

  protected readonly sidebarOpen = signal(false);
  protected readonly collapsed = signal(false);

  /** Phones/tablets show an icon rail; there the edge arrow opens the full menu over the page. */
  private readonly mobileQuery = window.matchMedia('(max-width: 960px)');
  protected readonly isMobile = signal(this.mobileQuery.matches);
  private readonly onViewportChange = (e: MediaQueryListEvent) => this.isMobile.set(e.matches);
  /** Whether the sidebar currently shows its labels (drives the edge arrow's icon and aria). */
  protected readonly expanded = computed(() => (this.isMobile() ? this.sidebarOpen() : !this.collapsed()));

  constructor() {
    effect(() => {
      document.body.setAttribute('data-theme', this.theme.theme());
    });
    this.mobileQuery.addEventListener('change', this.onViewportChange);
  }

  ngOnDestroy(): void {
    document.body.removeAttribute('data-theme');
    this.mobileQuery.removeEventListener('change', this.onViewportChange);
  }

  protected readonly links: readonly SidebarLink[] = [
    { path: '/coach', label: 'Roadmap Reviews', icon: 'ri-dashboard-3-line', exact: true },
    { path: '/coach/requests', label: 'Client Requests', icon: 'ri-mail-line' },
    { path: '/coach/shop/products', label: 'Shop Products', icon: 'ri-store-2-line' },
    { path: '/coach/shop/orders', label: 'Shop Orders', icon: 'ri-shopping-bag-3-line' },
  ];

  protected readonly name = computed(() => this.store.user()?.name ?? 'Coach');
  protected readonly initial = computed(() => this.name().trim().charAt(0).toUpperCase() || 'C');
  protected readonly roleLabel = computed(() => (this.store.role() === Role.ADMIN ? 'Admin' : 'Coach'));

  protected toggleSidebar(): void {
    this.sidebarOpen.update((v) => !v);
  }

  protected toggleCollapse(): void {
    this.collapsed.update((v) => !v);
  }

  /** Edge arrow: collapses/expands in place on desktop, opens/closes the overlay menu on phones. */
  protected toggleSidebarPanel(): void {
    if (this.isMobile()) {
      this.toggleSidebar();
    } else {
      this.toggleCollapse();
    }
  }

  protected closeSidebar(): void {
    this.sidebarOpen.set(false);
  }

  protected logout(): void {
    this.auth.logout();
  }
}
