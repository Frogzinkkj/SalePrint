import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Movimentacao } from '../models/movimentacao.model';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class MovimentacaoService extends ApiService {
  historico(impressoraId: number): Observable<Movimentacao[]> {
    return this.get<Movimentacao[]>(`/movimentacoes/historico/${impressoraId}`);
  }
}
