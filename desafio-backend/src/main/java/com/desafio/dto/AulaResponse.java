package com.desafio.dto;

import com.desafio.entity.Aula;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Aula com os dados de disciplina, professor e horário")
public class AulaResponse {

    private Long id;
    private Long disciplinaId;
    private String disciplinaNome;
    private Long professorId;
    private String professorNome;
    private Long horarioId;
    private String horarioDiaSemana;
    private String horarioHoraInicio;
    private String horarioHoraFim;
    private Integer vagas;

    public AulaResponse() {
    }

    public static AulaResponse from(Aula aula) {
        AulaResponse response = new AulaResponse();
        response.setId(aula.getId());
        response.setDisciplinaId(aula.getDisciplina().getId());
        response.setDisciplinaNome(aula.getDisciplina().getNome());
        response.setProfessorId(aula.getProfessor().getId());
        response.setProfessorNome(aula.getProfessor().getNome());
        response.setHorarioId(aula.getHorario().getId());
        response.setHorarioDiaSemana(aula.getHorario().getDiaSemana());
        response.setHorarioHoraInicio(aula.getHorario().getHoraInicio());
        response.setHorarioHoraFim(aula.getHorario().getHoraFim());
        response.setVagas(aula.getVagas());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDisciplinaId() {
        return disciplinaId;
    }

    public void setDisciplinaId(Long disciplinaId) {
        this.disciplinaId = disciplinaId;
    }

    public String getDisciplinaNome() {
        return disciplinaNome;
    }

    public void setDisciplinaNome(String disciplinaNome) {
        this.disciplinaNome = disciplinaNome;
    }

    public Long getProfessorId() {
        return professorId;
    }

    public void setProfessorId(Long professorId) {
        this.professorId = professorId;
    }

    public String getProfessorNome() {
        return professorNome;
    }

    public void setProfessorNome(String professorNome) {
        this.professorNome = professorNome;
    }

    public Long getHorarioId() {
        return horarioId;
    }

    public void setHorarioId(Long horarioId) {
        this.horarioId = horarioId;
    }

    public String getHorarioDiaSemana() {
        return horarioDiaSemana;
    }

    public void setHorarioDiaSemana(String horarioDiaSemana) {
        this.horarioDiaSemana = horarioDiaSemana;
    }

    public String getHorarioHoraInicio() {
        return horarioHoraInicio;
    }

    public void setHorarioHoraInicio(String horarioHoraInicio) {
        this.horarioHoraInicio = horarioHoraInicio;
    }

    public String getHorarioHoraFim() {
        return horarioHoraFim;
    }

    public void setHorarioHoraFim(String horarioHoraFim) {
        this.horarioHoraFim = horarioHoraFim;
    }

    public Integer getVagas() {
        return vagas;
    }

    public void setVagas(Integer vagas) {
        this.vagas = vagas;
    }
}