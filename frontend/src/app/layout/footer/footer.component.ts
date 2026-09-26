import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { GuestCtaDirective } from '../../shared/directives/guest-cta.directive';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [RouterLink, GuestCtaDirective],
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FooterComponent {
  protected readonly year = new Date().getFullYear();
}
