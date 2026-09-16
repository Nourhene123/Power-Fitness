import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ProgramApiService } from '../data/program.service';
import { MyProgramDto } from '../../../core/models/program.model';
import { PlanViewComponent } from '../../../shared/components/plan-view/plan-view.component';

@Component({
  selector: 'app-full-program',
  standalone: true,
  imports: [RouterLink, PlanViewComponent],
  templateUrl: './full-program.component.html',
  styleUrl: './full-program.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FullProgramComponent implements OnInit {
  private readonly api = inject(ProgramApiService);

  protected readonly loading = signal(true);
  protected readonly program = signal<MyProgramDto | null>(null);

  ngOnInit(): void {
    this.api.myProgram().subscribe({
      next: (dto) => {
        this.program.set(dto);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
