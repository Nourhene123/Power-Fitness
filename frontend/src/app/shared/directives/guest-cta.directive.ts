import { Directive, HostListener, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { AuthStore } from '../../core/auth/auth.store';

/**
 * Put on any `routerLink="/register"` (or `/login`) marketing CTA — "Build Your Roadmap",
 * "Start your free assessment", "Get Started", etc.
 *
 * Those routes have `canActivate: [guestGuard]`, which silently blocks an already-authenticated
 * visitor and redirects to `/` — invisible when they're already on `/`, so the button looks
 * completely dead. This directive intercepts the click before that happens and sends a logged-in
 * visitor to the page they actually want instead: their dashboard, their assessment, or the
 * coach panel.
 *
 * Guests are unaffected — the click falls through to the normal `routerLink` navigation.
 */
@Directive({
  selector: '[appGuestCta]',
  standalone: true,
})
export class GuestCtaDirective {
  private readonly store = inject(AuthStore);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  @HostListener('click', ['$event'])
  protected onClick(event: MouseEvent): void {
    const user = this.store.user();
    if (!user) {
      return; // Guest: let routerLink navigate to /register (or /login) as written.
    }
    event.preventDefault();
    void this.router.navigateByUrl(this.auth.landingRoute(user));
  }
}
