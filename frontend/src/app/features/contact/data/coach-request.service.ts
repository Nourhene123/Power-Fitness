import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/api/api';

export interface CreateCoachRequest {
  name: string;
  email: string;
  phone: string;
  experience: 'beginner' | 'intermediate' | 'advanced';
  goals: string;
}

export interface CoachRequest extends CreateCoachRequest {
  id: number;
  status: 'pending' | 'contacted' | 'completed';
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class CoachRequestService {
  private readonly http = inject(HttpClient);
  private readonly base = `${inject(API_BASE_URL)}/coach-requests`;

  submit(request: CreateCoachRequest): Observable<CoachRequest> {
    return this.http.post<CoachRequest>(this.base, request);
  }
}
