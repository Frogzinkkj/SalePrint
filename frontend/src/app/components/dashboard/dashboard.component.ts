import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';
import { CommonModule } from '@angular/common';
import { catchError, finalize, timeout, forkJoin } from 'rxjs';
import { of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DashboardStats, Impressora } from '../../core/models/impressora.model';
import { ImpressoraService } from '../../core/services/impressora.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatTooltipModule, MatProgressSpinnerModule, MatButtonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly impressoraService = inject(ImpressoraService);
  private readonly cdr = inject(ChangeDetectorRef);

  stats: DashboardStats | null = null;
  modelosDistribuicao: Array<{ modelo: string; quantidade: number }> = [];
  loading = true;
  error = '';

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.loading = true;
    this.error = '';
    this.stats = null;
    this.modelosDistribuicao = [];
    this.cdr.detectChanges();

    forkJoin({
      stats: this.http
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
          })
        ),
      impressoras: this.impressoraService.listar().pipe(
        catchError(() => of([]))
      )
    })
    .pipe(
      finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      })
    )
    .subscribe(({ stats, impressoras }) => {
      if (stats) {
        this.stats = stats;
      } else if (!this.error) {
        this.error = 'Resposta vazia do servidor.';
      }
      this.cdr.detectChanges();

      if (impressoras && impressoras.length > 0) {
        this.calcularDistribuicaoModelos(impressoras);
      }
    });
  }

  private calcularDistribuicaoModelos(impressoras: Impressora[]): void {
    const modeloMap = new Map<string, number>();
    
    impressoras.forEach((imp) => {
      const modelo = imp.modelo || 'Desconhecido';
      modeloMap.set(modelo, (modeloMap.get(modelo) || 0) + 1);
    });

    this.modelosDistribuicao = Array.from(modeloMap.entries())
      .map(([modelo, quantidade]) => ({ modelo, quantidade }))
      .sort((a, b) => b.quantidade - a.quantidade);

    this.cdr.detectChanges();
  }
}
