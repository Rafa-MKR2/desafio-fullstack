package com.desafio.dto;

import jakarta.validation.constraints.NotNull;

public class MatriculaRequest {

    @NotNull(message = "aulaId é obrigatório")
    private Long aulaId;

    public Long getAulaId() {
        return aulaId;
    }

    public void setAulaId(Long aulaId) {
        this.aulaId = aulaId;
    }
}