import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-about',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './about.component.html',
  styleUrl: './about.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AboutComponent {
  protected readonly approach = [
    { icon: 'ri-flask-line', title: 'Built on science',
      text: 'Calories, macros and training volume come from established formulas — Mifflin-St Jeor, TDEE, progressive overload — not trends.' },
    { icon: 'ri-user-heart-line', title: 'Reviewed by a human',
      text: 'The platform drafts your plan in seconds. Then I check it, adjust the details, and only approve it when it\'s right for you.' },
    { icon: 'ri-scales-3-line', title: 'Sustainable, not extreme',
      text: 'Moderate surplus or deficit, realistic training frequency, proper recovery. Results you can actually keep.' },
    { icon: 'ri-line-chart-line', title: 'Tracked and adjusted',
      text: 'You log your progress, I watch the trend. When something stalls, the program changes — you\'re never left guessing.' },
  ];

  protected readonly steps = [
    { title: 'You complete the assessment',
      text: 'Height, weight, training days, meals, experience, goal and lifestyle — about 10 minutes.' },
    { title: 'Your roadmap is generated',
      text: 'Calorie target, macros and a 12-week workout split, matched to your equipment and available days.' },
    { title: 'I review and approve it',
      text: 'I adjust exercises, volume and nutrition where needed, add notes, and release it to you — or ask a follow-up question first.' },
    { title: 'We track and adapt',
      text: 'You train from your dashboard and log progress. I monitor it and update the plan every few weeks.' },
  ];

  protected readonly values = [
    { icon: 'ri-focus-3-line', title: 'Commitment',
      text: 'I\'m invested in every client reaching — and holding — their goal, not just starting strong.' },
    { icon: 'ri-git-repository-private-line', title: 'Integrity',
      text: 'Honest advice, realistic expectations, and no claims that aren\'t backed by evidence.' },
    { icon: 'ri-medal-line', title: 'Excellence',
      text: 'Programming, communication and follow-up held to a standard I\'d expect as a client myself.' },
    { icon: 'ri-group-line', title: 'Support',
      text: 'A calm, encouraging environment where questions are welcome and progress is the only competition.' },
  ];
}
