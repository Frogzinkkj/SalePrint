import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Localidade } from '../models/localidade.model';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class LocalidadeService extends ApiService {
  listar(): Observable<Localidade[]> {
    return this.get<Localidade[]>('/localidades');
  }
}
