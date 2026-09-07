/** Modelos dos endpoints de catálogo (GET /disciplinas, /professores, /horarios, /cursos). */

export interface Disciplina {
  id: number;
  nome: string;
  cargaHoraria: number;
}

export interface Professor {
  id: number;
  nome: string;
  disciplinaIds: number[];
}

export interface Horario {
  id: number;
  diaSemana: string;
  horaInicio: string;
  horaFim: string;
}

export interface Curso {
  id: number;
  nome: string;
}
