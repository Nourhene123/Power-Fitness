import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthStore } from '../../core/auth/auth.store';
import { AuthService } from '../../core/auth/auth.service';
import { AccountApiService } from './data/account.service';

@Component({
  selector: 'app-account',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './account.component.html',
  styleUrl: './account.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccountComponent implements OnInit {
  private readonly api = inject(AccountApiService);
  private readonly auth = inject(AuthService);
  protected readonly store = inject(AuthStore);
  private readonly fb = inject(FormBuilder);

  protected readonly saved = signal(false);
  protected readonly saving = signal(false);
  protected readonly assessmentCount = signal(0);

  protected readonly form = this.fb.nonNullable.group({
    name: [this.store.user()?.name ?? '', Validators.required],
  });

  ngOnInit(): void {
    this.api.summary().subscribe((s) => this.assessmentCount.set(s.assessmentCount));
  }

  protected submit(): void {
    if (this.form.invalid) return;
    const name = this.form.getRawValue().name.trim();
    if (!name) return;
    this.saving.set(true);
    this.saved.set(false);
    this.api.updateProfile(name).subscribe({
      next: () => {
        this.store.patchUser({ name });
        this.saving.set(false);
        this.saved.set(true);
      },
      error: () => this.saving.set(false),
    });
  }

  protected logout(): void {
    this.auth.logout();
  }
}
