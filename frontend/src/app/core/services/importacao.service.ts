import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ImportacaoResult {
  sucesso: boolean;
  totalLinhasArquivo: number;
  totalLinhasDados: number;
  totalImportadas: number;
  totalErros: number;
  erros: string[];
  avisos: string[];
}

@Injectable({ providedIn: 'root' })
export class ImportacaoService {
  private readonly http = inject(HttpClient);

  importarCsv(file: File): Observable<ImportacaoResult> {
    const formData = new FormData();
    formData.append('file', file);
    const url = `${environment.apiUrl}/importacao/csv`;
    console.log('📤 POST para URL:', url);
    console.log('📦 FormData com arquivo:', file.name, file.size);
    return this.http.post<ImportacaoResult>(url, formData);
  }
}
