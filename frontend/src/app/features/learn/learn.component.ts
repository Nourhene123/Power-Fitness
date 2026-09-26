import { TitleCasePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LEARN, LearnContent, LearnLevel } from './learn.data';
import { GuestCtaDirective } from '../../shared/directives/guest-cta.directive';

@Component({
  selector: 'app-learn',
  standalone: true,
  imports: [RouterLink, TitleCasePipe, GuestCtaDirective],
  templateUrl: './learn.component.html',
  styleUrl: './learn.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LearnComponent {
  readonly level = input<string>('beginner');

  protected readonly levels: LearnLevel[] = ['beginner', 'intermediate', 'advanced'];

  protected readonly content = computed<LearnContent>(() => {
    const key = this.level() as LearnLevel;
    return LEARN[key] ?? LEARN.beginner;
  });
}
