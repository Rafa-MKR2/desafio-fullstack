import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import type { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { Aula, AulaPayload, FiltrosAulas } from '../models/aula';

/** Consome o CRUD de aulas (GET para aluno/coordenador; escrita só coordenador). */
@Injectable({ providedIn: 'root' })
export class AulaService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  listar(filtros: FiltrosAulas = {}): Observable<Aula[]> {
    let params = new HttpParams();
    if (filtros.disciplinaId != null) {
      params = params.set('disciplinaId', String(filtros.disciplinaId));
    }
    if (filtros.professorId != null) {
      params = params.set('professorId', String(filtros.professorId));
    }
    if (filtros.diaSemana != null && filtros.diaSemana.trim() !== '') {
      params = params.set('diaSemana', filtros.diaSemana.trim());
    }
    return this.http.get<Aula[]>(`${this.apiUrl}/aulas`, { params });
  }

  buscar(id: number): Observable<Aula> {
    return this.http.get<Aula>(`${this.apiUrl}/aulas/${id}`);
  }

  criar(payload: AulaPayload): Observable<Aula> {
    return this.http.post<Aula>(`${this.apiUrl}/aulas`, payload);
  }

  atualizar(id: number, payload: AulaPayload): Observable<Aula> {
    return this.http.put<Aula>(`${this.apiUrl}/aulas/${id}`, payload);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/aulas/${id}`);
  }
}
