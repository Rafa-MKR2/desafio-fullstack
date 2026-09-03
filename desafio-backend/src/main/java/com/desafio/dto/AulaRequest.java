package com.desafio.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class AulaRequest {

    @NotNull(message = "disciplinaId é obrigatório")
    private Long disciplinaId;

    @NotNull(message = "professorId é obrigatório")
    private Long professorId;

    @NotNull(message = "horarioId é obrigatório")
    private Long horarioId;

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