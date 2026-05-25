import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Setor } from '../models/setor.model';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class SetorService extends ApiService {
  listar(): Observable<Setor[]> {
    return this.get<Setor[]>('/setores');
  }

  listarPorLocalidade(localidadeId: number): Observable<Setor[]> {
    return this.get<Setor[]>(`/setores/localidade/${localidadeId}`);
  }
}
