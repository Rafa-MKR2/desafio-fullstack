package com.desafio.dto;

import com.desafio.entity.Disciplina;
import com.desafio.entity.Professor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(description = "Professor com suas disciplinas")
public class ProfessorResponse {

    private Long id;
    private String nome;
    private List<Long> disciplinaIds;

    public static ProfessorResponse from(Professor professor) {
        ProfessorResponse response = new ProfessorResponse();
        response.setId(professor.getId());
        response.setNome(professor.getNome());
        response.setDisciplinaIds(professor.getDisciplinas().stream()
                .map(Disciplina::getId)
                .sorted()
                .toList());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<Long> getDisciplinaIds() {
        return disciplinaIds;
    }

    public void setDisciplinaIds(List<Long> disciplinaIds) {
        this.disciplinaIds = disciplinaIds;
    }
}