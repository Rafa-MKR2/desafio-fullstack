package com.desafio.dto;

import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Dados para matrícula de um aluno em uma aula")
public class MatriculaRequest {

    @Schema(description = "Id da aula", example = "1")
    @NotNull(message = "aulaId é obrigatório")
    private Long aulaId;

    public Long getAulaId() {
        return aulaId;
    }

    public void setAulaId(Long aulaId) {
        this.aulaId = aulaId;
    }
}