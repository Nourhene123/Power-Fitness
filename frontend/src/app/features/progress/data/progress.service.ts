import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/api/api';
import {
  HabitKey,
  HabitToggleResult,
  LogWeighInRequest,
  LogWorkoutRequest,
  ProgressLogDto,
  TimelineItemDto,
  WorkoutSessionDto,
} from '../../../core/models/progress.model';

@Injectable({ providedIn: 'root' })
export class ProgressApiService {
  private readonly http = inject(HttpClient);
  private readonly base = inject(API_BASE_URL);

  logWorkout(body: LogWorkoutRequest): Observable<WorkoutSessionDto> {
    return this.http.post<WorkoutSessionDto>(`${this.base}/me/workout-sessions`, body);
  }

  recentWorkouts(): Observable<WorkoutSessionDto[]> {
    return this.http.get<WorkoutSessionDto[]>(`${this.base}/me/workout-sessions`);
  }

  logWeighIn(body: LogWeighInRequest): Observable<ProgressLogDto> {
    return this.http.post<ProgressLogDto>(`${this.base}/me/weigh-ins`, body);
  }

  recentWeighIns(): Observable<ProgressLogDto[]> {
    return this.http.get<ProgressLogDto[]>(`${this.base}/me/weigh-ins`);
  }

  toggleHabit(habit: HabitKey): Observable<HabitToggleResult> {
    return this.http.post<HabitToggleResult>(`${this.base}/me/habits/toggle`, { habit });
  }

  timeline(): Observable<TimelineItemDto[]> {
    return this.http.get<TimelineItemDto[]>(`${this.base}/me/timeline`);
  }
}
