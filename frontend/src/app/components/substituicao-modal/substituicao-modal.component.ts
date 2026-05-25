import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Impressora } from '../../core/models/impressora.model';
import { ImpressoraService } from '../../core/services/impressora.service';
import { SubstituicaoService } from '../../core/services/substituicao.service';
import { HttpErrorResponse } from '@angular/common/http';

export interface SubstituicaoModalData {
  impressoraNova?: Impressora;
}

@Component({
  selector: 'app-substituicao-modal',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
    MatAutocompleteModule
  ],
  templateUrl: './substituicao-modal.component.html',
  styleUrl: './substituicao-modal.component.scss'
})
export class SubstituicaoModalComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly substituicaoService = inject(SubstituicaoService);
  private readonly impressoraService = inject(ImpressoraService);
  private readonly dialogRef = inject(MatDialogRef<SubstituicaoModalComponent>);
  private readonly snackBar = inject(MatSnackBar);
  readonly data = inject<SubstituicaoModalData>(MAT_DIALOG_DATA, { optional: true });

  impressoras: Impressora[] = [];
  sugestoesAntiga: Impressora[] = [];

  form = this.fb.group({
    numeroSerieNova: ['', Validators.required],
    numeroSerieAntiga: ['', Validators.required],
    responsavel: ['', Validators.required],
    observacao: [''],
    atualizarStatus: [true]
  });

  ngOnInit(): void {
    // Carregar toda a lista de impressoras sem filtros
    this.impressoraService.listar({}).subscribe({
      next: (lista) => {
        this.impressoras = lista;
        if (this.data?.impressoraNova) {
          this.form.patchValue({
            numeroSerieNova: this.data.impressoraNova.numeroSerie
          });
        }
        this.filtrarAntiga();
      },
      error: (err) => {
        console.error('Erro ao carregar impressoras:', err);
        this.snackBar.open('Erro ao carregar impressoras', 'Fechar', { duration: 5000 });
      }
    });
  }

  filtrarAntiga(): void {
    const termo = (this.form.get('numeroSerieAntiga')?.value || '').trim().toLowerCase();
    const novaSn = (this.form.get('numeroSerieNova')?.value || '').trim().toLowerCase();
    if (!termo) {
      this.sugestoesAntiga = this.impressoras.filter((i) => i.numeroSerie.toLowerCase() !== novaSn);
      return;
    }
    this.sugestoesAntiga = this.impressoras.filter(
      (i) =>
        i.numeroSerie.toLowerCase() !== novaSn &&
        (i.numeroSerie.toLowerCase().includes(termo) ||
          i.ip.toLowerCase().includes(termo) ||
          (i.setorNome?.toLowerCase().includes(termo) ?? false))
    );
  }

  selecionarAntiga(event: MatAutocompleteSelectedEvent): void {
    const imp = event.option.value as Impressora;
    this.form.patchValue({ numeroSerieAntiga: imp.numeroSerie });
  }

  confirmar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    this.substituicaoService
      .registrar({
        numeroSerieAntiga: v.numeroSerieAntiga!,
        numeroSerieNova: v.numeroSerieNova!,
        responsavel: v.responsavel!,
        observacao: v.observacao || undefined,
        atualizarStatus: v.atualizarStatus ?? true
      })
      .subscribe({
        next: () => {
          this.snackBar.open('Substituição registrada', 'Fechar', { duration: 3000 });
          this.dialogRef.close(true);
        },
        error: (err: HttpErrorResponse) => {
          this.snackBar.open(err.error?.message || 'Erro ao registrar substituição', 'Fechar', {
            duration: 5000
          });
        }
      });
  }

  cancelar(): void {
    this.dialogRef.close(false);
  }
}
