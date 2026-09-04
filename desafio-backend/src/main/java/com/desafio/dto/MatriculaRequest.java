package com.desafio.dto;

import jakarta.validation.constraints.NotNull;

public class MatriculaRequest {

    @NotNull(message = "alunoId é obrigatório")
    private Long alunoId;

    @NotNull(message = "aulaId é obrigatório")
    private Long aulaId;

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