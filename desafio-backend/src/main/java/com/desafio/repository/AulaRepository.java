package com.desafio.repository;

import com.desafio.entity.Aula;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class AulaRepository implements PanacheRepository<Aula> {

    public List<Aula> listarPorDisciplina(Long disciplinaId) {
        return list("disciplina.id", disciplinaId);
    }

    public List<Aula> listarPorProfessor(Long professorId) {
        return list("professor.id", professorId);
    }

    public List<Aula> listarComFiltros(Long disciplinaId, Long professorId, String diaSemana) {
        Map<String, Object> params = new HashMap<>();
        List<String> clausulas = new ArrayList<>();

        if (disciplinaId != null) {
            clausulas.add("disciplina.id = :disciplinaId");
            params.put("disciplinaId", disciplinaId);
        }
        if (professorId != null) {
            clausulas.add("professor.id = :professorId");
            params.put("professorId", professorId);
        }
        if (diaSemana != null && !diaSemana.isBlank()) {
            clausulas.add("lower(horario.diaSemana) = :diaSemana");
            params.put("diaSemana", diaSemana.toLowerCase());
        }

        if (clausulas.isEmpty()) {
            return listAll();
        }
        return list(String.join(" and ", clausulas), params);
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