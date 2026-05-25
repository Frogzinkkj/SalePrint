import { AfterViewInit, ChangeDetectorRef, Component, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Subject, Subscription, debounceTime, distinctUntilChanged, finalize } from 'rxjs';
import { ImpressoraService } from '../../core/services/impressora.service';
import { SetorService } from '../../core/services/setor.service';
import { Impressora } from '../../core/models/impressora.model';
import { Setor } from '../../core/models/setor.model';
import { STATUS_LABELS, StatusImpressora } from '../../core/models/status-impressora';
import { MovimentacaoModalComponent } from '../movimentacao-modal/movimentacao-modal.component';
import { SubstituicaoModalComponent } from '../substituicao-modal/substituicao-modal.component';

@Component({
  selector: 'app-impressora-list',
  standalone: true,
  imports: [
    FormsModule,
    RouterLink,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatAutocompleteModule,
    MatDialogModule,
    MatSnackBarModule,
    MatChipsModule,
    MatCheckboxModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './impressora-list.component.html',
  styleUrl: './impressora-list.component.scss'
})
export class ImpressoraListComponent implements OnInit, AfterViewInit, OnDestroy {
  private readonly impressoraService = inject(ImpressoraService);
  private readonly setorService = inject(SetorService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  private readonly filtroSetor$ = new Subject<string>();
  private filtroSetorSub?: Subscription;

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  displayedColumns = ['marca', 'modelo', 'ip', 'numeroSerie', 'status', 'setor', 'acoes'];
  dataSource = new MatTableDataSource<Impressora>([]);
  setores: Setor[] = [];
  setoresFiltrados: Setor[] = [];
  statusOptions = Object.values(StatusImpressora);
  statusLabels = STATUS_LABELS;

  busca = '';
  filtroStatus: StatusImpressora | '' = '';
  filtroSetorTexto = '';
  exibirTodos = true;
  pageSizeOptions: number[] = [10, 25, 50, 100];
  pageSize = 100;
  totalRegistros = 0;
  loading = false;

  ngOnInit(): void {
    this.setorService.listar().subscribe((s) => {
      this.setores = s;
      this.atualizarSugestoesSetor();
    });

    this.filtroSetorSub = this.filtroSetor$
      .pipe(debounceTime(300), distinctUntilChanged())
      .subscribe(() => this.carregar());

    this.carregar();
  }

  ngAfterViewInit(): void {
    this.configurarPaginacao();
  }

  ngOnDestroy(): void {
    this.filtroSetorSub?.unsubscribe();
  }

  onSetorInput(): void {
    this.atualizarSugestoesSetor();
    this.filtroSetor$.next(this.filtroSetorTexto);
  }

  onSetorSelecionado(event: MatAutocompleteSelectedEvent): void {
    const setor = event.option.value as Setor;
    this.filtroSetorTexto = setor.nome;
    this.carregar();
  }

  atualizarSugestoesSetor(): void {
    const termo = this.filtroSetorTexto.trim().toLowerCase();
    if (!termo) {
      this.setoresFiltrados = [...this.setores];
      return;
    }
    this.setoresFiltrados = this.setores.filter(
      (s) =>
        s.nome.toLowerCase().includes(termo) ||
        (s.localidadeNome?.toLowerCase().includes(termo) ?? false)
    );
  }

  carregar(): void {
    this.loading = true;
    const filtros: {
      busca?: string;
      status?: StatusImpressora;
      setor?: string;
    } = {};

    if (this.busca.trim()) filtros.busca = this.busca.trim();
    if (this.filtroStatus) filtros.status = this.filtroStatus;
    if (this.filtroSetorTexto.trim()) filtros.setor = this.filtroSetorTexto.trim();

    this.impressoraService
      .listar(filtros)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (data) => {
          this.dataSource.data = [...data];
          this.totalRegistros = data.length;
          this.configurarPaginacao();
          this.cdr.detectChanges();
        },
        error: () => {
          this.snackBar.open('Erro ao carregar impressoras', 'Fechar', { duration: 4000 });
        }
      });
  }

  configurarPaginacao(): void {
    if (!this.paginator) {
      return;
    }

    const total = this.totalRegistros;
    const base = [10, 25, 50, 100];
    this.pageSizeOptions = total > 100 ? [...base, total] : total > 0 ? [...new Set([...base, total])] : base;

    if (this.exibirTodos && total > 0) {
      this.dataSource.paginator = null;
      this.pageSize = total;
      this.paginator.pageSize = total;
      this.paginator.length = total;
    } else {
      this.dataSource.paginator = this.paginator;
      this.pageSize = Math.min(this.pageSize, Math.max(total, 1));
      this.paginator.pageSize = this.pageSize;
      this.paginator.length = total;
    }

    this.paginator.pageSizeOptions = this.pageSizeOptions;
  }

  onExibirTodosChange(): void {
    this.configurarPaginacao();
    this.cdr.detectChanges();
  }

  onPageChange(event: PageEvent): void {
    this.pageSize = event.pageSize;
    this.exibirTodos = event.pageSize >= this.totalRegistros && this.totalRegistros > 0;
  }

  limparFiltros(): void {
    this.busca = '';
    this.filtroStatus = '';
    this.filtroSetorTexto = '';
    this.atualizarSugestoesSetor();
    this.carregar();
  }

  abrirSubstituicao(impressora: Impressora): void {
    const ref = this.dialog.open(SubstituicaoModalComponent, {
      width: '560px',
      data: { impressoraNova: impressora }
    });
    ref.afterClosed().subscribe((ok) => {
      if (ok) this.carregar();
    });
  }

  abrirMovimentacao(impressora: Impressora): void {
    const ref = this.dialog.open(MovimentacaoModalComponent, {
      width: '520px',
      data: impressora
    });
    ref.afterClosed().subscribe((ok) => {
      if (ok) this.carregar();
    });
  }

  verHistorico(impressora: Impressora): void {
    this.router.navigate(['/impressoras', impressora.id, 'historico']);
  }

  labelStatus(status: StatusImpressora): string {
    return this.statusLabels[status];
  }
}
