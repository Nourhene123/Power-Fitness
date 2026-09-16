import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/api/api';
import { AccountSummaryDto } from './account.model';
import { User } from '../../../core/models/user.model';

@Injectable({ providedIn: 'root' })
export class AccountApiService {
  private readonly http = inject(HttpClient);
  private readonly base = inject(API_BASE_URL);

  summary(): Observable<AccountSummaryDto> {
    return this.http.get<AccountSummaryDto>(`${this.base}/me/account`);
  }

  updateProfile(name: string): Observable<User> {
    return this.http.patch<User>(`${this.base}/users/me`, { name });
  }
}
