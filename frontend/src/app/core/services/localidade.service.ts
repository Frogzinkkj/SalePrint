import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Localidade } from '../models/localidade.model'; // Verifique se o caminho está correto

@Injectable({
  providedIn: 'root'
})
export class LocalidadeService {
  private http = inject(HttpClient);
  // Ajuste a URL da API conforme o seu backend
  private apiUrl = 'http://localhost:8080/api/localidades'; 

  // 👇 ESTE É O MÉTODO QUE ESTÁ FALTANDO 👇
  criar(localidade: Partial<Localidade>): Observable<Localidade> {
    return this.http.post<Localidade>(this.apiUrl, localidade);
  }

  listar(): Observable<Localidade[]> {
    return this.http.get<Localidade[]>(this.apiUrl);
  }

  // Se você tiver outros métodos (buscarPorId, deletar, etc), pode mantê-los aqui!
}