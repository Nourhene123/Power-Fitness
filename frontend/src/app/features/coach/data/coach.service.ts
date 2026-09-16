import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/api/api';
import {
  ClientProfileDto,
  ClientProgressDto,
  CoachDashboardDto,
  CoachVersionDto,
  PlanEditorDto,
} from './coach.model';
import { ChangeFieldVm } from '../../../core/models/program.model';
import { PlanContent } from '../../../core/models/plan-content.model';

@Injectable({ providedIn: 'root' })
export class CoachService {
  private readonly http = inject(HttpClient);
  private readonly base = inject(API_BASE_URL);

  dashboard(): Observable<CoachDashboardDto> {
    return this.http.get<CoachDashboardDto>(`${this.base}/coach/dashboard`);
  }

  clientProfile(userId: number): Observable<ClientProfileDto> {
    return this.http.get<ClientProfileDto>(`${this.base}/coach/clients/${userId}/profile`);
  }

  clientProgress(userId: number): Observable<ClientProgressDto> {
    return this.http.get<ClientProgressDto>(`${this.base}/coach/clients/${userId}/progress`);
  }

  planEditor(versionId: number): Observable<PlanEditorDto> {
    return this.http.get<PlanEditorDto>(`${this.base}/coach/program-versions/${versionId}`);
  }

  saveDraft(versionId: number, content: PlanContent, coachNote: string): Observable<PlanEditorDto> {
    return this.http.put<PlanEditorDto>(`${this.base}/coach/program-versions/${versionId}`, { content, coachNote });
  }

  approve(versionId: number, coachNote: string): Observable<void> {
    return this.http.post<void>(`${this.base}/coach/program-versions/${versionId}/approve`, { coachNote });
  }

  requestChanges(versionId: number, note: string, fields: ChangeFieldVm[]): Observable<void> {
    return this.http.post<void>(`${this.base}/coach/program-versions/${versionId}/request-changes`, { note, fields });
  }

  newVersionFromActive(programId: number): Observable<CoachVersionDto> {
    return this.http.post<CoachVersionDto>(`${this.base}/coach/programs/${programId}/new-version`, {});
  }
}
