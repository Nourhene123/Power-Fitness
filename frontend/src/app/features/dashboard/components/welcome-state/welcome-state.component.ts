import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-welcome-state',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="member-hero welcome">
      <i class="ri-survey-line hero-icon"></i>
      <h1>Welcome, <span>{{ name() }}</span>!</h1>
      <p class="lead">Complete your assessment to get a personalized 12-week roadmap, reviewed by your coach.</p>
      <a routerLink="/assessment" class="btn-nav btn-nav-primary">
        <i class="ri-flashlight-fill"></i> Start assessment
      </a>
    </div>
  `,
  styleUrl: '../../dashboard-hero.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WelcomeStateComponent {
  readonly name = input.required<string>();
}
