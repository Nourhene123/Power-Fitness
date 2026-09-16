import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, ElementRef, HostListener, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { NotificationService } from '../../../core/notifications/notification.service';
import { AppNotification } from '../../../core/notifications/notification.model';

@Component({
  selector: 'app-notif-bell',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './notif-bell.component.html',
  styleUrl: './notif-bell.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotifBellComponent {
  private readonly service = inject(NotificationService);
  private readonly router = inject(Router);
  private readonly host = inject(ElementRef<HTMLElement>);

  protected readonly open = signal(false);
  protected readonly items = signal<AppNotification[]>([]);
  protected readonly unread = computed(() => this.items().filter((n) => !n.read).length);

  constructor() {
    this.load();
  }

  private load(): void {
    this.service.recent().subscribe((items) => this.items.set(items));
  }

  protected toggle(): void {
    this.open.update((v) => !v);
    if (this.open()) this.load();
  }

  protected markAllRead(): void {
    this.service.markAllRead().subscribe(() => {
      this.items.update((list) => list.map((n) => ({ ...n, read: true })));
    });
  }

  protected select(n: AppNotification): void {
    if (!n.read) {
      this.service.markRead(n.id).subscribe();
      this.items.update((list) => list.map((x) => (x.id === n.id ? { ...x, read: true } : x)));
    }
    this.open.set(false);
    if (n.link) void this.router.navigateByUrl(n.link);
  }

  @HostListener('document:click', ['$event'])
  protected onDocumentClick(event: MouseEvent): void {
    if (this.open() && !this.host.nativeElement.contains(event.target as Node)) {
      this.open.set(false);
    }
  }
}
