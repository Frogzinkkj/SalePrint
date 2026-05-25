import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { DashboardStats, Impressora } from '../models/impressora.model';
import { MovimentacaoRequest } from '../models/movimentacao.model';
import { StatusImpressora } from '../models/status-impressora';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class ImpressoraService extends ApiService {
  listar(filtros?: {
    busca?: string;
    status?: StatusImpressora;
    setorId?: number;
    setor?: string;
  }): Observable<Impressora[]> {
    return this.get<Impressora[]>('/impressoras', filtros as Record<string, string | number>);
  }

  buscarPorId(id: number): Observable<Impressora> {
    return this.get<Impressora>(`/impressoras/${id}`);
  }

  dashboard(): Observable<DashboardStats> {
    return this.get<DashboardStats>('/dashboard');
  }

  criar(impressora: Impressora): Observable<Impressora> {
    return this.post<Impressora>('/impressoras', impressora);
  }

  atualizar(id: number, impressora: Impressora): Observable<Impressora> {
    return this.put<Impressora>(`/impressoras/${id}`, impressora);
  }

  movimentar(request: MovimentacaoRequest): Observable<Impressora> {
    return this.post<Impressora>('/impressoras/movimentar', request);
  }

  excluir(id: number): Observable<void> {
    return this.delete(`/impressoras/${id}`);
  }
}
