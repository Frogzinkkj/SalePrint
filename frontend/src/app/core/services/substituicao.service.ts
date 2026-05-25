import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Substituicao, SubstituicaoRequest } from '../models/substituicao.model';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class SubstituicaoService extends ApiService {
  private readonly path = '/substituicoes';

  listar(): Observable<Substituicao[]> {
    return this.get<Substituicao[]>(this.path).pipe(
      catchError(err => {
        console.error('Erro ao listar substituições:', err);
        throw err;
      })
    );
  }

  listarPorImpressora(impressoraId: number): Observable<Substituicao[]> {
    return this.get<Substituicao[]>(`${this.path}/impressora/${impressoraId}`);
  }

  registrar(request: SubstituicaoRequest): Observable<Substituicao> {
    return this.post<Substituicao>(this.path, request);
  }
}
