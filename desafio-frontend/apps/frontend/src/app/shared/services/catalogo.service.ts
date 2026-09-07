import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import type { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { Curso, Disciplina, Horario, Professor } from '../models/catalogos';

/** Consome os catálogos de leitura (aluno e coordenador). */
@Injectable({ providedIn: 'root' })
export class CatalogoService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  listarDisciplinas(): Observable<Disciplina[]> {
    return this.http.get<Disciplina[]>(`${this.apiUrl}/disciplinas`);
  }

  listarProfessores(): Observable<Professor[]> {
    return this.http.get<Professor[]>(`${this.apiUrl}/professores`);
  }

  listarHorarios(): Observable<Horario[]> {
    return this.http.get<Horario[]>(`${this.apiUrl}/horarios`);
  }

  listarCursos(): Observable<Curso[]> {
    return this.http.get<Curso[]>(`${this.apiUrl}/cursos`);
  }
}
