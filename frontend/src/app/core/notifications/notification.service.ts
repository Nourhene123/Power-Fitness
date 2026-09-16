import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { API_BASE_URL } from '../api/api';
import { AppNotification } from './notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly http = inject(HttpClient);
  private readonly base = inject(API_BASE_URL);

  recent(): Observable<AppNotification[]> {
    return this.http.get<AppNotification[]>(`${this.base}/me/notifications`);
  }

  unreadCount(): Observable<number> {
    return this.http
      .get<{ count: number }>(`${this.base}/me/notifications/unread-count`)
      .pipe(map((r) => r.count));
  }

  markRead(id: number): Observable<void> {
    return this.http.post<void>(`${this.base}/me/notifications/${id}/read`, {});
  }

  markAllRead(): Observable<void> {
    return this.http.post<void>(`${this.base}/me/notifications/read-all`, {});
  }
}
