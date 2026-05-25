import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Impressora } from '../../core/models/impressora.model';
import { ImpressoraService } from '../../core/services/impressora.service';
import { SetorService } from '../../core/services/setor.service';
import { Setor } from '../../core/models/setor.model';
import { STATUS_LABELS, StatusImpressora } from '../../core/models/status-impressora';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-movimentacao-modal',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule
  ],
  templateUrl: './movimentacao-modal.component.html'
})
export class MovimentacaoModalComponent {
  private readonly fb = inject(FormBuilder);
  private readonly impressoraService = inject(ImpressoraService);
  private readonly setorService = inject(SetorService);
  private readonly dialogRef = inject(MatDialogRef<MovimentacaoModalComponent>);
  private readonly snackBar = inject(MatSnackBar);
  readonly impressora = inject<Impressora>(MAT_DIALOG_DATA);

  setores: Setor[] = [];
  statusOptions = Object.values(StatusImpressora);
  statusLabels = STATUS_LABELS;

  form = this.fb.group({
    statusAplicado: [this.impressora.status, Validators.required],
    setorDestinoId: [this.impressora.setorId, Validators.required],
    responsavel: ['', Validators.required],
    osQualycopy: [''],
    descricao: ['']
  });

  constructor() {
    this.setorService.listar().subscribe((s) => (this.setores = s));
    this.form.get('statusAplicado')?.valueChanges.subscribe((status) => {
      const osControl = this.form.get('osQualycopy');
      if (status === StatusImpressora.EM_MANUTENCAO_QUALYCOPY) {
        osControl?.setValidators([Validators.required]);
      } else {
        osControl?.clearValidators();
      }
      osControl?.updateValueAndValidity();
    });
  }

  confirmar(): void {
    if (this.form.invalid || !this.impressora.id) return;

    const v = this.form.getRawValue();
    this.impressoraService
      .movimentar({
        impressoraId: this.impressora.id,
        setorDestinoId: v.setorDestinoId!,
        statusAplicado: v.statusAplicado!,
        responsavel: v.responsavel!,
        osQualycopy: v.osQualycopy || undefined,
        descricao: v.descricao || undefined
      })
      .subscribe({
        next: () => {
          this.snackBar.open('Movimentação registrada', 'Fechar', { duration: 3000 });
          this.dialogRef.close(true);
        },
        error: (err: HttpErrorResponse) => {
          this.snackBar.open(err.error?.message || 'Erro na movimentação', 'Fechar', { duration: 5000 });
        }
      });
  }

  cancelar(): void {
    this.dialogRef.close(false);
  }
}
