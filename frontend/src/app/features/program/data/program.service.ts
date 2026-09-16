import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/api/api';
import {
  ChangeRequestDto,
  MyProgramDto,
  ProgramVersionDetail,
  ProgramVersionSummary,
} from '../../../core/models/program.model';

@Injectable({ providedIn: 'root' })
export class ProgramApiService {
  private readonly http = inject(HttpClient);
  private readonly base = inject(API_BASE_URL);

  myProgram(): Observable<MyProgramDto> {
    return this.http.get<MyProgramDto>(`${this.base}/me/program`);
  }

  versions(): Observable<ProgramVersionSummary[]> {
    return this.http.get<ProgramVersionSummary[]>(`${this.base}/me/program/versions`);
  }

  version(id: number): Observable<ProgramVersionDetail> {
    return this.http.get<ProgramVersionDetail>(`${this.base}/me/program/versions/${id}`);
  }

  changeRequest(): Observable<ChangeRequestDto> {
    return this.http.get<ChangeRequestDto>(`${this.base}/me/program/change-request`);
  }

  resolveChangeRequest(answers: Record<string, string>, message: string): Observable<void> {
    return this.http.post<void>(`${this.base}/me/program/change-request/resolve`, { answers, message });
  }
}
