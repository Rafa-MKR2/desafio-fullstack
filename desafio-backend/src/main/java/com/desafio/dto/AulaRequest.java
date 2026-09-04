package com.desafio.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Dados para criação ou edição de uma aula")
public class AulaRequest {

    @Schema(description = "Id da disciplina", example = "1")
    @NotNull(message = "disciplinaId é obrigatório")
    private Long disciplinaId;

    @Schema(description = "Id do professor", example = "1")
    @NotNull(message = "professorId é obrigatório")
    private Long professorId;

    @Schema(description = "Id do horário", example = "1")
    @NotNull(message = "horarioId é obrigatório")
    private Long horarioId;

    @Schema(description = "Quantidade de vagas", example = "30")
    @NotNull(message = "vagas é obrigatório")
    @Min(value = 1, message = "vagas deve ser no mínimo 1")
    private Integer vagas;

    public Long getDisciplinaId() {
        return disciplinaId;
    }

    public void setDisciplinaId(Long disciplinaId) {
        this.disciplinaId = disciplinaId;
    }

    public Long getProfessorId() {
        return professorId;
    }

    public void setProfessorId(Long professorId) {
        this.professorId = professorId;
    }

    public Long getHorarioId() {
        return horarioId;
    }

    public void setHorarioId(Long horarioId) {
        this.horarioId = horarioId;
    }

    public Integer getVagas() {
        return vagas;
    }

    public void setVagas(Integer vagas) {
        this.vagas = vagas;
    }
}