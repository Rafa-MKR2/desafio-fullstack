package com.desafio.repository;

import com.desafio.entity.Aula;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class AulaRepository implements PanacheRepository<Aula> {

    public List<Aula> listarPorDisciplina(Long disciplinaId) {
        return list("ativo = true and disciplina.id", disciplinaId);
    }

    public List<Aula> listarPorProfessor(Long professorId) {
        return list("ativo = true and professor.id", professorId);
    }

    public List<Aula> listarComFiltros(Long disciplinaId, Long professorId, String diaSemana,
                                       Long cursoId, Long horarioId, Boolean vagasDisponiveis,
                                       Long coordenadorId) {
        StringBuilder jpql = new StringBuilder("SELECT a FROM Aula a ");
        Map<String, Object> params = new HashMap<>();
        List<String> clausulas = new ArrayList<>();
        clausulas.add("a.ativo = true");

        if (coordenadorId != null) {
            clausulas.add("a.coordenador.id = :coordenadorId");
            params.put("coordenadorId", coordenadorId);
        }
        if (cursoId != null) {
            jpql.append("JOIN a.cursosAutorizados c ");
            clausulas.add("c.id = :cursoId");
            params.put("cursoId", cursoId);
        }
        if (disciplinaId != null) {
            clausulas.add("a.disciplina.id = :disciplinaId");
            params.put("disciplinaId", disciplinaId);
        }
        if (professorId != null) {
            clausulas.add("a.professor.id = :professorId");
            params.put("professorId", professorId);
        }
        if (diaSemana != null && !diaSemana.isBlank()) {
            clausulas.add("lower(a.horario.diaSemana) = :diaSemana");
            params.put("diaSemana", diaSemana.toLowerCase());
        }
        if (horarioId != null) {
            clausulas.add("a.horario.id = :horarioId");
            params.put("horarioId", horarioId);
        }
        if (Boolean.TRUE.equals(vagasDisponiveis)) {
            // Apenas aulas com vaga restante (vagas > nº de matriculados).
            clausulas.add("size(a.matriculas) < a.vagas");
        }

        jpql.append("WHERE ").append(String.join(" and ", clausulas)).append(" order by a.id");

        TypedQuery<Aula> query = getEntityManager().createQuery(jpql.toString(), Aula.class);
        params.forEach(query::setParameter);
        return query.getResultList();
    }

    public List<Aula> listarPorProfessorMesmoHorario(Long professorId, String diaSemana,
                                                     String horaInicio, String horaFim) {
        return getEntityManager()
                .createQuery(
                        "SELECT a FROM Aula a WHERE a.ativo = true " +
                        "AND a.professor.id = :professorId " +
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

    public List<Aula> listarAtivas() {
        return list("ativo = true order by id");
    }
}