import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Setor } from '../models/setor.model'; // Verifique se o caminho está correto

@Injectable({
  providedIn: 'root'
})
export class SetorService {
  private http = inject(HttpClient);
  // Ajuste a URL da API conforme o seu backend
  private apiUrl = 'http://localhost:8080/api/setores'; 

  // 👇 ESTE É O MÉTODO QUE ESTÁ FALTANDO 👇
  criar(setor: Partial<Setor>): Observable<Setor> {
    return this.http.post<Setor>(this.apiUrl, setor);
  }

  listar(): Observable<Setor[]> {
    return this.http.get<Setor[]>(this.apiUrl);
  }

  // Se você tiver outros métodos (buscarPorId, deletar, etc), pode mantê-los aqui!
}