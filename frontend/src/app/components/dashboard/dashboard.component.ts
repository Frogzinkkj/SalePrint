import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';
import { catchError, finalize, timeout } from 'rxjs';
import { of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DashboardStats } from '../../core/models/impressora.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [MatCardModule, MatIconModule, MatTooltipModule, MatProgressSpinnerModule, MatButtonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly cdr = inject(ChangeDetectorRef);

  stats: DashboardStats | null = null;
  loading = true;
  error = '';

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.loading = true;
    this.error = '';
    this.stats = null;
    this.cdr.detectChanges();

    this.http
      .get<DashboardStats>(`${environment.apiUrl}/dashboard`)
      .pipe(
        timeout(15000),
        catchError((err) => {
          const msg =
            err?.error?.message ||
            err?.message ||
            'Falha ao conectar em ' + environment.apiUrl + '/dashboard';
          this.error = msg;
          return of(null);
        }),
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe((stats) => {
        if (stats) {
          this.stats = stats;
        } else if (!this.error) {
          this.error = 'Resposta vazia do servidor.';
        }
        this.cdr.detectChanges();
      });
  }
}
