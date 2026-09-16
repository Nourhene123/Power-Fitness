import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { AuthStore } from '../../core/auth/auth.store';
import { Role } from '../../core/models/role.enum';
import { NotifBellComponent } from '../../shared/components/notif-bell/notif-bell.component';

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
})
export class CoachLayoutComponent {
  private readonly auth = inject(AuthService);
  protected readonly store = inject(AuthStore);

  protected readonly sidebarOpen = signal(false);

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

  protected closeSidebar(): void {
    this.sidebarOpen.set(false);
  }

  protected logout(): void {
    this.auth.logout();
  }
}
