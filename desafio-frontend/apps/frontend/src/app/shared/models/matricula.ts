/** Modelos do recurso de matrículas (MatriculaResponse/MatriculaRequest do backend). */

export interface Matricula {
  id: number;
  alunoId: number;
  aulaId: number;
}

/** Corpo de POST /matriculas. */
export interface MatriculaPayload {
  aulaId: number;
}

/** Payload de erro padronizado devolvido pelo backend (ErrorResponse). */
export interface ApiError {
  code?: string;
  message?: string;
}
