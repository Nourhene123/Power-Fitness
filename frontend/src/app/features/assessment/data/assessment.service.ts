import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/api/api';

export interface AssessmentSubmitted {
  assessmentId: number;
  analysisId: number;
  programId: number;
  versionId: number;
  programStatus: string;
  roadmapTitle: string;
}

@Injectable({ providedIn: 'root' })
export class AssessmentService {
  private readonly http = inject(HttpClient);
  private readonly base = inject(API_BASE_URL);

  hasAssessment(): Observable<{ hasAssessment: boolean }> {
    return this.http.get<{ hasAssessment: boolean }>(`${this.base}/assessments/me`);
  }

  submit(payload: Record<string, unknown>): Observable<AssessmentSubmitted> {
    return this.http.post<AssessmentSubmitted>(`${this.base}/assessments`, payload);
  }
}
