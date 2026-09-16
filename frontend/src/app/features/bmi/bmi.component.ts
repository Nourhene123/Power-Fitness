import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

type BmiBand = 'under' | 'healthy' | 'over';

interface BmiResult {
  value: number;
  category: string;
  band: BmiBand;
  markerPct: number;
  h: number;
  w: number;
}

@Component({
  selector: 'app-bmi',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './bmi.component.html',
  styleUrl: './bmi.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BmiComponent {
  private readonly fb = inject(FormBuilder);

  protected readonly result = signal<BmiResult | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    height: [null as number | null, [Validators.required, Validators.min(100), Validators.max(250)]],
    weight: [null as number | null, [Validators.required, Validators.min(30), Validators.max(300)]],
  });

  protected calculate(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { height, weight } = this.form.getRawValue();
    const bmi = weight! / Math.pow(height! / 100, 2);

    let category: string;
    let band: BmiBand;
    if (bmi < 18.5) [category, band] = ['Underweight', 'under'];
    else if (bmi < 25) [category, band] = ['Healthy weight', 'healthy'];
    else if (bmi < 30) [category, band] = ['Overweight', 'over'];
    else [category, band] = ['Obese', 'over'];

    this.result.set({
      value: Math.round(bmi * 10) / 10,
      category,
      band,
      markerPct: Math.max(0, Math.min(100, ((bmi - 15) / (40 - 15)) * 100)),
      h: Math.round(height!),
      w: Math.round(weight!),
    });
  }
}
