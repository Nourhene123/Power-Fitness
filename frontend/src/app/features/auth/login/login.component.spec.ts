import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Subject } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { CurrentUser } from '../../../core/auth/auth.models';
import { LoginComponent } from './login.component';

describe('LoginComponent — slow server hint', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let response: Subject<CurrentUser>;

  const hint = () => fixture.nativeElement.querySelector('.slow-hint') as HTMLElement | null;

  beforeEach(() => {
    response = new Subject<CurrentUser>();
    TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: { login: () => response.asObservable(), landingRoute: () => '/' } },
      ],
    });
    fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();

    const form = (fixture.componentInstance as unknown as { form: { setValue(v: object): void } }).form;
    form.setValue({ email: 'member@example.com', password: 'not-a-real-secret' });
  });

  function submit(): void {
    (fixture.nativeElement.querySelector('form') as HTMLFormElement).dispatchEvent(new Event('submit'));
    fixture.detectChanges();
  }

  it('shows the waking-up hint only after 5 seconds without a response', fakeAsync(() => {
    submit();
    tick(4900);
    fixture.detectChanges();
    expect(hint()).toBeNull();

    tick(200);
    fixture.detectChanges();
    expect(hint()?.textContent).toContain('waking up');

    response.error({ message: 'Invalid credentials' });
    fixture.detectChanges();
    expect(hint()).toBeNull();
  }));

  it('never shows the hint when the server answers quickly', fakeAsync(() => {
    submit();
    tick(1000);
    response.error({ message: 'Invalid credentials' });
    tick(10000);
    fixture.detectChanges();
    expect(hint()).toBeNull();
  }));
});
