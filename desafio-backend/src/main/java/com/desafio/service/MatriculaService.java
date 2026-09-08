package com.desafio.service;

import com.desafio.entity.Aluno;
import com.desafio.entity.Aula;
import com.desafio.entity.Curso;
import com.desafio.entity.Matricula;
import com.desafio.exception.CursoNaoAutorizadoException;
import com.desafio.exception.HorarioConflitanteException;
import com.desafio.exception.MatriculaDuplicadaException;
import com.desafio.exception.NotFoundException;
import com.desafio.exception.VagasEsgotadasException;
import com.desafio.repository.AlunoRepository;
import com.desafio.repository.AulaRepository;
import com.desafio.repository.MatriculaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Set;

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
            throw new NotFoundException("Aluno não encontrado: " + alunoId);
        }

        Aula aula = aulaRepository.findById(aulaId, LockModeType.PESSIMISTIC_WRITE);
        if (aula == null) {
            throw new NotFoundException("Aula não encontrada: " + aulaId);
        }

        validarCursoAutorizado(aluno, aula);

        long matriculados = matriculaRepository.countByAula(aulaId);
        if (matriculados >= aula.getVagas()) {
            throw new VagasEsgotadasException("Não há vagas disponíveis para a aula: " + aulaId);
        }

        if (matriculaRepository.existsByAlunoAndAula(alunoId, aulaId)) {
            throw new MatriculaDuplicadaException("Aluno já matriculado nesta aula");
        }

        validarChoqueHorario(alunoId, aula);

        Matricula matricula = new Matricula();
        matricula.setAluno(aluno);
        matricula.setAula(aula);
        matriculaRepository.persist(matricula);

        return matricula;
    }

    /**
     * Bloqueia a matrícula quando a aula restringe os cursos autorizados e o
     * curso do aluno não está entre eles.
     */
    private void validarCursoAutorizado(Aluno aluno, Aula aula) {
        Set<Curso> autorizados = aula.getCursosAutorizados();
        if (autorizados.isEmpty()) {
            return;
        }
        if (aluno.getCurso() == null || !autorizados.contains(aluno.getCurso())) {
            throw new CursoNaoAutorizadoException(
                    "Aula não autorizada para o curso do aluno");
        }
    }

    private void validarChoqueHorario(Long alunoId, Aula aula) {
        List<Aula> aulasDoAluno = matriculaRepository.findAulasByAluno(alunoId);

        for (Aula existente : aulasDoAluno) {
            if (HorarioUtil.horariosSobrepostos(existente, aula)) {
                throw new HorarioConflitanteException(
                        "Choque de horário com a aula na disciplina: " + existente.getDisciplina().getNome());
            }
        }
    }
}