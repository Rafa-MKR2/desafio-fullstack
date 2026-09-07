import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import type { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { Aula } from '../models/aula';
import type { Matricula } from '../models/matricula';

/** Consome as matrículas do aluno autenticado (resolvido pelo JWT no backend). */
@Injectable({ providedIn: 'root' })
export class MatriculaService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  matricular(aulaId: number): Observable<Matricula> {
    return this.http.post<Matricula>(`${this.apiUrl}/matriculas`, { aulaId });
  }

  listar(): Observable<Matricula[]> {
    return this.http.get<Matricula[]>(`${this.apiUrl}/matriculas`);
  }

  listarAulas(): Observable<Aula[]> {
    return this.http.get<Aula[]>(`${this.apiUrl}/matriculas/aulas`);
  }
}
