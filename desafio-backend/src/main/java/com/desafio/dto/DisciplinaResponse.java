package com.desafio.dto;

import com.desafio.entity.Disciplina;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Disciplina com seus dados básicos")
public class DisciplinaResponse {

    private Long id;
    private String nome;
    private Integer cargaHoraria;

    public static DisciplinaResponse from(Disciplina disciplina) {
        DisciplinaResponse response = new DisciplinaResponse();
        response.setId(disciplina.getId());
        response.setNome(disciplina.getNome());
        response.setCargaHoraria(disciplina.getCargaHoraria());
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

    public Integer getCargaHoraria() {
        return cargaHoraria;
    }

    public void setCargaHoraria(Integer cargaHoraria) {
        this.cargaHoraria = cargaHoraria;
    }
}