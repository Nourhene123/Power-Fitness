import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ChangeRequestVm } from '../../../../core/models/dashboard.model';

@Component({
  selector: 'app-changes-requested-state',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="member-hero changes-requested">
      <div class="hero-head">
        <i class="ri-error-warning-fill"></i>
        <h1>Your coach needs a quick update</h1>
      </div>

      @if (request().coachNote) {
        <div class="hero-note">
          <p>{{ request().coachNote }}</p>
        </div>
      }

      @if (request().fields.length > 0) {
        <p class="hero-fields-lead">Just these fields — no need to redo the whole assessment:</p>
        <div class="badge-row">
          @for (f of request().fields; track f.field) {
            <span class="badge-tag">{{ f.label }}</span>
          }
        </div>
        <a routerLink="/program/answer-coach" class="btn-nav btn-nav-primary">
          <i class="ri-chat-check-line"></i> Answer &amp; resubmit
        </a>
      } @else {
        <a routerLink="/assessment" class="btn-nav btn-nav-primary">
          <i class="ri-edit-2-fill"></i> Update assessment
        </a>
      }
    </div>
  `,
  styleUrl: '../../dashboard-hero.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ChangesRequestedStateComponent {
  readonly request = input.required<ChangeRequestVm>();
}
