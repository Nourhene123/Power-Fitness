import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { AuthStore } from '../../core/auth/auth.store';
import { CurrentUser } from '../../core/auth/auth.models';
import { Role } from '../../core/models/role.enum';
import { GuestCtaDirective } from './guest-cta.directive';

@Component({
  standalone: true,
  imports: [GuestCtaDirective],
  template: `<a routerLink="/register" appGuestCta>Build Your Roadmap</a>`,
})
class HostComponent {}

describe('GuestCtaDirective', () => {
  let fixture: ComponentFixture<HostComponent>;
  let store: AuthStore;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HostComponent],
      providers: [provideRouter([]), { provide: AuthService, useValue: { landingRoute: (u: CurrentUser) => (u.role === Role.COACH ? '/coach' : '/dashboard') } }],
    });
    fixture = TestBed.createComponent(HostComponent);
    fixture.detectChanges();
    store = TestBed.inject(AuthStore);
    router = TestBed.inject(Router);
  });

  function click(): MouseEvent {
    const event = new MouseEvent('click', { cancelable: true });
    fixture.nativeElement.querySelector('a').dispatchEvent(event);
    return event;
  }

  it('lets a guest navigate normally to the guarded route (does not intercept)', () => {
    spyOn(router, 'navigateByUrl');
    const event = click();
    expect(event.defaultPrevented).toBe(false);
    expect(router.navigateByUrl).not.toHaveBeenCalled();
  });

  it('redirects an authenticated coach to the coach panel instead of letting the guard silently block them', () => {
    store.setUser({ id: 1, name: 'Ilyes', email: 'coach@example.com', avatar: null, role: Role.COACH, hasAssessment: true });
    spyOn(router, 'navigateByUrl');
    const event = click();
    expect(event.defaultPrevented).toBe(true);
    expect(router.navigateByUrl).toHaveBeenCalledWith('/coach');
  });

  it('redirects an authenticated member to their dashboard', () => {
    store.setUser({ id: 2, name: 'Alex', email: 'member@example.com', avatar: null, role: Role.USER, hasAssessment: true });
    spyOn(router, 'navigateByUrl');
    const event = click();
    expect(event.defaultPrevented).toBe(true);
    expect(router.navigateByUrl).toHaveBeenCalledWith('/dashboard');
  });
});
