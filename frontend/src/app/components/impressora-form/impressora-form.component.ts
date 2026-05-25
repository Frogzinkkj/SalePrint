import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { ImpressoraService } from '../../core/services/impressora.service';
import { SetorService } from '../../core/services/setor.service';
import { LocalidadeService } from '../../core/services/localidade.service';
import { Setor } from '../../core/models/setor.model';
import { Localidade } from '../../core/models/localidade.model';
import { STATUS_LABELS, StatusImpressora } from '../../core/models/status-impressora';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-impressora-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatCardModule
  ],
  templateUrl: './impressora-form.component.html',
  styleUrl: './impressora-form.component.scss'
})
export class ImpressoraFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly impressoraService = inject(ImpressoraService);
  private readonly setorService = inject(SetorService);
  private readonly localidadeService = inject(LocalidadeService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  setores: Setor[] = [];
  localidades: Localidade[] = [];
  statusOptions = Object.values(StatusImpressora);
  statusLabels = STATUS_LABELS;
  editId: number | null = null;
  loading = false;
  isSaving = false;

  showAddLocation = false;
  newLocationName = '';
  showAddSector = false;
  newSectorName = '';
  errorMsg: string | null = null;

  form = this.fb.group({
    marca: ['', Validators.required],
    modelo: ['', Validators.required],
    numeroSerie: ['', [Validators.required]],
    ip: ['', Validators.required],
    status: [StatusImpressora.ATIVA, Validators.required],
    localidadeId: [null as number | null, Validators.required],
    setorId: [null as number | null, Validators.required],
    observacao: ['']
  });

  get filteredSectors(): Setor[] {
    const locId = this.form.get('localidadeId')?.value;
    return this.setores.filter(s => s.localidadeId === locId);
  }

  get isSerieDisabled(): boolean {
    return !!this.editId;
  }

  ngOnInit(): void {
    this.loadData();
    const id = this.route.snapshot.paramMap.get('id');
    if (id && id !== 'nova') {
      this.editId = Number(id);
      this.form.get('numeroSerie')?.disable();
      this.impressoraService.buscarPorId(this.editId).subscribe({
        next: (imp) => {
          this.form.patchValue({
            marca: imp.marca,
            modelo: imp.modelo,
            numeroSerie: imp.numeroSerie,
            ip: imp.ip,
            status: imp.status,
            localidadeId: this.setores.find(s => s.id === imp.setorId)?.localidadeId,
            setorId: imp.setorId,
            observacao: imp.observacao ?? ''
          });
        },
        error: () => this.snackBar.open('Impressora não encontrada', 'Fechar', { duration: 4000 })
      });
    }
  }

  private loadData(): void {
    this.localidadeService.listar().subscribe(locs => this.localidades = locs);
    this.setorService.listar().subscribe(setores => this.setores = setores);
  }

  toggleAddLocation(): void {
    this.showAddLocation = !this.showAddLocation;
    this.newLocationName = '';
  }

  toggleAddSector(): void {
    this.showAddSector = !this.showAddSector;
    this.newSectorName = '';
  }

  addNewLocation(): void {
    if (!this.newLocationName.trim()) return;
    this.localidadeService.criar({ nome: this.newLocationName }).subscribe({
      next: (loc: Localidade) => {
        this.localidades.push(loc);
        this.form.patchValue({ localidadeId: loc.id });
        this.showAddLocation = false;
        this.newLocationName = '';
      },
      error: () => this.snackBar.open('Erro ao adicionar localidade', 'Fechar', { duration: 4000 })
    });
  }

  addNewSector(): void {
    const locId = this.form.get('localidadeId')?.value;
    if (!this.newSectorName.trim() || !locId) return;
    this.setorService.criar({ nome: this.newSectorName, localidadeId: locId }).subscribe({
      next: (setor: Setor) => {
        this.setores.push(setor);
        this.form.patchValue({ setorId: setor.id });
        this.showAddSector = false;
        this.newSectorName = '';
      },
      error: () => this.snackBar.open('Erro ao adicionar setor', 'Fechar', { duration: 4000 })
    });
  }

  salvar(): void {
    this.errorMsg = null;

    const marca = this.form.get('marca')?.value;
    const modelo = this.form.get('modelo')?.value;
    const numeroSerie = this.form.get('numeroSerie')?.value;
    const ip = this.form.get('ip')?.value;
    const status = this.form.get('status')?.value;
    const setorId = this.form.get('setorId')?.value;

    if (!marca?.trim() || !modelo?.trim() || !numeroSerie?.trim() || !ip?.trim() || !status || !setorId) {
      this.errorMsg = 'Por favor, preencha todos os campos obrigatórios (*) marcados.';
      return;
    }

    this.isSaving = true;
    const payload = {
      marca: marca.trim(),
      modelo: modelo.trim(),
      numeroSerie: numeroSerie.trim(),
      ip: ip.trim(),
      status,
      setorId,
      observacao: this.form.get('observacao')?.value?.trim() || undefined
    };

    const req = this.editId
      ? this.impressoraService.atualizar(this.editId, { ...payload, id: this.editId })
      : this.impressoraService.criar(payload);

    req.subscribe({
      next: () => {
        this.snackBar.open('Impressora salva com sucesso', 'Fechar', { duration: 3000 });
        this.router.navigate(['/impressoras']);
      },
      error: (err: HttpErrorResponse) => {
        this.errorMsg = err.error?.message || 'Erro ao salvar impressora';
        this.snackBar.open(this.errorMsg || 'Erro ao salvar impressora', 'Fechar', { duration: 5000 });
        this.isSaving = false;
      },
      complete: () => (this.isSaving = false)
    });
  }

  voltar(): void {
    this.router.navigate(['/impressoras']);
  }
}
