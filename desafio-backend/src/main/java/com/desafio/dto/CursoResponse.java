package com.desafio.dto;

import com.desafio.entity.Curso;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Curso com seu nome")
public class CursoResponse {

    private Long id;
    private String nome;

    public static CursoResponse from(Curso curso) {
        CursoResponse response = new CursoResponse();
        response.setId(curso.getId());
        response.setNome(curso.getNome());
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
}