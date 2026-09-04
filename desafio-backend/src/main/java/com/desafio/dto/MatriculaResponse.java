package com.desafio.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Dados da matrícula criada")
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