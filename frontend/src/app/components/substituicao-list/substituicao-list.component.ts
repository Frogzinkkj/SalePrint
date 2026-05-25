import { DatePipe } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { SubstituicaoService } from '../../core/services/substituicao.service';
import { Substituicao } from '../../core/models/substituicao.model';
import { SubstituicaoModalComponent } from '../substituicao-modal/substituicao-modal.component';

@Component({
  selector: 'app-substituicao-list',
  standalone: true,
  imports: [
    DatePipe,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatTooltipModule,
    MatDialogModule,
    MatSnackBarModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './substituicao-list.component.html',
  styleUrl: './substituicao-list.component.scss'
})
export class SubstituicaoListComponent implements OnInit {
  private readonly substituicaoService = inject(SubstituicaoService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly cdr = inject(ChangeDetectorRef);

  displayedColumns = ['data', 'antiga', 'nova', 'responsavel'];
  dataSource = new MatTableDataSource<Substituicao>([]);
  loading = false;
  error: string | null = null;

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.loading = true;
    this.error = null;
    this.cdr.detectChanges();

    this.substituicaoService.listar().subscribe({
      next: (data) => {
        this.dataSource.data = data || [];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erro ao carregar substituições:', err);
        this.error = 'Erro ao carregar substituições. ' + (err?.message || 'Verifique se o backend está rodando.');
        this.snackBar.open(this.error, 'Fechar', { duration: 5000 });
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  abrirModal(): void {
    const ref = this.dialog.open(SubstituicaoModalComponent, { width: '560px' });
    ref.afterClosed().subscribe((ok) => {
      if (ok) this.carregar();
    });
  }
}
