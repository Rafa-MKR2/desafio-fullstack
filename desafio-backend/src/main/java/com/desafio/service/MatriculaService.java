package com.desafio.service;

import com.desafio.entity.Aluno;
import com.desafio.entity.Aula;
import com.desafio.entity.Matricula;
import com.desafio.exception.HorarioConflitanteException;
import com.desafio.exception.VagasEsgotadasException;
import com.desafio.repository.AlunoRepository;
import com.desafio.repository.AulaRepository;
import com.desafio.repository.MatriculaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class MatriculaService {

    @Inject
    AlunoRepository alunoRepository;

    @Inject
    AulaRepository aulaRepository;

    @Inject
    MatriculaRepository matriculaRepository;

    @Transactional
    public Matricula matricular(Long alunoId, Long aulaId) {
        Aluno aluno = alunoRepository.findById(alunoId);
        if (aluno == null) {
            throw new IllegalArgumentException("Aluno não encontrado: " + alunoId);
        }

        Aula aula = aulaRepository.findById(aulaId, LockModeType.PESSIMISTIC_WRITE);
        if (aula == null) {
            throw new IllegalArgumentException("Aula não encontrada: " + aulaId);
        }

        if (aula.getVagas() == null || aula.getVagas() <= 0) {
            throw new VagasEsgotadasException("Não há vagas disponíveis para a aula: " + aulaId);
        }

        if (matriculaRepository.existsByAlunoAndAula(alunoId, aulaId)) {
            throw new IllegalArgumentException("Aluno já matriculado nesta aula");
        }

        validarChoqueHorario(alunoId, aula);

        aula.setVagas(aula.getVagas() - 1);
        aulaRepository.persist(aula);

        Matricula matricula = new Matricula();
        matricula.setAluno(aluno);
        matricula.setAula(aula);
        matriculaRepository.persist(matricula);

        return matricula;
    }

    private void validarChoqueHorario(Long alunoId, Aula aula) {
        List<Aula> aulasDoAluno = matriculaRepository.findAulasByAluno(alunoId);

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