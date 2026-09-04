package com.desafio.dto;

import com.desafio.entity.Matricula;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Matrícula do aluno autenticado")
public class MatriculaResponse {

    private Long id;
    private Long alunoId;
    private Long aulaId;

    public MatriculaResponse() {
    }

    public MatriculaResponse(Long id, Long alunoId, Long aulaId) {
        this.id = id;
        this.alunoId = alunoId;
        this.aulaId = aulaId;
    }

    public static MatriculaResponse from(Matricula matricula) {
        return new MatriculaResponse(
                matricula.getId(),
                matricula.getAluno().getId(),
                matricula.getAula().getId());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAlunoId() {
        return alunoId;
    }

    public void setAlunoId(Long alunoId) {
        this.alunoId = alunoId;
    }

    public Long getAulaId() {
        return aulaId;
    }

    public void setAulaId(Long aulaId) {
        this.aulaId = aulaId;
    }
}