/** Modelos do recurso de aulas (AulaResponse/AulaRequest do backend). */

export interface Aula {
  id: number;
  disciplinaId: number;
  disciplinaNome: string;
  professorId: number;
  professorNome: string;
  horarioId: number;
  horarioDiaSemana: string;
  horarioHoraInicio: string;
  horarioHoraFim: string;
  vagas: number;
  vagasOcupadas: number;
  vagasRestantes: number;
}

/** Corpo de criação/edição de aula (POST/PUT /aulas). */
export interface AulaPayload {
  disciplinaId: number;
  professorId: number;
  horarioId: number;
  vagas: number;
}

/** Filtros aceitos por GET /aulas. Valores null/undefined são omitidos. */
export interface FiltrosAulas {
  disciplinaId?: number | null;
  professorId?: number | null;
  diaSemana?: string | null;
}
