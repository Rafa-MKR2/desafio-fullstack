package com.desafio.service;

import com.desafio.entity.Aluno;
import com.desafio.entity.Aula;
import com.desafio.entity.Matricula;
import com.desafio.exception.HorarioConflitanteException;
import com.desafio.exception.VagasEsgotadasException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class MatriculaService {

    @Inject
    EntityManager entityManager;

    @Transactional
    public Matricula matricular(Long alunoId, Long aulaId) {
        Aluno aluno = entityManager.find(Aluno.class, alunoId);
        if (aluno == null) {
            throw new IllegalArgumentException("Aluno não encontrado: " + alunoId);
        }

        Aula aula = lockAulaForUpdate(aulaId);

        if (aula.getVagas() == null || aula.getVagas() <= 0) {
            throw new VagasEsgotadasException("Não há vagas disponíveis para a aula: " + aulaId);
        }

        if (jaMatriculado(alunoId, aulaId)) {
            throw new IllegalArgumentException("Aluno já matriculado nesta aula");
        }

        validarChoqueHorario(alunoId, aula);

        aula.setVagas(aula.getVagas() - 1);

        Matricula matricula = new Matricula();
        matricula.setAluno(aluno);
        matricula.setAula(aula);
        entityManager.persist(matricula);

        return matricula;
    }

    private Aula lockAulaForUpdate(Long aulaId) {
        Aula aula = entityManager.find(Aula.class, aulaId, LockModeType.PESSIMISTIC_WRITE);
        if (aula == null) {
            throw new IllegalArgumentException("Aula não encontrada: " + aulaId);
        }
        return aula;
    }

    private boolean jaMatriculado(Long alunoId, Long aulaId) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(m) FROM Matricula m WHERE m.aluno.id = :alunoId AND m.aula.id = :aulaId",
                        Long.class)
                .setParameter("alunoId", alunoId)
                .setParameter("aulaId", aulaId)
                .getSingleResult();
        return count > 0;
    }

    private void validarChoqueHorario(Long alunoId, Aula aula) {
        List<Aula> aulasDoAluno = entityManager.createQuery(
                        "SELECT m.aula FROM Matricula m WHERE m.aluno.id = :alunoId",
                        Aula.class)
                .setParameter("alunoId", alunoId)
                .getResultList();

        for (Aula existente : aulasDoAluno) {
            if (horariosSobrepostos(existente, aula)) {
                throw new HorarioConflitanteException(
                        "Choque de horário com a aula na disciplina: " + existente.getDisciplina().getNome());
            }
        }
    }

    private boolean horariosSobrepostos(Aula a, Aula b) {
        String diaA = a.getHorario().getDiaSemana();
        String diaB = b.getHorario().getDiaSemana();

        if (diaA == null || diaB == null || !diaA.equalsIgnoreCase(diaB)) {
            return false;
        }

        int inicioA = toMinutos(a.getHorario().getHoraInicio());
        int fimA = toMinutos(a.getHorario().getHoraFim());
        int inicioB = toMinutos(b.getHorario().getHoraInicio());
        int fimB = toMinutos(b.getHorario().getHoraFim());

        return inicioA < fimB && inicioB < fimA;
    }

    private int toMinutos(String hora) {
        if (hora == null) {
            throw new IllegalStateException("Horário inválido (nulo)");
        }
        String[] partes = hora.split(":");
        return Integer.parseInt(partes[0]) * 60 + Integer.parseInt(partes[1]);
    }
}