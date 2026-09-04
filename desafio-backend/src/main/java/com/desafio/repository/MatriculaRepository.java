package com.desafio.repository;

import com.desafio.entity.Aula;
import com.desafio.entity.Matricula;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public long countByAula(Long aulaId) {
        return count("aula.id", aulaId);
    }

    public Map<Long, Long> countByAulaIds(Collection<Long> aulaIds) {
        if (aulaIds == null || aulaIds.isEmpty()) {
            return Map.of();
        }
        List<Object[]> rows = getEntityManager()
                .createQuery(
                        "SELECT m.aula.id, COUNT(m) FROM Matricula m " +
                        "WHERE m.aula.id IN :aulaIds GROUP BY m.aula.id",
                        Object[].class)
                .setParameter("aulaIds", aulaIds)
                .getResultList();
        Map<Long, Long> contagem = new HashMap<>();
        for (Object[] row : rows) {
            contagem.put((Long) row[0], ((Number) row[1]).longValue());
        }
        return contagem;
    }
}