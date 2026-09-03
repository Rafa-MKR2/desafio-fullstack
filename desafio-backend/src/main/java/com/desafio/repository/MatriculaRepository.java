package com.desafio.repository;

import com.desafio.entity.Aula;
import com.desafio.entity.Matricula;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class MatriculaRepository implements PanacheRepository<Matricula> {

    public boolean existsByAlunoAndAula(Long alunoId, Long aulaId) {
        return count("aluno.id = ?1 and aula.id = ?2", alunoId, aulaId) > 0;
    }

    public Optional<Matricula> findByAlunoAndAula(Long alunoId, Long aulaId) {
        return find("aluno.id = ?1 and aula.id = ?2", alunoId, aulaId).firstResultOptional();
    }

    public List<Aula> findAulasByAluno(Long alunoId) {
        return getEntityManager()
                .createQuery("SELECT m.aula FROM Matricula m WHERE m.aluno.id = :alunoId", Aula.class)
                .setParameter("alunoId", alunoId)
                .getResultList();
    }
}