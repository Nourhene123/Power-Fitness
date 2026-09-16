import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../../core/api/api';
import { CoachRequest } from '../../../contact/data/coach-request.service';

export interface CoachRequestStats {
  total: number;
  pending: number;
  contacted: number;
  completed: number;
}

@Injectable({ providedIn: 'root' })
export class CoachRequestsAdminService {
  private readonly http = inject(HttpClient);
  private readonly base = `${inject(API_BASE_URL)}/admin/coach-requests`;

  list(): Observable<CoachRequest[]> {
    return this.http.get<CoachRequest[]>(this.base);
  }

  stats(): Observable<CoachRequestStats> {
    return this.http.get<CoachRequestStats>(`${this.base}/stats`);
  }

  updateStatus(id: number, status: CoachRequest['status']): Observable<CoachRequest> {
    return this.http.patch<CoachRequest>(`${this.base}/${id}`, { status });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
