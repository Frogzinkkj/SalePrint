import { CommonModule } from '@angular/common';
import { Component, inject, ViewChild, ElementRef } from '@angular/core';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ImportacaoService } from '../../core/services/importacao.service';

interface CsvRow {
  localidade?: string;
  setor?: string;
  marca?: string;
  modelo?: string;
  numeroSerie?: string;
  ip?: string;
  status?: string;
  observacao?: string;
  [key: string]: any;
}

@Component({
  selector: 'app-importacao',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatCheckboxModule,
    MatTableModule,
    MatTooltipModule
  ],
  templateUrl: './importacao.component.html',
  styleUrl: './importacao.component.scss'
})
export class ImportacaoComponent {
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;
  
  private readonly importacaoService = inject(ImportacaoService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);

  csvRows: CsvRow[] = [];
  overwriteMode = false;
  loading = false;
  dragActive = false;
  importResult: { success: boolean; imported: number; skipped: number; errors: string[] } | null = null;
  displayedColumns = ['localidade', 'setor', 'marcaModelo', 'ip', 'numeroSerie', 'status'];

  private csvTemplateDemo = `localidade,setor,marca,modelo,numeroSerie,ip,status,observacao
Dom Bosco,Faturamento,HP,LaserJet M15w,BRC98229F,192.168.1.110,ATIVA,Fica na recepção externa
Dom Bosco,Financeiro,Brother,HL-L1212W,E8391K10931,192.168.1.111,BACKUP,Pronto reserva
Camaçari,TI,Epson,EcoTank L4260,K82J91823,192.168.2.140,ATIVA,Impressora colorida projetos
Camaçari,Logística,Zebra,GK420t,ZBR8312019,192.168.2.141,ATIVA,Etiquetadora de caixas
Dom Bosco,Portaria,SAMSUNG,ML-2165,Z8Y2819203,192.168.1.112,EM_MANUTENCAO_QUALYCOPY,Fusor danificado`;

  handleDrag(e: DragEvent): void {
    e.preventDefault();
    e.stopPropagation();
    this.dragActive = e.type === 'dragenter' || e.type === 'dragover';
  }

  handleDrop(e: DragEvent): void {
    e.preventDefault();
    e.stopPropagation();
    this.dragActive = false;
    
    if (e.dataTransfer?.files?.[0]) {
      this.readAndProcessCsvFile(e.dataTransfer.files[0]);
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      this.readAndProcessCsvFile(input.files[0]);
    }
  }

  private readAndProcessCsvFile(file: File): void {
    const reader = new FileReader();
    reader.onload = (e) => {
      const text = e.target?.result as string;
      this.parseCsvText(text);
    };
    reader.readAsText(file);
  }

  private parseCsvText(text: string): void {
    try {
      const lines = text.split('\n').map(l => l.trim()).filter(l => l.length > 0);
      
      if (lines.length <= 1) {
        alert('O arquivo CSV está vazio ou possui apenas cabeçalhos.');
        return;
      }

      const headers = lines[0].split(',').map(h => h.trim().toLowerCase());
      const parsedRows: CsvRow[] = [];

      for (let i = 1; i < lines.length; i++) {
        const values = lines[i].split(',').map(v => v.trim());
        if (values.length === 0) continue;

        const obj: CsvRow = {};
        headers.forEach((h, index) => {
          obj[h] = values[index] || '';
        });
        parsedRows.push(obj);
      }

      this.csvRows = parsedRows;
      this.importResult = null;
    } catch (e) {
      alert('Falha ao analisar formato de texto CSV. Utilize delimitador por vírgula (,).');
    }
  }

  triggerLoadDemo(): void {
    this.parseCsvText(this.csvTemplateDemo);
  }

  downloadTemplate(): void {
    const blob = new Blob([this.csvTemplateDemo], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', 'modelo_impressoras.csv');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }

  executeCsvMigration(): void {
    if (this.csvRows.length === 0) return;
    this.loading = true;

    this.importacaoService.importarCsv(new File([this.csvRowsToFile()], 'import.csv', { type: 'text/csv' })).subscribe({
      next: (result) => {
        this.importResult = {
          success: true,
          imported: result.totalImportadas,
          skipped: result.totalLinhasDados - result.totalImportadas,
          errors: result.erros || []
        };
        this.csvRows = [];
        this.loading = false;

        if (result.totalImportadas > 0) {
          this.snackBar.open(`${result.totalImportadas} impressora(s) importada(s)!`, 'Ver lista', { duration: 6000 }).onAction().subscribe(() => {
            this.router.navigate(['/impressoras']);
          });
        }
      },
      error: (err) => {
        this.importResult = {
          success: false,
          imported: 0,
          skipped: 0,
          errors: [err.error?.message || 'Erro ao importar']
        };
        this.loading = false;
      }
    });
  }

  private csvRowsToFile(): string {
    if (this.csvRows.length === 0) return '';
    
    const headers = Object.keys(this.csvRows[0]).join(',');
    const rows = this.csvRows.map(row => 
      Object.values(row).map(v => `"${v}"`).join(',')
    );
    
    return [headers, ...rows].join('\n');
  }

  clearCsvRows(): void {
    this.csvRows = [];
    this.importResult = null;
  }

  toggleOverwriteMode(): void {
    this.overwriteMode = !this.overwriteMode;
  }
}
