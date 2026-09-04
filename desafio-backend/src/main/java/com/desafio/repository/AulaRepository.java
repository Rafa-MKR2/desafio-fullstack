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

    public List<Aula> listarPorProfessorMesmoHorario(Long professorId, String diaSemana,
                                                     String horaInicio, String horaFim) {
        return getEntityManager()
                .createQuery(
                        "SELECT a FROM Aula a WHERE a.professor.id = :professorId " +
                        "AND a.horario.diaSemana = :diaSemana " +
                        "AND a.horario.horaInicio < :horaFim " +
                        "AND a.horario.horaFim > :horaInicio",
                        Aula.class)
                .setParameter("professorId", professorId)
                .setParameter("diaSemana", diaSemana)
                .setParameter("horaInicio", horaInicio)
                .setParameter("horaFim", horaFim)
                .getResultList();
    }
}