package com.desafio.repository;

import com.desafio.entity.Aula;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class AulaRepository implements PanacheRepository<Aula> {

    public List<Aula> listarPorDisciplina(Long disciplinaId) {
        return list("disciplina.id", disciplinaId);
    }

    public List<Aula> listarPorProfessor(Long professorId) {
        return list("professor.id", professorId);
    }
}